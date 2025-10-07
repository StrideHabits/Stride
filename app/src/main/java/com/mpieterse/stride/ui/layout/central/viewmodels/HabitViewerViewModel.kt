// HabitViewerViewModel.kt
package com.mpieterse.stride.ui.layout.central.viewmodels

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpieterse.stride.core.net.ApiResult
import com.mpieterse.stride.data.dto.checkins.CheckInDto
import com.mpieterse.stride.data.dto.habits.HabitDto
import com.mpieterse.stride.data.repo.CheckInRepository
import com.mpieterse.stride.data.repo.HabitRepository
import com.mpieterse.stride.core.services.HabitNameOverrideService
import com.mpieterse.stride.core.services.AppEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.*

@HiltViewModel
class HabitViewerViewModel @Inject constructor(
    private val habits: HabitRepository,
    private val checkins: CheckInRepository,
    private val nameOverrideService: HabitNameOverrideService,
    private val eventBus: AppEventBus
) : ViewModel() {

    data class UiState(
        val loading: Boolean = true,
        val error: String? = null,
        val habitId: String = "",
        val habitName: String = "",
        val displayName: String = "",
        val habitImage: Bitmap? = null,
        val streakDays: Int = 0,
        val completedDates: List<Int> = emptyList(),
        val localNameOverride: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun load(habitId: String) = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)

        val habitName = when (val hr = habits.list()) {
            is ApiResult.Ok<*> -> (hr.data as List<*>)
                .filterIsInstance<HabitDto>()
                .firstOrNull { it.id == habitId }?.name ?: "(Unknown habit)"
            is ApiResult.Err -> {
                _state.value = _state.value.copy(
                    loading = false,
                    error = "Habit load failed: ${hr.code ?: ""} ${hr.message ?: ""}"
                )
                return@launch
            }
        }

        val mine: List<CheckInDto> = when (val cr = checkins.list()) {
            is ApiResult.Ok<*> -> (cr.data as List<*>)
                .filterIsInstance<CheckInDto>()
                .filter { it.habitId == habitId }
            is ApiResult.Err -> {
                _state.value = _state.value.copy(
                    loading = false,
                    habitName = habitName,
                    error = "Check-ins load failed: ${cr.code ?: ""} ${cr.message ?: ""}"
                )
                return@launch
            }
        }

        val today = LocalDate.now()
        val monthStart = today.withDayOfMonth(1)
        val completedThisMonth = mine.mapNotNull { checkin ->
            try {
                java.time.LocalDate.parse(checkin.dayKey)
            } catch (e: Exception) { null }
        }.filter { it.year == monthStart.year && it.month == monthStart.month }
            .map { it.dayOfMonth }
            .distinct()
            .sorted()
        
        

        val streak = computeStreakDays(mine.mapNotNull { checkin ->
            try {
                java.time.LocalDate.parse(checkin.dayKey)
            } catch (e: Exception) { null }
        }.distinct())

        _state.value = _state.value.copy(
            loading = false,
            habitId = habitId,
            habitName = habitName,
            displayName = nameOverrideService.getDisplayName(habitId, habitName),
            streakDays = streak,
            completedDates = completedThisMonth
        )
    }

    fun completeToday(habitId: String) = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)
        val today = LocalDate.now().toString()
        when (val r = checkins.create(habitId, today)) {
            is ApiResult.Ok<*> -> {
                // Re-load to refresh streak and calendar
                // This check-in is now saved to the API and will be visible in the home screen
                load(habitId)
                // Emit event to notify home screen
                eventBus.emit(AppEventBus.AppEvent.CheckInCompleted)
            }
            is ApiResult.Err -> {
                _state.value = _state.value.copy(
                    loading = false,
                    error = "Complete failed: ${r.code ?: ""} ${r.message ?: ""}"
                )
            }
        }
    }

    fun toggleCheckIn(habitId: String, isoDate: String) = viewModelScope.launch {
        Log.d("HabitViewerViewModel", "toggleCheckIn called: habitId=$habitId isoDate=$isoDate")
        _state.value = _state.value.copy(loading = true, error = null)
        
        try {
            // Create new check-in directly - let the API handle duplicates
            when (val r = checkins.create(habitId, isoDate)) {
                is ApiResult.Ok<*> -> {
                    // Successfully created check-in, refresh data and emit event
                    load(habitId)
                    eventBus.emit(AppEventBus.AppEvent.CheckInCompleted)
                }
                is ApiResult.Err -> {
                    val errorMessage = when {
                        r.code == 409 -> "Check-in already exists for this date"
                        r.code == 401 -> "Authentication required - please log in"
                        r.code == 403 -> "Access denied"
                        r.code == 404 -> "Habit not found"
                        else -> "Failed to create check-in: ${r.message ?: "Unknown error"}"
                    }
                    _state.value = _state.value.copy(
                        loading = false,
                        error = errorMessage
                    )
                    // Clear error after a short delay for user-friendly messages
                    if (r.code == 409) {
                        kotlinx.coroutines.delay(2000)
                        _state.value = _state.value.copy(error = null)
                    }
                }
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                loading = false,
                error = "Network error: ${e.message ?: "Please check your connection"}"
            )
        }
    }

    fun updateLocalName(newName: String, habitId: String) {
        // Store in shared service so home screen can access it
        nameOverrideService.updateHabitName(habitId, newName)
        // Update local state to trigger UI refresh
        _state.value = _state.value.copy(
            localNameOverride = newName,
            displayName = newName
        )
        // Emit event to notify home screen
        viewModelScope.launch {
            eventBus.emit(AppEventBus.AppEvent.HabitNameChanged)
        }
    }

    fun getDisplayName(): String {
        // Use the service to get the display name, which will be reactive to changes
        return nameOverrideService.getDisplayName(_state.value.habitId, _state.value.habitName)
    }

    // Count consecutive days ending today.
    private fun computeStreakDays(dates: List<LocalDate>): Int {
        if (dates.isEmpty()) return 0
        val set = dates.toHashSet()
        var d = LocalDate.now()
        var streak = 0
        while (set.contains(d)) {
            streak++
            d = d.minusDays(1)
        }
        return streak
    }

    private fun String.toLocalDateOrNull(): LocalDate? {
        return try {
            Instant.parse(this).atZone(ZoneId.systemDefault()).toLocalDate()
        } catch (_: Exception) {
            try { LocalDate.parse(this) } catch (_: Exception) { null }
        }
    }
}
