package com.swipeit.app.feature.trash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.swipeit.app.R
import com.swipeit.app.core.designsystem.SwipeItColors
import com.swipeit.app.core.designsystem.SwipeItDimens
import com.swipeit.app.core.designsystem.SwipeItIcons
import com.swipeit.app.core.designsystem.SwipeItMotion
import com.swipeit.app.core.designsystem.SwipeItTheme
import com.swipeit.app.domain.model.DecidedPhoto
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList

const val TRASH_GRID_COLUMNS = 3
private const val SELECTED_CELL_SCALE = 0.88f

/** 3-column grid of trashed photos. Coil sizes each thumbnail to its cell. */
@Composable
fun TrashGrid(
    photos: ImmutableList<DecidedPhoto>,
    isSelectionMode: Boolean,
    selectedIds: ImmutableSet<Long>,
    onPhotoClick: (Long) -> Unit,
    onPhotoLongClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(TRASH_GRID_COLUMNS),
        modifier = modifier,
        contentPadding = PaddingValues(SwipeItDimens.ScreenPaddingCompact),
        horizontalArrangement = Arrangement.spacedBy(SwipeItDimens.SpacingXSmall),
        verticalArrangement = Arrangement.spacedBy(SwipeItDimens.SpacingXSmall),
    ) {
        items(photos, key = { it.photo.id }) { item ->
            TrashCell(
                item = item,
                isSelectionMode = isSelectionMode,
                isSelected = item.photo.id in selectedIds,
                onClick = { onPhotoClick(item.photo.id) },
                onLongClick = { onPhotoLongClick(item.photo.id) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TrashCell(
    item: DecidedPhoto,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) SELECTED_CELL_SCALE else 1f,
        animationSpec = SwipeItMotion.feedback(),
        label = "trashCellScale",
    )
    val shape = RoundedCornerShape(SwipeItDimens.CornerSmall)
    val selectionState = stringResource(if (isSelected) R.string.trash_selected_cd else R.string.trash_not_selected_cd)

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .combinedClickable(role = Role.Image, onClick = onClick, onLongClick = onLongClick)
            .semantics {
                if (isSelectionMode) {
                    selected = isSelected
                    stateDescription = selectionState
                }
            },
    ) {
        AsyncImage(
            model = item.photo.uri,
            contentDescription = item.photo.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(shape)
                .background(SwipeItColors.SurfaceVariant)
                .then(if (isSelected) Modifier.border(SwipeItDimens.RingWidth, SwipeItColors.Keep, shape) else Modifier),
        )
        if (isSelectionMode) {
            SelectionCheck(
                isSelected = isSelected,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(SwipeItDimens.SpacingSmall),
            )
        }
    }
}

@Composable
private fun SelectionCheck(isSelected: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(SwipeItDimens.RecentBadgeSize + SwipeItDimens.SpacingXSmall)
            .background(if (isSelected) SwipeItColors.Keep else SwipeItColors.OverlayScrim, CircleShape)
            .border(SwipeItDimens.BorderWidth * 2, if (isSelected) SwipeItColors.Keep else SwipeItColors.TextPrimary, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Icon(
                painter = painterResource(SwipeItIcons.Check),
                contentDescription = null,
                tint = SwipeItColors.OnAccent,
                modifier = Modifier.size(SwipeItDimens.RecentBadgeIconSize + SwipeItDimens.SpacingXSmall),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0B10)
@Composable
private fun TrashGridPreview() {
    SwipeItTheme {
        TrashGrid(
            photos = previewTrash.toImmutableList(),
            isSelectionMode = true,
            selectedIds = persistentSetOf(previewTrash[1].photo.id),
            onPhotoClick = {},
            onPhotoLongClick = {},
        )
    }
}
