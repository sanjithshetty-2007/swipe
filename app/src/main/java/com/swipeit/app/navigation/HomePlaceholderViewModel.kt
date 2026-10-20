package com.swipeit.app.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swipeit.app.domain.repository.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

private const val STOP_TIMEOUT_MILLIS = 5_000L

/** Temporary: shows device photo count and the newest photo until the Sort screen exists. */
@HiltViewModel
class HomePlaceholderViewModel @Inject constructor(
    photoRepository: PhotoRepository,
) : ViewModel() {

    private val refreshRequests = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomePlaceholderUiState> = refreshRequests
        .flatMapLatest { photoRepository.observeDevicePhotos() }
        .map { photos ->
            HomePlaceholderUiState(
                isLoading = false,
                photoCount = photos.size,
                firstPhoto = photos.firstOrNull(),
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), HomePlaceholderUiState())

    fun onEvent(event: HomePlaceholderUiEvent) {
        when (event) {
            HomePlaceholderUiEvent.Refresh -> refreshRequests.update { it + 1 }
        }
    }
}
