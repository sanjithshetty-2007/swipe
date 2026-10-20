package com.swipeit.app.data.media

import android.net.Uri

/** Raw image row read from MediaStore. */
data class MediaImage(
    val id: Long,
    val contentUri: Uri,
    val displayName: String,
    val sizeBytes: Long,
    val dateTakenMillis: Long,
    val album: String,
    val width: Int,
    val height: Int,
    val mimeType: String,
)
