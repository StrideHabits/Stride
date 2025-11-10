// HabitViewerViewModel.kt
package com.mpieterse.stride.ui.layout.central.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpieterse.stride.core.services.AppEventBus
import com.mpieterse.stride.core.services.HabitNameOverrideService
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
    private val eventBus: AppEventBus,
    private val habitCacheStore: HabitCacheStore,
    private val authenticationService: AuthenticationService
) : ViewModel() {

    data class UiState(
        val loading: Boolean = true,
        val error: String? = null,
        val habitId: String = "",
        val habitName: String = "",
        val displayName: String = "",
        val habitImage: Bitmap? = null,
        val habitImageUrl: String? = null,
        val habitTag: String? = null,
        val habitFrequency: Int = 0,
        val streakDays: Int = 0,
        val completedDates: List<Int> = emptyList()
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    private var observeJob: Job? = null

    fun load(habitId: String) {
        _state.update { it.copy(loading = true, error = null, habitId = habitId) }

        // Observe habit name locally and map to displayName via override service
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            combine(
                habits.observeAll()
                    .map { list -> list.firstOrNull { it.id == habitId }?.name ?: "(Unknown habit)" }
                    .distinctUntilChanged(),
                checkins.observe(habitId)
                    .map { list -> list.filter { !it.deleted } }
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
                // Decide toggle using current local state
                val local = checkins.observe(habitId).firstOrNull().orEmpty()
                val hasForDay = local.any { it.dayKey == isoDate && !it.deleted }
                val targetOn = !hasForDay
                checkins.toggle(habitId, isoDate, on = targetOn)
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

    private fun monthDays(list: List<CheckInEntity>): List<Int> {
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

    private fun computeStreakDays(list: List<CheckInEntity>): Int {
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
