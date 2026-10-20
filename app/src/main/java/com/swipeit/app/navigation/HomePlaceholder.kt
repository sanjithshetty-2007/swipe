package com.swipeit.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.swipeit.app.R
import com.swipeit.app.core.designsystem.SwipeItDimens
import com.swipeit.app.core.designsystem.SwipeItTheme
import com.swipeit.app.core.permissions.PartialAccessBanner

/** Temporary home until the Sort screen exists. */
@Composable
fun HomePlaceholder(
    isPartialAccess: Boolean,
    onSelectMoreClick: () -> Unit,
    onAllowAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(SwipeItDimens.ScreenPadding),
    ) {
        if (isPartialAccess) {
            PartialAccessBanner(
                onSelectMoreClick = onSelectMoreClick,
                onAllowAllClick = onAllowAllClick,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.home_placeholder),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun HomeGrantedPreview() {
    SwipeItTheme {
        HomePlaceholder(isPartialAccess = false, onSelectMoreClick = {}, onAllowAllClick = {})
    }
}

@Preview(showSystemUi = true)
@Composable
private fun HomePartialPreview() {
    SwipeItTheme {
        HomePlaceholder(isPartialAccess = true, onSelectMoreClick = {}, onAllowAllClick = {})
    }
}
