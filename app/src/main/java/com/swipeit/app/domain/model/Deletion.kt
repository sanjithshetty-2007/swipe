package com.swipeit.app.domain.model

/** A platform confirmation the UI must show before the device may delete photos. */
interface DeletionConsent

/** One step of deleting photos from the device. [deleted] are gone from the device and from Room. */
sealed interface DeletionStep {
    val deleted: List<Photo>

    data class Done(override val deleted: List<Photo>) : DeletionStep

    /** The user must approve [consent]; afterwards continue with [awaiting]. */
    data class NeedsConsent(
        override val deleted: List<Photo>,
        val consent: DeletionConsent,
        val awaiting: List<Photo>,
    ) : DeletionStep

    data class Failed(override val deleted: List<Photo>, val remaining: List<Photo>) : DeletionStep
}

enum class DeletionOutcome {
    COMPLETED,
    CANCELLED,
    FAILED,
}

data class DeletionResult(
    val deletedCount: Int,
    val freedBytes: Long,
    val outcome: DeletionOutcome,
)
