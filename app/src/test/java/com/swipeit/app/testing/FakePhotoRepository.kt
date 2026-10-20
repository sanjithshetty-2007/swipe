package com.swipeit.app.testing

import com.swipeit.app.domain.model.DecidedPhoto
import com.swipeit.app.domain.model.Decision
import com.swipeit.app.domain.model.Photo
import com.swipeit.app.domain.model.SortOrder
import com.swipeit.app.domain.model.Stats
import com.swipeit.app.domain.repository.PhotoRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory repository. Set [writeGate] to hold writes until completed; [failWrites] to throw. */
class FakePhotoRepository(photos: List<Photo>) : PhotoRepository {

    private val device = MutableStateFlow(photos)
    private val decisions = MutableStateFlow<List<DecidedPhoto>>(emptyList())
    private var clock = 0L

    var writeGate: CompletableDeferred<Unit>? = null
    var failWrites: Boolean = false

    override fun unsortedQueue(sortOrder: SortOrder): Flow<List<Photo>> =
        combine(device, decisions) { all, decided ->
            val decidedIds = decided.mapTo(HashSet()) { it.photo.id }
            all.filterNot { it.id in decidedIds }
        }

    override fun trashed(): Flow<List<DecidedPhoto>> = byDecision(Decision.TRASHED)

    override fun kept(): Flow<List<DecidedPhoto>> = byDecision(Decision.KEPT)

    override fun recent(): Flow<List<DecidedPhoto>> = decisions.map { it.sortedByDescending(DecidedPhoto::decidedAt) }

    override fun stats(): Flow<Stats> = combine(device, decisions) { all, decided ->
        val decidedIds = decided.mapTo(HashSet()) { it.photo.id }
        Stats(
            unsorted = all.count { it.id !in decidedIds },
            kept = decided.count { it.decision == Decision.KEPT },
            trashed = decided.count { it.decision == Decision.TRASHED },
            trashBytes = decided.filter { it.decision == Decision.TRASHED }.sumOf { it.photo.sizeBytes },
        )
    }

    override suspend fun decide(photo: Photo, decision: Decision) {
        writeGate?.await()
        if (failWrites) error("write failed")
        decisions.update { list -> list.filterNot { it.photo.id == photo.id } + DecidedPhoto(photo, decision, clock++) }
    }

    override suspend fun reset(id: Long) {
        decisions.update { list -> list.filterNot { it.photo.id == id } }
    }

    override suspend fun undoLast(): Photo? {
        val latest = decisions.value.maxByOrNull { it.decidedAt } ?: return null
        reset(latest.photo.id)
        // Mirror the real repository: the undone photo returns to the front.
        device.update { all -> listOf(latest.photo) + all.filterNot { it.id == latest.photo.id } }
        return latest.photo
    }

    override suspend fun restore(ids: List<Long>) {
        decisions.update { list ->
            list.map { if (it.photo.id in ids) it.copy(decision = Decision.KEPT) else it }
        }
    }

    override suspend fun reconcile(existingIds: Set<Long>) {
        decisions.update { list -> list.filter { it.photo.id in existingIds } }
    }

    override fun requestRefresh() = Unit

    private fun byDecision(decision: Decision) = decisions.map { list -> list.filter { it.decision == decision } }
}
