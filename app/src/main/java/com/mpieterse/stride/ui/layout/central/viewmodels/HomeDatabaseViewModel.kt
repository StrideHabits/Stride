package com.mpieterse.stride.ui.layout.central.viewmodels

import android.content.Context
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpieterse.stride.core.net.ApiResult
import com.mpieterse.stride.data.dto.uploads.UploadResponse
import com.mpieterse.stride.data.dto.checkins.CheckInDto
import com.mpieterse.stride.data.dto.habits.HabitDto
import com.mpieterse.stride.data.local.HabitCacheStore
import com.mpieterse.stride.data.local.PendingHabit
import com.mpieterse.stride.data.local.PendingHabitsStore
import com.mpieterse.stride.data.local.TokenStore
import com.mpieterse.stride.data.repo.CheckInRepository
import com.mpieterse.stride.data.repo.HabitRepository
import com.mpieterse.stride.data.repo.UploadRepository
import com.mpieterse.stride.core.services.AppEventBus
import com.mpieterse.stride.core.services.AuthenticationService
import com.mpieterse.stride.core.services.HabitNameOverrideService
import com.mpieterse.stride.ui.layout.central.models.HabitDraft
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
        // Debug-style bits:
        val status: String = "—",
        val logs: List<String> = emptyList()
    )

    private val _state = MutableStateFlow(UiState(loading = false))
    val state: StateFlow<UiState> = _state

    init {
        // Don't refresh immediately - let the screen handle timing after authentication
        // This prevents race conditions where refresh happens before token is ready
        // Listen for real-time events
        viewModelScope.launch {
            eventBus.events.collect { event ->
                when (event) {
                    is AppEventBus.AppEvent.HabitCreated,
                    is AppEventBus.AppEvent.HabitUpdated,
                    is AppEventBus.AppEvent.CheckInCompleted,
                    is AppEventBus.AppEvent.HabitNameChanged -> {
                        refreshInternal()
                    }
                }
            }
        }
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

    fun refresh() = viewModelScope.launch { //This method refreshes the habit data from the API using ViewModel lifecycle management (Android Developers, 2024).
        refreshInternal()
    }
    
    fun forceRefresh() = viewModelScope.launch {
        // Force refresh to get latest data from API
        refreshInternal()
    }
    
    /**
     * Refreshes data after waiting for authentication token to be available.
     * This is more robust than using a fixed delay.
     */
    fun refreshWhenReady() = viewModelScope.launch {
        // Wait for token to be available (or timeout after reasonable wait)
        tokenStore.tokenFlow.first { token -> !token.isNullOrBlank() }
        refreshInternal()
    }
    
    private fun refreshInternal() = viewModelScope.launch {
        withLoading {
            _state.value = _state.value.copy(error = null)

            val lastThreeDays = getLastThreeDays()
            val pendingHabits = pendingHabitsStore.getAll()
            val pendingRows = pendingHabits.map { toPendingRow(it) }
            val daysHeader = headerFor(lastThreeDays)

            val activeUserId = runCatching { authenticationService.getCurrentUser()?.uid }.getOrNull()
            val mostRecentCache = habitCacheStore.getMostRecentEntry()
            val cacheEntry = when {
                !activeUserId.isNullOrBlank() -> habitCacheStore.getEntryForUser(activeUserId)
                else -> mostRecentCache?.second
            }
            val cacheUserId = when {
                !activeUserId.isNullOrBlank() -> activeUserId
                else -> mostRecentCache?.first
            }
            val cachedRows = cacheEntry?.let { entry ->
                buildHabitRows(entry.habits, entry.checkins, lastThreeDays)
            }.orEmpty()
            if (cachedRows.isNotEmpty()) {
                val merged = mergeRows(cachedRows, pendingRows)
                _state.value = _state.value.copy(
                    habits = merged,
                    daysHeader = daysHeader,
                    error = null
                )
            }
            
            // Load habits
            val habitsResult = habitsRepo.list()
            if (habitsResult is ApiResult.Err) {
                val errorCode = habitsResult.code
                val errorMessage = habitsResult.message ?: "Unknown error"
                
                // Handle 401 Unauthorized - token might be expired or invalid
                if (errorCode == 401) {
                    _state.value = _state.value.copy(
                        error = "Session expired. Please sign in again.",
                        habits = mergeRows(cachedRows, pendingRows),
                        daysHeader = daysHeader
                    )
                    setStatus("ERR 401 • Authentication required")
                    log("habits ❌ 401 - Authentication required")
                } else {
                    _state.value = _state.value.copy(
                        error = "${errorCode ?: ""} $errorMessage",
                        habits = mergeRows(cachedRows, pendingRows),
                        daysHeader = daysHeader
                    )
                    setStatus("ERR ${errorCode ?: ""} • list habits")
                    log("habits ❌ ${errorCode} ${errorMessage}")
                }
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
            val items = buildHabitRows(habitDtos, allCheckins, lastThreeDays)

            val combinedItems = items + pendingRows

            _state.value = _state.value.copy(
                habits = combinedItems, 
                daysHeader = daysHeader,
                error = null
            )
            setStatus("OK • list habits")
            log("habits ✅ count=${combinedItems.size} (pending=${pendingRows.size})")

            if (!activeUserId.isNullOrBlank()) {
                habitCacheStore.update(activeUserId, habitDtos, allCheckins)
            } else if (!cacheUserId.isNullOrBlank()) {
                habitCacheStore.update(cacheUserId, habitDtos, allCheckins)
            }
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

    fun createHabit(draft: HabitDraft, onDone: (Boolean) -> Unit = {}) = viewModelScope.launch { //This method creates a new habit through the API using ViewModel lifecycle management (Android Developers, 2024).
        withLoading {
            val hasToken = runCatching { tokenStore.hasToken() }.getOrDefault(false)
            if (!hasToken) {
                queuePendingHabit(draft)
                onDone(true)
                return@withLoading
            }

            when (val imageResult = resolveImageUrl(draft)) {
                is ApiResult.Err -> {
                    setStatus("QUEUED • habit")
                    log("create ⏳ queued ${draft.name} (image upload pending)")
                    queuePendingHabit(draft)
                    onDone(true)
                    return@withLoading
                }
                is ApiResult.Ok -> {
                    when (val r = habitsRepo.create(draft.name, draft.frequency, draft.tag, imageResult.data)) {
                        is ApiResult.Ok<*> -> {
                            setStatus("OK • create habit")
                            val h = r.data as? com.mpieterse.stride.data.dto.habits.HabitDto
                            log("create ✅ ${h?.name ?: "—"} (${h?.id ?: "?"})")
                            eventBus.emit(AppEventBus.AppEvent.HabitCreated)
                            refresh()
                            onDone(true)
                        }
                        is ApiResult.Err -> {
                            if (r.code == 401 || r.code == 403 || r.code == null) {
                                queuePendingHabit(draft)
                                onDone(true)
                            } else {
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
            }
    }

    fun checkInHabit(habitId: String, dayIndex: Int) = viewModelScope.launch { //This method creates a check-in for a habit using ViewModel lifecycle management (Android Developers, 2024).
        if (habitId.startsWith(PENDING_PREFIX)) {
            log("check-in skipped for pending habit: $habitId")
            return@launch
        }
        log("checkInHabit called: habitId=$habitId dayIndex=$dayIndex")
        val lastThreeDays = getLastThreeDays()
        log("lastThreeDays: $lastThreeDays")
        if (dayIndex >= 0 && dayIndex < lastThreeDays.size) {
            val targetDate = lastThreeDays[dayIndex]
            val isoDate = targetDate.toString() // yyyy-MM-dd format
            
            when (val result = checkinsRepo.create(habitId, isoDate)) {
                is ApiResult.Ok<*> -> {
                    setStatus("OK • check-in")
                    log("check-in ✅ habit=$habitId date=$isoDate")
                    // Emit event to notify other screens
                    eventBus.emit(AppEventBus.AppEvent.CheckInCompleted)
                    // Refresh to update UI
                    refresh()
                }
                is ApiResult.Err -> {
                    setStatus("ERR ${result.code ?: ""} • check-in")
                    log("check-in ❌ ${result.code} ${result.message}")
                    _state.value = _state.value.copy(
                        error = "${result.code ?: ""} ${result.message ?: "Failed to check-in"}"
                    )
                }
            }
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


