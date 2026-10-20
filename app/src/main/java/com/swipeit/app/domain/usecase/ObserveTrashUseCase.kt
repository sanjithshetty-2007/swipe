package com.swipeit.app.domain.usecase

import com.swipeit.app.domain.model.DecidedPhoto
import com.swipeit.app.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Trashed photos, most recently trashed first. */
class ObserveTrashUseCase @Inject constructor(private val repository: PhotoRepository) {
    operator fun invoke(): Flow<List<DecidedPhoto>> = repository.trashed()
}
