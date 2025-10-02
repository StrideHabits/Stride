package com.mpieterse.stride.core.services

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.Preferences.Key
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mpieterse.stride.core.models.configuration.ConfigurationKey
import com.mpieterse.stride.core.utils.Clogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import java.util.UUID
import javax.inject.Inject

/**
 * Service to interact with the application DataStore.
 */
class ConfigurationsService
@Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "ConfigurationsService"
        private const val KEY = "ConfigurationsOptions"
        private const val TIMEOUT_MILLISECONDS = 5_000L

        private val Context.dataStore by preferencesDataStore(
            name = KEY,
            // Triage instructions for corruption
            corruptionHandler = ReplaceFileCorruptionHandler { exception ->
                Clogger.e(TAG, "DataStore is corrupt and must be rebuilt", exception)
                emptyPreferences()
            } // Clear all options
        )
    }


// --- Fields


    private val dataStore: DataStore<Preferences> = context.dataStore


// --- Functions


    /**
     * @throws TimeoutCancellationException
     * @throws IOException
     * @throws Exception
     */
    suspend fun <T> put(
        key: Key<T>, value: T
    ) {
        val scopeId = newId()
        logTrace(scopeId, "PUT", "STARTED", "key=[${key.name}], value=[$value]")

        runCatching {
            retryAfterTimeout(scopeId, "PUT") {
                dataStore.edit { configuration ->
                    configuration[key] = value
                }
            }
        }.apply {
            onSuccess {
                logTrace(scopeId, "PUT", "SUCCESS")
            }

            onFailure { exception ->
                when (exception) {
                    is TimeoutCancellationException -> {
                        logTrace(scopeId, "PUT", "TIMEOUT", error = exception)
                    }

                    else -> {
                        logTrace(scopeId, "PUT", "FAILURE", error = exception)
                    }
                }

                throw exception
            }
        }
    }


    /**
     * @throws TimeoutCancellationException
     * @throws IOException
     * @throws Exception
     */
    suspend fun <T> get(
        key: Key<T>
    ): T? {
        val scopeId = newId()
        logTrace(scopeId, "GET", "STARTED", "key=[${key.name}]")

        return runCatching {
            retryAfterTimeout(scopeId, "GET") {
                dataStore.data.first()[key]
            }
        }.apply {
            onSuccess {
                logTrace(scopeId, "GET", "SUCCESS")
            }

            onFailure { exception ->
                when (exception) {
                    is TimeoutCancellationException -> {
                        logTrace(scopeId, "GET", "TIMEOUT", error = exception)
                    }

                    else -> {
                        logTrace(scopeId, "GET", "FAILURE", error = exception)
                    }
                }
            }
        }.getOrNull()
    }


    /**
     * Completely erases the configuration options.
     *
     * **Note:** This action is destructive and cannot be undone afterwards. The
     * configuration option schema will be destroyed and all data within it will
     * be permanently erased. Use with caution.
     *
     * @throws TimeoutCancellationException
     * @throws IOException
     * @throws Exception
     */
    suspend fun erase() {
        val scopeId = newId()
        logTrace(scopeId, "ERASE", "STARTED")

        runCatching {
            retryAfterTimeout(scopeId, "ERASE") {
                dataStore.edit { configuration ->
                    configuration.clear()
                }
            }
        }.apply {
            onSuccess {
                logTrace(scopeId, "ERASE", "SUCCESS")
            }

            onFailure { exception ->
                when (exception) {
                    is TimeoutCancellationException -> {
                        logTrace(scopeId, "ERASE", "TIMEOUT", error = exception)
                    }

                    else -> {
                        logTrace(scopeId, "ERASE", "FAILURE", error = exception)
                    }
                }
            }
        }
    }


    suspend fun <T> put(pref: ConfigurationKey<T>, value: T) {
        val encoded = pref.encode(value)
        put(pref.key, encoded)
    }

    
    suspend fun <T> get(pref: ConfigurationKey<T>): T {
        val raw = get(pref.key)
        return pref.decode(raw)
    }


// --- Internals


    private fun newId(): String = UUID.randomUUID().toString()


    private fun logTrace(
        id: String, caller: String, status: String, append: String = "", error: Throwable? = null
    ) {
        val message = buildString {
            append("{ event=[$caller], scope=[$id], status=[$status]")
            if (append.isNotEmpty()) append(", $append")
            append(" }")
        }

        when (error) {
            null -> Clogger.d(TAG, message)
            else -> Clogger.e(TAG, message, error)
        }
    }


    private suspend fun <T> retryAfterTimeout(
        id: String, caller: String, execute: suspend () -> T
    ): T {
        return runCatching {
            withTimeout(TIMEOUT_MILLISECONDS) {
                execute()
            }
        }.recoverCatching { exception ->
            if (exception is TimeoutCancellationException) {
                logTrace(id, caller, "RECOVER", error = exception)
                withTimeout(TIMEOUT_MILLISECONDS) {
                    execute()
                }
            } else {
                throw exception
            }
        }.getOrThrow()
    }
}