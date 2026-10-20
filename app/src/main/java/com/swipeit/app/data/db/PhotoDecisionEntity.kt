package com.swipeit.app.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.swipeit.app.domain.model.Decision

@Entity(
    tableName = "photo_decisions",
    indices = [Index("status"), Index("decidedAt")],
)
data class PhotoDecisionEntity(
    @PrimaryKey val mediaId: Long,
    val uri: String,
    val status: Decision,
    val decidedAt: Long,
    val sizeBytes: Long,
    val dateTaken: Long,
    val displayName: String,
    val album: String,
)

/** Row of `SELECT status, COUNT(*) ... GROUP BY status`. */
data class StatusCount(
    val status: Decision,
    val count: Int,
)
