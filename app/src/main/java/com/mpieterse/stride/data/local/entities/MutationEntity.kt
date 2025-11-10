// data/local/entities/MutationEntity.kt
package com.mpieterse.stride.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "mutations",
    indices = [Index(value = ["state","createdAtMs"])]
)
data class MutationEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,

    // idempotency + routing
    val requestId: String,                   // UUID v4
    val targetId: String,                    // habit.id or checkin.id
    val targetType: TargetType,              // Habit or CheckIn
    val op: MutationOp,                      // Create | Update | Delete

    // payload (nullable by op/type)
    val name: String? = null,
    val frequency: Int? = null,
    val tag: String? = null,
    val imageUrl: String? = null,
    val habitId: String? = null,
    val dayKey: String? = null,
    val completedAt: String? = null,
    val deleted: Boolean = false,

    // optimistic concurrency
    val baseVersion: String? = null,

    // local bookkeeping
    val state: MutationState = MutationState.Pending,
    val attemptCount: Int = 0,
    val createdAtMs: Long = System.currentTimeMillis(),
    val lastError: String? = null
)

enum class TargetType { Habit, CheckIn }
enum class MutationOp { Create, Update, Delete }
enum class MutationState { Pending, InFlight, Failed, Applied }
