package com.swipeit.app.core.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
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
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
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

@Preview
@Composable
private fun SwipeItButtonsPreview() {
    SwipeItTheme {
        Column {
            SwipeItPrimaryButton(text = stringResource(R.string.permission_allow), onClick = {})
            SwipeItSecondaryButton(text = stringResource(R.string.permission_allow_all), onClick = {})
            SwipeItTextButton(text = stringResource(R.string.permission_select_more), onClick = {})
        }
    }
}
