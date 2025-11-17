package com.mpieterse.stride.core.models.configuration.schema

import androidx.datastore.preferences.core.stringPreferencesKey
import com.mpieterse.stride.core.models.configuration.EnumConfigurationKey
import com.mpieterse.stride.core.models.configuration.StringConfigurationKey
import com.mpieterse.stride.core.models.configuration.options.AlertFrequency
import com.mpieterse.stride.core.models.configuration.options.AppAppearance
import com.mpieterse.stride.core.models.configuration.options.SyncFrequency

object ConfigurationSchema { //This object defines the application configuration schema using Android DataStore preferences (Android Developers, 2024).
    val appAppearance = EnumConfigurationKey(
        key = stringPreferencesKey("app_appearance"),
        enumClass = AppAppearance::class.java,
        defaultValue = AppAppearance.SYSTEM
    )

    val alertFrequency = EnumConfigurationKey(
        key = stringPreferencesKey("alert_frequency"),
        enumClass = AlertFrequency::class.java,
        defaultValue = AlertFrequency.ALL
    )

    val syncFrequency = EnumConfigurationKey(
        key = stringPreferencesKey("sync_frequency"),
        enumClass = SyncFrequency::class.java,
        defaultValue = SyncFrequency.ALWAYS
    )

    val appUiCulture = StringConfigurationKey(
        key = stringPreferencesKey("app_ui_culture"),
        defaultValue = "en"
    )
}