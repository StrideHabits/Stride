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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.ExistingWorkPolicy
import com.mpieterse.stride.workers.PushWorker
import com.mpieterse.stride.workers.PullWorker

@HiltViewModel
class DebugViewModel @Inject constructor(
    private val auth: AuthRepository,
    private val habitsRepo: HabitRepository,
    private val settingsRepo: SettingsRepository,
    private val uploadsRepo: UploadRepository,
    private val checkinsRepo: CheckInRepository
) : ViewModel() {

    data class HabitRow(val id: String, val name: String)

    // keep UI compatibility
    fun listHabits() = observeHabits()
    fun listCheckIns() = listCheckInsSummary()

    data class UiState(
        val loading: Boolean = false,
        val status: String = "—",
        val logs: List<String> = emptyList(),
        val currentEmail: String? = null,
        val habits: List<HabitRow> = emptyList(),
        val selectedHabitId: String? = null,
        val selectedHabitName: String? = null,
        val selectedHabitLocalDates: List<String> = emptyList() // yyyy-MM-dd
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    private var habitsJob: Job? = null
    private var checkinsJob: Job? = null

    private fun log(msg: String) {
        _state.value = _state.value.copy(logs = _state.value.logs + msg.take(400))
    }
    private fun setStatus(s: String) { _state.value = _state.value.copy(status = s) }
    private suspend fun <T> withLoading(block: suspend () -> T): T {
        _state.value = _state.value.copy(loading = true)
        return try { block() } finally { _state.value = _state.value.copy(loading = false) }
    }

    // ---------------- Auth ----------------

    fun register(name: String, email: String, pass: String) = viewModelScope.launch {
        withLoading {
            when (val r = auth.register(name, email, pass)) {
                is ApiResult.Ok<*> -> { setStatus("OK • register"); log("register ✅"); _state.value = _state.value.copy(currentEmail = email) }
                is ApiResult.Err   -> { setStatus("ERR ${r.code ?: ""} • register"); log("register ❌ ${r.code} ${r.message}") }
            }
        }
    }

    fun login(email: String, pass: String, ctx: Context? = null) = viewModelScope.launch {
        withLoading {
            when (val r = auth.login(email, pass)) {
                is ApiResult.Ok<*> -> {
                    setStatus("OK • login"); log("login ✅ token=****")
                    _state.value = _state.value.copy(currentEmail = email)
                    // start local observers
                    observeHabits()
                    // optional: initial pull + periodic schedule
                    ctx?.let {
                        PullWorker.enqueueOnce(it)
                        PullWorker.schedulePeriodic(it)
                        log("pull ▶️ initial + periodic")
                    }
                }
                is ApiResult.Err -> { setStatus("ERR ${r.code ?: ""} • login"); log("login ❌ ${r.code} ${r.message}") }
            }
        }
    }

    // ---------------- Habits (offline-first) ----------------

    fun observeHabits() {
        habitsJob?.cancel()
        habitsJob = viewModelScope.launch {
            habitsRepo.observeAll().collectLatest { list ->
                val rows = list.map { HabitRow(it.id, it.name) }
                _state.value = _state.value.copy(habits = rows)
                setStatus("OK • habits (local)")
            }
        }
    }

    fun createHabit(name: String, frequency: Int, tag: String?, imageUrl: String?) = viewModelScope.launch {
        withLoading {
            try {
                habitsRepo.createLocal(
                    name = name.trim(),
                    frequency = frequency.coerceAtLeast(0),
                    tag = tag?.trim().takeUnless { it.isNullOrEmpty() },
                    imageUrl = imageUrl?.trim().takeUnless { it.isNullOrEmpty() }
                )
                setStatus("OK • create habit (queued)")
                log("create ✅ $name (queued)")
            } catch (e: Exception) {
                setStatus("ERR • create habit"); log("create ❌ ${e.message}")
            }
        }
    }

    fun selectHabit(id: String, name: String) {
        _state.value = _state.value.copy(selectedHabitId = id, selectedHabitName = name, selectedHabitLocalDates = emptyList())
        observeSelectedHabitCheckins()
    }

    // ---------------- Check-ins (offline-first) ----------------

    fun completeForDate(habitId: String, dayKey: String) = viewModelScope.launch {
        val id = habitId.trim(); val date = dayKey.trim()
        if (id.isEmpty() || date.isEmpty()) { log("complete ⚠️ missing habitId or date"); return@launch }
        withLoading {
            try {
                checkinsRepo.toggle(id, date, on = true)
                setStatus("OK • complete (queued)")
                log("complete ✅ habit=$id @ $date")
                if (_state.value.selectedHabitId == id) observeSelectedHabitCheckins()
            } catch (e: Exception) {
                setStatus("ERR • complete"); log("complete ❌ ${e.message}")
            }
        }
    }

    fun quickComplete(dayOffset: Long) = viewModelScope.launch {
        val id = _state.value.selectedHabitId ?: return@launch
        val date = LocalDate.now().minusDays(dayOffset).toString()
        completeForDate(id, date)
    }

    private fun observeSelectedHabitCheckins() {
        checkinsJob?.cancel()
        val id = _state.value.selectedHabitId ?: return
        checkinsJob = viewModelScope.launch {
            checkinsRepo.observe(id).collectLatest { list ->
                _state.value = _state.value.copy(selectedHabitLocalDates = localCompletedDates(list))
            }
        }
    }

    fun listCheckInsSummary() = viewModelScope.launch {
        // derive from current local state without network
        val rows = _state.value.habits
        var total = 0
        val perHabit = mutableListOf<Pair<String, Int>>()
        for (h in rows) {
            val local: List<CheckInEntity> = try { checkinsRepo.observe(h.id).first() } catch (_: Exception) { emptyList() }
            val cnt = local.count { !it.deleted }
            total += cnt
            perHabit += h.name to cnt
        }
        setStatus("OK • list check-ins (local)")
        log("checkins ✅ total=$total  ${perHabit.joinToString { "${it.first}:${it.second}" }}")
    }

    private fun localCompletedDates(list: List<CheckInEntity>): List<String> =
        list.asSequence().filter { !it.deleted }.map { it.dayKey }.sorted().toList()

    // ---------------- Settings ----------------

    fun getSettings() = viewModelScope.launch {
        withLoading {
            when (val r = settingsRepo.get()) {
                is ApiResult.Ok<*> -> { setStatus("OK • get settings"); log("settings ✅ ${r.data}") }
                is ApiResult.Err   -> { setStatus("ERR ${r.code ?: ""} • get settings"); log("settings ❌ ${r.code} ${r.message}") }
            }
        }
    }

    fun updateSettings(hour: Int?, theme: String?) = viewModelScope.launch {
        withLoading {
            when (val r = settingsRepo.update(SettingsDto(hour, theme))) {
                is ApiResult.Ok<*> -> { setStatus("OK • update settings"); log("settings update ✅") }
                is ApiResult.Err   -> { setStatus("ERR ${r.code ?: ""} • update settings"); log("settings update ❌ ${r.code} ${r.message}") }
            }
        }
    }

    // ---------------- Upload ----------------

    fun upload(path: String) = viewModelScope.launch {
        withLoading {
            when (val r = uploadsRepo.upload(path)) {
                is ApiResult.Ok<*> -> { setStatus("OK • upload"); log("upload ✅ ${r.data}") }
                is ApiResult.Err   -> { setStatus("ERR ${r.code ?: ""} • upload"); log("upload ❌ ${r.code} ${r.message}") }
            }
        }
    }

    // ---------------- SYNC (debug buttons) ----------------

    fun pushNow(ctx: Context) = viewModelScope.launch {
        val wm = WorkManager.getInstance(ctx)
        val req = OneTimeWorkRequestBuilder<PushWorker>().addTag("debug-push-now").build()
        setStatus("PUSH • enqueued"); log("push ▶️ enqueued ${req.id}")
        wm.enqueueUniqueWork("debug-push-now", ExistingWorkPolicy.REPLACE, req)
        wm.getWorkInfoByIdFlow(req.id).collectLatest { info ->
            when (info?.state) {
                WorkInfo.State.ENQUEUED  -> setStatus("PUSH • enqueued")
                WorkInfo.State.RUNNING   -> setStatus("PUSH • running")
                WorkInfo.State.SUCCEEDED -> { setStatus("PUSH • success"); log("push ✅ ${req.id}") }
                WorkInfo.State.FAILED    -> { setStatus("PUSH • failed"); log("push ❌ ${info.outputData}") }
                WorkInfo.State.CANCELLED -> { setStatus("PUSH • cancelled"); log("push ⛔ cancelled") }
                WorkInfo.State.BLOCKED   -> setStatus("PUSH • blocked")
                null -> {}
            }
        }
    }

    fun pullNow(ctx: Context) = viewModelScope.launch {
        val wm = WorkManager.getInstance(ctx)
        val req = OneTimeWorkRequestBuilder<PullWorker>().addTag("debug-pull-now").build()
        setStatus("PULL • enqueued"); log("pull ▶️ enqueued ${req.id}")
        wm.enqueueUniqueWork("debug-pull-now", ExistingWorkPolicy.REPLACE, req)
        wm.getWorkInfoByIdFlow(req.id).collectLatest { info ->
            when (info?.state) {
                WorkInfo.State.ENQUEUED  -> setStatus("PULL • enqueued")
                WorkInfo.State.RUNNING   -> setStatus("PULL • running")
                WorkInfo.State.SUCCEEDED -> { setStatus("PULL • success"); log("pull ✅ ${req.id}") }
                WorkInfo.State.FAILED    -> { setStatus("PULL • failed"); log("pull ❌ ${info.outputData}") }
                WorkInfo.State.CANCELLED -> { setStatus("PULL • cancelled"); log("pull ⛔ cancelled") }
                WorkInfo.State.BLOCKED   -> setStatus("PULL • blocked")
                null -> {}
            }
        }
    }

    fun ensurePullScheduled(ctx: Context) {
        PullWorker.schedulePeriodic(ctx)
        setStatus("PULL • periodic scheduled")
        log("pull ⏰ periodic scheduled")
    }
}
