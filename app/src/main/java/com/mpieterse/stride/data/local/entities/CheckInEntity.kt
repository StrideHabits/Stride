// data/local/entities/CheckInEntity.kt  (add unique pair habitId+dayKey if you didn’t)
package com.mpieterse.stride.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "check_ins",
    indices = [Index(value = ["habitId","dayKey"], unique = true)]
)
data class CheckInEntity(
    @PrimaryKey val id: String,              // recommend "$habitId_$dayKey" for idempotency
    val habitId: String,
    val dayKey: String,                      // yyyy-MM-dd
    val completedAt: String,                 // ISO-8601 UTC
    val deleted: Boolean,
    val updatedAt: String,
    val rowVersion: String,
    val syncState: SyncState = SyncState.Synced
)

enum class SyncState { Synced, Pending, Failed }
