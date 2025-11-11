// data/local/db/AppDatabase.kt
package com.mpieterse.stride.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mpieterse.stride.data.local.dao.CheckInDao
import com.mpieterse.stride.data.local.dao.HabitDao
import com.mpieterse.stride.data.local.dao.MutationDao
import com.mpieterse.stride.data.local.entities.CheckInEntity
import com.mpieterse.stride.data.local.entities.HabitEntity
import com.mpieterse.stride.data.local.entities.MutationEntity

@Database(
    entities = [HabitEntity::class, CheckInEntity::class, MutationEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habits(): HabitDao
    abstract fun checkIns(): CheckInDao
    abstract fun mutations(): MutationDao
}
