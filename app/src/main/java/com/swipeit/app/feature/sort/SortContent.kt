package com.swipeit.app.feature.sort

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.swipeit.app.R
import com.swipeit.app.core.designsystem.SwipeItDimens
import com.swipeit.app.core.designsystem.SwipeItIcons
import com.swipeit.app.core.designsystem.SwipeItTheme
import com.swipeit.app.core.designsystem.component.EmptyState
import com.swipeit.app.core.designsystem.component.PhotoInfoOverlay
import com.swipeit.app.core.designsystem.component.ProgressHeaderCard
import com.swipeit.app.core.designsystem.component.SwipeItLogo
import com.swipeit.app.core.designsystem.component.photoInfoSubtitle
import com.swipeit.app.core.permissions.PartialAccessBanner
import com.swipeit.app.domain.model.Photo

@Composable
fun SortContent(
    uiState: SortUiState,
    isPartialAccess: Boolean,
    onSelectMoreClick: () -> Unit,
    onAllowAllClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onOpenPreview: (photoId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = SwipeItDimens.ScreenPaddingCompact),
        verticalArrangement = Arrangement.spacedBy(SwipeItDimens.SpacingMedium),
    ) {
        SortHeader(onSettingsClick = onSettingsClick)
        if (isPartialAccess) {
            PartialAccessBanner(onSelectMoreClick = onSelectMoreClick, onAllowAllClick = onAllowAllClick)
        }
        if (uiState.total > 0) {
            ProgressHeaderCard(remaining = uiState.remaining, total = uiState.total)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = SwipeItDimens.SpacingMedium),
            contentAlignment = Alignment.Center,
        ) {
            val topPhoto = uiState.topPhoto
            when {
                uiState.isLoading -> {
                    val loadingLabel = stringResource(R.string.sort_loading)
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.semantics { contentDescription = loadingLabel },
                    )
                }
                topPhoto == null -> EmptyState(
                    iconRes = SwipeItIcons.Photos,
                    title = stringResource(R.string.sort_empty_title),
                    message = stringResource(R.string.sort_empty_message),
                )
                else -> PhotoCard(photo = topPhoto, onExpandClick = { onOpenPreview(topPhoto.id) })
            }
        }
    }
}

@Composable
private fun SortHeader(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SwipeItLogo()
        IconButton(onClick = onSettingsClick) {
            Icon(
                painter = painterResource(SwipeItIcons.Settings),
                contentDescription = stringResource(R.string.action_settings),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PhotoCard(photo: Photo, onExpandClick: () -> Unit) {
    val shape = MaterialTheme.shapes.large
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(BorderStroke(SwipeItDimens.BorderWidth, MaterialTheme.colorScheme.outline), shape),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AsyncImage(
            model = photo.uri,
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

private val previewPhoto = Photo(
    id = 1L,
    uri = "content://media/external/images/media/1",
    name = "IMG_20241112_181455.jpg",
    sizeBytes = 3_565_158L,
    dateTaken = 1_731_434_095_000L,
    album = "Camera",
    width = 3000,
    height = 4000,
)

@Composable
private fun SortContentPreview(uiState: SortUiState, isPartialAccess: Boolean = false) {
    SwipeItTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            SortContent(
                uiState = uiState,
                isPartialAccess = isPartialAccess,
                onSelectMoreClick = {},
                onAllowAllClick = {},
                onSettingsClick = {},
                onOpenPreview = {},
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun SortWithPhotoPreview() {
    SortContentPreview(SortUiState(isLoading = false, topPhoto = previewPhoto, remaining = 342, total = 1_200))
}

@Preview(showSystemUi = true)
@Composable
private fun SortPartialPreview() {
    SortContentPreview(
        SortUiState(isLoading = false, topPhoto = previewPhoto, remaining = 3, total = 5),
        isPartialAccess = true,
    )
}

@Preview(showSystemUi = true)
@Composable
private fun SortLoadingPreview() {
    SortContentPreview(SortUiState())
}

@Preview(showSystemUi = true)
@Composable
private fun SortAllDonePreview() {
    SortContentPreview(SortUiState(isLoading = false, remaining = 0, total = 1_200))
}
