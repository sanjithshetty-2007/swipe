package com.swipeit.app.domain.repository

import com.swipeit.app.domain.model.Photo
import kotlinx.coroutines.flow.Flow

interface PhotoRepository {
    /** All device photos, newest first; re-emits whenever the device library changes. */
    fun observeDevicePhotos(): Flow<List<Photo>>
}
