package com.mpieterse.stride.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.ds by preferencesDataStore("auth")

class TokenStore(private val ctx: Context) {
    private val KEY = stringPreferencesKey("jwt")
    val tokenFlow = ctx.ds.data.map { it[KEY] }
    suspend fun set(token: String) = ctx.ds.edit { it[KEY] = token } //This method stores authentication tokens using Android DataStore (Android Developers, 2024).
    suspend fun clear() = ctx.ds.edit { it.remove(KEY) } //This method clears stored authentication tokens using Android DataStore (Android Developers, 2024).
    
    /**
     * Get the current token synchronously (for checking if token exists).
     * Note: This is a blocking call, use sparingly.
     */
    suspend fun getToken(): String? {
        return ctx.ds.data.map { it[KEY] }.first()
    }
    
    /**
     * Check if a token exists and is not empty.
     */
    suspend fun hasToken(): Boolean {
        val token = getToken()
        return !token.isNullOrBlank()
    }
}
