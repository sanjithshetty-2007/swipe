package com.swipeit.app.data.repository

import android.os.Build
import android.util.Log
import com.swipeit.app.core.common.IoDispatcher
import com.swipeit.app.core.permissions.IntentSenderConsent
import com.swipeit.app.data.db.PhotoDecisionDao
import com.swipeit.app.data.media.DirectDeleteResult
import com.swipeit.app.data.media.MediaDeleter
import com.swipeit.app.domain.model.DeletionStep
import com.swipeit.app.domain.model.Photo
import com.swipeit.app.domain.repository.PhotoDeletionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PhotoDeletionRepo"
private const val SQL_IN_CHUNK_SIZE = 500

@Singleton
class PhotoDeletionRepositoryImpl @Inject constructor(
    private val deleter: MediaDeleter,
    private val dao: PhotoDecisionDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : PhotoDeletionRepository {

    override val systemConfirmsDeletion: Boolean get() = deleter.usesSystemDeleteRequest

    override suspend fun delete(photos: List<Photo>): DeletionStep {
        if (photos.isEmpty()) return DeletionStep.Done(emptyList())
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return deleteDirectly(photos)
        return try {
            val request = deleter.createDeleteRequest(photos.map { it.uri })
            DeletionStep.NeedsConsent(deleted = emptyList(), consent = IntentSenderConsent(request), awaiting = photos)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Could not create delete request", e)
            DeletionStep.Failed(deleted = emptyList(), remaining = photos)
        } catch (e: SecurityException) {
            Log.w(TAG, "Could not create delete request", e)
            DeletionStep.Failed(deleted = emptyList(), remaining = photos)
        }
    }

    override suspend fun onConsentGranted(photos: List<Photo>): DeletionStep {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return deleteDirectly(photos)
        // RESULT_OK from createDeleteRequest: the system has deleted them.
        forget(photos)
        return DeletionStep.Done(photos)
    }

    private suspend fun deleteDirectly(photos: List<Photo>): DeletionStep {
        val deleted = mutableListOf<Photo>()
        photos.forEachIndexed { index, photo ->
            when (val result = deleter.deleteDirectly(photo.uri)) {
                DirectDeleteResult.Deleted -> deleted += photo
                is DirectDeleteResult.NeedsConsent -> {
                    forget(deleted)
                    return DeletionStep.NeedsConsent(
                        deleted = deleted,
                        consent = IntentSenderConsent(result.intentSender),
                        awaiting = photos.subList(index, photos.size),
                    )
                }
                is DirectDeleteResult.Failed -> {
                    forget(deleted)
                    return DeletionStep.Failed(deleted = deleted, remaining = photos.subList(index, photos.size))
                }
            }
        }
        forget(deleted)
        return DeletionStep.Done(deleted)
    }

    /** Drops Room rows for photos that no longer exist on the device. */
    private suspend fun forget(photos: List<Photo>) = withContext(ioDispatcher) {
        photos.map { it.id }.chunked(SQL_IN_CHUNK_SIZE).forEach { dao.deleteAll(it) }
    }
}
