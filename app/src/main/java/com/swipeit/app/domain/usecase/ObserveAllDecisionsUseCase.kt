package com.swipeit.app.domain.usecase

import com.swipeit.app.domain.model.DecidedPhoto
import com.swipeit.app.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/** Every kept and trashed photo, keyed by photo id. */
class ObserveAllDecisionsUseCase @Inject constructor(private val repository: PhotoRepository) {
    operator fun invoke(): Flow<Map<Long, DecidedPhoto>> =
        combine(repository.kept(), repository.trashed()) { kept, trashed ->
            (kept + trashed).associateBy { it.photo.id }
        }
}
