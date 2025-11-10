// data/local/dao/CheckInDao.kt   (add markSynced helper)
package com.mpieterse.stride.data.local.dao

import androidx.room.*
import com.mpieterse.stride.data.local.entities.CheckInEntity
import kotlinx.coroutines.flow.Flow

// data/local/dao/CheckInDao.kt
@Dao
interface CheckInDao {
    @Query("SELECT * FROM check_ins WHERE habitId=:habitId AND deleted=0 ORDER BY dayKey DESC")
    fun byHabit(habitId: String): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins WHERE id=:id LIMIT 1")
    suspend fun getById(id: String): CheckInEntity?

    @Query("SELECT * FROM check_ins WHERE habitId=:habitId AND dayKey=:dayKey LIMIT 1")
    suspend fun getByHabitDay(habitId: String, dayKey: String): CheckInEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg items: CheckInEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CheckInEntity>)

    @Query("UPDATE check_ins SET syncState=:state, updatedAt=:updatedAt, rowVersion=:rowVersion WHERE id=:id")
    suspend fun markSynced(id: String, state: String, updatedAt: String, rowVersion: String)

    @Query("UPDATE check_ins SET syncState='Failed' WHERE id IN(:ids)")
    suspend fun markFailed(ids: List<String>)

    @Query("DELETE FROM check_ins WHERE id=:id")
    suspend fun hardDelete(id: String)

    // remap foreign key habitId when server re-keys
    @Query("UPDATE check_ins SET habitId=:newId WHERE habitId=:oldId")
    suspend fun remapHabitId(oldId: String, newId: String)
}
