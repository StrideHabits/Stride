package com.mpieterse.stride.ui.layout.central.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpieterse.stride.core.models.configuration.options.AlertFrequency
import com.mpieterse.stride.core.models.configuration.options.AppAppearance
import com.mpieterse.stride.core.models.configuration.options.SyncFrequency
import com.mpieterse.stride.core.models.configuration.schema.ConfigurationSchema
import com.mpieterse.stride.core.services.ConfigurationService
import com.mpieterse.stride.core.utils.Clogger
import com.mpieterse.stride.core.net.ApiResult
import com.mpieterse.stride.data.dto.settings.SettingsDto
import com.mpieterse.stride.data.repo.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class HomeSettingsViewModel @Inject constructor(
    private val configService: ConfigurationService,
    private val settingsRepo: SettingsRepository,
) : ViewModel() {

    companion object { private const val TAG = "HomeSettingsViewModel" }

    // UI state
    var theme by mutableStateOf(AppAppearance.LIGHT)
        private set
    var notifications by mutableStateOf(AlertFrequency.ALL)  // local-only
        private set
    var sync by mutableStateOf(SyncFrequency.ALWAYS)         // local-only
        private set

    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    init {
        // Load cached values fast, then hydrate theme from API
        viewModelScope.launch {
            theme = configService.get(ConfigurationSchema.appAppearance)
            notifications = configService.get(ConfigurationSchema.alertFrequency)
            sync = configService.get(ConfigurationSchema.syncFrequency)
            fetchFromApi()
        }
    }

    // ---- Public updaters ----

    fun updateTheme(value: AppAppearance) {
        theme = value
        viewModelScope.launch { configService.put(ConfigurationSchema.appAppearance, value) }
        pushThemeToApi()
    }

    fun updateAlerts(value: AlertFrequency) {
        notifications = value
        viewModelScope.launch { configService.put(ConfigurationSchema.alertFrequency, value) }
        // API does not support notifications field — local only.
    }

    fun updateSync(value: SyncFrequency) {
        sync = value
        viewModelScope.launch { configService.put(ConfigurationSchema.syncFrequency, value) }
        Clogger.i(TAG, "Sync set to $value")
    }

    // ---- API bridge (theme only) ----

    private fun pushThemeToApi() = viewModelScope.launch {
        val apiTheme = theme.toApiTheme()
        loading = true; error = null
        when (val r = settingsRepo.update(SettingsDto(dailyReminderHour = null, theme = apiTheme))) {
            is ApiResult.Ok -> {
                loading = false
                Clogger.i(TAG, "Settings updated on server: theme=$apiTheme")
            }
            is ApiResult.Err -> {
                loading = false
                error = "${r.code ?: ""} ${r.message ?: "Update failed"}"
                Clogger.e(TAG, "Update failed: $error")
            }
        }
    }

    private fun fetchFromApi() = viewModelScope.launch {
        loading = true; error = null
        when (val r = settingsRepo.get()) {
            is ApiResult.Ok -> {
                loading = false
                val dto = r.data
                val uiTheme = dto.theme?.toUiTheme() ?: theme
                theme = uiTheme
                // cache theme locally
                configService.put(ConfigurationSchema.appAppearance, uiTheme)
                Clogger.i(TAG, "Fetched settings from server: $dto")
            }
            is ApiResult.Err -> {
                loading = false
                error = "${r.code ?: ""} ${r.message ?: "Load failed"}"
                Clogger.w(TAG, "Using cached settings; server fetch failed: $error")
            }
        }
    }

    // ---- Mappers ----

    private fun AppAppearance.toApiTheme(): String = when (this) {
        AppAppearance.LIGHT -> "light"
        AppAppearance.NIGHT -> "dark"
        AppAppearance.SYSTEM -> "system"
    }

    private fun String.toUiTheme(): AppAppearance = when (lowercase()) {
        "light" -> AppAppearance.LIGHT
        "dark"  -> AppAppearance.NIGHT
        else    -> AppAppearance.SYSTEM
    }
}
