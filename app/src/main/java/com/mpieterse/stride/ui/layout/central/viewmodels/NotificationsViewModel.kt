package com.mpieterse.stride.ui.layout.central.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpieterse.stride.data.local.NotificationsStore
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
    private val notificationsStore: NotificationsStore
) : ViewModel() {

    data class UiState(
        val loading: Boolean = true,
        val notifications: List<NotificationData> = emptyList(),
        val settings: NotificationSettings = NotificationSettings(),
        val error: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    init {
        loadData()
    }

    private fun loadData() = viewModelScope.launch {
        try {
            combine(
                notificationsStore.notificationsFlow,
                notificationsStore.settingsFlow
            ) { notifications, settings ->
                _state.value = _state.value.copy(
                    loading = false,
                    notifications = notifications,
                    settings = settings,
                    error = null
                )
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                loading = false,
                error = "Failed to load notifications: ${e.message}"
            )
        }
    }

    fun addNotification(notification: NotificationData) = viewModelScope.launch {
        try {
            val currentNotifications = _state.value.notifications.toMutableList()
            currentNotifications.add(notification)
            notificationsStore.saveNotifications(currentNotifications)
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = "Failed to add notification: ${e.message}")
        }
    }

    fun updateNotification(updatedNotification: NotificationData) = viewModelScope.launch {
        try {
            val currentNotifications = _state.value.notifications.toMutableList()
            val index = currentNotifications.indexOfFirst { it.id == updatedNotification.id }
            if (index != -1) {
                currentNotifications[index] = updatedNotification
                notificationsStore.saveNotifications(currentNotifications)
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = "Failed to update notification: ${e.message}")
        }
    }

    fun deleteNotification(notificationId: String) = viewModelScope.launch {
        try {
            val currentNotifications = _state.value.notifications.filter { it.id != notificationId }
            notificationsStore.saveNotifications(currentNotifications)
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = "Failed to delete notification: ${e.message}")
        }
    }

    fun toggleNotificationEnabled(notificationId: String, enabled: Boolean) = viewModelScope.launch {
        try {
            val currentNotifications = _state.value.notifications.toMutableList()
            val index = currentNotifications.indexOfFirst { it.id == notificationId }
            if (index != -1) {
                currentNotifications[index] = currentNotifications[index].copy(isEnabled = enabled)
                notificationsStore.saveNotifications(currentNotifications)
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = "Failed to toggle notification: ${e.message}")
        }
    }

    fun updateSettings(newSettings: NotificationSettings) = viewModelScope.launch {
        try {
            notificationsStore.saveSettings(newSettings)
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = "Failed to update settings: ${e.message}")
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
