package com.mpieterse.stride.ui.layout.central.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpieterse.stride.core.net.ApiResult
import com.mpieterse.stride.data.dto.checkins.CheckInDto
import com.mpieterse.stride.data.dto.habits.HabitDto
import com.mpieterse.stride.data.dto.settings.SettingsDto
import com.mpieterse.stride.data.repo.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
        val status: String = "—"           // <-- screen reads this
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

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

    // ---- Actions ----

    fun register(name: String, email: String, pass: String) = viewModelScope.launch {
        withLoading {
            when (val r = auth.register(name, email, pass)) {
                is ApiResult.Ok<*> -> { setStatus("OK • register"); log("register ✅ ${(r.data as? Any) ?: ""}") }
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
                    setStatus("OK • list habits")
                    val count = (r.data as? List<*>)?.size ?: 0
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
                    setStatus("OK • create habit")
                    val h = r.data as? HabitDto
                    log("create ✅ ${h?.name ?: "—"} (${h?.id ?: "?"})")
                }
                is ApiResult.Err   -> { setStatus("ERR ${r.code ?: ""} • create habit"); log("create ❌ ${r.code} ${r.message}") }
            }
        }
    }

    fun completeToday(habitId: String, isoDate: String) = viewModelScope.launch {
        val id = habitId.trim()
        val date = isoDate.trim()
        withLoading {
            when (val r = checkins.create(id, date)) {
                is ApiResult.Ok<*> -> { setStatus("OK • complete"); val ci = r.data as CheckInDto; log("complete ✅ ${ci.habitId} @ ${ci.dayKey}") }
                is ApiResult.Err   -> { setStatus("ERR ${r.code ?: ""} • complete"); log("complete ❌ id='$id' len=${id.length}  ${r.code} ${r.message}") }
            }
        }
    }


    fun listCheckIns() = viewModelScope.launch {
        withLoading {
            when (val r = checkins.list()) {
                is ApiResult.Ok<*> -> {
                    setStatus("OK • list check-ins")
                    val count = (r.data as? List<*>)?.size ?: 0
                    log("checkins ✅ count=$count")
                }
                is ApiResult.Err   -> { setStatus("ERR ${r.code ?: ""} • list check-ins"); log("checkins ❌ ${r.code} ${r.message}") }
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
