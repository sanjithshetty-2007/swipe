package com.swipeit.app.feature.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.swipeit.app.R
import com.swipeit.app.core.designsystem.SwipeItColors
import com.swipeit.app.core.designsystem.SwipeItDimens
import com.swipeit.app.core.designsystem.SwipeItPrimaryButton
import com.swipeit.app.core.designsystem.SwipeItTheme
import com.swipeit.app.core.permissions.PhotoPermissionState

/** Stateless onboarding UI for the not-yet-granted permission states. */
@Composable
fun OnboardingContent(
    permissionState: PhotoPermissionState,
    onAllowClick: () -> Unit,
    onOpenSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(SwipeItDimens.ScreenPadding),
    ) {
        Spacer(Modifier.weight(1f))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(SwipeItDimens.SpacingSmall))
        Text(
            text = stringResource(R.string.onboarding_tagline),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(SwipeItDimens.SpacingXLarge))
        Column(verticalArrangement = Arrangement.spacedBy(SwipeItDimens.SpacingMedium)) {
            HintRow(R.drawable.ic_arrow_left, SwipeItColors.Trash, R.string.onboarding_hint_trash)
            HintRow(R.drawable.ic_arrow_right, SwipeItColors.Keep, R.string.onboarding_hint_keep)
            HintRow(R.drawable.ic_lock, SwipeItColors.Info, R.string.onboarding_hint_offline)
        }
        Spacer(Modifier.weight(1f))
        PermissionAction(
            permissionState = permissionState,
            onAllowClick = onAllowClick,
            onOpenSettingsClick = onOpenSettingsClick,
        )
    }
}

@Composable
private fun PermissionAction(
    permissionState: PhotoPermissionState,
    onAllowClick: () -> Unit,
    onOpenSettingsClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SwipeItDimens.SpacingMedium),
    ) {
        when (permissionState) {
            PhotoPermissionState.PermanentlyDenied -> {
                Text(
                    text = stringResource(R.string.permission_permanently_denied_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.permission_permanently_denied_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                SwipeItPrimaryButton(
                    text = stringResource(R.string.permission_open_settings),
                    onClick = onOpenSettingsClick,
                )
            }

            PhotoPermissionState.Denied -> {
                Text(
                    text = stringResource(R.string.permission_denied_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                SwipeItPrimaryButton(
                    text = stringResource(R.string.permission_allow),
                    onClick = onAllowClick,
                )
            }

            PhotoPermissionState.NotRequested,
            PhotoPermissionState.Granted,
            PhotoPermissionState.Partial,
            -> SwipeItPrimaryButton(
                text = stringResource(R.string.permission_allow),
                onClick = onAllowClick,
            )
        }
    }
}

@Composable
private fun HintRow(
    @DrawableRes iconRes: Int,
    tint: Color,
    @StringRes textRes: Int,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SwipeItDimens.SpacingMedium),
    ) {
        Box(
            modifier = Modifier
                .size(SwipeItDimens.IconBadgeSize)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(SwipeItDimens.IconSize),
            )
        }
        Text(
            text = stringResource(textRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun OnboardingNotRequestedPreview() {
    SwipeItTheme {
        OnboardingContent(PhotoPermissionState.NotRequested, onAllowClick = {}, onOpenSettingsClick = {})
    }
}

@Preview(showSystemUi = true)
@Composable
private fun OnboardingDeniedPreview() {
    SwipeItTheme {
        OnboardingContent(PhotoPermissionState.Denied, onAllowClick = {}, onOpenSettingsClick = {})
    }
}

@Preview(showSystemUi = true)
@Composable
private fun OnboardingPermanentlyDeniedPreview() {
    SwipeItTheme {
        OnboardingContent(PhotoPermissionState.PermanentlyDenied, onAllowClick = {}, onOpenSettingsClick = {})
    }
}
