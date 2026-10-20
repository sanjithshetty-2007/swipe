package com.swipeit.app.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.swipeit.app.core.designsystem.SwipeItMotion
import com.swipeit.app.feature.kept.KeptScreen
import com.swipeit.app.feature.preview.PreviewScreen
import com.swipeit.app.feature.settings.SettingsScreen
import com.swipeit.app.feature.sort.SortScreen
import com.swipeit.app.feature.trash.TrashScreen

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
        composable<PreviewRoute> { entry ->
            PreviewScreen(
                photoId = entry.toRoute<PreviewRoute>().photoId,
                onBackClick = { navController.popBackStack() },
            )
        }
        composable<SettingsRoute> {
            SettingsScreen(onBackClick = { navController.popBackStack() })
        }
    }
}
