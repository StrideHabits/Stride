package com.mpieterse.stride.core.models.configuration

import androidx.datastore.preferences.core.Preferences

sealed class ConfigurationKey<T>( //This sealed class defines configuration key structure using Android DataStore preferences (Android Developers, 2024).
    val key: Preferences.Key<String>,
    val defaultValue: T
) {
    abstract fun encode(value: T): String
    abstract fun decode(raw: String?): T
}