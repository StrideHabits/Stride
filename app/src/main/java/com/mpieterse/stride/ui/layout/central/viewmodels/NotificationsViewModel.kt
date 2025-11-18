package com.mpieterse.stride.ui.layout.central.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.mpieterse.stride.R
import com.mpieterse.stride.data.dto.habits.HabitDto
import com.mpieterse.stride.data.local.NotificationsStore
import com.mpieterse.stride.data.repo.HabitRepository
import com.mpieterse.stride.ui.layout.central.models.NotificationData
import com.mpieterse.stride.ui.layout.central.models.NotificationSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalTime

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationsStore: NotificationsStore,
    private val habitRepository: HabitRepository,
    private val notificationScheduler: com.mpieterse.stride.core.services.NotificationSchedulerService,
    private val eventBus: com.mpieterse.stride.core.services.AppEventBus,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    data class UiState(
        val loading: Boolean = true,
        val notifications: List<NotificationData> = emptyList(),
        val habits: List<HabitDto> = emptyList(),      // built from Room entities
        val settings: NotificationSettings = NotificationSettings(),
        val error: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    init { 
        loadData()
        
        // Listen for habit deletion events
        viewModelScope.launch {
            eventBus.events.collect { event ->
                when (event) {
                    is com.mpieterse.stride.core.services.AppEventBus.AppEvent.HabitDeleted -> {
                        cleanupNotificationsForHabit(event.habitId)
                    }
                    else -> { /* ignore other events */ }
                }
            }
        }
    }

    private fun loadData() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)

        // Observe habits from Room and map to DTO shape the UI already expects.
        val habitsFlow = habitRepository.observeAll().map { entities ->
            entities.map {
                HabitDto(
                    id = it.id,
                    name = it.name,
                    frequency = it.frequency,
                    tag = it.tag,
                    imageUrl = it.imageUrl,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            }
        }

        var isFirstEmission = true
        
        combine(
            notificationsStore.notificationsFlow,
            notificationsStore.settingsFlow,
            habitsFlow
        ) { notifications, settings, habits ->
            val habitIds = habits.map { it.id }.toSet()
            val habitNames = habits.map { it.name }.toSet()
            
            // Filter out orphaned notifications
            val validNotifications = notifications.filter { notification ->
                when {
                    // If habitId exists, check by ID
                    !notification.habitId.isNullOrBlank() -> notification.habitId in habitIds
                    // Otherwise, check by name (backward compatibility)
                    else -> notification.habitName in habitNames
                }
            }
            
            // Auto-cleanup if any orphaned notifications found
            if (validNotifications.size < notifications.size) {
                val orphaned = notifications.filter { it !in validNotifications }
                viewModelScope.launch {
                    // Save cleaned list
                    notificationsStore.saveNotifications(validNotifications)
                    // Cancel scheduled notifications for orphaned ones
                    orphaned.forEach { notificationScheduler.cancelNotification(it.id) }
                }
            }
            
            // Reschedule all notifications on first load
            if (isFirstEmission) {
                isFirstEmission = false
                viewModelScope.launch {
                    notificationScheduler.rescheduleAllNotifications(validNotifications, settings)
                }
            }
            
            UiState(
                loading = false,
                notifications = validNotifications,
                habits = habits,
                settings = settings,
                error = null
            )
        }.collect { ui -> _state.value = ui }
    }

    fun addNotification(notification: NotificationData) = viewModelScope.launch {
        try {
            Log.d("NotificationsViewModel", "Adding notification: ${notification.habitName} at ${notification.time}")
            val updated = _state.value.notifications.toMutableList().apply { add(notification) }
            notificationsStore.saveNotifications(updated)
            
            // Schedule the notification
            notificationScheduler.scheduleNotification(notification, _state.value.settings)
            
            _state.value = _state.value.copy(notifications = updated)
        } catch (e: Exception) {
            Log.e("NotificationsViewModel", "Failed to add notification", e)
            _state.value = _state.value.copy(error = appContext.getString(R.string.error_notification_add_failed, e.message ?: appContext.getString(R.string.error_unknown)))
        }
    }

    fun updateNotification(updatedNotification: NotificationData) = viewModelScope.launch {
        try {
            // Cancel old notification
            notificationScheduler.cancelNotification(updatedNotification.id)
            
            val list = _state.value.notifications.toMutableList()
            val idx = list.indexOfFirst { it.id == updatedNotification.id }
            if (idx != -1) {
                list[idx] = updatedNotification
                notificationsStore.saveNotifications(list)
                
                // Schedule updated notification
                notificationScheduler.scheduleNotification(updatedNotification, _state.value.settings)
                
                _state.value = _state.value.copy(notifications = list)
            }
        } catch (e: Exception) {
            Log.e("NotificationsViewModel", "Failed to update notification", e)
            _state.value = _state.value.copy(error = appContext.getString(R.string.error_notification_update_failed, e.message ?: appContext.getString(R.string.error_unknown)))
        }
    }

    fun deleteNotification(notificationId: String) = viewModelScope.launch {
        try {
            // Cancel scheduled notification
            notificationScheduler.cancelNotification(notificationId)
            
            val updated = _state.value.notifications.filter { it.id != notificationId }
            notificationsStore.saveNotifications(updated)
            _state.value = _state.value.copy(notifications = updated)
        } catch (e: Exception) {
            Log.e("NotificationsViewModel", "Failed to delete notification", e)
            _state.value = _state.value.copy(error = appContext.getString(R.string.error_notification_delete_failed, e.message ?: appContext.getString(R.string.error_unknown)))
        }
    }

    fun toggleNotificationEnabled(notificationId: String, enabled: Boolean) = viewModelScope.launch {
        try {
            val list = _state.value.notifications.toMutableList()
            val idx = list.indexOfFirst { it.id == notificationId }
            if (idx != -1) {
                val updatedNotification = list[idx].copy(isEnabled = enabled)
                list[idx] = updatedNotification
                notificationsStore.saveNotifications(list)
                
                // Reschedule notification based on new enabled state
                if (enabled) {
                    notificationScheduler.scheduleNotification(updatedNotification, _state.value.settings)
                } else {
                    notificationScheduler.cancelNotification(notificationId)
                }
                
                _state.value = _state.value.copy(notifications = list)
            }
        } catch (e: Exception) {
            Log.e("NotificationsViewModel", "Failed to toggle notification", e)
            _state.value = _state.value.copy(error = appContext.getString(R.string.error_notification_toggle_failed, e.message ?: appContext.getString(R.string.error_unknown)))
        }
    }

    fun updateSettings(newSettings: NotificationSettings) = viewModelScope.launch {
        try {
            notificationsStore.saveSettings(newSettings)
            
            // Reschedule all notifications with new settings
            notificationScheduler.rescheduleAllNotifications(
                _state.value.notifications,
                newSettings
            )
            
            _state.value = _state.value.copy(settings = newSettings)
        } catch (e: Exception) {
            Log.e("NotificationsViewModel", "Failed to update settings", e)
            _state.value = _state.value.copy(error = appContext.getString(R.string.error_settings_update_failed, e.message ?: appContext.getString(R.string.error_unknown)))
        }
    }
    
    private fun cleanupNotificationsForHabit(habitId: String) = viewModelScope.launch {
        try {
            val current = _state.value.notifications
            val habit = _state.value.habits.firstOrNull { it.id == habitId }
            
            val toRemove = current.filter { 
                it.habitId == habitId || 
                (it.habitId.isNullOrBlank() && it.habitName == habit?.name)
            }
            
            if (toRemove.isNotEmpty()) {
                val updated = current.filter { it !in toRemove }
                notificationsStore.saveNotifications(updated)
                
                // Cancel scheduled notifications
                toRemove.forEach { notificationScheduler.cancelNotification(it.id) }
                
                _state.value = _state.value.copy(notifications = updated)
                Log.d("NotificationsViewModel", "Cleaned up ${toRemove.size} notification(s) for deleted habit: $habitId")
            }
        } catch (e: Exception) {
            Log.e("NotificationsViewModel", "Failed to cleanup notifications for habit", e)
        }
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }

    fun refresh() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true)
        // Flows are already hot; just flip the flag. loadData() sets up collectors once.
        _state.value = _state.value.copy(loading = false)
    }
    
    fun refreshHabits() = viewModelScope.launch {
        // This will trigger the habitsFlow to refresh
        // The combine flow will automatically update
    }
}
