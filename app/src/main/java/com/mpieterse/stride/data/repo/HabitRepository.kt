package com.mpieterse.stride.data.repo

import com.mpieterse.stride.core.net.*
import com.mpieterse.stride.data.dto.habits.*
import com.mpieterse.stride.data.remote.SummitApiService
import javax.inject.Inject

class HabitRepository @Inject constructor(private val api: SummitApiService) {
    suspend fun list() = safeCall { api.getHabits() }
    suspend fun create(name: String) = safeCall { api.createHabit(HabitCreateDto(name)) }
    // No DELETE in Swagger → remove delete() from VM usage
}

