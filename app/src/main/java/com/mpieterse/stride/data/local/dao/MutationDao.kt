// data/local/dao/MutationDao.kt
package com.mpieterse.stride.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.mpieterse.stride.data.local.entities.MutationEntity
import com.mpieterse.stride.data.local.entities.MutationState
import com.mpieterse.stride.data.local.entities.TargetType

// data/local/dao/MutationDao.kt
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

    @Query("UPDATE mutations SET state='Pending', lastError=NULL WHERE localId IN(:ids)")
    suspend fun requeue(ids: List<Long>)

    @Query("DELETE FROM mutations WHERE state='Applied'")
    suspend fun purgeApplied()

    @Query(
        """
        SELECT DISTINCT targetId FROM mutations
        WHERE targetType = :targetType AND state IN (:states)
        """
    )
    suspend fun targetIdsWithStates(
        targetType: TargetType,
        states: List<MutationState>
    ): List<String>

    // remap any references to the temp habit id
    @Query("""
        UPDATE mutations 
        SET habitId = CASE WHEN habitId = :oldId THEN :newId ELSE habitId END,
            targetId = CASE WHEN targetType='Habit' AND targetId = :oldId THEN :newId ELSE targetId END
        WHERE habitId = :oldId OR (targetType='Habit' AND targetId = :oldId)
    """)
    suspend fun remapIds(oldId: String, newId: String)
}
