package com.mpieterse.stride.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mpieterse.stride.ui.layout.central.models.NotificationData
import com.mpieterse.stride.ui.layout.central.models.NotificationSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalTime

private val Context.notificationsDataStore by preferencesDataStore("notifications")

// Helper data classes for JSON serialization
private data class NotificationDataJson(
    val id: String,
    val habitId: String? = null,
    val habitName: String,
    val time: String, // HH:mm format
    val daysOfWeek: List<Int>,
    val isEnabled: Boolean = true,
    val message: String = "",
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
)

private data class NotificationSettingsJson(
    val globalNotificationsEnabled: Boolean = true,
    val defaultSoundEnabled: Boolean = true,
    val defaultVibrationEnabled: Boolean = true,
    val quietHoursStart: String? = null, // HH:mm format
    val quietHoursEnd: String? = null, // HH:mm format
    val quietHoursEnabled: Boolean = false
)

class NotificationsStore(private val context: Context) {
    
    companion object {
        private val NOTIFICATIONS_KEY = stringPreferencesKey("notifications_list")
        private val SETTINGS_KEY = stringPreferencesKey("notification_settings")
        private val gson = Gson()
    }
    
    val notificationsFlow: Flow<List<NotificationData>> = context.notificationsDataStore.data.map { prefs ->
        val json = prefs[NOTIFICATIONS_KEY] ?: "[]"
        parseNotificationsJson(json)
    }
    
    val settingsFlow: Flow<NotificationSettings> = context.notificationsDataStore.data.map { prefs ->
        val json = prefs[SETTINGS_KEY] ?: "{}"
        parseSettingsJson(json)
    }
    
    suspend fun saveNotifications(notifications: List<NotificationData>) { //This method saves notification data using Android DataStore (Android Developers, 2024).
        context.notificationsDataStore.edit { prefs ->
            prefs[NOTIFICATIONS_KEY] = notificationsToJson(notifications)
        }
    }
    
    suspend fun saveSettings(settings: NotificationSettings) { //This method saves notification settings using Android DataStore (Android Developers, 2024).
        context.notificationsDataStore.edit { prefs ->
            prefs[SETTINGS_KEY] = settingsToJson(settings)
        }
    }
    
    private fun parseNotificationsJson(json: String): List<NotificationData> {
        return try {
            if (json == "[]" || json.isBlank()) return emptyList()
            
            val listType = object : TypeToken<List<NotificationDataJson>>() {}.type
            val jsonList: List<NotificationDataJson> = gson.fromJson(json, listType)
            
            jsonList.map { jsonItem ->
                NotificationData(
                    id = jsonItem.id,
                    habitId = jsonItem.habitId,
                    habitName = jsonItem.habitName,
                    time = LocalTime.parse(jsonItem.time),
                    daysOfWeek = jsonItem.daysOfWeek,
                    isEnabled = jsonItem.isEnabled,
                    message = jsonItem.message,
                    soundEnabled = jsonItem.soundEnabled,
                    vibrationEnabled = jsonItem.vibrationEnabled
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun parseSettingsJson(json: String): NotificationSettings {
        return try {
            if (json == "{}" || json.isBlank()) {
                return NotificationSettings()
            }
            
            val jsonSettings: NotificationSettingsJson = gson.fromJson(json, NotificationSettingsJson::class.java)
            NotificationSettings(
                globalNotificationsEnabled = jsonSettings.globalNotificationsEnabled,
                defaultSoundEnabled = jsonSettings.defaultSoundEnabled,
                defaultVibrationEnabled = jsonSettings.defaultVibrationEnabled,
                quietHoursStart = jsonSettings.quietHoursStart?.let { LocalTime.parse(it) },
                quietHoursEnd = jsonSettings.quietHoursEnd?.let { LocalTime.parse(it) },
                quietHoursEnabled = jsonSettings.quietHoursEnabled
            )
        } catch (e: Exception) {
            NotificationSettings()
        }
    }
    
    private fun notificationsToJson(notifications: List<NotificationData>): String {
        return try {
            val jsonList = notifications.map { notification ->
                NotificationDataJson(
                    id = notification.id,
                    habitId = notification.habitId,
                    habitName = notification.habitName,
                    time = notification.time.toString(), // LocalTime.toString() gives HH:mm format
                    daysOfWeek = notification.daysOfWeek,
                    isEnabled = notification.isEnabled,
                    message = notification.message,
                    soundEnabled = notification.soundEnabled,
                    vibrationEnabled = notification.vibrationEnabled
                )
            }
            gson.toJson(jsonList)
        } catch (e: Exception) {
            "[]"
        }
    }
    
    private fun settingsToJson(settings: NotificationSettings): String {
        return try {
            val jsonSettings = NotificationSettingsJson(
                globalNotificationsEnabled = settings.globalNotificationsEnabled,
                defaultSoundEnabled = settings.defaultSoundEnabled,
                defaultVibrationEnabled = settings.defaultVibrationEnabled,
                quietHoursStart = settings.quietHoursStart?.toString(),
                quietHoursEnd = settings.quietHoursEnd?.toString(),
                quietHoursEnabled = settings.quietHoursEnabled
            )
            gson.toJson(jsonSettings)
        } catch (e: Exception) {
            "{}"
        }
    }
    
    suspend fun getAllNotifications(): List<NotificationData> {
        val json = context.notificationsDataStore.data.first()[NOTIFICATIONS_KEY] ?: "[]"
        return parseNotificationsJson(json)
    }
    
    suspend fun getNotificationById(id: String): NotificationData? {
        val notifications = getAllNotifications()
        return notifications.firstOrNull { it.id == id }
    }
}
