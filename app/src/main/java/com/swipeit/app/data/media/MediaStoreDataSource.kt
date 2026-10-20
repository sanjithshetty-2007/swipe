package com.swipeit.app.data.media

import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import com.swipeit.app.core.common.IoDispatcher
import com.swipeit.app.core.permissions.PhotoPermissions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext

private const val TAG = "MediaStoreDataSource"
private const val SECONDS_TO_MILLIS = 1000L

/** Reads images from MediaStore. Never writes or deletes. */
@Singleton
class MediaStoreDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    /**
     * Returns all images visible to the app (all, or only user-selected under partial access),
     * or null if MediaStore could not be read (no permission, or provider unavailable).
     */
    suspend fun queryImages(sortOrder: MediaSortOrder = MediaSortOrder.NEWEST_FIRST): List<MediaImage>? =
        withContext(ioDispatcher) {
            try {
                val cursor = context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    PROJECTION,
                    null,
                    null,
                    sortOrder.sql,
                )
                if (cursor == null) Log.w(TAG, "MediaStore returned a null cursor")
                cursor?.use { readAll(it) }
            } catch (e: SecurityException) {
                Log.w(TAG, "No permission to read MediaStore images", e)
                null
            }
        }

    /** True when the app can see the whole library (not just user-selected photos). */
    fun hasFullAccess(): Boolean =
        ContextCompat.checkSelfPermission(context, PhotoPermissions.fullAccess()) ==
            PackageManager.PERMISSION_GRANTED

    private suspend fun readAll(cursor: Cursor): List<MediaImage> {
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
        val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
        val takenCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
        val addedCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
        val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
        val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
        val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)

        val images = ArrayList<MediaImage>(cursor.count)
        while (cursor.moveToNext()) {
            coroutineContext.ensureActive()
            val id = cursor.getLong(idCol)
            val dateTaken = cursor.getLongOrZero(takenCol)
            images += MediaImage(
                id = id,
                contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id).toString(),
                displayName = cursor.getStringOrEmpty(nameCol),
                sizeBytes = cursor.getLongOrZero(sizeCol),
                dateTakenMillis = if (dateTaken > 0) dateTaken else cursor.getLongOrZero(addedCol) * SECONDS_TO_MILLIS,
                album = cursor.getStringOrEmpty(albumCol),
                width = cursor.getIntOrZero(widthCol),
                height = cursor.getIntOrZero(heightCol),
                mimeType = cursor.getStringOrEmpty(mimeCol),
            )
        }
        return images
    }

    private fun Cursor.getLongOrZero(index: Int): Long = if (isNull(index)) 0L else getLong(index)

    private fun Cursor.getIntOrZero(index: Int): Int = if (isNull(index)) 0 else getInt(index)

    private fun Cursor.getStringOrEmpty(index: Int): String = getString(index).orEmpty()

    private companion object {
        val PROJECTION = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.MIME_TYPE,
        )
    }
}
