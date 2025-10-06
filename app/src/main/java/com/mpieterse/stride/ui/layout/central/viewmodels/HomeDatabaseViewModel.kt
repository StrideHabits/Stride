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
        val tag: String = "Habit",                 // <- default chip text
        val progress: Float = 0f,
        val checklist: List<Boolean> = emptyList(),
        val streaked: Boolean = false,
    )

    data class UiState(
        val loading: Boolean = false,
        val habits: List<HabitRowUi> = emptyList(),
        val daysHeader: List<String> = listOf("SUN\n19", "SAT\n18", "FRI\n17"),
        val error: String? = null
    )

    private val _state = MutableStateFlow(UiState(loading = true))
    val state: StateFlow<UiState> = _state

    init {
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)
        when (val r = habitsRepo.list()) {
            is ApiResult.Ok<*> -> {
                val items = (r.data as List<*>).mapNotNull { any ->
                    val dto = any as? com.mpieterse.stride.data.dto.habits.HabitDto ?: return@mapNotNull null
                    HabitRowUi(
                        id = dto.id,
                        name = dto.name,
                        tag = "Habit",                      // <- remove dto.tag usage
                        progress = 0f,
                        checklist = listOf(false, false, false),
                        streaked = false
                    )
                }
                _state.value = _state.value.copy(loading = false, habits = items)
            }
            is ApiResult.Err -> {
                _state.value = _state.value.copy(
                    loading = false,
                    error = "${r.code ?: ""} ${r.message ?: "Unknown error"}"
                )
            }
        }
    }
}
