// data/dto/checkins/CheckinDtos.kt
package com.mpieterse.stride.data.dto.checkins

data class CheckInCreateDto(
    val habitId: String,
    val completedAt: String, // ISO-8601 instant e.g. 2025-10-04T00:00:00Z
    val dayKey: String       // yyyy-MM-dd (or whatever your API expects)
)

data class CheckInDto(
    val id: String,
    val habitId: String,
    val completedAt: String,
    val dayKey: String
)
