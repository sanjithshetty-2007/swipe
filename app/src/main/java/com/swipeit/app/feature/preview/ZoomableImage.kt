package com.swipeit.app.feature.preview

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.lerp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import com.swipeit.app.core.designsystem.SwipeItMotion
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

private const val ZOOMED_EPSILON = 1.01f

/**
 * Full-resolution image with pinch-zoom, pan and double-tap zoom. At 1x, single-finger drags are
 * left unconsumed so a parent pager can still page. Resets when [isActive] turns false.
 */
@Composable
fun ZoomableImage(
    imageUri: String,
    contentDescription: String?,
    isActive: Boolean,
    onZoomedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    val scope = rememberCoroutineScope()
    val currentOnZoomedChange by rememberUpdatedState(onZoomedChange)

    LaunchedEffect(Unit) {
        snapshotFlow { scale > ZOOMED_EPSILON }.distinctUntilChanged().collect { currentOnZoomedChange(it) }
    }
    LaunchedEffect(isActive) {
        if (!isActive) {
            scale = 1f
            offset = Offset.Zero
        }
    }

    fun clamp(target: Offset, atScale: Float): Offset {
        val maxX = size.width * (atScale - 1f) / 2f
        val maxY = size.height * (atScale - 1f) / 2f
        return Offset(target.x.coerceIn(-maxX, maxX), target.y.coerceIn(-maxY, maxY))
    }

    val context = LocalPlatformContext.current
    // Full resolution is allowed here; Coil still caps decoding at its max bitmap size.
    val request = remember(imageUri) {
        ImageRequest.Builder(context)
            .data(imageUri)
            .size(Size.ORIGINAL)
            .crossfade(SwipeItMotion.DurationMedium)
            .build()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size = it }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val pressed = event.changes.count { it.pressed }
                        if (pressed >= 2 || scale > ZOOMED_EPSILON) {
                            val centroid = event.calculateCentroid(useCurrent = true)
                            if (centroid.isSpecified) {
                                val newScale = (scale * event.calculateZoom()).coerceIn(1f, SwipeItMotion.MaxZoom)
                                val factor = newScale / scale
                                val fromCenter = centroid - Offset(size.width / 2f, size.height / 2f)
                                // Keep the content under the fingers in place while scaling, then pan.
                                val newOffset = fromCenter * (1f - factor) + offset * factor + event.calculatePan()
                                scale = newScale
                                offset = clamp(newOffset, newScale)
                            }
                            event.changes.forEach { if (it.positionChanged()) it.consume() }
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { tap ->
                        val startScale = scale
                        val startOffset = offset
                        val targetScale = if (startScale > ZOOMED_EPSILON) 1f else SwipeItMotion.DoubleTapZoom
                        val fromCenter = tap - Offset(size.width / 2f, size.height / 2f)
                        val targetOffset = if (targetScale == 1f) Offset.Zero else clamp(fromCenter * (1f - targetScale), targetScale)
                        scope.launch {
                            animate(0f, 1f, animationSpec = tween(SwipeItMotion.DurationMedium)) { fraction, _ ->
                                scale = lerp(startScale, targetScale, fraction)
                                offset = lerp(startOffset, targetOffset, fraction)
                            }
                        }
                    },
                )
            },
    ) {
        AsyncImage(
            model = request,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
        )
    }
}
