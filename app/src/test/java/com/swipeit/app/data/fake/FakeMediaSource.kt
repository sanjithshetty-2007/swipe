package com.swipeit.app.data.fake

import com.swipeit.app.data.media.MediaImage
import com.swipeit.app.data.media.MediaSortOrder
import com.swipeit.app.data.media.MediaSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** Device library stand-in. Set [images] (newest first, or null for a failed query) to simulate changes. */
class FakeMediaSource(initial: List<MediaImage>) : MediaSource {

    val images = MutableStateFlow<List<MediaImage>?>(initial)

    var fullAccess: Boolean = true

    override fun changes(): Flow<Unit> = images.map { }

    override suspend fun queryImages(sortOrder: MediaSortOrder): List<MediaImage>? =
        when (sortOrder) {
            MediaSortOrder.NEWEST_FIRST -> images.value
            MediaSortOrder.OLDEST_FIRST -> images.value?.asReversed()
        }

    override fun hasFullAccess(): Boolean = fullAccess

    companion object {
        fun image(id: Long) = MediaImage(
            id = id,
            contentUri = "content://media/external/images/media/$id",
            displayName = "IMG_$id.jpg",
            sizeBytes = id * 100,
            dateTakenMillis = id * 1_000,
            album = "Camera",
            width = 100,
            height = 100,
            mimeType = "image/jpeg",
        )
    }
}
