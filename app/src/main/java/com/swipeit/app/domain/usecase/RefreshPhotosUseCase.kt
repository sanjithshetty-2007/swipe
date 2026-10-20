package com.swipeit.app.domain.usecase

import com.swipeit.app.domain.repository.PhotoRepository
import javax.inject.Inject

/** Asks the repository to re-read the device library. */
class RefreshPhotosUseCase @Inject constructor(private val repository: PhotoRepository) {
    operator fun invoke() = repository.requestRefresh()
}
