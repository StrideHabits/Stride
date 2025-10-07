package com.mpieterse.stride.core.models.configuration

import androidx.datastore.preferences.core.Preferences

class EnumConfigurationKey<E : Enum<E>>( //This class handles enum-based configuration keys using Android DataStore preferences (Android Developers, 2024).
    key: Preferences.Key<String>,
    private val enumClass: Class<E>,
    defaultValue: E
) : ConfigurationKey<E>(key, defaultValue) {
    override fun encode(value: E): String = value.name //This method encodes enum values to strings for storage using Kotlin enum handling (Kotlin Foundation, 2024).
    override fun decode(raw: String?): E { //This method decodes string values to enums for retrieval using Kotlin enum handling (Kotlin Foundation, 2024).
        return raw
            ?.let { runCatching { java.lang.Enum.valueOf(enumClass, it) }.getOrNull() }
            ?: defaultValue
    }
}