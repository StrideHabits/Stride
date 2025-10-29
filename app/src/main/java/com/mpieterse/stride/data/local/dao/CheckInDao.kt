package com.mpieterse.stride.data.local.dao

import androidx.room.*
import com.mpieterse.stride.data.local.entities.CheckInEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {
    @Query("SELECT * FROM check_ins WHERE habitId=:habitId AND deleted=0 ORDER BY dayKey DESC")
    fun byHabit(habitId: String): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins WHERE id=:id LIMIT 1")
    suspend fun getById(id: String): CheckInEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg items: CheckInEntity)
}
