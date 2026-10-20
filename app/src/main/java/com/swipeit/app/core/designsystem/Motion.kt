package com.swipeit.app.core.designsystem

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/** Animation durations (ms), spring specs and swipe-feel constants. */
object SwipeItMotion {
    const val DurationShort = 150
    const val DurationMedium = 250
    const val DurationLong = 400

    /** Max card tilt while dragging (CLAUDE.md §7), reached at half the card width. */
    const val MaxCardRotationDegrees = 12f

    /** Horizontal drag, as a fraction of card width, that commits a keep/trash. */
    const val SwipeThresholdHorizontal = 0.35f

    /** Upward drag, as a fraction of card height, that commits a keep. */
    const val SwipeThresholdUp = 0.25f

    /** Fling speed (px/s) that commits a swipe regardless of distance. */
    const val FlingVelocityThreshold = 1000f

    /** How far past the card size the card travels when it flies off. */
    const val FlyOutDistanceFactor = 1.5f

    /** Scale applied to a pressed circle button. */
    const val PressedScale = 0.9f

    /** Each card behind the top one is this much smaller. */
    const val StackScaleStep = 0.05f

    /** Tilt of the 2nd and 3rd card in the stack. */
    val StackRotations = floatArrayOf(0f, -4f, 5f)

    /** Card springing back to centre after an incomplete swipe. */
    fun <T> cardSnapBack(): SpringSpec<T> =
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)

    /** Card leaving the screen after a committed swipe; short so decisions feel instant. */
    fun <T> cardFlyOut(): TweenSpec<T> = tween(durationMillis = DurationMedium, easing = FastOutLinearInEasing)

    /** Card behind moving forward when the top card leaves. */
    fun <T> stackPromote(): SpringSpec<T> =
        spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)

    /** Small UI feedback: button press, progress bar. */
    fun <T> feedback(): SpringSpec<T> =
        spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)
}
