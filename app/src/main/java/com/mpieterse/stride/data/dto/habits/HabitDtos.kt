package com.mpieterse.stride.data.dto.habits

data class HabitCreateDto(val name: String)
data class HabitDto(
    val id: String,
    val name: String,
    val createdAt: String? = null
)
