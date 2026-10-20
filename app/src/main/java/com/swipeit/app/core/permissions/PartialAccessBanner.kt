package com.swipeit.app.core.permissions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.swipeit.app.R
import com.swipeit.app.core.designsystem.SwipeItDimens
import com.swipeit.app.core.designsystem.SwipeItSecondaryButton
import com.swipeit.app.core.designsystem.SwipeItTextButton
import com.swipeit.app.core.designsystem.SwipeItTheme

/** Banner shown while the app only has Android 14+ "selected photos" access. */
@Composable
fun PartialAccessBanner(
    onSelectMoreClick: () -> Unit,
    onAllowAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(SwipeItDimens.BorderWidth, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(SwipeItDimens.SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(SwipeItDimens.SpacingSmall),
        ) {
            Text(
                text = stringResource(R.string.permission_partial_message),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    space = SwipeItDimens.SpacingSmall,
                    alignment = Alignment.End,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SwipeItTextButton(
                    text = stringResource(R.string.permission_select_more),
                    onClick = onSelectMoreClick,
                )
                SwipeItSecondaryButton(
                    text = stringResource(R.string.permission_allow_all),
                    onClick = onAllowAllClick,
                )
            }
        }
    }
}

@Preview
@Composable
private fun PartialAccessBannerPreview() {
    SwipeItTheme {
        PartialAccessBanner(onSelectMoreClick = {}, onAllowAllClick = {})
    }
}
