package com.swipeit.app.data.db

import com.swipeit.app.domain.model.DecidedPhoto
import com.swipeit.app.domain.model.Decision
import com.swipeit.app.domain.model.Photo

fun Photo.toEntity(decision: Decision, decidedAt: Long): PhotoDecisionEntity = PhotoDecisionEntity(
    mediaId = id,
    uri = uri,
    status = decision,
    decidedAt = decidedAt,
    sizeBytes = sizeBytes,
    dateTaken = dateTaken,
    displayName = name,
    album = album,
)

/** Dimensions are not stored, so width/height are 0. */
fun PhotoDecisionEntity.toPhoto(): Photo = Photo(
    id = mediaId,
    uri = uri,
    name = displayName,
    sizeBytes = sizeBytes,
    dateTaken = dateTaken,
    album = album,
    width = 0,
    height = 0,
)

fun PhotoDecisionEntity.toDecidedPhoto(): DecidedPhoto = DecidedPhoto(
    photo = toPhoto(),
    decision = status,
    decidedAt = decidedAt,
)
