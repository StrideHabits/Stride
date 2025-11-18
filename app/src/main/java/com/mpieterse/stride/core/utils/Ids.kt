package com.mpieterse.stride.core.utils

import java.nio.charset.StandardCharsets
import java.util.UUID

/**
 * Deterministic ID utilities.
 *
 * Why:
 * - Check-ins must be idempotent across retries and offline replays.
 * - Using a stable UUID derived from (habitId, dayKey) ensures the same logical row
 *   always maps to the same primary key. This prevents duplicate rows client/server.
 *
 * How:
 * - UUID.nameUUIDFromBytes() generates a version-3 style UUID from input bytes.
 * - We join the stable fields with a delimiter and hash them into a UUID.
 * - This is deterministic, collision-resistant for our domain, and requires no server call.
 *
 * Usage:
 *   val id = checkInId(habitId, dayKey)
 *   // Use for CheckInEntity.id, MutationEntity.targetId, and PushItem.id
 */
fun checkInId(habitId: String, dayKey: String): String =
    UUID.nameUUIDFromBytes("$habitId:$dayKey".toByteArray(StandardCharsets.UTF_8)).toString()
