package com.swipeit.app.feature.sort

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swipeit.app.R
import com.swipeit.app.domain.model.Photo
import com.swipeit.app.domain.usecase.KeepPhotoUseCase
import com.swipeit.app.domain.usecase.ObserveRecentDecisionsUseCase
import com.swipeit.app.domain.usecase.ObserveSortQueueUseCase
import com.swipeit.app.domain.usecase.ObserveStatsUseCase
import com.swipeit.app.domain.usecase.RefreshPhotosUseCase
import com.swipeit.app.domain.usecase.TrashPhotoUseCase
import com.swipeit.app.domain.usecase.UndoLastDecisionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

private const val TAG = "SortViewModel"
private const val STOP_TIMEOUT_MILLIS = 5_000L

/**
 * Drives the swipe deck. Decisions are optimistic: the photo is hidden from the deck at once
 * ([pendingIds]) and the write runs afterwards. Writes and undo are serialised by [writeMutex]
 * so an undo never overtakes the decision it should revert.
 */
@HiltViewModel
class SortViewModel @Inject constructor(
    observeSortQueue: ObserveSortQueueUseCase,
    observeStats: ObserveStatsUseCase,
    observeRecentDecisions: ObserveRecentDecisionsUseCase,
    private val keepPhoto: KeepPhotoUseCase,
    private val trashPhoto: TrashPhotoUseCase,
    private val undoLastDecision: UndoLastDecisionUseCase,
    private val refreshPhotos: RefreshPhotosUseCase,
) : ViewModel() {

    /** Decided in the UI but possibly not yet gone from the repository queue. */
    private val pendingIds = MutableStateFlow<Set<Long>>(emptySet())
    private val returningCard = MutableStateFlow<ReturningCard?>(null)
    private val writeMutex = Mutex()

    /** Swipes written by this screen, newest last; tells undo which way the card left. Guarded by [writeMutex]. */
    private val swipeHistory = ArrayDeque<ReturningCard>()

    private val effectChannel = Channel<SortEffect>(Channel.BUFFERED)
    val effects: Flow<SortEffect> = effectChannel.receiveAsFlow()

    private val queue = observeSortQueue().onEach { photos ->
        // Once the repository no longer lists a pending photo, its write has landed.
        val queuedIds = photos.mapTo(HashSet(photos.size)) { it.id }
        pendingIds.update { pending -> pending.filterTo(HashSet()) { it in queuedIds } }
    }

    val uiState: StateFlow<SortUiState> =
        combine(
            queue,
            observeStats(),
            pendingIds,
            observeRecentDecisions(),
            returningCard,
        ) { photos, stats, pending, recent, returning ->
            val visible = if (pending.isEmpty()) photos else photos.filterNot { it.id in pending }
            SortUiState(
                queue = visible.take(MAX_VISIBLE_CARDS).toImmutableList(),
                remaining = visible.size,
                total = maxOf(stats.unsorted + stats.kept + stats.trashed, visible.size),
                isLoading = false,
                recent = recent.toImmutableList(),
                returningCard = returning,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), SortUiState())

    fun onEvent(event: SortUiEvent) {
        when (event) {
            is SortUiEvent.Keep -> decide(event.photo, event.direction) { keepPhoto(it) }
            is SortUiEvent.Trash -> decide(event.photo, SwipeDirection.LEFT) { trashPhoto(it) }
            SortUiEvent.Undo -> undo()
            SortUiEvent.ReturnAnimationFinished -> returningCard.value = null
            SortUiEvent.Refresh -> refreshPhotos()
        }
    }

    private fun decide(photo: Photo, direction: SwipeDirection, write: suspend (Photo) -> Unit) {
        pendingIds.update { it + photo.id }
        if (!direction.isKeep) effectChannel.trySend(SortEffect.TrashedWithUndo)
        viewModelScope.launch {
            writeMutex.withLock {
                try {
                    write(photo)
                    swipeHistory.addLast(ReturningCard(photo.id, direction))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.w(TAG, "Saving decision for ${photo.id} failed", e)
                    pendingIds.update { it - photo.id }
                    effectChannel.send(SortEffect.ShowMessage(R.string.sort_error_save))
                }
            }
        }
    }

    private fun undo() {
        viewModelScope.launch {
            writeMutex.withLock {
                // Announce the return before the photo reappears, so the deck can animate it in.
                val expected = swipeHistory.lastOrNull()
                returningCard.value = expected
                try {
                    val restored = undoLastDecision()
                    when {
                        restored == null -> {
                            returningCard.value = null
                            effectChannel.send(SortEffect.ShowMessage(R.string.sort_nothing_to_undo))
                        }
                        else -> {
                            if (restored.id == expected?.photoId) swipeHistory.removeLast() else returningCard.value = null
                            pendingIds.update { it - restored.id }
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.w(TAG, "Undo failed", e)
                    returningCard.value = null
                    effectChannel.send(SortEffect.ShowMessage(R.string.sort_error_undo))
                }
            }
        }
    }
}
