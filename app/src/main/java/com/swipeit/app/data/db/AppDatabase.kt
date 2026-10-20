package com.swipeit.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [PhotoDecisionEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(DecisionConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun photoDecisionDao(): PhotoDecisionDao

    companion object {
        const val NAME = "swipeit.db"
    }
}
