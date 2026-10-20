package com.swipeit.app.data.media

import android.app.RecoverableSecurityException
import android.content.Context
import android.content.IntentSender
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.swipeit.app.core.common.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "MediaDeleter"

/** Outcome of deleting one uri directly (API 26-29). */
sealed interface DirectDeleteResult {
    data object Deleted : DirectDeleteResult

    data class NeedsConsent(val intentSender: IntentSender) : DirectDeleteResult

    data class Failed(val error: Exception) : DirectDeleteResult
}

/**
 * The only place that deletes device photos, and only through MediaStore / ContentResolver
 * (never File APIs). API 30+ always goes through the system confirmation dialog.
 */
@Singleton
class MediaDeleter @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    val usesSystemDeleteRequest: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

    /** Builds the system "Allow SwipeIt to delete N photos?" request. Nothing is deleted until the user agrees. */
    @RequiresApi(Build.VERSION_CODES.R)
    fun createDeleteRequest(uris: List<String>): IntentSender =
        MediaStore.createDeleteRequest(context.contentResolver, uris.map { it.toUri() }).intentSender

    /** API 26-29: deletes one uri; on API 29 a photo owned by another app needs consent first. */
    suspend fun deleteDirectly(uri: String): DirectDeleteResult = withContext(ioDispatcher) {
        try {
            context.contentResolver.delete(uri.toUri(), null, null)
            DirectDeleteResult.Deleted
        } catch (e: SecurityException) {
            // RecoverableSecurityException only exists from API 29; never reference it by type below that.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && e is RecoverableSecurityException) {
                DirectDeleteResult.NeedsConsent(e.userAction.actionIntent.intentSender)
            } else {
                Log.w(TAG, "Not allowed to delete $uri", e)
                DirectDeleteResult.Failed(e)
            }
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Invalid uri $uri", e)
            DirectDeleteResult.Failed(e)
        }
    }
}
