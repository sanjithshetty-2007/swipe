package com.swipeit.app.domain.usecase

import com.swipeit.app.domain.model.Photo
import com.swipeit.app.domain.repository.PhotoRepository
import javax.inject.Inject

/** Reverts the most recent decision; returns the photo now back on top of the queue, or null. */
class UndoLastDecisionUseCase @Inject constructor(private val repository: PhotoRepository) {
    suspend operator fun invoke(): Photo? = repository.undoLast()
}
