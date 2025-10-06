package com.mpieterse.stride.ui.layout.central.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpieterse.stride.core.net.ApiResult
import com.mpieterse.stride.data.repo.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeDatabaseViewModel @Inject constructor(
    private val habitsRepo: HabitRepository,
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
        val daysHeader: List<String> = listOf("SUN\n19", "SAT\n18", "FRI\n17"),
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
            when (val r = habitsRepo.list()) {
                is ApiResult.Ok<*> -> {
                    val items = (r.data as List<*>).mapNotNull { any ->
                        val dto = any as? com.mpieterse.stride.data.dto.habits.HabitDto ?: return@mapNotNull null
                        HabitRowUi(
                            id = dto.id,
                            name = dto.name,
                            tag = "Habit",
                            progress = 0f,
                            checklist = listOf(false, false, false),
                            streaked = false
                        )
                    }
                    _state.value = _state.value.copy(habits = items, error = null)
                    setStatus("OK • list habits")
                    log("habits ✅ count=${items.size}")
                }
                is ApiResult.Err -> {
                    _state.value = _state.value.copy(
                        error = "${r.code ?: ""} ${r.message ?: "Unknown error"}"
                    )
                    setStatus("ERR ${r.code ?: ""} • list habits")
                    log("habits ❌ ${r.code} ${r.message}")
                }
            }
        }
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
