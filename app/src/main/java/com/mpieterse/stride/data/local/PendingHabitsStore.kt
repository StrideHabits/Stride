package com.mpieterse.stride.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.pendingHabitsDataStore by preferencesDataStore("pending_habits")

data class PendingHabit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val frequency: Int = 0,
    val tag: String? = null,
    val imageUrl: String? = null,
    val imageDataBase64: String? = null,
    val imageMimeType: String? = null,
    val imageFileName: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Singleton
class PendingHabitsStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val KEY = stringPreferencesKey("pending_habits")
        private val gson = Gson()
    }

    val pendingHabitsFlow: Flow<List<PendingHabit>> =
        context.pendingHabitsDataStore.data.map { prefs ->
            val json = prefs[KEY] ?: "[]"
            parse(json)
        }

    suspend fun getAll(): List<PendingHabit> = pendingHabitsFlow.first()

    suspend fun add(habit: PendingHabit) {
        context.pendingHabitsDataStore.edit { prefs ->
            val current = parse(prefs[KEY] ?: "[]")
            val updated = (current + habit)
            prefs[KEY] = toJson(updated)
        }
    }

    suspend fun remove(id: String) {
        context.pendingHabitsDataStore.edit { prefs ->
            val current = parse(prefs[KEY] ?: "[]")
            val updated = current.filterNot { it.id == id }
            prefs[KEY] = toJson(updated)
        }
    }

    suspend fun replaceAll(habits: List<PendingHabit>) {
        context.pendingHabitsDataStore.edit { prefs ->
            prefs[KEY] = toJson(habits)
        }
    }

    private fun parse(json: String): List<PendingHabit> = try {
        if (json.isBlank() || json == "[]") emptyList()
        else gson.fromJson(json, object : TypeToken<List<PendingHabit>>() {}.type)
    } catch (_: Exception) {
        emptyList()
    }

    private fun toJson(habits: List<PendingHabit>): String = try {
        gson.toJson(habits)
    } catch (_: Exception) {
        "[]"
    }
}

