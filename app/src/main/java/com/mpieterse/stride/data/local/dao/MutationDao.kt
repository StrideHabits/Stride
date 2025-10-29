package com.mpieterse.stride.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mpieterse.stride.data.local.entities.MutationEntity

@Dao
interface MutationDao {
    @Query("SELECT * FROM change_log LIMIT :limit")
    suspend fun nextBatch(limit: Int): List<MutationEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(m: MutationEntity)

    @Query("DELETE FROM change_log WHERE requestId IN (:ids)")
    suspend fun delete(ids: List<String>)
}
