package com.swipeit.app.navigation

import com.swipeit.app.domain.model.Photo

data class HomePlaceholderUiState(
    val isLoading: Boolean = true,
    val photoCount: Int = 0,
    val firstPhoto: Photo? = null,
)

sealed interface HomePlaceholderUiEvent {
    /** Re-query the library, e.g. after returning from the partial-access picker or settings. */
    data object Refresh : HomePlaceholderUiEvent
}
