package com.swipeit.app.data.repository

import com.swipeit.app.data.media.MediaObserver
import com.swipeit.app.data.media.MediaSortOrder
import com.swipeit.app.data.media.MediaStoreDataSource
import com.swipeit.app.data.media.toPhoto
import com.swipeit.app.domain.model.Photo
import com.swipeit.app.domain.repository.PhotoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepositoryImpl @Inject constructor(
    private val mediaStoreDataSource: MediaStoreDataSource,
    private val mediaObserver: MediaObserver,
) : PhotoRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeDevicePhotos(): Flow<List<Photo>> =
        mediaObserver.changes().mapLatest {
            mediaStoreDataSource.queryImages(MediaSortOrder.NEWEST_FIRST).map { it.toPhoto() }
        }
}
