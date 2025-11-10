// data/local/dao/HabitDao.kt
package com.mpieterse.stride.data.local.dao

import androidx.room.*
import com.mpieterse.stride.data.local.entities.HabitEntity
import kotlinx.coroutines.flow.Flow

// data/local/dao/HabitDao.kt
@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE deleted=0 ORDER BY name")
    fun all(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id=:id LIMIT 1")
    suspend fun get(id: String): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg items: HabitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<HabitEntity>)

    @Query("UPDATE habits SET syncState=:state, updatedAt=:updatedAt, rowVersion=:rowVersion WHERE id=:id")
    suspend fun markSynced(id: String, state: String, updatedAt: String, rowVersion: String)

    @Query("DELETE FROM habits WHERE id=:id")
    suspend fun hardDelete(id: String)
}
