// data/local/dao/MutationDao.kt
package com.mpieterse.stride.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.mpieterse.stride.data.local.entities.MutationEntity
import com.mpieterse.stride.data.local.entities.MutationState

@Dao
interface MutationDao {
    @Insert
    suspend fun insert(m: MutationEntity): Long

    @Query("""
        SELECT * FROM mutations 
        WHERE state='Pending'
        ORDER BY createdAtMs ASC 
        LIMIT :limit
    """)
    suspend fun nextBatch(limit: Int): List<MutationEntity>

    @Query("UPDATE mutations SET state='Applied' WHERE localId IN(:ids)")
    suspend fun markApplied(ids: List<Long>)

    @Query("UPDATE mutations SET state=:state, attemptCount=attemptCount+1, lastError=:error WHERE localId IN(:ids)")
    suspend fun mark(ids: List<Long>, state: MutationState, error: String?)

    @Query("DELETE FROM mutations WHERE state='Applied'")
    suspend fun purgeApplied()
}
