package com.mpieterse.stride.data.repo

import com.mpieterse.stride.core.net.*
import com.mpieterse.stride.data.dto.habits.*
import com.mpieterse.stride.data.remote.SummitApiService
import javax.inject.Inject

class HabitRepository @Inject constructor(private val api: SummitApiService) {
    suspend fun list() = safeCall { api.getHabits() }
    suspend fun create(name: String) = safeCall { api.createHabit(CreateHabitRequest(name)) }
    suspend fun delete(id: String) = safeCall { api.deleteHabit(id) }
}
