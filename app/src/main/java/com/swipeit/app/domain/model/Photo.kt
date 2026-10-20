package com.swipeit.app.domain.model

/** A device photo. [uri] is a `content://` string; [dateTaken] is epoch millis. */
data class Photo(
    val id: Long,
    val uri: String,
    val name: String,
    val sizeBytes: Long,
    val dateTaken: Long,
    val album: String,
    val width: Int,
    val height: Int,
)
