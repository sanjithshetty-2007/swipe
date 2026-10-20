package com.swipeit.app.feature.trash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swipeit.app.R
import com.swipeit.app.domain.model.DeletionConsent
import com.swipeit.app.domain.model.DeletionOutcome
import com.swipeit.app.domain.model.Photo
import com.swipeit.app.domain.usecase.DeletePhotosFromDeviceUseCase
import com.swipeit.app.domain.usecase.ObserveTrashUseCase
import com.swipeit.app.domain.usecase.RestoreFromTrashUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "TrashViewModel"
private const val STOP_TIMEOUT_MILLIS = 5_000L

private data class Selection(val isActive: Boolean = false, val ids: Set<Long> = emptySet())

/**
 * Trash grid, selection and device deletion. Deletion suspends on [consentAnswer] while the
 * screen shows the OS confirmation, so the chunk loop in the use case stays linear.
 */
@HiltViewModel
class TrashViewModel @Inject constructor(
    observeTrash: ObserveTrashUseCase,
    private val restoreFromTrash: RestoreFromTrashUseCase,
    private val deletePhotosFromDevice: DeletePhotosFromDeviceUseCase,
) : ViewModel() {

    private val selection = MutableStateFlow(Selection())
    private val isDeleting = MutableStateFlow(false)
    private val pendingConfirmation = MutableStateFlow<DeleteTarget?>(null)
    private var consentAnswer: CompletableDeferred<Boolean>? = null

    private val effectChannel = Channel<TrashEffect>(Channel.BUFFERED)
    val effects: Flow<TrashEffect> = effectChannel.receiveAsFlow()

    val uiState: StateFlow<TrashUiState> =
        combine(observeTrash(), selection, isDeleting, pendingConfirmation) { photos, sel, deleting, confirm ->
            val ids = photos.mapTo(HashSet(photos.size)) { it.photo.id }
            TrashUiState(
                photos = photos.toImmutableList(),
                totalBytes = photos.sumOf { it.photo.sizeBytes },
                isLoading = false,
                isSelectionMode = sel.isActive && photos.isNotEmpty(),
                // Photos restored or deleted elsewhere drop out of the selection.
                selectedIds = sel.ids.filter { it in ids }.toImmutableSet(),
                isDeleting = deleting,
                pendingConfirmation = confirm,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), TrashUiState())

    fun onEvent(event: TrashUiEvent) {
        when (event) {
            TrashUiEvent.EnterSelection -> selection.value = Selection(isActive = true)
            TrashUiEvent.ExitSelection -> selection.value = Selection()
            is TrashUiEvent.StartSelectionWith -> selection.value = Selection(isActive = true, ids = setOf(event.photoId))
            is TrashUiEvent.ToggleSelected -> selection.update { sel ->
                val ids = if (event.photoId in sel.ids) sel.ids - event.photoId else sel.ids + event.photoId
                sel.copy(isActive = true, ids = ids)
            }
            TrashUiEvent.RestoreSelected -> restore(uiState.value.selectedIds.toList())
            TrashUiEvent.RestoreAll -> restore(uiState.value.photos.map { it.photo.id })
            is TrashUiEvent.Delete -> requestDelete(event.target)
            TrashUiEvent.ConfirmDelete -> pendingConfirmation.value?.let { target ->
                pendingConfirmation.value = null
                startDeletion(photosFor(target))
            }
            TrashUiEvent.DismissConfirmation -> pendingConfirmation.value = null
            is TrashUiEvent.ConsentResult -> consentAnswer?.complete(event.granted)
        }
    }

    private fun requestDelete(target: DeleteTarget) {
        if (isDeleting.value || uiState.value.countFor(target) == 0) return
        if (deletePhotosFromDevice.systemConfirmsDeletion) {
            startDeletion(photosFor(target))
        } else {
            pendingConfirmation.value = target
        }
    }

    private fun photosFor(target: DeleteTarget): List<Photo> {
        val state = uiState.value
        val all = state.photos.map { it.photo }
        return if (target == DeleteTarget.ALL) all else all.filter { it.id in state.selectedIds }
    }

    private fun restore(ids: List<Long>) {
        if (ids.isEmpty()) return
        viewModelScope.launch {
            try {
                restoreFromTrash(ids)
                selection.value = Selection()
                effectChannel.send(TrashEffect.ShowRestored(ids.size))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Restore failed", e)
                effectChannel.send(TrashEffect.ShowMessage(R.string.trash_restore_failed))
            }
        }
    }

    private fun startDeletion(photos: List<Photo>) {
        if (photos.isEmpty() || isDeleting.value) return
        isDeleting.value = true
        viewModelScope.launch {
            try {
                val result = deletePhotosFromDevice(photos, ::awaitConsent)
                if (result.deletedCount > 0) selection.value = Selection()
                val effect = when {
                    result.outcome == DeletionOutcome.FAILED -> TrashEffect.ShowMessage(R.string.trash_delete_failed)
                    result.deletedCount > 0 -> TrashEffect.ShowFreed(result.freedBytes)
                    else -> TrashEffect.ShowMessage(R.string.trash_delete_cancelled)
                }
                effectChannel.send(effect)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Deletion failed", e)
                effectChannel.send(TrashEffect.ShowMessage(R.string.trash_delete_failed))
            } finally {
                consentAnswer = null
                isDeleting.value = false
            }
        }
    }

    private suspend fun awaitConsent(consent: DeletionConsent): Boolean {
        val answer = CompletableDeferred<Boolean>()
        consentAnswer = answer
        effectChannel.send(TrashEffect.LaunchConsent(consent))
        return answer.await()
    }
}
