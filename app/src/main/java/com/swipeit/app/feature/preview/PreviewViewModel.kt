package com.swipeit.app.feature.preview

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.swipeit.app.R
import com.swipeit.app.domain.model.Photo
import com.swipeit.app.domain.usecase.ChangeDecisionUseCase
import com.swipeit.app.domain.usecase.ObserveAllDecisionsUseCase
import com.swipeit.app.domain.usecase.ObserveRecentDecisionsUseCase
import com.swipeit.app.domain.usecase.ObserveSortQueueUseCase
import com.swipeit.app.domain.usecase.ObserveTrashUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "PreviewViewModel"
private const val STOP_TIMEOUT_MILLIS = 5_000L

/**
 * Opens on [PreviewRoute.photoId]. If the photo is in the [PreviewRoute.source] list (recent or trash) the pager spans
 * them (order frozen at open, so pages never reshuffle when a decision changes); otherwise it
 * shows that single photo, taken from the deck or the kept/trashed sets.
 */
@HiltViewModel
class PreviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeRecentDecisions: ObserveRecentDecisionsUseCase,
    private val observeTrash: ObserveTrashUseCase,
    private val observeSortQueue: ObserveSortQueueUseCase,
    private val observeAllDecisions: ObserveAllDecisionsUseCase,
    private val changeDecision: ChangeDecisionUseCase,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<PreviewRoute>()

    private data class Snapshot(val photos: List<Photo>, val initialPage: Int, val source: PreviewSource?)

    private val snapshot: Flow<Snapshot> = flow {
        val list = when (route.source) {
            PreviewSource.RECENT -> observeRecentDecisions().first()
            PreviewSource.TRASH -> observeTrash().first()
        }
        val index = list.indexOfFirst { it.photo.id == route.photoId }
        if (index >= 0) {
            emit(Snapshot(list.map { it.photo }, index, route.source))
        } else {
            val photo = observeSortQueue().first().firstOrNull { it.id == route.photoId }
                ?: observeAllDecisions().first()[route.photoId]?.photo
            emit(Snapshot(listOfNotNull(photo), initialPage = 0, source = null))
        }
    }.shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)

    private val effectChannel = Channel<PreviewEffect>(Channel.BUFFERED)
    val effects: Flow<PreviewEffect> = effectChannel.receiveAsFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<PreviewUiState> = snapshot
        .flatMapLatest { snap ->
            observeAllDecisions().map { decisions ->
                PreviewUiState(
                    items = snap.photos.map { PreviewItem(it, decisions[it.id]?.decision) }.toImmutableList(),
                    initialPage = snap.initialPage,
                    source = snap.source,
                    isLoading = false,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), PreviewUiState())

    fun onEvent(event: PreviewUiEvent) {
        when (event) {
            is PreviewUiEvent.ChangeDecision -> change(event)
        }
    }

    private fun change(event: PreviewUiEvent.ChangeDecision) {
        val current = uiState.value.items.firstOrNull { it.photo.id == event.photo.id }?.decision
        if (current == event.decision) return
        viewModelScope.launch {
            try {
                changeDecision(event.photo, event.decision)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Changing decision for ${event.photo.id} failed", e)
                effectChannel.send(PreviewEffect.ShowMessage(R.string.preview_error_save))
            }
        }
    }
}
