// app/src/main/java/com/mpieterse/stride/data/mappers/HabitMappers.kt
package com.mpieterse.stride.data.mappers

import com.mpieterse.stride.data.dto.habits.HabitDto
import com.mpieterse.stride.data.local.entities.HabitEntity
import com.mpieterse.stride.data.local.entities.SyncState

fun HabitDto.toEntity(syncState: SyncState = SyncState.Synced) = HabitEntity(
    id = id,
    name = name,
    frequency = frequency,
    tag = tag,
    imageUrl = imageUrl,
    deleted = false,
    createdAt = createdAt,
    updatedAt = updatedAt,
    rowVersion = "",
    syncState = syncState
)

fun HabitEntity.toDto() = HabitDto(
    id = id,
    name = name,
    frequency = frequency,
    tag = tag,
    imageUrl = imageUrl,
    createdAt = createdAt,
    updatedAt = updatedAt
)
