package com.swipeit.app.core.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build

/** Permission names per API level and the pure state resolution logic. */
internal object PhotoPermissions {

    // Lint cannot see the sdkInt guard below; the constants are plain strings, safe on any API.
    @SuppressLint("InlinedApi")
    fun fullAccess(sdkInt: Int = Build.VERSION.SDK_INT): String =
        if (sdkInt >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    @SuppressLint("InlinedApi")
    fun partialAccess(sdkInt: Int = Build.VERSION.SDK_INT): String? =
        if (sdkInt >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
        } else {
            null
        }

    fun requestSet(sdkInt: Int = Build.VERSION.SDK_INT): Array<String> =
        listOfNotNull(fullAccess(sdkInt), partialAccess(sdkInt)).toTypedArray()

    /**
     * Maps raw permission facts to a [PhotoPermissionState].
     * A denial without rationale after we have asked means the system stopped showing the dialog.
     */
    fun resolve(
        fullAccessGranted: Boolean,
        partialAccessGranted: Boolean,
        hasRequested: Boolean,
        shouldShowRationale: Boolean,
    ): PhotoPermissionState = when {
        fullAccessGranted -> PhotoPermissionState.Granted
        partialAccessGranted -> PhotoPermissionState.Partial
        shouldShowRationale -> PhotoPermissionState.Denied
        hasRequested -> PhotoPermissionState.PermanentlyDenied
        else -> PhotoPermissionState.NotRequested
    }
}
