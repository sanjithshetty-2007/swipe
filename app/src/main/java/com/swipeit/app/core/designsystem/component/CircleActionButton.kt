package com.swipeit.app.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.swipeit.app.R
import com.swipeit.app.core.designsystem.SwipeItColors
import com.swipeit.app.core.designsystem.SwipeItDimens
import com.swipeit.app.core.designsystem.SwipeItIcons
import com.swipeit.app.core.designsystem.SwipeItMotion
import com.swipeit.app.core.designsystem.SwipeItTheme

/** Visual style of a [CircleActionButton]. */
enum class CircleActionStyle {
    /** Filled coral. */
    Trash,

    /** Outlined, neutral surface. */
    Neutral,

    /** Green ring with glow. */
    Keep,
}

private data class CircleActionColors(
    val container: Color,
    val content: Color,
    val border: Color?,
    val borderWidth: Dp,
    val glow: Color?,
)

private fun CircleActionStyle.colors(): CircleActionColors = when (this) {
    CircleActionStyle.Trash -> CircleActionColors(
        container = SwipeItColors.Trash,
        content = SwipeItColors.OnAccent,
        border = null,
        borderWidth = 0.dp,
        glow = null,
    )
    CircleActionStyle.Neutral -> CircleActionColors(
        container = SwipeItColors.SurfaceVariant,
        content = SwipeItColors.TextPrimary,
        border = SwipeItColors.Outline,
        borderWidth = SwipeItDimens.BorderWidth,
        glow = null,
    )
    CircleActionStyle.Keep -> CircleActionColors(
        container = SwipeItColors.Background,
        content = SwipeItColors.Keep,
        border = SwipeItColors.Keep,
        borderWidth = SwipeItDimens.RingWidth,
        glow = SwipeItColors.KeepGlow,
    )
}

/** Round action button used in the swipe action row. Shrinks slightly while pressed. */
@Composable
fun CircleActionButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    style: CircleActionStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = SwipeItDimens.ActionButtonSizeLarge,
    enabled: Boolean = true,
) {
    val colors = style.colors()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) SwipeItMotion.PressedScale else 1f,
        animationSpec = SwipeItMotion.feedback(),
        label = "circleActionScale",
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (colors.glow != null) {
                    Modifier.shadow(
                        elevation = SwipeItDimens.ActionGlowElevation,
                        shape = CircleShape,
                        ambientColor = colors.glow,
                        spotColor = colors.glow,
                    )
                } else {
                    Modifier
                },
            )
            .clip(CircleShape)
            .background(colors.container)
            .then(
                if (colors.border != null) Modifier.border(colors.borderWidth, colors.border, CircleShape) else Modifier,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = colors.content,
            modifier = Modifier.size(size * SwipeItDimens.ActionIconFraction),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0B10)
@Composable
private fun CircleActionButtonPreview() {
    SwipeItTheme {
        Row(
            modifier = Modifier.padding(SwipeItDimens.SpacingLarge),
            horizontalArrangement = Arrangement.spacedBy(SwipeItDimens.SpacingLarge),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircleActionButton(
                iconRes = SwipeItIcons.Trash,
                contentDescription = stringResource(R.string.action_trash),
                style = CircleActionStyle.Trash,
                onClick = {},
            )
            CircleActionButton(
                iconRes = SwipeItIcons.Up,
                contentDescription = stringResource(R.string.action_keep_up),
                style = CircleActionStyle.Neutral,
                onClick = {},
                size = SwipeItDimens.ActionButtonSizeSmall,
            )
            CircleActionButton(
                iconRes = SwipeItIcons.Keep,
                contentDescription = stringResource(R.string.action_keep),
                style = CircleActionStyle.Keep,
                onClick = {},
            )
        }
    }
}
