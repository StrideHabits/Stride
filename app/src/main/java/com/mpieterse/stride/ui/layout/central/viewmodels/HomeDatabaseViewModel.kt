package com.mpieterse.stride.ui.layout.central.viewmodels

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

@HiltViewModel
class HomeDatabaseViewModel @Inject constructor(
    private val habitsRepo: HabitRepository,
    private val checkinsRepo: CheckInRepository,
    private val nameOverrideService: HabitNameOverrideService,
    private val eventBus: AppEventBus,
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
        val status: String = "—",
        val logs: List<String> = emptyList()
    )

    private val _state = MutableStateFlow(UiState(loading = true))
    val state: StateFlow<UiState> = _state

    init {
        refresh()
        viewModelScope.launch {
            eventBus.events.collect { ev ->
                when (ev) {
                    is AppEventBus.AppEvent.HabitCreated,
                    is AppEventBus.AppEvent.HabitUpdated,
                    is AppEventBus.AppEvent.CheckInCompleted,
                    is AppEventBus.AppEvent.HabitNameChanged -> refreshInternal()
                }
            }
        }
    }

    fun refresh() = viewModelScope.launch { refreshInternal() }
    fun forceRefresh() = viewModelScope.launch { refreshInternal() }

    private fun log(msg: String) {
        _state.value = _state.value.copy(logs = _state.value.logs + msg.take(400))
    }
    private fun setStatus(s: String) { _state.value = _state.value.copy(status = s) }

    // suspend wrapper so we can call suspend code inside
    private suspend fun <T> withLoading(block: suspend () -> T): T {
        _state.value = _state.value.copy(loading = true)
        return try { block() } finally { _state.value = _state.value.copy(loading = false) }
    }

    private fun lastThreeDays(): List<LocalDate> {
        val t = LocalDate.now()
        return listOf(t.minusDays(2), t.minusDays(1), t)
    }

    private suspend fun refreshInternal() = withLoading {
        _state.value = _state.value.copy(error = null)

        // use existing repo methods
        val habitsResult = habitsRepo.list()
        val habitDtos: List<HabitDto> = when (habitsResult) {
            is ApiResult.Ok<*> -> (habitsResult.data as? List<*>)?.filterIsInstance<HabitDto>() ?: emptyList()
            is ApiResult.Err -> {
                _state.value = _state.value.copy(
                    error = "${habitsResult.code ?: ""} ${habitsResult.message ?: "Unknown error"}"
                )
                setStatus("ERR ${habitsResult.code ?: ""} • list habits")
                log("habits ❌ ${habitsResult.code} ${habitsResult.message}")
                return@withLoading
            }
            else -> emptyList()
        }


        val days = lastThreeDays()
        val header = days.map { d -> "${d.dayOfWeek.name.take(3)}\n${d.dayOfMonth}" }

        val rows = habitDtos.map { h ->
            val local = try { checkinsRepo.observe(h.id).first() } catch (_: Exception) { emptyList() }
            val completed = localCompletedSet(local)

            val completedLast3 = days.map { d -> completed.contains(d) }
            val progress = if (days.isNotEmpty()) completedLast3.count { it }.toFloat() / days.size else 0f
            val todayDone = completed.contains(LocalDate.now())

            HabitRowUi(
                id = h.id,
                name = nameOverrideService.getDisplayName(h.id, h.name),
                tag = h.tag ?: "Habit",
                progress = progress,
                checklist = completedLast3,
                streaked = todayDone
            )
        }

        _state.value = _state.value.copy(
            habits = rows,
            daysHeader = header,
            error = null
        )
        setStatus("OK • list habits")
        log("habits ✅ count=${rows.size}")
    }

    private fun localCompletedSet(list: List<CheckInEntity>): Set<LocalDate> =
        list.asSequence()
            .filter { !it.deleted }
            .mapNotNull { runCatching { LocalDate.parse(it.dayKey) }.getOrNull() }
            .toSet()

    fun createHabit(name: String, onDone: (Boolean) -> Unit = {}) = viewModelScope.launch {
        withLoading {
            when (val r = habitsRepo.create(name)) {            // existing repo method
                is ApiResult.Ok<*> -> {
                    setStatus("OK • create habit")
                    log("create ✅ ${(r.data as? HabitDto)?.name ?: "—"}")
                    eventBus.emit(AppEventBus.AppEvent.HabitCreated)
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
                else -> onDone(false)
            }
        }
    }

    fun checkInHabit(habitId: String, dayIndex: Int) = viewModelScope.launch {
        val days = lastThreeDays()
        if (dayIndex !in days.indices) return@launch
        val iso = days[dayIndex].toString()
        try {
            checkinsRepo.toggle(habitId, iso, on = true)
            setStatus("OK • check-in enqueue")
            eventBus.emit(AppEventBus.AppEvent.CheckInCompleted)
            refresh()
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = "Failed to queue check-in")
            setStatus("ERR • check-in enqueue")
        }
    }
}
