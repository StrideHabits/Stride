package com.mpieterse.stride.data.dto.habits

data class HabitCreateDto( //This data class represents habit creation data for API requests using Kotlin data classes (Kotlin Foundation, 2024).
    val name: String,
    val frequency: Int = 0,
    val tag: String? = null,
    val imageUrl: String? = null
)

data class HabitDto( //This data class represents habit data received from API responses using Kotlin data classes (Kotlin Foundation, 2024).
    val id: String,
    val name: String,
    val frequency: Int,
    val tag: String?,
    val imageUrl: String?,
    val createdAt: String,
    val updatedAt: String
)
