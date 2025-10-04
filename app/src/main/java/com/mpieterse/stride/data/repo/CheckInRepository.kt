// data/repo/CheckInRepository.kt
package com.mpieterse.stride.data.repo

import com.mpieterse.stride.core.net.*
import com.mpieterse.stride.data.dto.checkins.CheckInCreateDto
import com.mpieterse.stride.data.remote.SummitApiService
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import javax.inject.Inject

class CheckInRepository @Inject constructor(private val api: SummitApiService) {
    suspend fun list() = safeCall { api.getCheckIns() }

    suspend fun create(habitId: String, isoDate: String) = safeCall {
        val day = LocalDate.parse(isoDate.trim())                     // yyyy-MM-dd
        val completedAt = ZonedDateTime.of(day.atStartOfDay(), ZoneOffset.UTC).toInstant().toString()
        api.createCheckIn(CheckInCreateDto(habitId.trim(), completedAt, day.toString()))
    }
}
