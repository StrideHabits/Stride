package com.mpieterse.stride.ui.layout.central.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpieterse.stride.core.net.ApiResult
import com.mpieterse.stride.data.dto.habits.HabitDto
import com.mpieterse.stride.data.dto.settings.SettingsDto
import com.mpieterse.stride.data.local.entities.CheckInEntity
import com.mpieterse.stride.data.repo.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class DebugViewModel @Inject constructor(
    private val auth: AuthRepository,
    private val habits: HabitRepository,
    private val settings: SettingsRepository,
    private val uploads: UploadRepository,
    private val checkins: CheckInRepository
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val logs: List<String> = emptyList(),
        val status: String = "—"
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    private fun log(msg: String) {
        _state.value = _state.value.copy(logs = _state.value.logs + msg.take(400))
    }
    private fun setStatus(s: String) {
        _state.value = _state.value.copy(status = s)
    }
    private suspend fun <T> withLoading(block: suspend () -> T): T {
        _state.value = _state.value.copy(loading = true)
        return try { block() } finally { _state.value = _state.value.copy(loading = false) }
    }

    // ---- Actions ----

    fun register(name: String, email: String, pass: String) = viewModelScope.launch {
        withLoading {
            when (val r = auth.register(name, email, pass)) {
                is ApiResult.Ok<*> -> { setStatus("OK • register"); log("register ✅") }
                is ApiResult.Err   -> { setStatus("ERR ${r.code ?: ""} • register"); log("register ❌ ${r.code} ${r.message}") }
            }
        }
    }

    fun login(email: String, pass: String) = viewModelScope.launch {
        withLoading {
            when (val r = auth.login(email, pass)) {
                is ApiResult.Ok<*> -> { setStatus("OK • login"); log("login ✅ token=****") }
                is ApiResult.Err   -> { setStatus("ERR ${r.code ?: ""} • login"); log("login ❌ ${r.code} ${r.message}") }
            }
        }
    }

    fun listHabits() = viewModelScope.launch {
        withLoading {
            when (val r = habits.list()) {
                is ApiResult.Ok<*> -> {
                    val count = (r.data as? List<*>)?.size ?: 0
                    setStatus("OK • list habits")
                    log("habits ✅ count=$count")
                }
                is ApiResult.Err   -> { setStatus("ERR ${r.code ?: ""} • list habits"); log("habits ❌ ${r.code} ${r.message}") }
            }
        }
    }

    fun createHabit(name: String) = viewModelScope.launch {
        withLoading {
            when (val r = habits.create(name)) {
                is ApiResult.Ok<*> -> {
                    val h = r.data as? HabitDto
                    setStatus("OK • create habit")
                    log("create ✅ ${h?.name ?: "—"} (${h?.id ?: "?"})")
                }
                is ApiResult.Err   -> { setStatus("ERR ${r.code ?: ""} • create habit"); log("create ❌ ${r.code} ${r.message}") }
            }
        }
    }

    // Offline-first completion using local toggle + push worker
    fun completeToday(habitId: String, isoDate: String) = viewModelScope.launch {
        val id = habitId.trim()
        val date = isoDate.trim()
        withLoading {
            try {
                checkins.toggle(id, date, on = true)
                setStatus("OK • complete")
                log("complete ✅ habit=$id @ $date (queued)")
            } catch (e: Exception) {
                setStatus("ERR • complete")
                log("complete ❌ id='$id' date='$date' ${e.message}")
            }
        }
    }

    // Snapshot total check-ins across all habits from Room
    fun listCheckIns() = viewModelScope.launch {
        withLoading {
            try {
                val habitsResult = habits.list()
                val habitDtos = (habitsResult as? ApiResult.Ok<*>)?.data
                    ?.let { it as? List<*> }?.filterIsInstance<HabitDto>() ?: emptyList()

                var total = 0
                val perHabit = mutableListOf<Pair<String, Int>>()
                for (h in habitDtos) {
                    val local: List<CheckInEntity> = try { checkins.observe(h.id).first() } catch (_: Exception) { emptyList() }
                    val cnt = local.count { !it.deleted }
                    total += cnt
                    perHabit += h.name to cnt
                }
                setStatus("OK • list check-ins")
                log("checkins ✅ total=$total  ${perHabit.joinToString { "${it.first}:${it.second}" }}")
            } catch (e: Exception) {
                setStatus("ERR • list check-ins")
                log("checkins ❌ ${e.message}")
            }
        }
    }

    fun getSettings() = viewModelScope.launch {
        withLoading {
            when (val r = settings.get()) {
                is ApiResult.Ok<*> -> { setStatus("OK • get settings"); log("settings ✅ ${r.data}") }
                is ApiResult.Err   -> { setStatus("ERR ${r.code ?: ""} • get settings"); log("settings ❌ ${r.code} ${r.message}") }
            }
        }
    }

    fun updateSettings(hour: Int?, theme: String?) = viewModelScope.launch {
        withLoading {
            when (val r = settings.update(SettingsDto(hour, theme))) {
                is ApiResult.Ok<*> -> { setStatus("OK • update settings"); log("settings update ✅") }
                is ApiResult.Err   -> { setStatus("ERR ${r.code ?: ""} • update settings"); log("settings update ❌ ${r.code} ${r.message}") }
            }
        }
    }

    fun upload(path: String) = viewModelScope.launch {
        withLoading {
            when (val r = uploads.upload(path)) {
                is ApiResult.Ok<*> -> { setStatus("OK • upload"); log("upload ✅ ${r.data}") }
                is ApiResult.Err   -> { setStatus("ERR ${r.code ?: ""} • upload"); log("upload ❌ ${r.code} ${r.message}") }
            }
        }
    }
}
