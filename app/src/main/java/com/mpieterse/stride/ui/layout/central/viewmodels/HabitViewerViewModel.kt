// HabitViewerViewModel.kt
package com.mpieterse.stride.ui.layout.central.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpieterse.stride.core.net.ApiResult
import com.mpieterse.stride.core.services.AppEventBus
import com.mpieterse.stride.core.services.HabitNameOverrideService
import com.mpieterse.stride.data.dto.habits.HabitDto
import com.mpieterse.stride.data.local.entities.CheckInEntity
import com.mpieterse.stride.data.repo.CheckInRepository
import com.mpieterse.stride.data.repo.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

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
        val completedDates: List<Int> = emptyList()
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    private var observeJob: Job? = null

    fun load(habitId: String) {
        _state.update { it.copy(loading = true, error = null, habitId = habitId) }

        // 1) Get habit name online once
        viewModelScope.launch {
            val habitName = when (val hr = habits.list()) {
                is ApiResult.Ok -> hr.data
                    .filterIsInstance<HabitDto>()
                    .firstOrNull { it.id == habitId }?.name ?: "(Unknown habit)"
                is ApiResult.Err -> {
                    _state.update { it.copy(loading = false, error = "Habit load failed") }
                    return@launch
                }
                else -> {
                    _state.update { it.copy(loading = false, error = "Unexpected result") }
                    return@launch
                }
            }

            // 2) Start observing local check-ins for live UI
            observeJob?.cancel()
            observeJob = viewModelScope.launch {
                checkins.observe(habitId)
                    .onStart { _state.update { it.copy(loading = true) } }
                    .catch { _state.update { it.copy(loading = false, error = "Local load failed") } }
                    .collect { list ->
                        val completedThisMonth = monthDays(list)
                        val streak = computeStreakDays(list)
                        _state.update {
                            it.copy(
                                loading = false,
                                habitName = habitName,
                                displayName = nameOverrideService.getDisplayName(habitId, habitName),
                                completedDates = completedThisMonth,
                                streakDays = streak
                            )
                        }
                    }
            }
        }
    }

    fun completeToday(habitId: String) = toggleCheckIn(habitId, LocalDate.now().toString())

    fun toggleCheckIn(habitId: String, isoDate: String) {
        // Real toggle: if a non-deleted row exists for that day, set off; else set on.
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val local = checkins.observe(habitId).firstOrNull() ?: emptyList()
                val hasForDay = local.any { it.dayKey == isoDate && !it.deleted }
                val targetOn = !hasForDay
                checkins.toggle(habitId, isoDate, on = targetOn)
                eventBus.emit(AppEventBus.AppEvent.CheckInCompleted)
                // UI will refresh via Flow collector
                _state.update { it.copy(loading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = "Write failed") }
            }
        }
    }

    fun updateLocalName(newName: String) {
        val id = _state.value.habitId
        val real = if (id.isEmpty()) return else newName
        nameOverrideService.updateHabitName(id, real)
        _state.update { it.copy(displayName = newName) }
        viewModelScope.launch { eventBus.emit(AppEventBus.AppEvent.HabitNameChanged) }
    }

    // Helpers

    private fun monthDays(list: List<CheckInEntity>): List<Int> {
        val today = LocalDate.now()
        val monthStart = today.withDayOfMonth(1)
        return list.asSequence()
            .filter { !it.deleted }
            .mapNotNull { runCatching { LocalDate.parse(it.dayKey) }.getOrNull() }
            .filter { it.year == monthStart.year && it.month == monthStart.month }
            .map { it.dayOfMonth }
            .distinct()
            .sorted()
            .toList()
    }

    // Count consecutive days ending today from local non-deleted rows
    private fun computeStreakDays(list: List<CheckInEntity>): Int {
        val days = list.asSequence()
            .filter { !it.deleted }
            .mapNotNull { runCatching { LocalDate.parse(it.dayKey) }.getOrNull() }
            .toHashSet()
        var d = LocalDate.now(ZoneId.systemDefault())
        var streak = 0
        while (days.contains(d)) {
            streak++
            d = d.minusDays(1)
        }
        return streak
    }
}
