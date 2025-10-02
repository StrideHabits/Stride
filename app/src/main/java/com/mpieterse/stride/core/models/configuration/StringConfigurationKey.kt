package com.mpieterse.stride.core.models.configuration

import androidx.datastore.preferences.core.Preferences

class StringConfigurationKey(
    key: Preferences.Key<String>,
    defaultValue: String
) : ConfigurationKey<String>(key, defaultValue) {
    override fun encode(value: String): String = value
    override fun decode(raw: String?): String = raw ?: defaultValue
}