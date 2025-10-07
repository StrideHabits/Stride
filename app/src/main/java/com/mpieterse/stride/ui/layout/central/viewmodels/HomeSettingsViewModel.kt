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
import com.mpieterse.stride.core.services.AuthenticationService
import com.mpieterse.stride.core.services.ConfigurationService
import com.mpieterse.stride.core.utils.Clogger
import dagger.hilt.android.lifecycle.HiltViewModel
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


// --- Functions


    var theme by mutableStateOf(AppAppearance.LIGHT)
        private set


    var notifications by mutableStateOf(AlertFrequency.ALL)
        private set


    var sync by mutableStateOf(SyncFrequency.ALWAYS)
        private set


    init {
        viewModelScope.launch {
            theme = configService.get(ConfigurationSchema.appAppearance)
            notifications = configService.get(ConfigurationSchema.alertFrequency)
            sync = configService.get(ConfigurationSchema.syncFrequency)
        }
    }


    fun updateTheme(value: AppAppearance) {
        theme = value
        viewModelScope.launch { 
            configService.put(ConfigurationSchema.appAppearance, value)
            Clogger.i(
                TAG, "Theme set to $value"
            )
        }
    }


    fun updateAlerts(value: AlertFrequency) {
        notifications = value
        viewModelScope.launch { 
            configService.put(ConfigurationSchema.alertFrequency, value)
            Clogger.i(
                TAG, "Notifications set to $value"
            )
        }
    }


    fun updateSync(value: SyncFrequency) {
        sync = value
        viewModelScope.launch { 
            configService.put(ConfigurationSchema.syncFrequency, value)
            Clogger.i(
                TAG, "Sync set to $value"
            )
        }
    }
    
    fun logout() = authService.logout()
}