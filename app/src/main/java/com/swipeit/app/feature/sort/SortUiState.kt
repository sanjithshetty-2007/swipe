package com.swipeit.app.feature.sort

import androidx.annotation.StringRes
import com.swipeit.app.domain.model.DecidedPhoto
import com.swipeit.app.domain.model.Photo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class SortUiState(
    /** Top of the deck, at most [MAX_VISIBLE_CARDS]; already excludes optimistically decided photos. */
    val queue: ImmutableList<Photo> = persistentListOf(),
    val remaining: Int = 0,
    val total: Int = 0,
    val isLoading: Boolean = true,
    /** Last decisions, oldest first. */
    val recent: ImmutableList<DecidedPhoto> = persistentListOf(),
    /** An undone photo that should fly back onto the deck from [ReturningCard.direction]. */
    val returningCard: ReturningCard? = null,
) {
    /** Fraction of the library sorted, 0..1. */
    val progress: Float get() = if (total > 0) (total - remaining).coerceAtLeast(0).toFloat() / total else 0f

    val isEmpty: Boolean get() = !isLoading && queue.isEmpty()

    val canUndo: Boolean get() = recent.isNotEmpty()
}

data class ReturningCard(val photoId: Long, val direction: SwipeDirection)

sealed interface SortUiEvent {
    data class Keep(val photo: Photo, val direction: SwipeDirection = SwipeDirection.RIGHT) : SortUiEvent

    data class Trash(val photo: Photo) : SortUiEvent

    data object Undo : SortUiEvent

    /** The deck finished flying [SortUiState.returningCard] back in. */
    data object ReturnAnimationFinished : SortUiEvent

    /** Re-query the library, e.g. after returning from the partial-access picker or settings. */
    data object Refresh : SortUiEvent
}

sealed interface SortEffect {
    data class ShowMessage(@StringRes val messageRes: Int) : SortEffect

    /** "Moved to Trash · UNDO" after a trash swipe. */
    data object TrashedWithUndo : SortEffect
}
