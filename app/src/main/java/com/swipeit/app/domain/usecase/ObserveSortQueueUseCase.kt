package com.swipeit.app.domain.usecase

import com.swipeit.app.domain.model.Photo
import com.swipeit.app.domain.model.SortOrder
import kotlinx.coroutines.flow.Flow
import com.swipeit.app.domain.repository.PhotoRepository
import javax.inject.Inject

class ObserveSortQueueUseCase @Inject constructor(private val repository: PhotoRepository) {
    operator fun invoke(sortOrder: SortOrder = SortOrder.NEWEST_FIRST): Flow<List<Photo>> =
        repository.unsortedQueue(sortOrder)
}
