package com.swipeit.app.feature.trash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.swipeit.app.R
import com.swipeit.app.core.common.ByteFormatter
import com.swipeit.app.core.designsystem.SwipeItColors
import com.swipeit.app.core.designsystem.SwipeItDangerButton
import com.swipeit.app.core.designsystem.SwipeItDimens
import com.swipeit.app.core.designsystem.SwipeItIcons
import com.swipeit.app.core.designsystem.SwipeItSecondaryButton
import com.swipeit.app.core.designsystem.SwipeItTheme
import com.swipeit.app.core.designsystem.component.EmptyState
import com.swipeit.app.domain.model.DecidedPhoto
import com.swipeit.app.domain.model.Decision
import com.swipeit.app.domain.model.Photo
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList

@Composable
fun TrashContent(
    uiState: TrashUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (TrashUiEvent) -> Unit,
    onOpenPreview: (photoId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TrashHeader(uiState = uiState, onEvent = onEvent)
            when {
                uiState.isLoading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                uiState.isEmpty -> EmptyState(
                    iconRes = SwipeItIcons.Trash,
                    iconTint = SwipeItColors.Trash,
                    title = stringResource(R.string.trash_empty_title),
                    message = stringResource(R.string.trash_placeholder_message),
                    modifier = Modifier.weight(1f),
                )
                else -> {
                    TrashGrid(
                        photos = uiState.photos,
                        isSelectionMode = uiState.isSelectionMode,
                        selectedIds = uiState.selectedIds,
                        onPhotoClick = { id ->
                            if (uiState.isSelectionMode) onEvent(TrashUiEvent.ToggleSelected(id)) else onOpenPreview(id)
                        },
                        onPhotoLongClick = { id ->
                            if (uiState.isSelectionMode) {
                                onEvent(TrashUiEvent.ToggleSelected(id))
                            } else {
                                onEvent(TrashUiEvent.StartSelectionWith(id))
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                    TrashBottomActions(uiState = uiState, onEvent = onEvent)
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }

    uiState.pendingConfirmation?.let { target ->
        val count = uiState.countFor(target)
        AlertDialog(
            onDismissRequest = { onEvent(TrashUiEvent.DismissConfirmation) },
            title = { Text(stringResource(R.string.trash_confirm_title)) },
            text = { Text(pluralStringResource(R.plurals.trash_confirm_message, count, count)) },
            confirmButton = {
                TextButton(onClick = { onEvent(TrashUiEvent.ConfirmDelete) }) {
                    Text(stringResource(R.string.trash_confirm_delete), color = SwipeItColors.Trash)
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(TrashUiEvent.DismissConfirmation) }) {
                    Text(stringResource(R.string.trash_confirm_cancel))
                }
            },
        )
    }
}

@Composable
private fun TrashHeader(uiState: TrashUiState, onEvent: (TrashUiEvent) -> Unit) {
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    val locale = LocalConfiguration.current.locales[0]
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = SwipeItDimens.ScreenPaddingCompact, end = SwipeItDimens.SpacingXSmall, top = SwipeItDimens.SpacingSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SwipeItDimens.SpacingMedium),
    ) {
        Box(
            modifier = Modifier
                .size(SwipeItDimens.IconBadgeSize)
                .background(SwipeItColors.Trash.copy(alpha = HEADER_ICON_ALPHA), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(SwipeItIcons.Trash), contentDescription = null, tint = SwipeItColors.Trash)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (uiState.isSelectionMode) {
                    pluralStringResource(R.plurals.trash_selected_count, uiState.selectedCount, uiState.selectedCount)
                } else {
                    stringResource(R.string.trash_title)
                },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (!uiState.isLoading) {
                Text(
                    text = pluralStringResource(
                        R.plurals.trash_summary,
                        uiState.photos.size,
                        uiState.photos.size,
                        ByteFormatter.format(uiState.totalBytes, locale),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (!uiState.isEmpty && !uiState.isLoading) {
            TextButton(
                onClick = { onEvent(if (uiState.isSelectionMode) TrashUiEvent.ExitSelection else TrashUiEvent.EnterSelection) },
                enabled = !uiState.isDeleting,
            ) {
                Text(stringResource(if (uiState.isSelectionMode) R.string.trash_cancel_selection else R.string.trash_select))
            }
            Box {
                IconButton(onClick = { menuOpen = true }, enabled = !uiState.isDeleting) {
                    Icon(painterResource(SwipeItIcons.More), contentDescription = stringResource(R.string.preview_more_options))
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.trash_restore_all)) },
                        onClick = {
                            menuOpen = false
                            onEvent(TrashUiEvent.RestoreAll)
                        },
                    )
                }
            }
        }
    }
}

private const val HEADER_ICON_ALPHA = 0.15f

/** Selection bar (Restore / Delete) in selection mode, otherwise the pinned "Delete All" button. */
@Composable
private fun TrashBottomActions(uiState: TrashUiState, onEvent: (TrashUiEvent) -> Unit) {
    val modifier = Modifier
        .fillMaxWidth()
        .padding(SwipeItDimens.ScreenPaddingCompact)
    if (uiState.isSelectionMode) {
        val hasSelection = uiState.selectedCount > 0 && !uiState.isDeleting
        Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(SwipeItDimens.SpacingMedium)) {
            SwipeItSecondaryButton(
                text = stringResource(R.string.trash_restore),
                onClick = { onEvent(TrashUiEvent.RestoreSelected) },
                modifier = Modifier.weight(1f),
                enabled = hasSelection,
            )
            SwipeItDangerButton(
                text = stringResource(R.string.trash_delete),
                onClick = { onEvent(TrashUiEvent.Delete(DeleteTarget.SELECTED)) },
                modifier = Modifier.weight(1f),
                enabled = hasSelection,
                isLoading = uiState.isDeleting,
                loadingDescription = stringResource(R.string.trash_deleting),
            )
        }
    } else {
        SwipeItDangerButton(
            text = stringResource(R.string.trash_delete_all),
            onClick = { onEvent(TrashUiEvent.Delete(DeleteTarget.ALL)) },
            modifier = modifier,
            isLoading = uiState.isDeleting,
            loadingDescription = stringResource(R.string.trash_deleting),
        )
    }
}

internal val previewTrash: List<DecidedPhoto> = List(7) { index ->
    DecidedPhoto(
        photo = Photo(
            id = index + 1L,
            uri = "content://media/external/images/media/${index + 1}",
            name = "IMG_$index.jpg",
            sizeBytes = 2_400_000L + index * 300_000L,
            dateTaken = 1_731_434_095_000L,
            album = "Camera",
            width = 0,
            height = 0,
        ),
        decision = Decision.TRASHED,
        decidedAt = index.toLong(),
    )
}

@Composable
private fun TrashContentPreview(uiState: TrashUiState) {
    SwipeItTheme {
        TrashContent(
            uiState = uiState,
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
            onOpenPreview = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun TrashGridStatePreview() {
    TrashContentPreview(
        TrashUiState(photos = previewTrash.toImmutableList(), totalBytes = 24_000_000L, isLoading = false),
    )
}

@Preview(showSystemUi = true)
@Composable
private fun TrashSelectionPreview() {
    TrashContentPreview(
        TrashUiState(
            photos = previewTrash.toImmutableList(),
            totalBytes = 24_000_000L,
            isLoading = false,
            isSelectionMode = true,
            selectedIds = persistentSetOf(1L, 3L),
        ),
    )
}

@Preview(showSystemUi = true)
@Composable
private fun TrashEmptyPreview() {
    TrashContentPreview(TrashUiState(isLoading = false))
}

@Preview(showSystemUi = true)
@Composable
private fun TrashConfirmPreview() {
    TrashContentPreview(
        TrashUiState(
            photos = previewTrash.toImmutableList(),
            totalBytes = 24_000_000L,
            isLoading = false,
            pendingConfirmation = DeleteTarget.ALL,
        ),
    )
}
