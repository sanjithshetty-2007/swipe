package com.swipeit.app.data.media

import android.content.Context
import android.database.ContentObserver
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

private const val CHANGE_DEBOUNCE_MILLIS = 300L

/** Emits whenever the MediaStore image collection changes. */
@Singleton
class MediaObserver @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /** Emits once immediately, then once per burst of changes (debounced 300 ms). */
    @OptIn(FlowPreview::class)
    fun changes(): Flow<Unit> = callbackFlow {
        val observer = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }
        context.contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            observer,
        )
        awaitClose { context.contentResolver.unregisterContentObserver(observer) }
    }
        .debounce(CHANGE_DEBOUNCE_MILLIS)
        .onStart { emit(Unit) }
}
