package com.mpieterse.stride.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.mpieterse.stride.ui.layout.central.models.NotificationData
import com.mpieterse.stride.ui.layout.central.models.NotificationSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime
import java.util.UUID

private val Context.notificationsDataStore by preferencesDataStore("notifications")

class NotificationsStore(private val context: Context) {
    
    companion object {
        private val NOTIFICATIONS_KEY = stringPreferencesKey("notifications_list")
        private val SETTINGS_KEY = stringPreferencesKey("notification_settings")
    }
    
    val notificationsFlow: Flow<List<NotificationData>> = context.notificationsDataStore.data.map { prefs ->
        val json = prefs[NOTIFICATIONS_KEY] ?: "[]"
        parseNotificationsJson(json)
    }
    
    val settingsFlow: Flow<NotificationSettings> = context.notificationsDataStore.data.map { prefs ->
        val json = prefs[SETTINGS_KEY] ?: "{}"
        parseSettingsJson(json)
    }
    
    suspend fun saveNotifications(notifications: List<NotificationData>) {
        context.notificationsDataStore.edit { prefs ->
            prefs[NOTIFICATIONS_KEY] = notificationsToJson(notifications)
        }
    }
    
    suspend fun saveSettings(settings: NotificationSettings) {
        context.notificationsDataStore.edit { prefs ->
            prefs[SETTINGS_KEY] = settingsToJson(settings)
        }
    }
    
    private fun parseNotificationsJson(json: String): List<NotificationData> {
        return try {
            // Simple JSON parsing - in a real app you'd use a proper JSON library
            if (json == "[]") return emptyList()
            
            // For now, return some default notifications
            // In a real implementation, you'd parse the JSON properly
            listOf(
                NotificationData(
                    id = "1",
                    habitName = "Go to the gym",
                    time = LocalTime.of(7, 0),
                    daysOfWeek = listOf(1, 3, 5),
                    isEnabled = true,
                    message = "Time for your workout! 💪",
                    soundEnabled = true,
                    vibrationEnabled = true
                ),
                NotificationData(
                    id = "2", 
                    habitName = "Read for 30 minutes",
                    time = LocalTime.of(21, 0),
                    daysOfWeek = listOf(1, 2, 3, 4, 5, 6, 7),
                    isEnabled = true,
                    message = "Reading time! 📚",
                    soundEnabled = true,
                    vibrationEnabled = false
                )
            )
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun parseSettingsJson(json: String): NotificationSettings {
        return try {
            if (json == "{}") {
                NotificationSettings(
                    globalNotificationsEnabled = true,
                    defaultSoundEnabled = true,
                    defaultVibrationEnabled = true,
                    quietHoursStart = LocalTime.of(22, 0),
                    quietHoursEnd = LocalTime.of(7, 0),
                    quietHoursEnabled = true
                )
            } else {
                // Parse JSON properly in real implementation
                NotificationSettings()
            }
        } catch (e: Exception) {
            NotificationSettings()
        }
    }
    
    private fun notificationsToJson(notifications: List<NotificationData>): String {
        // Simple JSON serialization - in a real app you'd use a proper JSON library
        return "[]" // For now, just return empty array
    }
    
    private fun settingsToJson(settings: NotificationSettings): String {
        // Simple JSON serialization - in a real app you'd use a proper JSON library  
        return "{}" // For now, just return empty object
    }
}
