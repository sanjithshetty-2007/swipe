package com.swipeit.app.core.permissions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log

private const val TAG = "AppSettings"

/** Opens this app's system settings page. Returns false if no activity could handle it. */
fun Context.openAppSettings(): Boolean {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null),
    )
    return try {
        startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        Log.w(TAG, "Unable to open app settings", e)
        false
    }
}
