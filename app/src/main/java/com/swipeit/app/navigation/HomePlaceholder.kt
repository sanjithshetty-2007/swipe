package com.swipeit.app.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.swipeit.app.R
import com.swipeit.app.core.designsystem.SwipeItDimens
import com.swipeit.app.core.designsystem.SwipeItTheme
import com.swipeit.app.core.permissions.PartialAccessBanner
import com.swipeit.app.domain.model.Photo

/** Temporary home until the Sort screen exists. */
@Composable
fun HomePlaceholderScreen(
    isPartialAccess: Boolean,
    onSelectMoreClick: () -> Unit,
    onAllowAllClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomePlaceholderViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Access may have changed while away (partial picker, settings); the first resume is the initial load.
    var hasResumedOnce by remember { mutableStateOf(false) }
    LifecycleResumeEffect(viewModel) {
        if (hasResumedOnce) viewModel.onEvent(HomePlaceholderUiEvent.Refresh) else hasResumedOnce = true
        onPauseOrDispose { }
    }

    HomePlaceholderContent(
        uiState = uiState,
        isPartialAccess = isPartialAccess,
        onSelectMoreClick = onSelectMoreClick,
        onAllowAllClick = onAllowAllClick,
        modifier = modifier,
    )
}

@Composable
fun HomePlaceholderContent(
    uiState: HomePlaceholderUiState,
    isPartialAccess: Boolean,
    onSelectMoreClick: () -> Unit,
    onAllowAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(SwipeItDimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(SwipeItDimens.SpacingLarge),
    ) {
        if (isPartialAccess) {
            PartialAccessBanner(
                onSelectMoreClick = onSelectMoreClick,
                onAllowAllClick = onAllowAllClick,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SwipeItDimens.SpacingMedium, Alignment.CenterVertically),
        ) {
            Text(
                text = when {
                    uiState.isLoading -> stringResource(R.string.home_loading)
                    else -> pluralStringResource(R.plurals.home_photos_found, uiState.photoCount, uiState.photoCount)
                },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            uiState.firstPhoto?.let { photo -> FirstPhotoCard(photo) }
        }
    }
}

@Composable
private fun FirstPhotoCard(photo: Photo) {
    val shape = RoundedCornerShape(SwipeItDimens.CardCornerRadius)
    AsyncImage(
        model = photo.uri,
        contentDescription = photo.name,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .width(SwipeItDimens.PreviewCardWidth)
            .aspectRatio(SwipeItDimens.CardAspectRatio)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(BorderStroke(SwipeItDimens.BorderWidth, MaterialTheme.colorScheme.outline), shape),
    )
    Text(
        text = photo.name,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private val previewPhoto = Photo(
    id = 1L,
    uri = "content://media/external/images/media/1",
    name = "Screenshot_20261020.png",
    sizeBytes = 1_234_567L,
    dateTaken = 0L,
    album = "Screenshots",
    width = 1080,
    height = 2400,
)

@Preview(showSystemUi = true)
@Composable
private fun HomeLoadingPreview() {
    SwipeItTheme {
        HomePlaceholderContent(HomePlaceholderUiState(), isPartialAccess = false, onSelectMoreClick = {}, onAllowAllClick = {})
    }
}

@Preview(showSystemUi = true)
@Composable
private fun HomeGrantedPreview() {
    SwipeItTheme {
        HomePlaceholderContent(
            HomePlaceholderUiState(isLoading = false, photoCount = 1_284, firstPhoto = previewPhoto),
            isPartialAccess = false,
            onSelectMoreClick = {},
            onAllowAllClick = {},
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun HomePartialPreview() {
    SwipeItTheme {
        HomePlaceholderContent(
            HomePlaceholderUiState(isLoading = false, photoCount = 3, firstPhoto = previewPhoto),
            isPartialAccess = true,
            onSelectMoreClick = {},
            onAllowAllClick = {},
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun HomeEmptyPreview() {
    SwipeItTheme {
        HomePlaceholderContent(
            HomePlaceholderUiState(isLoading = false, photoCount = 0),
            isPartialAccess = false,
            onSelectMoreClick = {},
            onAllowAllClick = {},
        )
    }
}
