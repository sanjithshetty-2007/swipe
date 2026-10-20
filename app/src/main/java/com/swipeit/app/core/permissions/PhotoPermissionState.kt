package com.swipeit.app.core.permissions

/** Current access level to the device photo library. */
sealed interface PhotoPermissionState {
    /** Full library access. */
    data object Granted : PhotoPermissionState

    /** Android 14+ "selected photos only" access. */
    data object Partial : PhotoPermissionState

    /** Denied, but the system will still show the request dialog. */
    data object Denied : PhotoPermissionState

    /** Denied and the system no longer shows the dialog; only app settings can fix it. */
    data object PermanentlyDenied : PhotoPermissionState

    /** Not asked yet and no prior denial reported by the system. */
    data object NotRequested : PhotoPermissionState
}

val PhotoPermissionState.canReadPhotos: Boolean
    get() = this is PhotoPermissionState.Granted || this is PhotoPermissionState.Partial
