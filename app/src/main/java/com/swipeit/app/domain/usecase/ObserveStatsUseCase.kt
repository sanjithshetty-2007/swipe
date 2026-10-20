package com.swipeit.app.domain.usecase

import com.swipeit.app.domain.model.Stats
import kotlinx.coroutines.flow.Flow
import com.swipeit.app.domain.repository.PhotoRepository
import javax.inject.Inject

class ObserveStatsUseCase @Inject constructor(private val repository: PhotoRepository) {
    operator fun invoke(): Flow<Stats> = repository.stats()
}
