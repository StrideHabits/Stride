package com.mpieterse.stride.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "check_ins")
data class CheckInEntity(
    @PrimaryKey val id: String,
    val habitId: String,
    val dayKey: String,              // "yyyy-MM-dd"
    val completedAt: String,         // ISO-8601 UTC
    val deleted: Boolean,
    val updatedAt: String,           // from server
    val rowVersion: String,          // base64 from server
    val syncState: SyncState = SyncState.Synced
)

enum class SyncState { Synced, Pending, Failed }
