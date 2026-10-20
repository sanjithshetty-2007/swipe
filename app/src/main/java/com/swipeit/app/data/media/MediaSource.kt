package com.swipeit.app.data.media

import kotlinx.coroutines.flow.Flow

/** Read-only view of device images, abstracted so repository logic can be unit-tested. */
interface MediaSource {
    /** Emits once on start, then whenever the device image set may have changed. */
    fun changes(): Flow<Unit>

    /** Visible images in [sortOrder], or null if they could not be read. */
    suspend fun queryImages(sortOrder: MediaSortOrder): List<MediaImage>?

    /** True when the whole library is visible (not just user-selected photos). */
    fun hasFullAccess(): Boolean
}
