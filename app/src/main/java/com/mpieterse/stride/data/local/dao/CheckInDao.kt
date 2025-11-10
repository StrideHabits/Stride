// data/local/dao/CheckInDao.kt
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

    @Query("SELECT * FROM check_ins WHERE id=:id LIMIT 1")
    fun observeEntityById(id: String): Flow<CheckInEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg items: CheckInEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CheckInEntity>)

    @Query("UPDATE check_ins SET syncState=:state, updatedAt=:updatedAt, rowVersion=:rowVersion WHERE id=:id")
    suspend fun markSynced(id: String, state: String, updatedAt: String, rowVersion: String)

    @Query("UPDATE check_ins SET syncState='Failed' WHERE id IN(:ids)")
    suspend fun markFailed(ids: List<String>)

    @Query("DELETE FROM check_ins")
    suspend fun clearAll()
}
