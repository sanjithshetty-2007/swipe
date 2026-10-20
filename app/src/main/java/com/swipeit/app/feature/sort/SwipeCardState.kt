package com.swipeit.app.feature.sort

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import com.swipeit.app.core.designsystem.SwipeItMotion
import kotlinx.coroutines.flow.first
import kotlin.math.abs
import kotlin.math.max

/** Drag offset and swipe decision logic for the top card of the deck. */
@Stable
class SwipeCardState(initialOffset: Offset = Offset.Zero) {

    private val offsetAnimatable = Animatable(initialOffset, Offset.VectorConverter)

    val offset: Offset get() = offsetAnimatable.value

    /** Card size in px, set from layout. Thresholds are 0 until known. */
    var size: IntSize by mutableStateOf(IntSize.Zero)

    /** True once a decision is committed; further drags and taps are ignored. */
    var isLeaving: Boolean by mutableStateOf(false)
        private set

    private val thresholdX: Float get() = size.width * SwipeItMotion.SwipeThresholdHorizontal
    private val thresholdUp: Float get() = size.height * SwipeItMotion.SwipeThresholdUp

    /** Signed horizontal progress toward a decision (negative = left). */
    private val horizontal: Float get() = if (thresholdX > 0f) offset.x / thresholdX else 0f

    /** Upward progress toward a decision (positive = up). */
    private val upward: Float get() = if (thresholdUp > 0f) -offset.y / thresholdUp else 0f

    val rotationDegrees: Float
        get() {
            val halfWidth = size.width / 2f
            if (halfWidth == 0f) return 0f
            val max = SwipeItMotion.MaxCardRotationDegrees
            return (offset.x / halfWidth * max).coerceIn(-max, max)
        }

    /** 0..1 strength of the trash feedback. */
    val trashProgress: Float
        get() = if (horizontal < 0f && -horizontal >= upward) (-horizontal).coerceIn(0f, 1f) else 0f

    /** 0..1 strength of the keep feedback (right or up). */
    val keepProgress: Float
        get() = if (trashProgress > 0f) 0f else max(horizontal, upward).coerceIn(0f, 1f)

    suspend fun dragBy(delta: Offset) {
        if (!isLeaving) offsetAnimatable.snapTo(offsetAnimatable.value + delta)
    }

    /** Called on release: commits a swipe (animating off) or springs back. Returns the decision. */
    suspend fun settle(velocity: Velocity): SwipeDirection? {
        if (isLeaving) return null
        val direction = resolveDirection(velocity)
        if (direction == null) {
            offsetAnimatable.animateTo(
                targetValue = Offset.Zero,
                animationSpec = SwipeItMotion.cardSnapBack(),
                initialVelocity = Offset(velocity.x, velocity.y),
            )
            return null
        }
        return if (animateOut(direction)) direction else null
    }

    /** Flies the card off screen in [direction]. Returns false if it was already leaving. */
    suspend fun animateOut(direction: SwipeDirection): Boolean {
        if (isLeaving) return false
        isLeaving = true
        offsetAnimatable.animateTo(offscreen(direction, from = offset), SwipeItMotion.cardFlyOut())
        return true
    }

    /** Set when this card is an undone photo that should fly back in from where it left. */
    var pendingEntry: SwipeDirection? by mutableStateOf(null)
        private set

    private var entryStarted by mutableStateOf(false)

    /** Keeps the card invisible until [playEntry] has moved it off screen (avoids a centre flash). */
    val isHiddenForEntry: Boolean get() = pendingEntry != null && !entryStarted

    fun prepareEntry(direction: SwipeDirection) {
        pendingEntry = direction
    }

    /** Reverse of [animateOut]: waits for layout, jumps off screen, springs back to centre. */
    suspend fun playEntry() {
        val direction = pendingEntry ?: return
        try {
            snapshotFlow { size }.first { it != IntSize.Zero }
            offsetAnimatable.snapTo(offscreen(direction, from = Offset.Zero))
            entryStarted = true
            offsetAnimatable.animateTo(Offset.Zero, SwipeItMotion.cardSnapBack())
        } finally {
            entryStarted = true
            pendingEntry = null
        }
    }

    private fun offscreen(direction: SwipeDirection, from: Offset): Offset {
        val travelX = size.width * SwipeItMotion.FlyOutDistanceFactor
        val travelY = size.height * SwipeItMotion.FlyOutDistanceFactor
        return when (direction) {
            SwipeDirection.LEFT -> Offset(-travelX, from.y)
            SwipeDirection.RIGHT -> Offset(travelX, from.y)
            SwipeDirection.UP -> Offset(from.x, -travelY)
        }
    }

    suspend fun snapBack() {
        if (!isLeaving) offsetAnimatable.animateTo(Offset.Zero, SwipeItMotion.cardSnapBack())
    }

    private fun resolveDirection(velocity: Velocity): SwipeDirection? {
        val fling = SwipeItMotion.FlingVelocityThreshold
        val mostlyHorizontal = abs(velocity.x) >= abs(velocity.y)
        return when {
            trashProgress >= 1f -> SwipeDirection.LEFT
            keepProgress >= 1f -> if (upward > horizontal) SwipeDirection.UP else SwipeDirection.RIGHT
            mostlyHorizontal && velocity.x > fling -> SwipeDirection.RIGHT
            mostlyHorizontal && velocity.x < -fling -> SwipeDirection.LEFT
            !mostlyHorizontal && velocity.y < -fling -> SwipeDirection.UP
            else -> null
        }
    }
}
