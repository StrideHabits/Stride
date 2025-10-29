package com.mpieterse.stride.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mpieterse.stride.core.dependencies.dataStore
import kotlinx.coroutines.flow.first

object SyncPrefs {
    private val KEY_SINCE = stringPreferencesKey("since_checkins")

    suspend fun getSince(ctx: Context): String? =
        ctx.dataStore.data.first()[KEY_SINCE]

    suspend fun setSince(ctx: Context, value: String?) {
        ctx.dataStore.edit { prefs ->
            if (value == null) prefs.remove(KEY_SINCE) else prefs[KEY_SINCE] = value
        }
    }
}
