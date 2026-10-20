package com.swipeit.app.data.db

import androidx.room.TypeConverter
import com.swipeit.app.domain.model.Decision

/** Stores [Decision] as its enum name ("KEPT" / "TRASHED"). */
class DecisionConverter {
    @TypeConverter
    fun fromDecision(decision: Decision): String = decision.name

    @TypeConverter
    fun toDecision(value: String): Decision = Decision.valueOf(value)
}
