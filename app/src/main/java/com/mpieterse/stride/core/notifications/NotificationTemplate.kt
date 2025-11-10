package com.mpieterse.stride.core.notifications

import com.mpieterse.stride.ui.layout.central.models.NotificationData

/**
 * Basic notification template system for habit reminders.
 * 
 * Templates define how notifications are displayed and can be customized
 * based on the notification data.
 */
data class NotificationTemplate(
    val id: String,
    val title: String,
    val body: String,
    val iconResId: Int? = null
) {
    companion object {
        /**
         * Creates a default habit reminder template.
         */
        fun createHabitReminderTemplate(notification: NotificationData): NotificationTemplate {
            val title = if (notification.message.isNotEmpty()) {
                notification.habitName
            } else {
                "Habit Reminder: ${notification.habitName}"
            }
            
            val body = if (notification.message.isNotEmpty()) {
                notification.message
            } else {
                "Time to work on ${notification.habitName}!"
            }
            
            return NotificationTemplate(
                id = notification.id,
                title = title,
                body = body
            )
        }
        
        /**
         * Creates a custom template with placeholders.
         */
        fun createCustomTemplate(
            notification: NotificationData,
            titleTemplate: String = "{habitName}",
            bodyTemplate: String = "{message}"
        ): NotificationTemplate {
            val title = titleTemplate
                .replace("{habitName}", notification.habitName)
                .replace("{time}", notification.time.toString())
            
            val body = bodyTemplate
                .replace("{message}", notification.message.ifEmpty { "Time to work on ${notification.habitName}!" })
                .replace("{habitName}", notification.habitName)
            
            return NotificationTemplate(
                id = notification.id,
                title = title,
                body = body
            )
        }
    }
}

/**
 * Notification template engine that applies templates to notification data.
 */
object NotificationTemplateEngine {
    /**
     * Applies a template to notification data and returns the formatted notification content.
     */
    fun applyTemplate(notification: NotificationData): NotificationTemplate {
        return NotificationTemplate.createHabitReminderTemplate(notification)
    }
    
    /**
     * Applies a custom template with specified title and body patterns.
     */
    fun applyCustomTemplate(
        notification: NotificationData,
        titlePattern: String? = null,
        bodyPattern: String? = null
    ): NotificationTemplate {
        return if (titlePattern != null || bodyPattern != null) {
            NotificationTemplate.createCustomTemplate(
                notification,
                titleTemplate = titlePattern ?: "{habitName}",
                bodyTemplate = bodyPattern ?: "{message}"
            )
        } else {
            applyTemplate(notification)
        }
    }
}

