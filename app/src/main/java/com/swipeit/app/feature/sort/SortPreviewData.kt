package com.swipeit.app.feature.sort

import com.swipeit.app.domain.model.Photo

/** Fake photos for @Preview only. */
internal val previewPhotos: List<Photo> = List(3) { index ->
    Photo(
        id = index + 1L,
        uri = "content://media/external/images/media/${index + 1}",
        name = "IMG_20241112_18145$index.jpg",
        sizeBytes = 3_565_158L + index * 250_000L,
        dateTaken = 1_731_434_095_000L - index * 86_400_000L,
        album = "Camera",
        width = 3000,
        height = 4000,
    )
}
