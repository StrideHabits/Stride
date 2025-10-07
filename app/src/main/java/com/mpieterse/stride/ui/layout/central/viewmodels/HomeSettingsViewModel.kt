package com.mpieterse.stride.ui.layout.central.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpieterse.stride.core.models.configuration.options.AlertFrequency
import com.mpieterse.stride.core.models.configuration.options.AppAppearance
import com.mpieterse.stride.core.models.configuration.options.SyncFrequency
import com.mpieterse.stride.core.models.configuration.schema.ConfigurationSchema
import com.mpieterse.stride.core.services.AuthenticationService
import com.mpieterse.stride.core.services.ConfigurationService
import com.mpieterse.stride.core.utils.Clogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeSettingsViewModel @Inject constructor(
    private val configService: ConfigurationService,
    private val authService: AuthenticationService
) : ViewModel() {
    companion object {
        private const val TAG = "HomeSettingsViewModel"
    }

    private val _theme = MutableStateFlow(AppAppearance.LIGHT)
    val theme: StateFlow<AppAppearance> = _theme

    private val _notifications = MutableStateFlow(AlertFrequency.ALL)
    val notifications: StateFlow<AlertFrequency> = _notifications

    private val _sync = MutableStateFlow(SyncFrequency.ALWAYS)
    val sync: StateFlow<SyncFrequency> = _sync

    init {
        viewModelScope.launch {
            _theme.value = configService.get(ConfigurationSchema.appAppearance)
            _notifications.value = configService.get(ConfigurationSchema.alertFrequency)
            _sync.value = configService.get(ConfigurationSchema.syncFrequency)
        }
    }

    fun updateTheme(value: AppAppearance) { //This method updates the app theme setting using ViewModel lifecycle management (Android Developers, 2024).
        _theme.value = value
        viewModelScope.launch { configService.put(ConfigurationSchema.appAppearance, value) }
        Clogger.i(TAG, "Theme updated locally: $value")
    }

    fun updateAlerts(value: AlertFrequency) { //This method updates the alert frequency setting using ViewModel lifecycle management (Android Developers, 2024).
        _notifications.value = value
        viewModelScope.launch { configService.put(ConfigurationSchema.alertFrequency, value) }
    }

    fun updateSync(value: SyncFrequency) { //This method updates the sync frequency setting using ViewModel lifecycle management (Android Developers, 2024).
        _sync.value = value
        viewModelScope.launch { configService.put(ConfigurationSchema.syncFrequency, value) }
    }

    fun logout() = authService.logout() //This method logs out the user using Firebase Authentication (Google Inc., 2024).
}
