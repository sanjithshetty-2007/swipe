package com.swipeit.app.feature.preview

import kotlinx.serialization.Serializable

/** Full-screen preview of [photoId]; lives here so the ViewModel can read its own arguments. */
@Serializable
data class PreviewRoute(val photoId: Long)
