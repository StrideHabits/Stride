package com.mpieterse.stride.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mpieterse.stride.data.dto.checkins.CheckInDto
import com.mpieterse.stride.data.dto.habits.HabitDto
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.habitCacheDataStore by preferencesDataStore("habit_cache")

data class HabitImageOverride(
    val base64: String,
    val mimeType: String? = null,
    val fileName: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

data class HabitCacheEntry(
    val habits: List<HabitDto> = emptyList(),
    val checkins: List<CheckInDto> = emptyList(),
    val overrides: Map<String, HabitImageOverride> = emptyMap(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class HabitCachePayload(
    val users: Map<String, HabitCacheEntry> = emptyMap()
)

@Singleton
class HabitCacheStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val KEY = stringPreferencesKey("habit_cache_payload")
        private val gson = Gson()
    }

    private val payloadFlow: Flow<HabitCachePayload> =
        context.habitCacheDataStore.data.map { prefs ->
            parse(prefs[KEY])
        }

    suspend fun snapshot(): HabitCachePayload = payloadFlow.first()

    suspend fun getEntryForUser(userId: String): HabitCacheEntry? =
        snapshot().users[userId]

    suspend fun getMostRecentEntry(): Pair<String, HabitCacheEntry>? {
        val users = snapshot().users
        val maxEntry = users.maxByOrNull { it.value.updatedAt }
        return maxEntry?.let { it.key to it.value }
    }

    fun habitsFlow(userId: String): Flow<List<HabitDto>> =
        payloadFlow.map { payload ->
            payload.users[userId]?.habits.orEmpty()
        }

    suspend fun update(userId: String, habits: List<HabitDto>, checkins: List<CheckInDto>) {
        context.habitCacheDataStore.edit { prefs ->
            val current = parse(prefs[KEY])
            val updatedUsers = current.users.toMutableMap()
            val currentEntry = updatedUsers[userId]
            updatedUsers[userId] = HabitCacheEntry(
                habits = habits,
                checkins = checkins,
                overrides = currentEntry?.overrides.orEmpty(),
                updatedAt = System.currentTimeMillis()
            )
            prefs[KEY] = toJson(HabitCachePayload(users = updatedUsers))
        }
    }

    suspend fun upsertImageOverride(
        userId: String,
        habitId: String,
        override: HabitImageOverride?
    ) {
        context.habitCacheDataStore.edit { prefs ->
            val current = parse(prefs[KEY])
            val updatedUsers = current.users.toMutableMap()
            val existingEntry = updatedUsers[userId] ?: HabitCacheEntry()
            val currentOverrides = existingEntry.overrides.toMutableMap()
            if (override == null) {
                currentOverrides.remove(habitId)
            } else {
                currentOverrides[habitId] = override.copy(updatedAt = System.currentTimeMillis())
            }
            updatedUsers[userId] = existingEntry.copy(
                overrides = currentOverrides,
                updatedAt = System.currentTimeMillis()
            )
            prefs[KEY] = toJson(HabitCachePayload(users = updatedUsers))
        }
    }

    suspend fun getImageOverride(userId: String, habitId: String): HabitImageOverride? =
        getEntryForUser(userId)?.overrides?.get(habitId)

    suspend fun updateHabitMetadata(
        userId: String,
        habitId: String,
        transform: (HabitDto) -> HabitDto
    ) {
        context.habitCacheDataStore.edit { prefs ->
            val current = parse(prefs[KEY])
            val updatedUsers = current.users.toMutableMap()
            val entry = updatedUsers[userId] ?: HabitCacheEntry()
            val updatedHabits = entry.habits.map { habit ->
                if (habit.id == habitId) {
                    transform(habit)
                } else {
                    habit
                }
            }
            updatedUsers[userId] = entry.copy(
                habits = updatedHabits,
                updatedAt = System.currentTimeMillis()
            )
            prefs[KEY] = toJson(HabitCachePayload(users = updatedUsers))
        }
    }

    suspend fun clear() {
        context.habitCacheDataStore.edit { prefs ->
            prefs.remove(KEY)
        }
    }

    private fun parse(json: String?): HabitCachePayload =
        if (json.isNullOrBlank()) {
            HabitCachePayload()
        } else {
            runCatching {
                gson.fromJson<HabitCachePayload>(
                    json,
                    object : TypeToken<HabitCachePayload>() {}.type
                )
            }.getOrDefault(HabitCachePayload())
        }

    private fun toJson(payload: HabitCachePayload): String =
        runCatching { gson.toJson(payload) }.getOrDefault("")
}

