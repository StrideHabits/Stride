// data/dto/checkins/CheckinDtos.kt
package com.mpieterse.stride.data.dto.checkins

data class CheckInCreateDto( //This data class represents check-in creation data for API requests using Kotlin data classes (Kotlin Foundation, 2024).
    val habitId: String, // Will be serialized as UUID string
    val completedAt: String?, // ISO-8601 instant e.g. 2025-10-04T00:00:00Z
    val dayKey: String?       // yyyy-MM-dd (or whatever your API expects)
)

data class CheckInDto( //This data class represents check-in data received from API responses using Kotlin data classes (Kotlin Foundation, 2024).
    val id: String,
    val habitId: String,
    val completedAt: String,
    val dayKey: String
)
