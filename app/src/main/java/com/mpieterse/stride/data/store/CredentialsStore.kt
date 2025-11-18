package com.mpieterse.stride.data.store

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.mpieterse.stride.core.utils.Clogger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import java.io.File

data class StoredCredentials(val email: String, val password: String)

@Singleton
class CredentialsStore @Inject constructor(
    @ApplicationContext context: Context
) {
    companion object {
        private const val TAG = "CredentialsStore"
        private const val PREFS_FILE = "stride.credentials"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
    }

    private val prefs: SharedPreferences = createEncryptedPrefs(context)

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

    private fun createEncryptedPrefs(context: Context): SharedPreferences {
        return runCatching { buildEncryptedPrefs(context) }
            .getOrElse { firstError ->
                Clogger.e(TAG, "Encrypted prefs corrupted. Clearing and retrying.", firstError)
                clearCorruptedPrefs(context)
                runCatching { buildEncryptedPrefs(context) }
                    .getOrElse { secondError ->
                        Clogger.e(TAG, "Failed to reinitialize encrypted prefs after clearing.", secondError)
                        throw secondError
                    }
            }
    }

    private fun buildEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun clearCorruptedPrefs(context: Context) {
        context.deleteSharedPreferences(PREFS_FILE)
        val sharedPrefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
        if (sharedPrefsDir.exists()) {
            sharedPrefsDir.listFiles { _, name -> name.startsWith(PREFS_FILE) }?.forEach { file ->
                runCatching { file.delete() }
                    .onFailure { error ->
                        Clogger.w(TAG, "Failed to delete ${file.name}: ${error.message.orEmpty()}")
                    }
            }
        }
    }
}

