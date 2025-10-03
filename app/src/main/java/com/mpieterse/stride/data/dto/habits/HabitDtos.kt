package com.mpieterse.stride.data.dto.habits
data class HabitDto(val id: String, val name: String, val createdAt: String)
data class CreateHabitRequest(val name: String)
