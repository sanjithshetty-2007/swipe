package com.swipeit.app.feature.preview

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.swipeit.app.R
import com.swipeit.app.core.designsystem.SwipeItColors
import com.swipeit.app.core.designsystem.SwipeItDimens
import com.swipeit.app.core.designsystem.SwipeItIcons
import com.swipeit.app.core.designsystem.SwipeItTheme
import com.swipeit.app.core.designsystem.component.photoInfoSubtitle
import com.swipeit.app.domain.model.Decision
import kotlinx.collections.immutable.ImmutableList

@Composable
fun PreviewTopBar(
    title: String,
    canReset: Boolean,
    onCloseClick: () -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(SwipeItDimens.TopBarHeight)
            .padding(horizontal = SwipeItDimens.SpacingXSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onCloseClick) {
            Icon(painterResource(SwipeItIcons.Close), contentDescription = stringResource(R.string.preview_close))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(painterResource(SwipeItIcons.More), contentDescription = stringResource(R.string.preview_more_options))
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.preview_reset_unsorted)) },
                    enabled = canReset,
                    onClick = {
                        menuOpen = false
                        onResetClick()
                    },
                )
            }
        }
    }
}

/** Filename, "date · size" and the current decision chip. */
@Composable
fun PreviewDetails(item: PreviewItem, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SwipeItDimens.SpacingMedium),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.photo.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = photoInfoSubtitle(item.photo.dateTaken, item.photo.sizeBytes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DecisionChip(decision = item.decision)
    }
}

@Composable
fun DecisionChip(decision: Decision?, modifier: Modifier = Modifier) {
    val (color, labelRes, iconRes) = when (decision) {
        Decision.KEPT -> Triple(SwipeItColors.Keep, R.string.decision_kept, SwipeItIcons.Keep)
        Decision.TRASHED -> Triple(SwipeItColors.Trash, R.string.decision_trashed, SwipeItIcons.Trash)
        null -> Triple(SwipeItColors.TextSecondary, R.string.decision_unsorted, SwipeItIcons.Sort)
    }
    Surface(
        modifier = modifier.height(SwipeItDimens.ChipHeight),
        shape = CircleShape,
        color = color.copy(alpha = CHIP_CONTAINER_ALPHA),
        border = BorderStroke(SwipeItDimens.BorderWidth, color),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = SwipeItDimens.SpacingSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SwipeItDimens.SpacingXSmall),
        ) {
            Icon(painterResource(iconRes), contentDescription = null, tint = color, modifier = Modifier.size(SwipeItDimens.RecentBadgeSize))
            Text(text = stringResource(labelRes), style = MaterialTheme.typography.labelMedium, color = color)
        }
    }
}

private const val CHIP_CONTAINER_ALPHA = 0.15f
private const val THUMBNAILS_BEFORE_SELECTED = 2

/** "Move to Trash" (coral) and "Keep" (green); the button matching [decision] is filled. */
@Composable
fun DecisionButtons(
    decision: Decision?,
    onTrashClick: () -> Unit,
    onKeepClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SwipeItDimens.SpacingMedium),
    ) {
        DecisionButton(
            text = stringResource(R.string.action_trash),
            iconRes = SwipeItIcons.Trash,
            color = SwipeItColors.Trash,
            selected = decision == Decision.TRASHED,
            onClick = onTrashClick,
            modifier = Modifier.weight(1f),
        )
        DecisionButton(
            text = stringResource(R.string.action_keep),
            iconRes = SwipeItIcons.Keep,
            color = SwipeItColors.Keep,
            selected = decision == Decision.KEPT,
            onClick = onKeepClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DecisionButton(
    text: String,
    @DrawableRes iconRes: Int,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val buttonModifier = modifier
        .height(SwipeItDimens.ButtonHeight)
        .semantics { this.selected = selected }
    val content: @Composable () -> Unit = {
        Icon(painterResource(iconRes), contentDescription = null, modifier = Modifier.size(SwipeItDimens.IconSize))
        Text(text = text, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = SwipeItDimens.SpacingSmall))
    }
    if (selected) {
        Button(
            onClick = onClick,
            modifier = buttonModifier,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = SwipeItColors.OnAccent),
        ) { content() }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = buttonModifier,
            shape = CircleShape,
            border = BorderStroke(SwipeItDimens.BorderWidth, color),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = color),
        ) { content() }
    }
}

/** Small thumbnails under the pager; the current one has a green border. */
@Composable
fun PreviewThumbnailRow(
    items: ImmutableList<PreviewItem>,
    selectedIndex: Int,
    onThumbnailClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Long lists (trash) scroll; keep the current thumbnail in view as the pager moves.
    val listState = rememberLazyListState()
    LaunchedEffect(selectedIndex) {
        listState.animateScrollToItem((selectedIndex - THUMBNAILS_BEFORE_SELECTED).coerceAtLeast(0))
    }
    LazyRow(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SwipeItDimens.SpacingSmall, Alignment.CenterHorizontally),
    ) {
        itemsIndexed(items, key = { _, item -> item.photo.id }) { index, item ->
            val isSelected = index == selectedIndex
            val shape = RoundedCornerShape(SwipeItDimens.CornerSmall)
            AsyncImage(
                model = item.photo.uri,
                contentDescription = item.photo.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(SwipeItDimens.PreviewThumbSize)
                    .clip(shape)
                    .border(
                        width = if (isSelected) SwipeItDimens.PreviewThumbSelectedBorder else SwipeItDimens.BorderWidth,
                        color = if (isSelected) SwipeItColors.Keep else SwipeItColors.Outline,
                        shape = shape,
                    )
                    .clickable(role = Role.Tab) { onThumbnailClick(index) }
                    .semantics { this.selected = isSelected },
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0B10)
@Composable
private fun PreviewControlsPreview() {
    SwipeItTheme {
        val item = remember { previewItems[1] }
        Column(
            modifier = Modifier.padding(SwipeItDimens.SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(SwipeItDimens.SpacingMedium),
        ) {
            PreviewTopBar(title = "2 of 3 (recent)", canReset = true, onCloseClick = {}, onResetClick = {})
            PreviewThumbnailRow(items = previewItems, selectedIndex = 1, onThumbnailClick = {})
            PreviewDetails(item = item)
            Row(horizontalArrangement = Arrangement.spacedBy(SwipeItDimens.SpacingSmall)) {
                DecisionChip(Decision.KEPT)
                DecisionChip(Decision.TRASHED)
                DecisionChip(null)
            }
            DecisionButtons(decision = Decision.TRASHED, onTrashClick = {}, onKeepClick = {})
        }
    }
}
