package com.swipeit.app.feature.sort

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swipeit.app.domain.usecase.ObserveSortQueueUseCase
import com.swipeit.app.domain.usecase.ObserveStatsUseCase
import com.swipeit.app.domain.usecase.RefreshPhotosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private const val STOP_TIMEOUT_MILLIS = 5_000L

/** Placeholder Sort: top of the queue and progress. Swiping arrives in the next step. */
@HiltViewModel
class SortViewModel @Inject constructor(
    observeSortQueue: ObserveSortQueueUseCase,
    observeStats: ObserveStatsUseCase,
    private val refreshPhotos: RefreshPhotosUseCase,
) : ViewModel() {

    val uiState: StateFlow<SortUiState> =
        combine(observeSortQueue(), observeStats()) { queue, stats ->
            SortUiState(
                isLoading = false,
                topPhoto = queue.firstOrNull(),
                remaining = queue.size,
                total = queue.size + stats.kept + stats.trashed,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), SortUiState())

    fun onEvent(event: SortUiEvent) {
        when (event) {
            SortUiEvent.Refresh -> refreshPhotos()
        }
    }
}
