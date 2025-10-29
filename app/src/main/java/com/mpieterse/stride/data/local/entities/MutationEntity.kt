package com.mpieterse.stride.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "change_log")
data class MutationEntity(
    @PrimaryKey val requestId: String,
    val checkInId: String,
    val habitId: String,
    val dayKey: String,
    val completedAt: String,
    val deleted: Boolean,
    val baseVersion: String? // null on create
)
