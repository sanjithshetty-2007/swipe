package com.swipeit.app.domain.usecase

import com.swipeit.app.domain.model.DecidedPhoto
import com.swipeit.app.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** The last few decisions, oldest first (left-to-right display order). */
class ObserveRecentDecisionsUseCase @Inject constructor(private val repository: PhotoRepository) {
    operator fun invoke(): Flow<List<DecidedPhoto>> = repository.recent().map { it.asReversed() }
}
