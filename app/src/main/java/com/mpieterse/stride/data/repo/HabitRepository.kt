// app/src/main/java/com/mpieterse/stride/data/repo/HabitRepository.kt
package com.mpieterse.stride.data.repo

import com.mpieterse.stride.data.dto.habits.HabitCreateDto
import com.mpieterse.stride.data.dto.habits.HabitDto
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun observeAll(): Flow<List<HabitDto>>
    fun observeById(id: String): Flow<HabitDto?>

    suspend fun getAll(forceRemote: Boolean = false): List<HabitDto>
    suspend fun getById(id: String, forceRemote: Boolean = false): HabitDto?

    suspend fun create(input: HabitCreateDto): HabitDto
    suspend fun update(id: String, input: HabitCreateDto): HabitDto
    suspend fun upsertLocal(dto: HabitDto)
    suspend fun delete(id: String)
    suspend fun clearLocal()

    suspend fun pushHabitCreates(): Boolean
    suspend fun pull(): Boolean
}
