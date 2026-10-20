package com.swipeit.app.feature.sort

import androidx.compose.foundation.background
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.swipeit.app.R
import com.swipeit.app.core.designsystem.SwipeItDimens
import com.swipeit.app.core.designsystem.SwipeItIcons
import com.swipeit.app.core.designsystem.SwipeItTheme
import com.swipeit.app.core.designsystem.component.EmptyState
import com.swipeit.app.core.designsystem.component.ProgressHeaderCard
import com.swipeit.app.core.designsystem.component.SwipeItLogo
import com.swipeit.app.core.permissions.PartialAccessBanner
import com.swipeit.app.domain.model.Photo
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@Composable
fun SortContent(
    uiState: SortUiState,
    isPartialAccess: Boolean,
    snackbarHostState: SnackbarHostState,
    onEvent: (SortUiEvent) -> Unit,
    onSelectMoreClick: () -> Unit,
    onAllowAllClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onOpenPreview: (photoId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val topPhoto = uiState.queue.firstOrNull()
    // New state per top photo: a fresh card never inherits the previous card's offset.
    // An undone photo starts off screen and flies back in from the side it left.
    val returning = uiState.returningCard
    val topCardState = remember(topPhoto?.id) {
        SwipeCardState().apply {
            if (returning != null && returning.photoId == topPhoto?.id) prepareEntry(returning.direction)
        }
    }
    LaunchedEffect(topCardState) {
        if (topCardState.pendingEntry != null) {
            topCardState.playEntry()
            onEvent(SortUiEvent.ReturnAnimationFinished)
        }
    }
    val onSwiped: (Photo, SwipeDirection) -> Unit = { photo, direction ->
        onEvent(if (direction.isKeep) SortUiEvent.Keep(photo, direction) else SortUiEvent.Trash(photo))
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = SwipeItDimens.ScreenPaddingCompact),
            verticalArrangement = Arrangement.spacedBy(SwipeItDimens.SpacingMedium),
        ) {
            SortHeader(
                canUndo = uiState.canUndo,
                onUndoClick = { onEvent(SortUiEvent.Undo) },
                onSettingsClick = onSettingsClick,
            )
            if (isPartialAccess) {
                PartialAccessBanner(onSelectMoreClick = onSelectMoreClick, onAllowAllClick = onAllowAllClick)
            }
            if (uiState.total > 0) {
                ProgressHeaderCard(remaining = uiState.remaining, total = uiState.total)
            }
            RecentStrip(items = uiState.recent, onItemClick = onOpenPreview)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    uiState.isLoading -> {
                        val loadingLabel = stringResource(R.string.sort_loading)
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.semantics { contentDescription = loadingLabel },
                        )
                    }
                    uiState.isEmpty -> EmptyState(
                        iconRes = SwipeItIcons.Photos,
                        title = stringResource(R.string.sort_empty_title),
                        message = stringResource(R.string.sort_empty_message),
                    )
                    else -> CardStack(
                        photos = uiState.queue,
                        topCardState = topCardState,
                        onSwiped = onSwiped,
                        onExpandClick = { onOpenPreview(it.id) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            if (topPhoto != null) {
                SwipeActionRow(
                    // Same path as a drag: fly the card out, then decide.
                    onActionClick = { direction ->
                        scope.launch {
                            if (topCardState.animateOut(direction)) onSwiped(topPhoto, direction)
                        }
                    },
                    enabled = !topCardState.isLeaving,
                )
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun SortHeader(
    canUndo: Boolean,
    onUndoClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SwipeItLogo(modifier = Modifier.weight(1f))
        IconButton(onClick = onUndoClick, enabled = canUndo) {
            Icon(
                painter = painterResource(SwipeItIcons.Undo),
                contentDescription = stringResource(R.string.action_undo),
            )
        }
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
private fun SortContentPreview(uiState: SortUiState, isPartialAccess: Boolean = false) {
    SwipeItTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            SortContent(
                uiState = uiState,
                isPartialAccess = isPartialAccess,
                snackbarHostState = remember { SnackbarHostState() },
                onEvent = {},
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
private fun SortDeckPreview() {
    SortContentPreview(
        SortUiState(
            queue = previewPhotos.toImmutableList(),
            remaining = 342,
            total = 1_200,
            isLoading = false,
            recent = previewRecent.toImmutableList(),
        ),
    )
}

@Preview(showSystemUi = true)
@Composable
private fun SortPartialPreview() {
    SortContentPreview(
        SortUiState(queue = previewPhotos.take(2).toImmutableList(), remaining = 2, total = 5, isLoading = false),
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
    SortContentPreview(SortUiState(queue = persistentListOf(), remaining = 0, total = 1_200, isLoading = false))
}
