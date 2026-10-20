package com.swipeit.app.core.designsystem

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

/** Animation durations (ms), spring specs and swipe-feel constants. */
object SwipeItMotion {
    const val DurationShort = 150
    const val DurationMedium = 250
    const val DurationLong = 400

    /** Max card tilt while dragging (CLAUDE.md §7). */
    const val MaxCardRotationDegrees = 12f

    /** Scale applied to a pressed circle button. */
    const val PressedScale = 0.9f

    /** Card springing back to centre after an incomplete swipe. */
    fun <T> cardSnapBack(): SpringSpec<T> =
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)

    /** Card flying off screen after a committed swipe. */
    fun <T> cardFlyOut(): SpringSpec<T> =
        spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)

    /** Small UI feedback: button press, progress bar. */
    fun <T> feedback(): SpringSpec<T> =
        spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)
}
