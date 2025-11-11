package com.mpieterse.stride.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,   // UUID (server or client)
    val name: String,
    val frequency: Int,
    val tag: String?,
    val imageUrl: String?,
    val deleted: Boolean = false,

    // server-sourced
    val createdAt: String = "",   // ISO-8601
    val updatedAt: String = "",
    val rowVersion: String = "",  // empty unless server adds rowversion later

    // local sync flag
    val syncState: SyncState = SyncState.Synced
)
