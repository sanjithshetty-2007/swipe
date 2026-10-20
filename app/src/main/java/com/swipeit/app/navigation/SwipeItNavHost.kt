package com.swipeit.app.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.swipeit.app.core.designsystem.SwipeItMotion
import com.swipeit.app.feature.kept.KeptScreen
import com.swipeit.app.feature.preview.PreviewRoute
import com.swipeit.app.feature.preview.PreviewScreen
import com.swipeit.app.feature.settings.SettingsScreen
import com.swipeit.app.feature.sort.SortScreen
import com.swipeit.app.feature.trash.TrashScreen

private const val PREVIEW_TRANSITION_SCALE = 0.92f

@Composable
fun SwipeItNavHost(
    navController: NavHostController,
    isPartialAccess: Boolean,
    onSelectMoreClick: () -> Unit,
    onAllowAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = SortRoute,
        modifier = modifier,
        enterTransition = { fadeIn(tween(SwipeItMotion.DurationMedium)) },
        exitTransition = { fadeOut(tween(SwipeItMotion.DurationShort)) },
    ) {
        composable<SortRoute> {
            SortScreen(
                isPartialAccess = isPartialAccess,
                onSelectMoreClick = onSelectMoreClick,
                onAllowAllClick = onAllowAllClick,
                onSettingsClick = { navController.navigate(SettingsRoute) { launchSingleTop = true } },
                onOpenPreview = { photoId -> navController.navigate(PreviewRoute(photoId)) },
            )
        }
        composable<TrashRoute> { TrashScreen() }
        composable<KeptRoute> { KeptScreen() }
        composable<PreviewRoute>(
            enterTransition = {
                fadeIn(tween(SwipeItMotion.DurationMedium)) +
                    scaleIn(tween(SwipeItMotion.DurationMedium), initialScale = PREVIEW_TRANSITION_SCALE)
            },
            popExitTransition = {
                fadeOut(tween(SwipeItMotion.DurationShort)) +
                    scaleOut(tween(SwipeItMotion.DurationShort), targetScale = PREVIEW_TRANSITION_SCALE)
            },
        ) {
            PreviewScreen(onCloseClick = { navController.popBackStack() })
        }
        composable<SettingsRoute> {
            SettingsScreen(onBackClick = { navController.popBackStack() })
        }
    }
}
