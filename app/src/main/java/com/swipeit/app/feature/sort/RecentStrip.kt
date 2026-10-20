package com.swipeit.app.feature.sort

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.swipeit.app.R
import com.swipeit.app.core.designsystem.SwipeItColors
import com.swipeit.app.core.designsystem.SwipeItDimens
import com.swipeit.app.core.designsystem.SwipeItIcons
import com.swipeit.app.core.designsystem.SwipeItMotion
import com.swipeit.app.core.designsystem.SwipeItTheme
import com.swipeit.app.domain.model.DecidedPhoto
import com.swipeit.app.domain.model.Decision
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay

/** A strip entry that is still on screen, plus where it was when it got removed. */
private data class StripEntry(val item: DecidedPhoto, val exitSlot: Int? = null)

/**
 * "Recent 5": overlapping, tilted thumbnails, oldest left to newest right. New entries slide in
 * from the right; removed ones drop and fade where they were. Hidden when there is nothing yet.
 */
@Composable
fun RecentStrip(
    items: ImmutableList<DecidedPhoto>,
    onItemClick: (photoId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val initialIds = remember { items.mapTo(HashSet()) { it.photo.id } }
    var previous by remember { mutableStateOf(items) }
    var exiting by remember { mutableStateOf<List<StripEntry>>(emptyList()) }

    LaunchedEffect(items) {
        val currentIds = items.mapTo(HashSet()) { it.photo.id }
        val removed = previous.mapIndexedNotNull { slot, item ->
            if (item.photo.id in currentIds) null else StripEntry(item, exitSlot = slot)
        }
        previous = items
        if (removed.isNotEmpty()) {
            exiting = exiting + removed
            delay(SwipeItMotion.DurationLong.toLong())
            val removedIds = removed.mapTo(HashSet()) { it.item.photo.id }
            exiting = exiting.filterNot { it.item.photo.id in removedIds }
        }
    }

    if (items.isEmpty() && exiting.isEmpty()) return

    val step = SwipeItDimens.RecentThumbWidth - SwipeItDimens.RecentThumbOverlap
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(SwipeItDimens.RecentStripHeight),
        horizontalArrangement = Arrangement.spacedBy(SwipeItDimens.SpacingMedium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.sort_recent_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
            modifier = Modifier
                .width(SwipeItDimens.RecentThumbWidth + step * (MAX_RECENT - 1))
                .height(SwipeItDimens.RecentThumbHeight),
        ) {
            val entries = exiting + items.map { StripEntry(it) }
            entries.forEach { entry ->
                key(entry.item.photo.id, entry.exitSlot != null) {
                    val slot = entry.exitSlot ?: items.indexOfFirst { it.photo.id == entry.item.photo.id }
                    RecentThumb(
                        item = entry.item,
                        slot = slot,
                        isExiting = entry.exitSlot != null,
                        animateEntry = entry.item.photo.id !in initialIds,
                        onClick = { onItemClick(entry.item.photo.id) },
                    )
                }
            }
        }
    }
}

private const val MAX_RECENT = 5

@Composable
private fun RecentThumb(
    item: DecidedPhoto,
    slot: Int,
    isExiting: Boolean,
    animateEntry: Boolean,
    onClick: () -> Unit,
) {
    val density = LocalDensity.current
    val stepPx = with(density) { (SwipeItDimens.RecentThumbWidth - SwipeItDimens.RecentThumbOverlap).toPx() }
    val slidePx = with(density) { SwipeItDimens.RecentSlideDistance.toPx() }
    val fallPx = with(density) { SwipeItDimens.RecentFallDistance.toPx() }

    val animatedSlot by animateFloatAsState(slot.toFloat(), SwipeItMotion.stackPromote(), label = "recentSlot")
    val appear = remember { Animatable(if (animateEntry && !isExiting) 0f else 1f) }
    LaunchedEffect(isExiting) {
        appear.animateTo(if (isExiting) 0f else 1f, tween(SwipeItMotion.DurationLong))
    }
    val tilt = if (item.photo.id % 2 == 0L) -SwipeItMotion.RecentTiltDegrees else SwipeItMotion.RecentTiltDegrees
    val shape = RoundedCornerShape(SwipeItDimens.RecentThumbCorner)
    val label = stringResource(
        R.string.sort_recent_item,
        item.photo.name,
        stringResource(if (item.decision == Decision.KEPT) R.string.decision_kept else R.string.decision_trashed),
    )

    Box(
        modifier = Modifier
            .size(SwipeItDimens.RecentThumbWidth, SwipeItDimens.RecentThumbHeight)
            .graphicsLayer {
                val hidden = 1f - appear.value
                // Entering slides in from the right; exiting drifts left while it falls.
                translationX = animatedSlot * stepPx + if (isExiting) -hidden * slidePx else hidden * slidePx
                translationY = if (isExiting) hidden * fallPx else 0f
                rotationZ = tilt * (1f + if (isExiting) hidden * 2f else 0f)
                alpha = appear.value
            }
            .clickable(enabled = !isExiting, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
    ) {
        val context = LocalPlatformContext.current
        val request = remember(item.photo.uri) {
            ImageRequest.Builder(context).data(item.photo.uri).crossfade(SwipeItMotion.DurationShort).build()
        }
        AsyncImage(
            model = request,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    this.shape = shape
                    clip = true
                }
                .background(SwipeItColors.SurfaceVariant)
                .border(SwipeItDimens.BorderWidth, SwipeItColors.Outline, shape),
        )
        DecisionBadge(
            decision = item.decision,
            modifier = Modifier.align(Alignment.BottomEnd),
        )
    }
}

@Composable
private fun DecisionBadge(decision: Decision, modifier: Modifier = Modifier) {
    val isKept = decision == Decision.KEPT
    Box(
        modifier = modifier
            .size(SwipeItDimens.RecentBadgeSize)
            .background(SwipeItColors.Background, CircleShape)
            .border(SwipeItDimens.BorderWidth, if (isKept) SwipeItColors.Keep else SwipeItColors.Trash, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(if (isKept) SwipeItIcons.Keep else SwipeItIcons.Trash),
            contentDescription = null,
            tint = if (isKept) SwipeItColors.Keep else SwipeItColors.Trash,
            modifier = Modifier.size(SwipeItDimens.RecentBadgeIconSize),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0B10, widthDp = 360)
@Composable
private fun RecentStripPreview() {
    SwipeItTheme {
        RecentStrip(
            items = (previewRecent + previewRecent.first().copy(photo = previewPhotos[0].copy(id = 200L)))
                .toImmutableList(),
            onItemClick = {},
        )
    }
}
