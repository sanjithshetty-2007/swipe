package com.swipeit.app.core.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.swipeit.app.R

/** Full-width green pill with accent glow. */
@Composable
fun SwipeItPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = MaterialTheme.colorScheme.primary
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(SwipeItDimens.ButtonHeight)
            .shadow(
                elevation = SwipeItDimens.ButtonGlowElevation,
                shape = CircleShape,
                ambientColor = accent,
                spotColor = accent,
            ),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = accent,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

/** Outlined pill for secondary actions. */
@Composable
fun SwipeItSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = CircleShape,
        border = BorderStroke(SwipeItDimens.BorderWidth, MaterialTheme.colorScheme.primary),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Low-emphasis text action. */
@Composable
fun SwipeItTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Full-width coral pill for destructive actions; shows a spinner while [isLoading]. */
@Composable
fun SwipeItDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    loadingDescription: String? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(SwipeItDimens.ButtonHeight),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = SwipeItColors.Trash,
            contentColor = SwipeItColors.OnAccent,
            disabledContainerColor = SwipeItColors.Trash.copy(alpha = DISABLED_ALPHA),
            disabledContentColor = SwipeItColors.OnAccent,
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = SwipeItColors.OnAccent,
                strokeWidth = SwipeItDimens.ProgressBarHeight / 2,
                modifier = Modifier
                    .size(SwipeItDimens.IconSize)
                    .semantics { loadingDescription?.let { contentDescription = it } },
            )
        } else {
            Text(text = text, style = MaterialTheme.typography.titleMedium)
        }
    }
}

private const val DISABLED_ALPHA = 0.4f

@Preview
@Composable
private fun SwipeItButtonsPreview() {
    SwipeItTheme {
        Column {
            SwipeItPrimaryButton(text = stringResource(R.string.permission_allow), onClick = {})
            SwipeItSecondaryButton(text = stringResource(R.string.permission_allow_all), onClick = {})
            SwipeItTextButton(text = stringResource(R.string.permission_select_more), onClick = {})
            SwipeItDangerButton(text = stringResource(R.string.trash_delete_all), onClick = {})
            SwipeItDangerButton(text = stringResource(R.string.trash_delete_all), onClick = {}, isLoading = true)
        }
    }
}
