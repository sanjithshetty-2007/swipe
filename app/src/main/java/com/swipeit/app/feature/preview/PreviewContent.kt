package com.swipeit.app.feature.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.swipeit.app.R
import com.swipeit.app.core.designsystem.SwipeItDimens
import com.swipeit.app.core.designsystem.SwipeItIcons
import com.swipeit.app.core.designsystem.SwipeItTheme
import com.swipeit.app.core.designsystem.component.EmptyState
import com.swipeit.app.core.designsystem.component.SwipeItTopBar
import com.swipeit.app.domain.model.Decision
import com.swipeit.app.domain.model.Photo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@Composable
fun PreviewContent(
    uiState: PreviewUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (PreviewUiEvent) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Center),
            )
            uiState.isNotFound -> Column(Modifier.fillMaxSize()) {
                SwipeItTopBar(title = stringResource(R.string.preview_title), onBackClick = onCloseClick)
                EmptyState(
                    iconRes = SwipeItIcons.Photos,
                    title = stringResource(R.string.preview_not_found_title),
                    message = stringResource(R.string.preview_not_found_message),
                )
            }
            else -> PreviewPager(
                items = uiState.items,
                initialPage = uiState.initialPage,
                source = uiState.source,
                onEvent = onEvent,
                onCloseClick = onCloseClick,
            )
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun PreviewPager(
    items: ImmutableList<PreviewItem>,
    initialPage: Int,
    source: PreviewSource?,
    onEvent: (PreviewUiEvent) -> Unit,
    onCloseClick: () -> Unit,
) {
    val pagerState = rememberPagerState(initialPage = initialPage.coerceIn(0, items.lastIndex)) { items.size }
    val scope = rememberCoroutineScope()
    var isZoomed by remember { mutableStateOf(false) }
    LaunchedEffect(pagerState.currentPage) { isZoomed = false }

    val current = items[pagerState.currentPage.coerceIn(0, items.lastIndex)]
    val change: (Decision?) -> Unit = { decision -> onEvent(PreviewUiEvent.ChangeDecision(current.photo, decision)) }

    Column(modifier = Modifier.fillMaxSize()) {
        PreviewTopBar(
            title = when (source) {
                PreviewSource.RECENT -> stringResource(R.string.preview_title_recent, pagerState.currentPage + 1, items.size)
                PreviewSource.TRASH -> stringResource(R.string.preview_title_trash, pagerState.currentPage + 1, items.size)
                null -> stringResource(R.string.preview_title)
            },
            canReset = current.decision != null,
            onCloseClick = onCloseClick,
            onResetClick = { change(null) },
        )
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = !isZoomed,
            key = { page -> items[page].photo.id },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) { page ->
            val item = items[page]
            ZoomableImage(
                imageUri = item.photo.uri,
                contentDescription = item.photo.name,
                isActive = page == pagerState.currentPage,
                onZoomedChange = { zoomed -> if (page == pagerState.currentPage) isZoomed = zoomed },
            )
        }
        Column(
            modifier = Modifier.padding(SwipeItDimens.ScreenPaddingCompact),
            verticalArrangement = Arrangement.spacedBy(SwipeItDimens.SpacingMedium),
        ) {
            if (items.size > 1) {
                PreviewThumbnailRow(
                    items = items,
                    selectedIndex = pagerState.currentPage,
                    onThumbnailClick = { index -> scope.launch { pagerState.animateScrollToPage(index) } },
                )
            }
            PreviewDetails(item = current)
            DecisionButtons(
                decision = current.decision,
                onTrashClick = { change(Decision.TRASHED) },
                onKeepClick = { change(Decision.KEPT) },
            )
        }
    }
}

internal val previewItems: ImmutableList<PreviewItem> = List(3) { index ->
    PreviewItem(
        photo = Photo(
            id = index + 1L,
            uri = "content://media/external/images/media/${index + 1}",
            name = "IMG_20241112_18145$index.jpg",
            sizeBytes = 3_565_158L,
            dateTaken = 1_731_434_095_000L,
            album = "Camera",
            width = 0,
            height = 0,
        ),
        decision = listOf(Decision.KEPT, Decision.TRASHED, null)[index],
    )
}.toImmutableList()

@Composable
private fun PreviewContentPreview(uiState: PreviewUiState) {
    SwipeItTheme {
        PreviewContent(
            uiState = uiState,
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
            onCloseClick = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun PreviewRecentPreview() {
    PreviewContentPreview(
        PreviewUiState(items = previewItems, initialPage = 1, source = PreviewSource.RECENT, isLoading = false),
    )
}

@Preview(showSystemUi = true)
@Composable
private fun PreviewSinglePreview() {
    PreviewContentPreview(
        PreviewUiState(items = previewItems.takeLast(1).toImmutableList(), source = null, isLoading = false),
    )
}

@Preview(showSystemUi = true)
@Composable
private fun PreviewNotFoundPreview() {
    PreviewContentPreview(PreviewUiState(isLoading = false))
}
