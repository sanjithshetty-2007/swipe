package com.swipeit.app.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SwipeItDarkColorScheme = darkColorScheme(
    primary = SwipeItColors.Keep,
    onPrimary = SwipeItColors.OnAccent,
    secondary = SwipeItColors.Info,
    onSecondary = SwipeItColors.OnAccent,
    error = SwipeItColors.Trash,
    onError = SwipeItColors.OnAccent,
    background = SwipeItColors.Background,
    onBackground = SwipeItColors.TextPrimary,
    surface = SwipeItColors.Surface,
    onSurface = SwipeItColors.TextPrimary,
    surfaceVariant = SwipeItColors.SurfaceVariant,
    onSurfaceVariant = SwipeItColors.TextSecondary,
    outline = SwipeItColors.Outline,
)

/** App theme. Dark only for now (CLAUDE.md §7). */
@Composable
fun SwipeItTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SwipeItDarkColorScheme,
        typography = SwipeItTypography,
        content = content,
    )
}
