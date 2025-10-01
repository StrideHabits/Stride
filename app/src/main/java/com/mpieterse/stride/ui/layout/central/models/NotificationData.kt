package com.mpieterse.stride.ui.layout.central.models

import java.time.LocalTime

/**
 * Data class representing a habit notification/reminder
 */
data class NotificationData(
    val id: String,
    val habitName: String,
    val time: LocalTime,
    val daysOfWeek: List<Int>, // 1-7 (Monday-Sunday)
    val isEnabled: Boolean = true,
    val message: String = "",
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
) {
    companion object {
        /**
         * Get day names for display
         */
        fun getDayName(dayNumber: Int): String {
            return when (dayNumber) {
                1 -> "Monday"
                2 -> "Tuesday"
                3 -> "Wednesday"
                4 -> "Thursday"
                5 -> "Friday"
                6 -> "Saturday"
                7 -> "Sunday"
                else -> "Unknown"
            }
        }
        
        /**
         * Get short day names for compact display
         */
        fun getShortDayName(dayNumber: Int): String {
            return when (dayNumber) {
                1 -> "Mon"
                2 -> "Tue"
                3 -> "Wed"
                4 -> "Thu"
                5 -> "Fri"
                6 -> "Sat"
                7 -> "Sun"
                else -> "?"
            }
        }
    }
}

/**
 * Notification frequency options
 */
enum class NotificationFrequency(val displayName: String, val days: List<Int>) {
    DAILY("Daily", listOf(1, 2, 3, 4, 5, 6, 7)),
    WEEKDAYS("Weekdays", listOf(1, 2, 3, 4, 5)),
    WEEKENDS("Weekends", listOf(6, 7)),
    CUSTOM("Custom", emptyList())
}

/**
 * Notification settings for the app
 */
data class NotificationSettings(
    val globalNotificationsEnabled: Boolean = true,
    val defaultSoundEnabled: Boolean = true,
    val defaultVibrationEnabled: Boolean = true,
    val quietHoursStart: LocalTime? = null,
    val quietHoursEnd: LocalTime? = null,
    val quietHoursEnabled: Boolean = false
)
