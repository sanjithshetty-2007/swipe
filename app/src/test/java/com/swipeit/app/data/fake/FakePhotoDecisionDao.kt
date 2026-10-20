package com.swipeit.app.data.fake

import com.swipeit.app.data.db.PhotoDecisionDao
import com.swipeit.app.data.db.PhotoDecisionEntity
import com.swipeit.app.data.db.StatusCount
import com.swipeit.app.domain.model.Decision
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory [PhotoDecisionDao] mirroring the Room queries. */
class FakePhotoDecisionDao : PhotoDecisionDao {

    private val rows = MutableStateFlow<Map<Long, PhotoDecisionEntity>>(emptyMap())

    val currentIds: Set<Long> get() = rows.value.keys

    fun row(id: Long): PhotoDecisionEntity? = rows.value[id]

    override fun observeByStatus(status: Decision): Flow<List<PhotoDecisionEntity>> =
        rows.map { all -> all.values.filter { it.status == status }.sortedByDescending { it.decidedAt } }

    override fun observeRecent(limit: Int): Flow<List<PhotoDecisionEntity>> =
        rows.map { all -> all.values.sortedByDescending { it.decidedAt }.take(limit) }

    override fun observeAllIds(): Flow<List<Long>> = rows.map { it.keys.toList() }

    override fun observeCounts(): Flow<List<StatusCount>> =
        rows.map { all ->
            all.values.groupingBy { it.status }.eachCount().map { (status, count) -> StatusCount(status, count) }
        }

    override fun observeTrashSize(): Flow<Long> =
        rows.map { all -> all.values.filter { it.status == Decision.TRASHED }.sumOf { it.sizeBytes } }

    override suspend fun upsert(entity: PhotoDecisionEntity) {
        rows.update { it + (entity.mediaId to entity) }
    }

    override suspend fun delete(id: Long) {
        rows.update { it - id }
    }

    override suspend fun deleteAll(ids: List<Long>) {
        rows.update { it - ids.toSet() }
    }

    override suspend fun updateStatus(ids: List<Long>, status: Decision, now: Long) {
        rows.update { all ->
            all.mapValues { (id, row) -> if (id in ids) row.copy(status = status, decidedAt = now) else row }
        }
    }

    override suspend fun getLatest(): PhotoDecisionEntity? = rows.value.values.maxByOrNull { it.decidedAt }

    override suspend fun getAllIds(): List<Long> = rows.value.keys.toList()
}
