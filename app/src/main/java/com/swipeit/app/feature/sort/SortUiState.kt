package com.swipeit.app.feature.sort

import com.swipeit.app.domain.model.Photo

data class SortUiState(
    val isLoading: Boolean = true,
    val topPhoto: Photo? = null,
    val remaining: Int = 0,
    val total: Int = 0,
)

sealed interface SortUiEvent {
    /** Re-query the library, e.g. after returning from the partial-access picker or settings. */
    data object Refresh : SortUiEvent
}
