package com.swipeit.app.feature.sort

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.swipeit.app.R
import com.swipeit.app.core.designsystem.SwipeItDimens
import com.swipeit.app.core.designsystem.SwipeItIcons
import com.swipeit.app.core.designsystem.SwipeItTheme
import com.swipeit.app.core.designsystem.component.CircleActionButton
import com.swipeit.app.core.designsystem.component.CircleActionStyle

/** Coral trash, up-arrow and green heart buttons under the deck. */
@Composable
fun SwipeActionRow(
    onActionClick: (SwipeDirection) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = SwipeItDimens.SpacingMedium),
        horizontalArrangement = Arrangement.spacedBy(SwipeItDimens.SpacingLarge, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircleActionButton(
            iconRes = SwipeItIcons.Trash,
            contentDescription = stringResource(R.string.action_trash),
            style = CircleActionStyle.Trash,
            onClick = { onActionClick(SwipeDirection.LEFT) },
            enabled = enabled,
        )
        CircleActionButton(
            iconRes = SwipeItIcons.Up,
            contentDescription = stringResource(R.string.action_keep_up),
            style = CircleActionStyle.Neutral,
            onClick = { onActionClick(SwipeDirection.UP) },
            size = SwipeItDimens.ActionButtonSizeSmall,
            enabled = enabled,
        )
        CircleActionButton(
            iconRes = SwipeItIcons.Keep,
            contentDescription = stringResource(R.string.action_keep),
            style = CircleActionStyle.Keep,
            onClick = { onActionClick(SwipeDirection.RIGHT) },
            enabled = enabled,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0B10)
@Composable
private fun SwipeActionRowPreview() {
    SwipeItTheme {
        SwipeActionRow(onActionClick = {})
    }
}
