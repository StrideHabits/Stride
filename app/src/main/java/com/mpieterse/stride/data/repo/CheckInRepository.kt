// app/src/main/java/com/mpieterse/stride/data/repo/CheckInRepository.kt
package com.mpieterse.stride.data.repo

import com.mpieterse.stride.data.dto.checkins.CheckInCreateDto
import com.mpieterse.stride.data.dto.checkins.CheckInDto
import kotlinx.coroutines.flow.Flow

interface CheckInRepository {
    fun observeForHabit(habitId: String): Flow<List<CheckInDto>>
    fun observeById(id: String): Flow<CheckInDto?>

    suspend fun getForHabit(habitId: String, forceRemote: Boolean = false): List<CheckInDto>
    suspend fun getById(id: String, forceRemote: Boolean = false): CheckInDto?

    suspend fun create(input: CheckInCreateDto): CheckInDto
    suspend fun upsertLocal(dto: CheckInDto)
    suspend fun delete(id: String)
    suspend fun clearLocal()

    // NEW
    suspend fun toggle(habitId: String, dayKey: String, on: Boolean)

    suspend fun pushBatch(): Boolean
    suspend fun pull(): Boolean
}
