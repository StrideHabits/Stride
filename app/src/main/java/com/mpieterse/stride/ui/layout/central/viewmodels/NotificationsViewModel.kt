package com.mpieterse.stride.ui.layout.central.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpieterse.stride.core.net.ApiResult
import com.mpieterse.stride.core.services.NotificationSchedulerService
import com.mpieterse.stride.data.dto.habits.HabitDto
import com.mpieterse.stride.data.local.NotificationsStore
import com.mpieterse.stride.data.repo.HabitRepository
import com.mpieterse.stride.ui.layout.central.models.NotificationData
import com.mpieterse.stride.ui.layout.central.models.NotificationSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationsStore: NotificationsStore,
    private val habitRepository: HabitRepository,
    private val notificationScheduler: NotificationSchedulerService
) : ViewModel() {

    data class UiState(
        val loading: Boolean = true,
        val notifications: List<NotificationData> = emptyList(),
        val habits: List<HabitDto> = emptyList(),
        val settings: NotificationSettings = NotificationSettings(),
        val error: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                // Load habits from API
                loadHabits()
                
                // Collect combined flow and update state
                var isFirstEmission = true
                combine(
                    notificationsStore.notificationsFlow,
                    notificationsStore.settingsFlow
                ) { notifications, settings ->
                    val newState = _state.value.copy(
                        loading = false,
                        notifications = notifications,
                        settings = settings,
                        error = null
                    )
                    _state.value = newState
                    
                    // Only reschedule on first load, not on every emission (to avoid infinite loops)
                    // Individual operations (add/update/delete) will handle scheduling themselves
                    if (isFirstEmission) {
                        isFirstEmission = false
                        notificationScheduler.rescheduleAllNotifications(notifications, settings)
                    }
                }.collect { }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    error = "Failed to load notifications: ${e.message}"
                )
            }
        }
    }
    
    private suspend fun loadHabits() {
        try {
            val habitsResult = habitRepository.list()
            val habits = if (habitsResult is ApiResult.Ok<*>) {
                (habitsResult.data as List<*>).filterIsInstance<HabitDto>()
            } else {
                emptyList()
            }
            _state.value = _state.value.copy(habits = habits)
        } catch (e: Exception) {
            Log.e("NotificationsViewModel", "Failed to load habits", e)
            // Don't update error state here, just log it
        }
    }
    
    /**
     * Refresh habits from the API.
     * Useful when habits might have been added/updated and we need the latest list.
     */
    fun refreshHabits() = viewModelScope.launch {
        loadHabits()
    }

    fun addNotification(notification: NotificationData) = viewModelScope.launch { //This method adds a new notification using ViewModel lifecycle management (Android Developers, 2024).
        try {
            Log.d("NotificationsViewModel", "Adding notification: ${notification.habitName} at ${notification.time}")
            val currentNotifications = _state.value.notifications.toMutableList()
            currentNotifications.add(notification)
            notificationsStore.saveNotifications(currentNotifications)
            Log.d("NotificationsViewModel", "Notification saved successfully. Total count: ${currentNotifications.size}")
            
            // Schedule the notification
            notificationScheduler.scheduleNotification(notification, _state.value.settings)
            
            // Update the state immediately to reflect the change
            _state.value = _state.value.copy(notifications = currentNotifications)
        } catch (e: Exception) {
            Log.e("NotificationsViewModel", "Failed to add notification", e)
            _state.value = _state.value.copy(error = "Failed to add notification: ${e.message}")
        }
    }

    fun updateNotification(updatedNotification: NotificationData) = viewModelScope.launch { //This method updates an existing notification using ViewModel lifecycle management (Android Developers, 2024).
        try {
            val currentNotifications = _state.value.notifications.toMutableList()
            val index = currentNotifications.indexOfFirst { it.id == updatedNotification.id }
            if (index != -1) {
                // Cancel old notification
                notificationScheduler.cancelNotification(updatedNotification.id)
                
                currentNotifications[index] = updatedNotification
                notificationsStore.saveNotifications(currentNotifications)
                
                // Schedule updated notification
                notificationScheduler.scheduleNotification(updatedNotification, _state.value.settings)
                
                _state.value = _state.value.copy(notifications = currentNotifications)
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = "Failed to update notification: ${e.message}")
        }
    }

    fun deleteNotification(notificationId: String) = viewModelScope.launch { //This method deletes a notification using ViewModel lifecycle management (Android Developers, 2024).
        try {
            // Cancel scheduled notification
            notificationScheduler.cancelNotification(notificationId)
            
            val currentNotifications = _state.value.notifications.filter { it.id != notificationId }
            notificationsStore.saveNotifications(currentNotifications)
            _state.value = _state.value.copy(notifications = currentNotifications)
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = "Failed to delete notification: ${e.message}")
        }
    }

    fun toggleNotificationEnabled(notificationId: String, enabled: Boolean) = viewModelScope.launch {
        try {
            val currentNotifications = _state.value.notifications.toMutableList()
            val index = currentNotifications.indexOfFirst { it.id == notificationId }
            if (index != -1) {
                val updatedNotification = currentNotifications[index].copy(isEnabled = enabled)
                currentNotifications[index] = updatedNotification
                notificationsStore.saveNotifications(currentNotifications)
                
                // Reschedule notification based on new enabled state
                if (enabled) {
                    notificationScheduler.scheduleNotification(updatedNotification, _state.value.settings)
                } else {
                    notificationScheduler.cancelNotification(notificationId)
                }
                
                _state.value = _state.value.copy(notifications = currentNotifications)
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = "Failed to toggle notification: ${e.message}")
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
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = "Failed to update settings: ${e.message}")
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun refresh() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true)
        loadData()
    }
}
