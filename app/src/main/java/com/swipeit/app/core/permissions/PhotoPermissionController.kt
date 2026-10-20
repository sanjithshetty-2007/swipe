package com.swipeit.app.core.permissions

import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect

/** Observable photo permission state plus the actions that can change it. */
@Stable
class PhotoPermissionController internal constructor(
    stateSource: State<PhotoPermissionState>,
    private val onRequest: () -> Unit,
    private val onOpenSettings: () -> Unit,
) {
    val state: PhotoPermissionState by stateSource

    /** Shows the system permission dialog (also used for "Select more" in partial mode). */
    fun request() = onRequest()

    /** Opens the app's system settings page. */
    fun openAppSettings() = onOpenSettings()
}

/**
 * Remembers the photo permission state for the current API level and re-checks it on every
 * ON_RESUME, since the user can change access in system settings while the app is backgrounded.
 */
@Composable
fun rememberPhotoPermissionState(): PhotoPermissionController {
    val activity = requireNotNull(LocalActivity.current) {
        "rememberPhotoPermissionState() must be called from within an Activity"
    }
    val permissions = remember { PhotoPermissions.requestSet() }
    var hasRequested by rememberSaveable { mutableStateOf(false) }
    val state: MutableState<PhotoPermissionState> = remember {
        mutableStateOf(activity.readPhotoPermissionState(hasRequested))
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        hasRequested = true
        state.value = activity.readPhotoPermissionState(hasRequested = true)
    }

    LifecycleResumeEffect(activity) {
        state.value = activity.readPhotoPermissionState(hasRequested)
        onPauseOrDispose { }
    }

    return remember(activity, launcher) {
        PhotoPermissionController(
            stateSource = state,
            onRequest = { launcher.launch(permissions) },
            onOpenSettings = { activity.openAppSettings() },
        )
    }
}

private fun Activity.readPhotoPermissionState(hasRequested: Boolean): PhotoPermissionState {
    val fullAccess = PhotoPermissions.fullAccess()
    return PhotoPermissions.resolve(
        fullAccessGranted = isGranted(fullAccess),
        partialAccessGranted = PhotoPermissions.partialAccess()?.let(::isGranted) == true,
        hasRequested = hasRequested,
        shouldShowRationale = shouldShowRequestPermissionRationale(fullAccess),
    )
}

private fun Activity.isGranted(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
