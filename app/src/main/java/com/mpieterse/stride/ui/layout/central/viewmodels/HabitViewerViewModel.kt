// HabitViewerViewModel.kt
package com.mpieterse.stride.ui.layout.central.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpieterse.stride.core.net.ApiResult
import com.mpieterse.stride.data.dto.checkins.CheckInDto
import com.mpieterse.stride.data.dto.habits.HabitDto
import com.mpieterse.stride.data.repo.CheckInRepository
import com.mpieterse.stride.data.repo.HabitRepository
import com.mpieterse.stride.core.services.HabitNameOverrideService
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
    private val nameOverrideService: HabitNameOverrideService
) : ViewModel() {

    data class UiState(
        val loading: Boolean = true,
        val error: String? = null,
        val habitName: String = "",
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
        val completedThisMonth = mine.mapNotNull { it.completedAt.toLocalDateOrNull() }
            .filter { it.year == monthStart.year && it.month == monthStart.month }
            .map { it.dayOfMonth }
            .distinct()
            .sorted()

        val streak = computeStreakDays(mine.mapNotNull { it.completedAt.toLocalDateOrNull() }.distinct())

        _state.value = _state.value.copy(
            loading = false,
            habitName = habitName,
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
            }
            is ApiResult.Err -> {
                _state.value = _state.value.copy(
                    loading = false,
                    error = "Complete failed: ${r.code ?: ""} ${r.message ?: ""}"
                )
            }
        }
    }

    fun updateLocalName(newName: String) {
        _state.value = _state.value.copy(localNameOverride = newName)
        // Store in shared service so home screen can access it
        nameOverrideService.updateHabitName(_state.value.habitName, newName)
    }

    fun getDisplayName(): String {
        return _state.value.localNameOverride ?: _state.value.habitName
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
