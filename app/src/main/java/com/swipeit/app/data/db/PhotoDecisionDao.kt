package com.swipeit.app.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.swipeit.app.domain.model.Decision
import kotlinx.coroutines.flow.Flow

const val RECENT_DECISIONS_LIMIT = 5

@Dao
interface PhotoDecisionDao {

    @Query("SELECT * FROM photo_decisions WHERE status = :status ORDER BY decidedAt DESC")
    fun observeByStatus(status: Decision): Flow<List<PhotoDecisionEntity>>

    @Query("SELECT * FROM photo_decisions ORDER BY decidedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = RECENT_DECISIONS_LIMIT): Flow<List<PhotoDecisionEntity>>

    @Query("SELECT mediaId FROM photo_decisions")
    fun observeAllIds(): Flow<List<Long>>

    @Query("SELECT status, COUNT(*) AS count FROM photo_decisions GROUP BY status")
    fun observeCounts(): Flow<List<StatusCount>>

    // 'TRASHED' matches DecisionConverter, which stores the enum name.
    @Query("SELECT COALESCE(SUM(sizeBytes), 0) FROM photo_decisions WHERE status = 'TRASHED'")
    fun observeTrashSize(): Flow<Long>

    @Upsert
    suspend fun upsert(entity: PhotoDecisionEntity)

    @Query("DELETE FROM photo_decisions WHERE mediaId = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM photo_decisions WHERE mediaId IN (:ids)")
    suspend fun deleteAll(ids: List<Long>)

    @Query("UPDATE photo_decisions SET status = :status, decidedAt = :now WHERE mediaId IN (:ids)")
    suspend fun updateStatus(ids: List<Long>, status: Decision, now: Long)

    @Query("SELECT * FROM photo_decisions ORDER BY decidedAt DESC LIMIT 1")
    suspend fun getLatest(): PhotoDecisionEntity?

    @Query("SELECT mediaId FROM photo_decisions")
    suspend fun getAllIds(): List<Long>
}
