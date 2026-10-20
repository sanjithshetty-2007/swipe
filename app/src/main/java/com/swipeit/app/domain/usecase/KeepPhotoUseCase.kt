package com.swipeit.app.domain.usecase

import com.swipeit.app.domain.model.Decision
import com.swipeit.app.domain.model.Photo
import com.swipeit.app.domain.repository.PhotoRepository
import javax.inject.Inject

class KeepPhotoUseCase @Inject constructor(private val repository: PhotoRepository) {
    suspend operator fun invoke(photo: Photo) = repository.decide(photo, Decision.KEPT)
}
