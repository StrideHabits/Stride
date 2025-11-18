package com.mpieterse.stride.core.models.configuration

import androidx.datastore.preferences.core.stringPreferencesKey
import com.mpieterse.stride.core.models.configuration.options.AppAppearance
import org.junit.Assert.assertEquals
import org.junit.Test

class EnumConfigurationKeyTest {

    private val key = stringPreferencesKey("appearance")

    @Test
    fun `encode returns enum name`() {
        val schemaKey = EnumConfigurationKey(
            key = key,
            enumClass = AppAppearance::class.java,
            defaultValue = AppAppearance.SYSTEM
        )

        assertEquals("LIGHT", schemaKey.encode(AppAppearance.LIGHT))
    }

    @Test
    fun `decode falls back to default when raw is null`() {
        val schemaKey = EnumConfigurationKey(
            key = key,
            enumClass = AppAppearance::class.java,
            defaultValue = AppAppearance.SYSTEM
        )

        assertEquals(AppAppearance.SYSTEM, schemaKey.decode(null))
    }
}

