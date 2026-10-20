package com.swipeit.app.feature.preview

import androidx.annotation.StringRes
import com.swipeit.app.domain.model.Decision
import com.swipeit.app.domain.model.Photo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** A pager page: the photo and its live decision (null = unsorted). */
data class PreviewItem(
    val photo: Photo,
    val decision: Decision?,
)

data class PreviewUiState(
    /** Order is frozen when the screen opens; only decisions update live. */
    val items: ImmutableList<PreviewItem> = persistentListOf(),
    val initialPage: Int = 0,
    /** True when paging across the recent decisions; false for a single photo. */
    val isRecent: Boolean = false,
    val isLoading: Boolean = true,
) {
    val isNotFound: Boolean get() = !isLoading && items.isEmpty()
}

sealed interface PreviewUiEvent {
    /** [decision] null resets the photo to unsorted (front of the deck). */
    data class ChangeDecision(val photo: Photo, val decision: Decision?) : PreviewUiEvent
}

sealed interface PreviewEffect {
    data class ShowMessage(@StringRes val messageRes: Int) : PreviewEffect
}
