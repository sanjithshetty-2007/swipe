package com.swipeit.app.feature.preview

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

/** Which list the preview pager walks through. Kept so R8 never renames its serializer. */
@Keep
@Serializable
enum class PreviewSource {
    RECENT,
    TRASH,
}

/** Full-screen preview of [photoId]; lives here so the ViewModel can read its own arguments. */
@Serializable
data class PreviewRoute(
    val photoId: Long,
    val source: PreviewSource = PreviewSource.RECENT,
)
