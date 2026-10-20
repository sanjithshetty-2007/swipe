package com.swipeit.app.domain.usecase

import com.swipeit.app.domain.repository.PhotoRepository
import javax.inject.Inject

/** Moves trashed photos back to kept. */
class RestoreFromTrashUseCase @Inject constructor(private val repository: PhotoRepository) {
    suspend operator fun invoke(ids: List<Long>) = repository.restore(ids)
}
