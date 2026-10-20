package com.swipeit.app.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

/** Scaffold with bottom bar (top-level screens only) around the NavHost. */
@Composable
fun SwipeItAppShell(
    isPartialAccess: Boolean,
    onSelectMoreClick: () -> Unit,
    onAllowAllClick: () -> Unit,
    viewModel: AppShellViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val trashCount by viewModel.trashCount.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentTopLevel = backStackEntry?.destination?.toTopLevelDestination()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (currentTopLevel != null) {
                SwipeItBottomBar(
                    currentDestination = currentTopLevel,
                    trashCount = trashCount,
                    onNavigate = { navController.navigateToTopLevel(it) },
                )
            }
        },
    ) { innerPadding ->
        SwipeItNavHost(
            navController = navController,
            isPartialAccess = isPartialAccess,
            onSelectMoreClick = onSelectMoreClick,
            onAllowAllClick = onAllowAllClick,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        )
    }
}

private fun NavDestination.toTopLevelDestination(): TopLevelDestination? =
    TopLevelDestination.entries.firstOrNull { destination ->
        hierarchy.any { it.hasRoute(destination.routeClass) }
    }

private fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
