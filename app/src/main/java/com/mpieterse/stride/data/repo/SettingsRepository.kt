package com.mpieterse.stride.data.repo

import com.mpieterse.stride.core.net.*
import com.mpieterse.stride.data.dto.settings.SettingsDto
import com.mpieterse.stride.data.remote.SummitApiService
import javax.inject.Inject

class SettingsRepository @Inject constructor(private val api: SummitApiService) {
    suspend fun get() = safeCall { api.getSettings() }
    suspend fun update(s: SettingsDto) = safeCall { api.updateSettings(s) }
}
