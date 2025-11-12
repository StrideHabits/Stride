package com.mpieterse.stride.data.store

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class StoredCredentials(val email: String, val password: String)

@Singleton
class CredentialsStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun save(email: String, password: String) {
        prefs.edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_PASSWORD, password)
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun get(): StoredCredentials? {
        val email = prefs.getString(KEY_EMAIL, null)
        val password = prefs.getString(KEY_PASSWORD, null)
        return if (!email.isNullOrBlank() && !password.isNullOrBlank()) {
            StoredCredentials(email, password)
        } else {
            null
        }
    }

    companion object {
        private const val PREFS_FILE = "stride.credentials"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
    }
}

