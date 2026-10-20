package com.swipeit.app.domain.model

/** A photo with its decision. [decidedAt] is epoch millis. */
data class DecidedPhoto(
    val photo: Photo,
    val decision: Decision,
    val decidedAt: Long,
)
