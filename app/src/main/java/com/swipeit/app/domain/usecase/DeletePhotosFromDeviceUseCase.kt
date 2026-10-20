package com.swipeit.app.domain.usecase

import com.swipeit.app.domain.model.DeletionConsent
import com.swipeit.app.domain.model.DeletionOutcome
import com.swipeit.app.domain.model.DeletionResult
import com.swipeit.app.domain.model.DeletionStep
import com.swipeit.app.domain.model.Photo
import com.swipeit.app.domain.repository.PhotoDeletionRepository
import javax.inject.Inject

/** Max photos per system delete request. */
const val DELETE_CHUNK_SIZE = 500

/**
 * Deletes [photos] from the device in chunks, asking requestConsent whenever the OS needs the
 * user to approve. Stops at the first refusal or failure; photos not deleted stay in Trash.
 */
class DeletePhotosFromDeviceUseCase @Inject constructor(
    private val repository: PhotoDeletionRepository,
) {
    val systemConfirmsDeletion: Boolean get() = repository.systemConfirmsDeletion

    suspend operator fun invoke(
        photos: List<Photo>,
        requestConsent: suspend (DeletionConsent) -> Boolean,
    ): DeletionResult {
        val deleted = mutableListOf<Photo>()
        fun result(outcome: DeletionOutcome) = DeletionResult(deleted.size, deleted.sumOf { it.sizeBytes }, outcome)

        for (chunk in photos.chunked(DELETE_CHUNK_SIZE)) {
            var step = repository.delete(chunk)
            var lastConsentedId: Long? = null
            while (true) {
                val current = step
                deleted += current.deleted
                when (current) {
                    is DeletionStep.Done -> break
                    is DeletionStep.Failed -> return result(DeletionOutcome.FAILED)
                    is DeletionStep.NeedsConsent -> {
                        val firstId = current.awaiting.firstOrNull()?.id ?: break
                        // Asking again for the photo that was just approved would loop forever.
                        if (firstId == lastConsentedId && current.deleted.isEmpty()) {
                            return result(DeletionOutcome.FAILED)
                        }
                        if (!requestConsent(current.consent)) return result(DeletionOutcome.CANCELLED)
                        lastConsentedId = firstId
                        step = repository.onConsentGranted(current.awaiting)
                    }
                }
            }
        }
        return result(DeletionOutcome.COMPLETED)
    }
}
