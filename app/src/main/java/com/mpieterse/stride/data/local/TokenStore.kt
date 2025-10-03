package com.mpieterse.stride.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.ds by preferencesDataStore("auth")

class TokenStore(private val ctx: Context) {
    private val KEY = stringPreferencesKey("jwt")
    val tokenFlow = ctx.ds.data.map { it[KEY] }
    suspend fun set(token: String) = ctx.ds.edit { it[KEY] = token }
    suspend fun clear() = ctx.ds.edit { it.remove(KEY) }
}
