package com.mpieterse.stride.ui.layout.central.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpieterse.stride.core.models.configuration.options.AlertFrequency
import com.mpieterse.stride.core.models.configuration.options.AppAppearance
import com.mpieterse.stride.core.models.configuration.options.SyncFrequency
import com.mpieterse.stride.core.models.configuration.schema.ConfigurationSchema
import com.mpieterse.stride.core.net.ApiResult
import com.mpieterse.stride.core.services.ConfigurationService
import com.mpieterse.stride.core.utils.Clogger
import com.mpieterse.stride.data.dto.settings.SettingsDto
import com.mpieterse.stride.data.repo.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeSettingsViewModel @Inject constructor(
    private val configService: ConfigurationService,
    private val settingsRepo: SettingsRepository,
) : ViewModel() {

    companion object { private const val TAG = "HomeSettingsViewModel" }

    // ---- Reactive UI state ----
    private val _theme = MutableStateFlow(AppAppearance.LIGHT)
    val theme: StateFlow<AppAppearance> = _theme

    private val _notifications = MutableStateFlow(AlertFrequency.ALL)
    val notifications: StateFlow<AlertFrequency> = _notifications

    private val _sync = MutableStateFlow(SyncFrequency.ALWAYS)
    val sync: StateFlow<SyncFrequency> = _sync

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        viewModelScope.launch {
            _theme.value = configService.get(ConfigurationSchema.appAppearance)
            _notifications.value = configService.get(ConfigurationSchema.alertFrequency)
            _sync.value = configService.get(ConfigurationSchema.syncFrequency)
            fetchFromApi()
        }
    }

    // ---- Public updaters ----

    fun updateTheme(value: AppAppearance) {
        _theme.value = value
        viewModelScope.launch { configService.put(ConfigurationSchema.appAppearance, value) }
        pushThemeToApi(value)
    }

    fun updateAlerts(value: AlertFrequency) {
        _notifications.value = value
        viewModelScope.launch { configService.put(ConfigurationSchema.alertFrequency, value) }
    }

    fun updateSync(value: SyncFrequency) {
        _sync.value = value
        viewModelScope.launch { configService.put(ConfigurationSchema.syncFrequency, value) }
        Clogger.i(TAG, "Sync set to $value")
    }

    // ---- API bridge (theme only) ----

    private fun pushThemeToApi(value: AppAppearance) = viewModelScope.launch {
        val apiTheme = value.toApiTheme()
        _loading.value = true; _error.value = null
        when (val r = settingsRepo.update(SettingsDto(dailyReminderHour = null, theme = apiTheme))) {
            is ApiResult.Ok -> {
                _loading.value = false
                Clogger.i(TAG, "Settings updated on server: theme=$apiTheme")
            }
            is ApiResult.Err -> {
                _loading.value = false
                _error.value = "${r.code ?: ""} ${r.message ?: "Update failed"}"
                Clogger.e(TAG, "Update failed: ${_error.value}")
            }
        }
    }

    private fun fetchFromApi() = viewModelScope.launch {
        _loading.value = true; _error.value = null
        when (val r = settingsRepo.get()) {
            is ApiResult.Ok -> {
                _loading.value = false
                val dto = r.data
                val uiTheme = dto.theme?.toUiTheme() ?: _theme.value
                _theme.value = uiTheme
                configService.put(ConfigurationSchema.appAppearance, uiTheme)
                Clogger.i(TAG, "Fetched settings from server: $dto")
            }
            is ApiResult.Err -> {
                _loading.value = false
                _error.value = "${r.code ?: ""} ${r.message ?: "Load failed"}"
                Clogger.w(TAG, "Using cached settings; server fetch failed: ${_error.value}")
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
        "dark" -> AppAppearance.NIGHT
        else -> AppAppearance.SYSTEM
    }
}
