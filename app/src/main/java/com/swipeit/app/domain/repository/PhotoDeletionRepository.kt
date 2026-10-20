package com.swipeit.app.domain.repository

import com.swipeit.app.domain.model.DeletionStep
import com.swipeit.app.domain.model.Photo

/** Real device deletion. Only ever reached from the Trash screen, never from a swipe. */
interface PhotoDeletionRepository {
    /** True when the OS shows its own confirmation dialog for every deletion (API 30+). */
    val systemConfirmsDeletion: Boolean

    /** Starts deleting [photos] from the device. */
    suspend fun delete(photos: List<Photo>): DeletionStep

    /** Continues after the user approved a [DeletionStep.NeedsConsent] for [photos]. */
    suspend fun onConsentGranted(photos: List<Photo>): DeletionStep
}
