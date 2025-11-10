// app/src/main/java/com/mpieterse/stride/data/repo/concrete/HabitRepositoryImpl.kt
package com.mpieterse.stride.data.repo.concrete

import com.mpieterse.stride.data.dto.habits.HabitCreateDto
import com.mpieterse.stride.data.dto.habits.HabitDto
import com.mpieterse.stride.data.repo.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepositoryImpl @Inject constructor(
    // inject API/DAO here
) : HabitRepository {

    // Replace with Room/Flow from DAO
    private val all = MutableStateFlow<List<HabitDto>>(emptyList())

    override fun observeAll(): Flow<List<HabitDto>> = all
    override fun observeById(id: String): Flow<HabitDto?> = MutableStateFlow(null)

    override suspend fun getAll(forceRemote: Boolean): List<HabitDto> = emptyList()
    override suspend fun getById(id: String, forceRemote: Boolean): HabitDto? = null

    override suspend fun create(input: HabitCreateDto): HabitDto =
        throw NotImplementedError("Implement remote create + cache")

    override suspend fun upsertLocal(dto: HabitDto) { /* cache */ }
    override suspend fun delete(id: String) { /* remote + cache */ }
    override suspend fun clearLocal() { /* clear cache */ }

    override suspend fun pushHabitCreates(): Boolean = false
    override suspend fun pull(): Boolean = false
}
