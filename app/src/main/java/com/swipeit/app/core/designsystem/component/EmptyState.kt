package com.swipeit.app.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.swipeit.app.R
import com.swipeit.app.core.designsystem.SwipeItColors
import com.swipeit.app.core.designsystem.SwipeItDimens
import com.swipeit.app.core.designsystem.SwipeItIcons
import com.swipeit.app.core.designsystem.SwipeItPrimaryButton
import com.swipeit.app.core.designsystem.SwipeItTheme

/** Centered icon, title, message and optional action. Fills the available space. */
@Composable
fun EmptyState(
    @DrawableRes iconRes: Int,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    iconTint: Color = SwipeItColors.Keep,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SwipeItDimens.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SwipeItDimens.SpacingMedium, Alignment.CenterVertically),
    ) {
        Box(
            modifier = Modifier
                .size(SwipeItDimens.EmptyStateIconSize)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(SwipeItDimens.EmptyStateIconSize * SwipeItDimens.ActionIconFraction),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = SwipeItDimens.EmptyStateMaxTextWidth),
        )
        action?.invoke()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0B10)
@Composable
private fun EmptyStatePreview() {
    SwipeItTheme {
        EmptyState(
            iconRes = SwipeItIcons.Photos,
            title = stringResource(R.string.sort_empty_title),
            message = stringResource(R.string.sort_empty_message),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0B10)
@Composable
private fun EmptyStateWithActionPreview() {
    SwipeItTheme {
        EmptyState(
            iconRes = SwipeItIcons.Trash,
            iconTint = SwipeItColors.Trash,
            title = stringResource(R.string.trash_title),
            message = stringResource(R.string.trash_placeholder_message),
            action = { SwipeItPrimaryButton(text = stringResource(R.string.permission_open_settings), onClick = {}) },
        )
    }
}
