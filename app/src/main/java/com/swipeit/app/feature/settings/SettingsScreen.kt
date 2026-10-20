package com.swipeit.app.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.swipeit.app.R
import com.swipeit.app.core.designsystem.SwipeItColors
import com.swipeit.app.core.designsystem.SwipeItIcons
import com.swipeit.app.core.designsystem.SwipeItTheme
import com.swipeit.app.core.designsystem.component.EmptyState
import com.swipeit.app.core.designsystem.component.SwipeItTopBar

/** Placeholder until settings exist. */
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsContent(onBackClick = onBackClick, modifier = modifier)
}

@Composable
fun SettingsContent(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        SwipeItTopBar(title = stringResource(R.string.settings_title), onBackClick = onBackClick)
        EmptyState(
            iconRes = SwipeItIcons.Settings,
            iconTint = SwipeItColors.Info,
            title = stringResource(R.string.settings_title),
            message = stringResource(R.string.settings_placeholder_message),
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun SettingsContentPreview() {
    SwipeItTheme {
        SettingsContent(onBackClick = {}, modifier = Modifier.background(MaterialTheme.colorScheme.background))
    }
}
