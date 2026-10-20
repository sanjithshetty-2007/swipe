package com.swipeit.app.navigation

import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.swipeit.app.R
import com.swipeit.app.core.designsystem.SwipeItColors
import com.swipeit.app.core.designsystem.SwipeItTheme

private const val MAX_BADGE_COUNT = 99

/** Sort · Trash · Kept. Active item is green; Trash shows a coral count badge. */
@Composable
fun SwipeItBottomBar(
    currentDestination: TopLevelDestination?,
    trashCount: Int,
    onNavigate: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        TopLevelDestination.entries.forEach { destination ->
            val label = stringResource(destination.labelRes)
            NavigationBarItem(
                selected = destination == currentDestination,
                onClick = { onNavigate(destination) },
                icon = {
                    BadgedBox(
                        badge = {
                            if (destination == TopLevelDestination.TRASH && trashCount > 0) {
                                Badge(containerColor = SwipeItColors.Trash, contentColor = SwipeItColors.OnAccent) {
                                    Text(
                                        text = if (trashCount > MAX_BADGE_COUNT) {
                                            stringResource(R.string.badge_overflow)
                                        } else {
                                            trashCount.toString()
                                        },
                                    )
                                }
                            }
                        },
                    ) {
                        Icon(painter = painterResource(destination.iconRes), contentDescription = label)
                    }
                },
                label = { Text(label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SwipeItColors.Keep,
                    selectedTextColor = SwipeItColors.Keep,
                    indicatorColor = SwipeItColors.KeepContainer,
                    unselectedIconColor = SwipeItColors.TextSecondary,
                    unselectedTextColor = SwipeItColors.TextSecondary,
                ),
            )
        }
    }
}

@Preview
@Composable
private fun SwipeItBottomBarPreview() {
    SwipeItTheme {
        SwipeItBottomBar(currentDestination = TopLevelDestination.SORT, trashCount = 12, onNavigate = {})
    }
}

@Preview
@Composable
private fun SwipeItBottomBarOverflowPreview() {
    SwipeItTheme {
        SwipeItBottomBar(currentDestination = TopLevelDestination.TRASH, trashCount = 340, onNavigate = {})
    }
}
