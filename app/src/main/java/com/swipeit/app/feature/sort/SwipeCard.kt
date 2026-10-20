package com.swipeit.app.feature.sort

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.swipeit.app.R
import com.swipeit.app.core.designsystem.SwipeItColors
import com.swipeit.app.core.designsystem.SwipeItDimens
import com.swipeit.app.core.designsystem.SwipeItIcons
import com.swipeit.app.core.designsystem.SwipeItMotion
import com.swipeit.app.core.designsystem.SwipeItTheme
import com.swipeit.app.domain.model.Photo
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

private val CardPivot = TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 0.85f)

/**
 * A deck card at [depth] (0 = top). Only the top card gets a [state]: it is draggable, tilts,
 * and shows KEEP!/TRASH feedback. [onSwiped] fires after the card has flown off screen.
 * The same composable is used at every depth so promotion never reloads the image.
 */
@Composable
fun SwipeCard(
    photo: Photo,
    depth: Int,
    state: SwipeCardState?,
    onSwiped: (SwipeDirection) -> Unit,
    onExpandClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnSwiped by rememberUpdatedState(onSwiped)
    val animatedDepth by animateFloatAsState(
        targetValue = depth.toFloat(),
        animationSpec = SwipeItMotion.stackPromote(),
        label = "cardDepth",
    )
    val stackOffsetPx = with(LocalDensity.current) { SwipeItDimens.StackOffsetStep.toPx() }
    val shape = MaterialTheme.shapes.large

    Box(
        modifier = modifier
            .then(if (state != null) Modifier.swipeable(state) { currentOnSwiped(it) } else Modifier)
            .graphicsLayer {
                val stackScale = 1f - animatedDepth * SwipeItMotion.StackScaleStep
                scaleX = stackScale
                scaleY = stackScale
                translationX = state?.offset?.x ?: 0f
                translationY = animatedDepth * stackOffsetPx + (state?.offset?.y ?: 0f)
                rotationZ = stackRotation(animatedDepth) + (state?.rotationDegrees ?: 0f)
                transformOrigin = CardPivot
                this.shape = shape
                clip = true
            },
    ) {
        PhotoCardFace(photo = photo, onExpandClick = onExpandClick)
        if (state != null) SwipeFeedbackOverlay(state)
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer { alpha = (animatedDepth * SwipeItDimens.StackDimStep).coerceIn(0f, 1f) }
                .background(SwipeItColors.Background),
        )
    }
}

private fun stackRotation(depth: Float): Float {
    val rotations = SwipeItMotion.StackRotations
    val lower = depth.toInt().coerceIn(0, rotations.lastIndex)
    val upper = (lower + 1).coerceAtMost(rotations.lastIndex)
    return lerp(rotations[lower], rotations[upper], depth - lower)
}

/**
 * Drag handling. Placed before the transform in the modifier chain so pointer positions stay
 * in the parent's (untranslated) space while the card moves.
 */
private fun Modifier.swipeable(state: SwipeCardState, onSwiped: (SwipeDirection) -> Unit): Modifier =
    this
        .onSizeChanged { state.size = it }
        .pointerInput(state) {
            val velocityTracker = VelocityTracker()
            coroutineScope {
                detectDragGestures(
                    onDragStart = { velocityTracker.resetTracking() },
                    onDragEnd = {
                        val velocity = velocityTracker.calculateVelocity()
                        launch { state.settle(velocity)?.let(onSwiped) }
                    },
                    onDragCancel = { launch { state.snapBack() } },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                        launch { state.dragBy(dragAmount) }
                    },
                )
            }
        }

/** Green/coral tint and KEEP!/TRASH stamps, faded in with drag distance (draw phase only). */
@Composable
private fun BoxScope.SwipeFeedbackOverlay(state: SwipeCardState) {
    Box(
        Modifier
            .matchParentSize()
            .graphicsLayer { alpha = state.keepProgress }
            .background(SwipeItColors.Keep.copy(alpha = SwipeItDimens.SwipeTintMaxAlpha)),
    )
    Box(
        Modifier
            .matchParentSize()
            .graphicsLayer { alpha = state.trashProgress }
            .background(SwipeItColors.Trash.copy(alpha = SwipeItDimens.SwipeTintMaxAlpha)),
    )
    SwipeStamp(
        text = stringResource(R.string.swipe_stamp_keep),
        iconRes = SwipeItIcons.Keep,
        color = SwipeItColors.Keep,
        rotationDegrees = -SwipeItDimens.StampRotationDegrees,
        progress = { state.keepProgress },
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(SwipeItDimens.StampInset),
    )
    SwipeStamp(
        text = stringResource(R.string.swipe_stamp_trash),
        iconRes = SwipeItIcons.Trash,
        color = SwipeItColors.Trash,
        rotationDegrees = SwipeItDimens.StampRotationDegrees,
        progress = { state.trashProgress },
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(SwipeItDimens.StampInset),
    )
}

@Composable
private fun SwipeStamp(
    text: String,
    @DrawableRes iconRes: Int,
    color: Color,
    rotationDegrees: Float,
    progress: () -> Float,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .graphicsLayer {
                alpha = progress()
                rotationZ = rotationDegrees
            }
            .border(SwipeItDimens.StampBorderWidth, color, MaterialTheme.shapes.small)
            .padding(horizontal = SwipeItDimens.StampPadding, vertical = SwipeItDimens.SpacingXSmall),
        horizontalArrangement = Arrangement.spacedBy(SwipeItDimens.SpacingSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(SwipeItDimens.StampIconSize),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = color,
        )
    }
}

@Composable
private fun SwipeCardPreview(offset: Offset) {
    SwipeItTheme {
        val state = remember { SwipeCardState(initialOffset = offset) }
        SwipeCard(
            photo = previewPhotos.first(),
            depth = 0,
            state = state,
            onSwiped = {},
            onExpandClick = {},
            modifier = Modifier
                .padding(48.dp)
                .width(280.dp)
                .aspectRatio(3f / 4f),
        )
    }
}

@Preview(widthDp = 400, heightDp = 520, showBackground = true, backgroundColor = 0xFF0B0B10)
@Composable
private fun SwipeCardKeepPreview() {
    SwipeCardPreview(Offset(260f, -40f))
}

@Preview(widthDp = 400, heightDp = 520, showBackground = true, backgroundColor = 0xFF0B0B10)
@Composable
private fun SwipeCardTrashPreview() {
    SwipeCardPreview(Offset(-260f, 20f))
}

@Preview(widthDp = 400, heightDp = 520, showBackground = true, backgroundColor = 0xFF0B0B10)
@Composable
private fun SwipeCardUpPreview() {
    SwipeCardPreview(Offset(0f, -300f))
}
