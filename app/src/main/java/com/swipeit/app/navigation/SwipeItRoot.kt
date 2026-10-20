package com.swipeit.app.navigation

import androidx.compose.runtime.Composable
import com.swipeit.app.core.permissions.PhotoPermissionState
import com.swipeit.app.core.permissions.canReadPhotos
import com.swipeit.app.core.permissions.rememberPhotoPermissionState
import com.swipeit.app.feature.onboarding.OnboardingScreen

/** App root: onboarding until photo access is Granted or Partial, then the main shell. */
@Composable
fun SwipeItRoot() {
    val permission = rememberPhotoPermissionState()
    val state = permission.state
    if (state.canReadPhotos) {
        SwipeItAppShell(
            isPartialAccess = state is PhotoPermissionState.Partial,
            onSelectMoreClick = permission::request,
            onAllowAllClick = permission::openAppSettings,
        )
    } else {
        OnboardingScreen(permission = permission)
    }
}
