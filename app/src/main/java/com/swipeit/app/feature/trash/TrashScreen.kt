package com.swipeit.app.feature.trash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.swipeit.app.R
import com.swipeit.app.core.designsystem.SwipeItColors
import com.swipeit.app.core.designsystem.SwipeItDimens
import com.swipeit.app.core.designsystem.SwipeItIcons
import com.swipeit.app.core.designsystem.SwipeItTheme
import com.swipeit.app.core.designsystem.component.EmptyState

/** Placeholder until the Trash grid is built. */
@Composable
fun TrashScreen(modifier: Modifier = Modifier) {
    TrashContent(modifier = modifier)
}

@Composable
fun TrashContent(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.trash_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(SwipeItDimens.ScreenPaddingCompact),
        )
        EmptyState(
            iconRes = SwipeItIcons.Trash,
            iconTint = SwipeItColors.Trash,
            title = stringResource(R.string.trash_title),
            message = stringResource(R.string.trash_placeholder_message),
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun TrashContentPreview() {
    SwipeItTheme {
        TrashContent(Modifier.background(MaterialTheme.colorScheme.background))
    }
}
