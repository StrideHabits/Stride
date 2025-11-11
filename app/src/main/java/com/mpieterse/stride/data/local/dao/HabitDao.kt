// app/src/main/java/com/mpieterse/stride/data/local/dao/HabitDao.kt
package com.mpieterse.stride.data.local.dao

import androidx.room.*
import com.mpieterse.stride.data.local.entities.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE deleted = 0 ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<HabitEntity?>

    @Query("SELECT * FROM habits WHERE deleted = 0")
    suspend fun list(): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg items: HabitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<HabitEntity>)

    @Query("UPDATE habits SET deleted = 1 WHERE id = :id")
    suspend fun markDeleted(id: String)

    @Query("DELETE FROM habits")
    suspend fun clearAll()

    @Transaction
    suspend fun replaceAll(items: List<HabitEntity>) {
        clearAll()
        upsert(*items.toTypedArray())
    }
}
