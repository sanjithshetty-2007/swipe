package com.swipeit.app.domain.usecase

import com.swipeit.app.domain.model.Decision
import com.swipeit.app.domain.model.Photo
import com.swipeit.app.domain.repository.PhotoRepository
import javax.inject.Inject

/** Marks [Photo] as trashed in the app only; the device file is untouched. */
class TrashPhotoUseCase @Inject constructor(private val repository: PhotoRepository) {
    suspend operator fun invoke(photo: Photo) = repository.decide(photo, Decision.TRASHED)
}
