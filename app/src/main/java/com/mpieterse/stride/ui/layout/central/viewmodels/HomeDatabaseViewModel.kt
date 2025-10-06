package com.mpieterse.stride.ui.layout.central.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpieterse.stride.core.net.ApiResult
import com.mpieterse.stride.data.repo.HabitRepository
import com.mpieterse.stride.data.repo.CheckInRepository
import com.mpieterse.stride.core.services.HabitNameOverrideService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

@HiltViewModel
class HomeDatabaseViewModel @Inject constructor(
    private val habitsRepo: HabitRepository,
    private val checkinsRepo: CheckInRepository,
    private val nameOverrideService: HabitNameOverrideService,
) : ViewModel() {

    data class HabitRowUi(
        val id: String,
        val name: String,
        val tag: String = "Habit",
        val progress: Float = 0f,
        val checklist: List<Boolean> = emptyList(),
        val streaked: Boolean = false,
    )

    data class UiState(
        val loading: Boolean = false,
        val habits: List<HabitRowUi> = emptyList(),
        val daysHeader: List<String> = emptyList(),
        val error: String? = null,
        // Debug-style bits:
        val status: String = "—",
        val logs: List<String> = emptyList()
    )

    private val _state = MutableStateFlow(UiState(loading = true))
    val state: StateFlow<UiState> = _state

    init {
        refresh()
    }

    // --- Debug-style helpers ---
    private fun log(msg: String) {
        _state.value = _state.value.copy(logs = _state.value.logs + msg.take(400))
    }
    private fun setStatus(s: String) {
        _state.value = _state.value.copy(status = s)
    }
    private inline fun withLoading(block: () -> Unit) {
        _state.value = _state.value.copy(loading = true)
        block()
        _state.value = _state.value.copy(loading = false)
    }

    fun refresh() = viewModelScope.launch {
        withLoading {
            _state.value = _state.value.copy(error = null)
            
            // Load habits
            val habitsResult = habitsRepo.list()
            if (habitsResult is ApiResult.Err) {
                _state.value = _state.value.copy(
                    error = "${habitsResult.code ?: ""} ${habitsResult.message ?: "Unknown error"}"
                )
                setStatus("ERR ${habitsResult.code ?: ""} • list habits")
                log("habits ❌ ${habitsResult.code} ${habitsResult.message}")
                return@withLoading
            }
            
            val habits = (habitsResult as ApiResult.Ok<*>).data as List<*>
            val habitDtos = habits.filterIsInstance<com.mpieterse.stride.data.dto.habits.HabitDto>()
            
            // Load check-ins to calculate progress
            val checkinsResult = checkinsRepo.list()
            val allCheckins = if (checkinsResult is ApiResult.Ok<*>) {
                (checkinsResult.data as List<*>).filterIsInstance<com.mpieterse.stride.data.dto.checkins.CheckInDto>()
            } else {
                emptyList()
            }
            
            // Create habit rows with real progress
            val items = habitDtos.map { habit ->
                val habitCheckins = allCheckins.filter { it.habitId == habit.id }
                val today = LocalDate.now()
                val lastThreeDays = getLastThreeDays()
                val completedLastThreeDays = habitCheckins.mapNotNull { checkin ->
                    try {
                        java.time.Instant.parse(checkin.completedAt).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                    } catch (e: Exception) { null }
                }.filter { date -> lastThreeDays.contains(date) }
                
                val progress = if (lastThreeDays.isNotEmpty()) completedLastThreeDays.size.toFloat() / lastThreeDays.size else 0f
                val checklist = lastThreeDays.map { date -> completedLastThreeDays.contains(date) }
                val streaked = completedLastThreeDays.contains(today)
                
                HabitRowUi(
                    id = habit.id,
                    name = nameOverrideService.getDisplayName(habit.id, habit.name),
                    tag = "Habit",
                    progress = progress,
                    checklist = checklist,
                    streaked = streaked
                )
            }
            
            // Generate 3-day header
            val lastThreeDays = getLastThreeDays()
            val daysHeader = lastThreeDays.map { date ->
                val dayName = date.dayOfWeek.name.take(3) // MON, TUE, etc.
                val dayNumber = date.dayOfMonth.toString()
                "$dayName\n$dayNumber"
            }
            
            _state.value = _state.value.copy(
                habits = items, 
                daysHeader = daysHeader,
                error = null
            )
            setStatus("OK • list habits")
            log("habits ✅ count=${items.size}")
        }
    }
    
    private fun getLastThreeDays(): List<LocalDate> {
        val today = LocalDate.now()
        return listOf(
            today.minusDays(2), // 2 days ago
            today.minusDays(1), // 1 day ago  
            today                // today
        )
    }

    fun createHabit(name: String, onDone: (Boolean) -> Unit = {}) = viewModelScope.launch {
        withLoading {
            when (val r = habitsRepo.create(name)) { // mirror Debug.createHabit
                is ApiResult.Ok<*> -> {
                    setStatus("OK • create habit")
                    val h = r.data as? com.mpieterse.stride.data.dto.habits.HabitDto
                    log("create ✅ ${h?.name ?: "—"} (${h?.id ?: "?"})")
                    // Refresh so the new habit shows up
                    refresh()
                    onDone(true)
                }
                is ApiResult.Err -> {
                    setStatus("ERR ${r.code ?: ""} • create habit")
                    log("create ❌ ${r.code} ${r.message}")
                    _state.value = _state.value.copy(
                        error = "${r.code ?: ""} ${r.message ?: "Failed to create"}"
                    )
                    onDone(false)
                }
            }
        }
    }
}


