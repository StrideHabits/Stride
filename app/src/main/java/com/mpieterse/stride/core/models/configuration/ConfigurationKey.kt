package com.mpieterse.stride.core.models.configuration

import androidx.datastore.preferences.core.Preferences

sealed class ConfigurationKey<T>(
    val key: Preferences.Key<String>,
    val defaultValue: T
) {
    abstract fun encode(value: T): String
    abstract fun decode(raw: String?): T
}