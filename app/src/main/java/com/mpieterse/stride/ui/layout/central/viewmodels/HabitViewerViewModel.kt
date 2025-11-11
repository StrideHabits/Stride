// app/src/main/java/com/mpieterse/stride/ui/layout/central/viewmodels/HabitViewerViewModel.kt
package com.mpieterse.stride.ui.layout.central.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpieterse.stride.core.services.AppEventBus
import com.mpieterse.stride.core.services.HabitNameOverrideService
import com.mpieterse.stride.core.utils.checkInId
import com.mpieterse.stride.data.dto.checkins.CheckInCreateDto
import com.mpieterse.stride.data.dto.checkins.CheckInDto
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

        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            combine(
                habits.observeById(habitId)
                    .map { it?.name ?: "(Unknown habit)" }
                    .distinctUntilChanged(),
                checkins.observeForHabit(habitId)
                    .map { list -> list } // DTOs already
                    .onStart { emit(emptyList()) }
                    .catch { emit(emptyList()) }
            ) { habitName, localCheckins ->
                val display = nameOverrideService.getDisplayName(habitId, habitName)
                val completedThisMonth = monthDays(localCheckins)
                val streak = computeStreakDays(localCheckins)
                Triple(habitName, display, Pair(completedThisMonth, streak))
            }
                .onStart { _state.update { it.copy(loading = true) } }
                .collect { (habitName, display, pair) ->
                    val (days, streak) = pair
                    _state.update {
                        it.copy(
                            loading = false,
                            habitName = habitName,
                            displayName = display,
                            completedDates = days,
                            streakDays = streak
                        )
                    }
                }
        }
    }

    fun completeToday(habitId: String) = toggleCheckIn(habitId, LocalDate.now().toString())

    fun toggleCheckIn(habitId: String, isoDate: String) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val local = checkins.observeForHabit(habitId).firstOrNull().orEmpty()
                val existing = local.firstOrNull { it.dayKey == isoDate }
                if (existing == null) {
                    // create
                    checkins.create(
                        CheckInCreateDto(
                            habitId = habitId,
                            dayKey = isoDate,
                            completedAt = "${isoDate}T00:00:00Z"
                        )
                    )
                } else {
                    // delete by deterministic id
                    val id = existing.id.ifEmpty { checkInId(habitId, isoDate) }
                    checkins.delete(id)
                }
                eventBus.emit(AppEventBus.AppEvent.CheckInCompleted)
                _state.update { it.copy(loading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = "Write failed") }
            }
        }
    }

    fun updateLocalName(newName: String) {
        val id = _state.value.habitId
        if (id.isEmpty()) return
        nameOverrideService.updateHabitName(id, newName)
        _state.update { it.copy(displayName = newName) }
        viewModelScope.launch { eventBus.emit(AppEventBus.AppEvent.HabitNameChanged) }
    }

    // Helpers

    private fun monthDays(list: List<CheckInDto>): List<Int> {
        val today = LocalDate.now()
        val monthStart = today.withDayOfMonth(1)
        return list.asSequence()
            .mapNotNull { runCatching { LocalDate.parse(it.dayKey) }.getOrNull() }
            .filter { it.year == monthStart.year && it.month == monthStart.month }
            .map { it.dayOfMonth }
            .distinct()
            .sorted()
            .toList()
    }

    private fun computeStreakDays(list: List<CheckInDto>): Int {
        val days = list.asSequence()
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
