package com.swipeit.app.data.media

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreMediaSource @Inject constructor(
    private val dataSource: MediaStoreDataSource,
    private val observer: MediaObserver,
) : MediaSource {
    override fun changes(): Flow<Unit> = observer.changes()

    override suspend fun queryImages(sortOrder: MediaSortOrder): List<MediaImage>? =
        dataSource.queryImages(sortOrder)

    override fun hasFullAccess(): Boolean = dataSource.hasFullAccess()
}
