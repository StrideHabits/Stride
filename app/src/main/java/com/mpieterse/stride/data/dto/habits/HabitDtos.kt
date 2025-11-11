// data/dto/habits/HabitDtos.kt
package com.mpieterse.stride.data.dto.habits

data class HabitCreateDto(
    val name: String,
    val frequency: Int = 0,
    val tag: String? = null,
    val imageUrl: String? = null
)

data class HabitDto(
    val id: String,
    val name: String,
    val frequency: Int,
    val tag: String?,
    val imageUrl: String?,
    val createdAt: String,
    val updatedAt: String
)
