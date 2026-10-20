package com.swipeit.app.feature.kept

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
import com.swipeit.app.core.designsystem.SwipeItDimens
import com.swipeit.app.core.designsystem.SwipeItIcons
import com.swipeit.app.core.designsystem.SwipeItTheme
import com.swipeit.app.core.designsystem.component.EmptyState

/** Placeholder until the Kept grid is built. */
@Composable
fun KeptScreen(modifier: Modifier = Modifier) {
    KeptContent(modifier = modifier)
}

@Composable
fun KeptContent(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.kept_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(SwipeItDimens.ScreenPaddingCompact),
        )
        EmptyState(
            iconRes = SwipeItIcons.Keep,
            title = stringResource(R.string.kept_title),
            message = stringResource(R.string.kept_placeholder_message),
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun KeptContentPreview() {
    SwipeItTheme {
        KeptContent(Modifier.background(MaterialTheme.colorScheme.background))
    }
}
