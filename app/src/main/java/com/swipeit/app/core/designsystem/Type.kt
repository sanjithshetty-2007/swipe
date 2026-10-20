package com.swipeit.app.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.swipeit.app.R

private const val LOGO_GLOW_BLUR_RADIUS = 28f

/** Bundled OFL handwritten font (Caveat Brush). Logo only; UI text uses the system sans. */
val LogoFontFamily = FontFamily(Font(R.font.caveat_brush_regular))

/** Material type scale on the system sans-serif. */
internal val SwipeItTypography = Typography()

object SwipeItTextStyles {
    val Logo = TextStyle(
        fontFamily = LogoFontFamily,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        color = SwipeItColors.Keep,
        shadow = Shadow(color = SwipeItColors.KeepGlow, offset = Offset.Zero, blurRadius = LOGO_GLOW_BLUR_RADIUS),
    )

    val LogoLarge = Logo.copy(fontSize = 72.sp, lineHeight = 76.sp)
}
