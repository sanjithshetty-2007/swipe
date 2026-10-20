package com.swipeit.app.data.media

import com.swipeit.app.domain.model.Photo

fun MediaImage.toPhoto(): Photo = Photo(
    id = id,
    uri = contentUri.toString(),
    name = displayName,
    sizeBytes = sizeBytes,
    dateTaken = dateTakenMillis,
    album = album,
    width = width,
    height = height,
)
