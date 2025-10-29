package com.mpieterse.stride.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.mpieterse.stride.data.local.dao.CheckInDao
import com.mpieterse.stride.data.local.dao.MutationDao
import com.mpieterse.stride.data.local.entities.CheckInEntity
import com.mpieterse.stride.data.local.entities.MutationEntity

// AppDatabase.kt
@Database(entities = [CheckInEntity::class, MutationEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun checkIns(): CheckInDao
    abstract fun mutations(): MutationDao
}
