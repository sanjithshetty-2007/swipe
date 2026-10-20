package com.swipeit.app.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.swipeit.app.core.permissions.PhotoPermissionController

/** Wires the permission controller into [OnboardingContent]. */
@Composable
fun OnboardingScreen(
    permission: PhotoPermissionController,
    modifier: Modifier = Modifier,
) {
    OnboardingContent(
        permissionState = permission.state,
        onAllowClick = permission::request,
        onOpenSettingsClick = permission::openAppSettings,
        modifier = modifier,
    )
}
