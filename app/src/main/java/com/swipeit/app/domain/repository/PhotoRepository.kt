package com.swipeit.app.domain.repository

import com.swipeit.app.domain.model.DecidedPhoto
import com.swipeit.app.domain.model.Decision
import com.swipeit.app.domain.model.Photo
import com.swipeit.app.domain.model.SortOrder
import com.swipeit.app.domain.model.Stats
import kotlinx.coroutines.flow.Flow

interface PhotoRepository {
    /** Device photos without a decision, in [sortOrder]; undone/reset photos come first. */
    fun unsortedQueue(sortOrder: SortOrder = SortOrder.NEWEST_FIRST): Flow<List<Photo>>

    /** Trashed photos, most recently decided first. */
    fun trashed(): Flow<List<DecidedPhoto>>

    /** Kept photos, most recently decided first. */
    fun kept(): Flow<List<DecidedPhoto>>

    /** The last few decisions, most recent first. */
    fun recent(): Flow<List<DecidedPhoto>>

    fun stats(): Flow<Stats>

    /** Records (or replaces) the decision for [photo]. Never touches the device file. */
    suspend fun decide(photo: Photo, decision: Decision)

    /** Removes the decision for [id]; the photo returns to the front of the queue. */
    suspend fun reset(id: Long)

    /** Reverts the most recent decision. Returns the photo, or null if there was none. */
    suspend fun undoLast(): Photo?

    /** Moves trashed photos [ids] to kept. */
    suspend fun restore(ids: List<Long>)

    /** Deletes decisions whose photo is not in [existingIds] (removed from the device). */
    suspend fun reconcile(existingIds: Set<Long>)

    /** Forces a device re-query, e.g. after the partial-access picker. */
    fun requestRefresh()
}
