package com.mpieterse.stride.core.models.configuration

import androidx.datastore.preferences.core.Preferences

class LongConfigurationKey(
    key: Preferences.Key<String>,
    defaultValue: Long
) : ConfigurationKey<Long>(key, defaultValue) {
    override fun encode(value: Long): String = value.toString()
    override fun decode(raw: String?): Long = raw?.toLongOrNull() ?: defaultValue
}