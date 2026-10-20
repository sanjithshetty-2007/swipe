package com.swipeit.app.testing

import com.swipeit.app.domain.model.DeletionConsent
import com.swipeit.app.domain.model.DeletionStep
import com.swipeit.app.domain.model.Photo
import com.swipeit.app.domain.repository.PhotoDeletionRepository

/** Opaque consent token for tests. */
data class FakeConsent(val forIds: List<Long>) : DeletionConsent

/**
 * Simulates both platform paths.
 * - [systemConfirmsDeletion] = true (API 30+): every chunk needs one consent, then is deleted.
 * - false (API 26-29): photos in [needsConsentIds] ask for consent once each; [failingIds] fail.
 * [onDeleted] mirrors removing Room rows.
 */
class FakeDeletionRepository(
    override val systemConfirmsDeletion: Boolean = true,
    private val needsConsentIds: Set<Long> = emptySet(),
    private val failingIds: Set<Long> = emptySet(),
    private val repeatConsentForever: Boolean = false,
    private val onDeleted: suspend (List<Photo>) -> Unit = {},
) : PhotoDeletionRepository {

    val deleteCalls = mutableListOf<List<Long>>()
    private val consented = mutableSetOf<Long>()

    override suspend fun delete(photos: List<Photo>): DeletionStep {
        deleteCalls += photos.map { it.id }
        if (systemConfirmsDeletion) {
            return DeletionStep.NeedsConsent(emptyList(), FakeConsent(photos.map { it.id }), photos)
        }
        return deleteDirectly(photos)
    }

    override suspend fun onConsentGranted(photos: List<Photo>): DeletionStep {
        if (systemConfirmsDeletion) {
            onDeleted(photos)
            return DeletionStep.Done(photos)
        }
        if (!repeatConsentForever) photos.firstOrNull()?.let { consented += it.id }
        return deleteDirectly(photos)
    }

    private suspend fun deleteDirectly(photos: List<Photo>): DeletionStep {
        val deleted = mutableListOf<Photo>()
        photos.forEachIndexed { index, photo ->
            when {
                photo.id in failingIds -> {
                    onDeleted(deleted)
                    return DeletionStep.Failed(deleted.toList(), photos.drop(index))
                }
                photo.id in needsConsentIds && photo.id !in consented -> {
                    onDeleted(deleted)
                    return DeletionStep.NeedsConsent(deleted.toList(), FakeConsent(listOf(photo.id)), photos.drop(index))
                }
                else -> deleted += photo
            }
        }
        onDeleted(deleted)
        return DeletionStep.Done(deleted)
    }
}
