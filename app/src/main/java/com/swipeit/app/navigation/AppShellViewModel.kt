package com.swipeit.app.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swipeit.app.domain.usecase.ObserveStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private const val STOP_TIMEOUT_MILLIS = 5_000L

/** State shared by the app chrome (bottom bar badge). */
@HiltViewModel
class AppShellViewModel @Inject constructor(
    observeStats: ObserveStatsUseCase,
) : ViewModel() {

    val trashCount: StateFlow<Int> = observeStats()
        .map { it.trashed }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), 0)
}
