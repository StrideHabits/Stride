package com.mpieterse.stride.ui.layout.central.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpieterse.stride.data.dto.habits.HabitDto
import com.mpieterse.stride.data.local.NotificationsStore
import com.mpieterse.stride.data.repo.HabitRepository
import com.mpieterse.stride.ui.layout.central.models.NotificationData
import com.mpieterse.stride.ui.layout.central.models.NotificationSettings
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val notificationScheduler: NotificationSchedulerService
) : ViewModel() {
    
    private var loadJob: Job? = null

    data class UiState(
        val loading: Boolean = true,
        val notifications: List<NotificationData> = emptyList(),
        val habits: List<HabitDto> = emptyList(),      // built from Room entities
        val settings: NotificationSettings = NotificationSettings(),
        val error: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    init { loadData() }

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

        combine(
            notificationsStore.notificationsFlow,
            notificationsStore.settingsFlow,
            habitsFlow
        ) { notifications, settings, habits ->
            UiState(
                loading = false,
                notifications = notifications,
                habits = habits,
                settings = settings,
                error = null
            )
        }.collect { ui -> _state.value = ui }
    }
    
    /**
     * Refresh habits from the API.
     * Useful when habits might have been added/updated and we need the latest list.
     */
    fun refreshHabits() = viewModelScope.launch {
        loadHabits()
    }

    fun addNotification(notification: NotificationData) = viewModelScope.launch {
        try {
            Log.d("NotificationsViewModel", "Adding notification: ${notification.habitName} at ${notification.time}")
            val updated = _state.value.notifications.toMutableList().apply { add(notification) }
            notificationsStore.saveNotifications(updated)
            _state.value = _state.value.copy(notifications = updated)
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = "Failed to add notification: ${e.message}")
        }
    }

    fun updateNotification(updatedNotification: NotificationData) = viewModelScope.launch {
        try {
            val list = _state.value.notifications.toMutableList()
            val idx = list.indexOfFirst { it.id == updatedNotification.id }
            if (idx != -1) {
                list[idx] = updatedNotification
                notificationsStore.saveNotifications(list)
                _state.value = _state.value.copy(notifications = list)
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = "Failed to update notification: ${e.message}")
        }
    }

    fun deleteNotification(notificationId: String) = viewModelScope.launch {
        try {
            val updated = _state.value.notifications.filter { it.id != notificationId }
            notificationsStore.saveNotifications(updated)
            _state.value = _state.value.copy(notifications = updated)
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = "Failed to delete notification: ${e.message}")
        }
    }

    fun toggleNotificationEnabled(notificationId: String, enabled: Boolean) = viewModelScope.launch {
        try {
            val list = _state.value.notifications.toMutableList()
            val idx = list.indexOfFirst { it.id == notificationId }
            if (idx != -1) {
                list[idx] = list[idx].copy(isEnabled = enabled)
                notificationsStore.saveNotifications(list)
                _state.value = _state.value.copy(notifications = list)
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = "Failed to toggle notification: ${e.message}")
        }
    }

    fun updateSettings(newSettings: NotificationSettings) = viewModelScope.launch {
        try {
            notificationsStore.saveSettings(newSettings)
            _state.value = _state.value.copy(settings = newSettings)
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = "Failed to update settings: ${e.message}")
        }
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }

    fun refresh() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true)
        // Flows are already hot; just flip the flag. loadData() sets up collectors once.
        _state.value = _state.value.copy(loading = false)
    }
}
