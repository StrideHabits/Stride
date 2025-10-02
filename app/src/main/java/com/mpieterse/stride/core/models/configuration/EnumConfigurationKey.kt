package com.mpieterse.stride.core.models.configuration

import androidx.datastore.preferences.core.Preferences

class EnumConfigurationKey<E : Enum<E>>(
    key: Preferences.Key<String>,
    private val enumClass: Class<E>,
    defaultValue: E
) : ConfigurationKey<E>(key, defaultValue) {
    override fun encode(value: E): String = value.name
    override fun decode(raw: String?): E {
        return raw
            ?.let { runCatching { java.lang.Enum.valueOf(enumClass, it) }.getOrNull() }
            ?: defaultValue
    }
}