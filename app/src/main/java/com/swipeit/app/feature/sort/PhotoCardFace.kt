package com.swipeit.app.feature.sort

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.swipeit.app.core.designsystem.SwipeItDimens
import com.swipeit.app.core.designsystem.SwipeItMotion
import com.swipeit.app.core.designsystem.SwipeItTheme
import com.swipeit.app.core.designsystem.component.PhotoInfoOverlay
import com.swipeit.app.core.designsystem.component.photoInfoSubtitle
import com.swipeit.app.domain.model.Photo

/**
 * Photo filling the card with the info overlay. No explicit size on the request, so Coil
 * decodes at the card's laid-out size, never full resolution.
 */
@Composable
fun PhotoCardFace(
    photo: Photo,
    onExpandClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalPlatformContext.current
    val request = remember(photo.uri) {
        ImageRequest.Builder(context)
            .data(photo.uri)
            .crossfade(SwipeItMotion.DurationMedium)
            .build()
    }
    val shape = MaterialTheme.shapes.large
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(SwipeItDimens.BorderWidth, MaterialTheme.colorScheme.outline, shape),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AsyncImage(
            model = request,
            contentDescription = photo.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        PhotoInfoOverlay(
            title = photo.name,
            subtitle = photoInfoSubtitle(photo.dateTaken, photo.sizeBytes),
            onExpandClick = onExpandClick,
        )
    }
}

@Preview
@Composable
private fun PhotoCardFacePreview() {
    SwipeItTheme {
        PhotoCardFace(
            photo = previewPhotos.first(),
            onExpandClick = {},
            modifier = Modifier
                .width(300.dp)
                .aspectRatio(3f / 4f)
                .clip(MaterialTheme.shapes.large),
        )
    }
}
