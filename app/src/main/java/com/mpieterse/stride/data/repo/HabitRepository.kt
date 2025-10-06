package com.mpieterse.stride.data.repo

import com.mpieterse.stride.core.net.*
import com.mpieterse.stride.data.dto.habits.*
import com.mpieterse.stride.data.remote.SummitApiService
import javax.inject.Inject

/**
 * Repository class implementing the Repository Design Pattern in Kotlin.
 *
 * This class acts as a clean data access layer between the ViewModel and data sources
 * (e.g., local database, remote API, or in-memory cache). It abstracts data operations
 * to ensure separation of concerns, maintainability, and scalability across the app.
 *
 * @see <a href="https://medium.com/@appdevinsights/repository-design-pattern-in-kotlin-1d1aeff1ad40">
 *      App Dev Insights (2024). Repository Design Pattern in Kotlin.</a>
 *      [Accessed 6 Oct. 2025].
 */


class HabitRepository @Inject constructor(private val api: SummitApiService) {
    suspend fun list() = safeCall { api.getHabits() }
    suspend fun create(name: String) = safeCall { api.createHabit(HabitCreateDto(name)) }
    // No DELETE in Swagger → remove delete() from VM usage
}

