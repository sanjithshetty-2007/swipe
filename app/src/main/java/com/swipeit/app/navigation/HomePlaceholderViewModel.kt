package com.swipeit.app.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swipeit.app.domain.usecase.ObserveSortQueueUseCase
import com.swipeit.app.domain.usecase.RefreshPhotosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private const val STOP_TIMEOUT_MILLIS = 5_000L

/** Temporary: shows the unsorted count and the top of the queue until the Sort screen exists. */
@HiltViewModel
class HomePlaceholderViewModel @Inject constructor(
    observeSortQueue: ObserveSortQueueUseCase,
    private val refreshPhotos: RefreshPhotosUseCase,
) : ViewModel() {

    val uiState: StateFlow<HomePlaceholderUiState> = observeSortQueue()
        .map { queue ->
            HomePlaceholderUiState(
                isLoading = false,
                photoCount = queue.size,
                firstPhoto = queue.firstOrNull(),
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), HomePlaceholderUiState())

    fun onEvent(event: HomePlaceholderUiEvent) {
        when (event) {
            HomePlaceholderUiEvent.Refresh -> refreshPhotos()
        }
    }
}
