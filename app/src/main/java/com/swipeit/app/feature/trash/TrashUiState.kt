package com.swipeit.app.feature.trash

import androidx.annotation.StringRes
import com.swipeit.app.domain.model.DecidedPhoto
import com.swipeit.app.domain.model.DeletionConsent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

/** What a delete action applies to. */
enum class DeleteTarget {
    SELECTED,
    ALL,
}

data class TrashUiState(
    /** Most recently trashed first. */
    val photos: ImmutableList<DecidedPhoto> = persistentListOf(),
    val totalBytes: Long = 0L,
    val isLoading: Boolean = true,
    val isSelectionMode: Boolean = false,
    val selectedIds: ImmutableSet<Long> = persistentSetOf(),
    val isDeleting: Boolean = false,
    /** In-app "are you sure?" dialog, only on API levels where the OS does not confirm itself. */
    val pendingConfirmation: DeleteTarget? = null,
) {
    val isEmpty: Boolean get() = !isLoading && photos.isEmpty()

    val selectedCount: Int get() = selectedIds.size

    fun countFor(target: DeleteTarget): Int = if (target == DeleteTarget.ALL) photos.size else selectedCount
}

sealed interface TrashUiEvent {
    data object EnterSelection : TrashUiEvent

    data object ExitSelection : TrashUiEvent

    /** Long-press: enter selection mode with [photoId] selected. */
    data class StartSelectionWith(val photoId: Long) : TrashUiEvent

    data class ToggleSelected(val photoId: Long) : TrashUiEvent

    data object RestoreSelected : TrashUiEvent

    data object RestoreAll : TrashUiEvent

    data class Delete(val target: DeleteTarget) : TrashUiEvent

    data object ConfirmDelete : TrashUiEvent

    data object DismissConfirmation : TrashUiEvent

    /** Result of the system dialog launched for [TrashEffect.LaunchConsent]. */
    data class ConsentResult(val granted: Boolean) : TrashUiEvent
}

sealed interface TrashEffect {
    /** Show the OS deletion confirmation and report back with [TrashUiEvent.ConsentResult]. */
    data class LaunchConsent(val consent: DeletionConsent) : TrashEffect

    data class ShowFreed(val bytes: Long) : TrashEffect

    data class ShowRestored(val count: Int) : TrashEffect

    data class ShowMessage(@StringRes val messageRes: Int) : TrashEffect
}
