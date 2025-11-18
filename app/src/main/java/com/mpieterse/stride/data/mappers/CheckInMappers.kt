// data/mappers/CheckInMappers.kt
package com.mpieterse.stride.data.mappers

import com.mpieterse.stride.data.dto.checkins.CheckInDto
import com.mpieterse.stride.data.local.entities.CheckInEntity
import com.mpieterse.stride.data.local.entities.SyncState
import com.mpieterse.stride.data.remote.models.Change

/**
 * Map the basic CRUD read model to a local entity.
 * Note: CRUD CheckInDto has no updatedAt/rowVersion/deleted.
 * We assume not deleted and leave server tokens blank.
 */
fun CheckInDto.toEntity(
    syncState: SyncState = SyncState.Synced
) = CheckInEntity(
    id = id,
    habitId = habitId,
    dayKey = dayKey,
    completedAt = completedAt,
    deleted = false,
    updatedAt = "",
    rowVersion = "",
    syncState = syncState
)

/**
 * Map the sync "Change" item to a local entity.
 * This carries deleted, updatedAt, rowVersion from the server.
 */
fun Change.toEntity(
    syncState: SyncState = SyncState.Synced
) = CheckInEntity(
    id = id,
    habitId = habitId,
    dayKey = dayKey,
    completedAt = completedAt,
    deleted = deleted,
    updatedAt = updatedAt,
    rowVersion = rowVersion,
    syncState = syncState
)

fun CheckInEntity.toDto() = CheckInDto(
    id = id,
    habitId = habitId,
    completedAt = completedAt,
    dayKey = dayKey
)

