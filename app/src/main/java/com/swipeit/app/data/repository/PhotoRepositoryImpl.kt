package com.swipeit.app.data.repository

import com.swipeit.app.core.common.ApplicationScope
import com.swipeit.app.core.common.DefaultDispatcher
import com.swipeit.app.core.common.IoDispatcher
import com.swipeit.app.core.common.TimeProvider
import com.swipeit.app.data.db.PhotoDecisionDao
import com.swipeit.app.data.db.toDecidedPhoto
import com.swipeit.app.data.db.toEntity
import com.swipeit.app.data.db.toPhoto
import com.swipeit.app.data.media.MediaImage
import com.swipeit.app.data.media.MediaSortOrder
import com.swipeit.app.data.media.MediaSource
import com.swipeit.app.data.media.toPhoto
import com.swipeit.app.domain.model.DecidedPhoto
import com.swipeit.app.domain.model.Decision
import com.swipeit.app.domain.model.Photo
import com.swipeit.app.domain.model.SortOrder
import com.swipeit.app.domain.model.Stats
import com.swipeit.app.domain.repository.PhotoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val SHARE_STOP_TIMEOUT_MILLIS = 5_000L
private const val SQL_IN_CHUNK_SIZE = 500

@Singleton
class PhotoRepositoryImpl @Inject constructor(
    private val mediaSource: MediaSource,
    private val dao: PhotoDecisionDao,
    private val timeProvider: TimeProvider,
    @ApplicationScope appScope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : PhotoRepository {

    private val refreshRequests = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /** Ids returned to the queue by undo/reset, most recent first; shown before everything else. */
    private val frontIds = MutableStateFlow<List<Long>>(emptyList())

    /** Device images newest first, re-queried on every change; reconciles Room on each full snapshot. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val deviceImages: Flow<List<MediaImage>> =
        merge(mediaSource.changes(), refreshRequests)
            .mapLatest { mediaSource.queryImages(MediaSortOrder.NEWEST_FIRST) }
            .onEach { images ->
                // Partial access or a failed query shows only part of the library: never reconcile then.
                if (images != null && mediaSource.hasFullAccess()) {
                    reconcile(images.mapTo(HashSet(images.size)) { it.id })
                }
            }
            .map { it.orEmpty() }
            .shareIn(appScope, SharingStarted.WhileSubscribed(SHARE_STOP_TIMEOUT_MILLIS), replay = 1)

    override fun unsortedQueue(sortOrder: SortOrder): Flow<List<Photo>> =
        combine(deviceImages, dao.observeAllIds(), frontIds) { images, decidedIds, front ->
            buildQueue(images, decidedIds.toHashSet(), front, sortOrder)
        }
            .distinctUntilChanged()
            .flowOn(defaultDispatcher)

    override fun trashed(): Flow<List<DecidedPhoto>> = observeStatus(Decision.TRASHED)

    override fun kept(): Flow<List<DecidedPhoto>> = observeStatus(Decision.KEPT)

    override fun recent(): Flow<List<DecidedPhoto>> =
        dao.observeRecent().map { rows -> rows.map { it.toDecidedPhoto() } }

    override fun stats(): Flow<Stats> =
        combine(
            deviceImages,
            dao.observeAllIds(),
            dao.observeCounts(),
            dao.observeTrashSize(),
        ) { images, decidedIds, counts, trashBytes ->
            val decided = decidedIds.toHashSet()
            Stats(
                unsorted = images.count { it.id !in decided },
                kept = counts.firstOrNull { it.status == Decision.KEPT }?.count ?: 0,
                trashed = counts.firstOrNull { it.status == Decision.TRASHED }?.count ?: 0,
                trashBytes = trashBytes,
            )
        }
            .distinctUntilChanged()
            .flowOn(defaultDispatcher)

    override suspend fun decide(photo: Photo, decision: Decision) = withContext(ioDispatcher) {
        dao.upsert(photo.toEntity(decision, timeProvider.nowMillis()))
        frontIds.update { it - photo.id }
    }

    override suspend fun reset(id: Long) = withContext(ioDispatcher) {
        frontIds.update { listOf(id) + (it - id) }
        dao.delete(id)
    }

    override suspend fun undoLast(): Photo? = withContext(ioDispatcher) {
        val latest = dao.getLatest() ?: return@withContext null
        frontIds.update { listOf(latest.mediaId) + (it - latest.mediaId) }
        dao.delete(latest.mediaId)
        latest.toPhoto()
    }

    override suspend fun restore(ids: List<Long>) = withContext(ioDispatcher) {
        val now = timeProvider.nowMillis()
        ids.chunked(SQL_IN_CHUNK_SIZE).forEach { dao.updateStatus(it, Decision.KEPT, now) }
    }

    override suspend fun reconcile(existingIds: Set<Long>) = withContext(ioDispatcher) {
        val missing = dao.getAllIds().filterNot { it in existingIds }
        missing.chunked(SQL_IN_CHUNK_SIZE).forEach { dao.deleteAll(it) }
        frontIds.update { front -> front.filter { it in existingIds } }
    }

    override fun requestRefresh() {
        refreshRequests.tryEmit(Unit)
    }

    private fun observeStatus(decision: Decision): Flow<List<DecidedPhoto>> =
        dao.observeByStatus(decision).map { rows -> rows.map { it.toDecidedPhoto() } }

    private fun buildQueue(
        newestFirst: List<MediaImage>,
        decided: Set<Long>,
        front: List<Long>,
        sortOrder: SortOrder,
    ): List<Photo> {
        val ordered = when (sortOrder) {
            SortOrder.NEWEST_FIRST -> newestFirst
            SortOrder.OLDEST_FIRST -> newestFirst.asReversed()
        }
        val unsorted = ordered.filterNot { it.id in decided }
        if (front.isEmpty()) return unsorted.map { it.toPhoto() }

        val byId = unsorted.associateBy { it.id }
        val head = front.mapNotNull { byId[it] }
        val headIds = head.mapTo(HashSet(head.size)) { it.id }
        return (head + unsorted.filterNot { it.id in headIds }).map { it.toPhoto() }
    }
}
