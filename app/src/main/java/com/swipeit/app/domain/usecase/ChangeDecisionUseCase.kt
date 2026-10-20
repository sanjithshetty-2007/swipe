package com.swipeit.app.domain.usecase

import com.swipeit.app.domain.model.Decision
import com.swipeit.app.domain.model.Photo
import com.swipeit.app.domain.repository.PhotoRepository
import javax.inject.Inject

/** Sets a new decision for [Photo]; null resets it to unsorted (front of the queue). */
class ChangeDecisionUseCase @Inject constructor(private val repository: PhotoRepository) {
    suspend operator fun invoke(photo: Photo, decision: Decision?) {
        if (decision == null) repository.reset(photo.id) else repository.decide(photo, decision)
    }
}
