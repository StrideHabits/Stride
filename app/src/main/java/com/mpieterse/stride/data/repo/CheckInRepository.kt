// data/repo/CheckInRepository.kt
package com.mpieterse.stride.data.repo

import com.mpieterse.stride.core.net.*
import com.mpieterse.stride.data.dto.checkins.CheckInCreateDto
import com.mpieterse.stride.data.remote.SummitApiService
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
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

class CheckInRepository @Inject constructor(private val api: SummitApiService) {
    suspend fun list() = safeCall { api.getCheckIns() } //This method retrieves all check-ins from the API using the Repository pattern (App Dev Insights, 2024).

    suspend fun create(habitId: String, isoDate: String) = safeCall { //This method creates a new check-in through the API using the Repository pattern (App Dev Insights, 2024).
        val day = LocalDate.parse(isoDate.trim())                     // yyyy-MM-dd
        val completedAt = ZonedDateTime.of(day.atStartOfDay(), ZoneOffset.UTC).toInstant().toString()
        val dto = CheckInCreateDto(habitId.trim(), completedAt, day.toString())
        api.createCheckIn(dto)
    }
}
