package com.mpieterse.stride.ui.layout.central.viewmodels

import android.content.Context
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpieterse.stride.core.services.AppEventBus
import com.mpieterse.stride.core.services.HabitNameOverrideService
import com.mpieterse.stride.data.local.entities.CheckInEntity
import com.mpieterse.stride.data.repo.CheckInRepository
import com.mpieterse.stride.data.repo.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class HomeDatabaseViewModel @Inject constructor(
    private val habitsRepo: HabitRepository,
    private val checkinsRepo: CheckInRepository,
    private val nameOverrideService: HabitNameOverrideService,
    private val eventBus: AppEventBus,
    private val tokenStore: TokenStore,
    private val pendingHabitsStore: PendingHabitsStore,
    private val uploadRepository: UploadRepository,
    private val habitCacheStore: HabitCacheStore,
    private val authenticationService: AuthenticationService,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    companion object {
        private const val PENDING_PREFIX = "pending:"
    }

    data class HabitRowUi(
        val id: String,
        val name: String,
        val tag: String = "Habit",
        val progress: Float = 0f,
        val checklist: List<Boolean> = emptyList(),
        val streaked: Boolean = false,
        val pending: Boolean = false,
    )

    data class UiState(
        val loading: Boolean = false,
        val habits: List<HabitRowUi> = emptyList(),
        val daysHeader: List<String> = emptyList(),
        val error: String? = null,
        val status: String = "—",
        val logs: List<String> = emptyList()
    )

    private val _state = MutableStateFlow(UiState(loading = false))
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

        // OFFLINE-FIRST: snapshot habits from Room
        val habitEntities = try { habitsRepo.observeAll().first() } catch (_: Exception) { emptyList() }

        val days = lastThreeDays()
        val header = days.map { d -> "${d.dayOfWeek.name.take(3)}\n${d.dayOfMonth}" }

        val rows = habitEntities.map { h ->
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
        setStatus("OK • list habits (local)")
        log("habits(local) ✅ count=${rows.size}")
    }

    private fun localCompletedSet(list: List<CheckInEntity>): Set<LocalDate> =
        list.asSequence()
            .filter { !it.deleted }
            .mapNotNull { runCatching { LocalDate.parse(it.dayKey) }.getOrNull() }
            .toSet()

    // OFFLINE-FIRST create: queue locally
    fun createHabit(name: String, onDone: (Boolean) -> Unit = {}) = viewModelScope.launch {
        withLoading {
            try {
                habitsRepo.createLocal(
                    name = name.trim(),
                    frequency = 0,
                    tag = null,
                    imageUrl = null
                )
                setStatus("OK • create habit (queued)")
                log("create(local) ✅ $name")
                eventBus.emit(AppEventBus.AppEvent.HabitCreated)
                refresh()
                onDone(true)
            } catch (e: Exception) {
                setStatus("ERR • create habit")
                _state.value = _state.value.copy(error = e.message ?: "Failed to create")
                log("create(local) ❌ ${e.message}")
                onDone(false)
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

    private suspend fun queuePendingHabit(draft: HabitDraft) {
        val pendingHabit = PendingHabit(
            name = draft.name,
            frequency = draft.frequency,
            tag = draft.tag,
            imageUrl = null,
            imageDataBase64 = draft.imageBase64,
            imageMimeType = draft.imageMimeType,
            imageFileName = draft.imageFileName
        )
        pendingHabitsStore.add(pendingHabit)
        setStatus("QUEUED • habit")
        log("create ⏳ queued ${pendingHabit.name}")
        val pendingRow = toPendingRow(pendingHabit)
        val header = if (_state.value.daysHeader.isEmpty()) headerFor(getLastThreeDays()) else _state.value.daysHeader
        val updatedHabits = _state.value.habits.filterNot { it.id == pendingRow.id } + pendingRow
        _state.value = _state.value.copy(
            habits = updatedHabits,
            daysHeader = header,
            error = null
        )
    }

    private fun toPendingRow(habit: PendingHabit): HabitRowUi =
        HabitRowUi(
            id = "$PENDING_PREFIX${habit.id}",
            name = habit.name,
            tag = habit.tag ?: "Pending",
            progress = 0f,
            checklist = emptyList(),
            streaked = false,
            pending = true
        )

    private fun headerFor(dates: List<LocalDate>): List<String> =
        dates.map { date ->
            val dayName = date.dayOfWeek.name.take(3)
            val dayNumber = date.dayOfMonth.toString()
            "$dayName\n$dayNumber"
        }

    private fun buildHabitRows(
        habits: List<HabitDto>,
        checkins: List<CheckInDto>,
        lastThreeDays: List<LocalDate>
    ): List<HabitRowUi> = habits.map { habit ->
        val habitCheckins = checkins.filter { it.habitId == habit.id }
        val today = LocalDate.now()
        val completedLastThreeDays = habitCheckins.mapNotNull { checkin ->
            try {
                java.time.LocalDate.parse(checkin.dayKey)
            } catch (e: Exception) {
                null
            }
        }.filter { date -> lastThreeDays.contains(date) }

        val progress = if (lastThreeDays.isNotEmpty()) completedLastThreeDays.size.toFloat() / lastThreeDays.size else 0f
        val checklist = lastThreeDays.map { date -> completedLastThreeDays.contains(date) }
        val streaked = completedLastThreeDays.contains(today)

        HabitRowUi(
            id = habit.id,
            name = nameOverrideService.getDisplayName(habit.id, habit.name),
            tag = habit.tag ?: "Habit",
            progress = progress,
            checklist = checklist,
            streaked = streaked,
            pending = false
        )
    }

    private fun mergeRows(
        primary: List<HabitRowUi>,
        pending: List<HabitRowUi>
    ): List<HabitRowUi> {
        if (pending.isEmpty()) return primary
        val existingIds = primary.map { it.id }.toMutableSet()
        val additional = pending.filter { existingIds.add(it.id) }
        return primary + additional
    }

    private suspend fun resolveImageUrl(draft: HabitDraft): ApiResult<String?> {
        if (!draft.imageBase64.isNullOrBlank()) {
            val file = createTempImageFile(draft) ?: return ApiResult.Err(null, "Failed to prepare image")
            return try {
                when (val upload = uploadRepository.upload(file.absolutePath)) {
                    is ApiResult.Ok -> {
                        val response = upload.data as? UploadResponse
                        ApiResult.Ok(response?.url ?: response?.path)
                    }
                    is ApiResult.Err -> upload
                }
            } finally {
                file.delete()
            }
        }
        return ApiResult.Ok(null)
    }

    private fun createTempImageFile(draft: HabitDraft): File? =
        runCatching {
            val bytes = Base64.decode(draft.imageBase64, Base64.DEFAULT)
            val fileName = draft.imageFileName ?: "${UUID.randomUUID()}.jpg"
            val safeName = if (fileName.contains(".")) fileName else "$fileName.jpg"
            val file = File(appContext.cacheDir, "habit_draft_$safeName")
            FileOutputStream(file).use { it.write(bytes) }
            file
        }.getOrElse {
            log("Failed to prepare temp image: ${it.message}")
            null
        }
}
