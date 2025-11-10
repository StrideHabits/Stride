// data/dto/checkins/CheckinDtos.kt
package com.mpieterse.stride.data.dto.checkins

data class CheckInCreateDto(
    val habitId: String,      // UUID string
    val completedAt: String,  // ISO-8601 UTC, e.g. 2025-10-04T00:00:00Z
    val dayKey: String        // yyyy-MM-dd
)

data class CheckInDto(
    val id: String,
    val habitId: String,
    val completedAt: String,
    val dayKey: String
)
