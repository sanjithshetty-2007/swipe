package com.swipeit.app.feature.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.swipeit.app.R
import com.swipeit.app.core.designsystem.SwipeItIcons
import com.swipeit.app.core.designsystem.SwipeItTheme
import com.swipeit.app.core.designsystem.component.EmptyState
import com.swipeit.app.core.designsystem.component.SwipeItTopBar

/** Placeholder until the zoomable preview is built. */
@Composable
fun PreviewScreen(
    photoId: Long,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PreviewContent(photoId = photoId, onBackClick = onBackClick, modifier = modifier)
}

@Composable
fun PreviewContent(
    photoId: Long,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        SwipeItTopBar(title = stringResource(R.string.preview_title), onBackClick = onBackClick)
        EmptyState(
            iconRes = SwipeItIcons.Expand,
            title = stringResource(R.string.preview_title),
            message = stringResource(R.string.preview_placeholder_message, photoId),
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun PreviewContentPreview() {
    SwipeItTheme {
        PreviewContent(
            photoId = 42L,
            onBackClick = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
        )
    }
}
