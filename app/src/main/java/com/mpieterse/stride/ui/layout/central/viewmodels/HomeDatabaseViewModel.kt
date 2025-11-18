// app/src/main/java/com/mpieterse/stride/ui/layout/central/viewmodels/HomeDatabaseViewModel.kt
package com.mpieterse.stride.ui.layout.central.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpieterse.stride.core.services.AppEventBus
import com.mpieterse.stride.core.services.HabitNameOverrideService
import com.mpieterse.stride.core.utils.Clogger
import com.mpieterse.stride.data.dto.checkins.CheckInDto
import com.mpieterse.stride.data.dto.habits.HabitCreateDto
import com.mpieterse.stride.data.repo.CheckInRepository
import com.mpieterse.stride.data.repo.HabitRepository
import com.mpieterse.stride.utils.ImageUploadHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException

@HiltViewModel
class HomeDatabaseViewModel @Inject constructor(
    private val habitsRepo: HabitRepository,
    private val checkinsRepo: CheckInRepository,
    private val nameOverrideService: HabitNameOverrideService,
    private val eventBus: AppEventBus,
    private val imageUploadHelper: ImageUploadHelper,
    @ApplicationContext private val appContext: Context,
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

    private data class ImageResolutionResult(
        val url: String?,
        val uploadFailed: Boolean
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
                    is AppEventBus.AppEvent.HabitDeleted -> refreshInternal()
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

        // Off-line snapshot from Room via repo
        val habits = try { habitsRepo.observeAll().first() } catch (_: Exception) { emptyList() }

        val days = lastThreeDays()
        val header = days.map { d ->
            val label = d.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            val dayNumber = d.dayOfMonth.toString().padStart(2, '0')
            "$label\n$dayNumber"
        }

        val rows = habits.map { h ->
            val local: List<CheckInDto> =
                try { checkinsRepo.observeForHabit(h.id).first() } catch (_: Exception) { emptyList() }

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

    private fun localCompletedSet(list: List<CheckInDto>): Set<LocalDate> =
        list.asSequence()
            .mapNotNull { runCatching { LocalDate.parse(it.dayKey) }.getOrNull() }
            .toSet()



    /**
     * Creates a habit with optional image upload.
     * 
     * @param onDone Callback invoked when habit creation completes.
     *   - `true`: Habit is at least saved locally (may include image upload failure or server/network errors - check `state.error` for details)
     *   - `false`: Habit creation completely failed (authentication errors, invalid data, etc.)
     * 
     * Note: Image upload failures are non-blocking. If image upload fails, `onDone(true)` is still
     * called, but `state.error` will contain `IMAGE_UPLOAD_WARNING` to inform the user.
     * 
     * Server errors (5xx) and network errors result in local save and `onDone(true)` since the
     * repository saves locally when remote operations fail, allowing sync when connectivity is restored.
     */
    fun createHabit(
        name: String,
        frequency: Int = 1,
        tag: String? = null,
        imageBase64: String? = null,
        imageMimeType: String? = null,
        imageUrl: String? = null,
        onDone: (Boolean) -> Unit = {}
    ) = viewModelScope.launch {
        withLoading {
            try {
                val imageResult = resolveImageForCreate(imageBase64, imageMimeType, imageUrl)
                habitsRepo.create(
                    HabitCreateDto(
                        name = name.trim(),
                        frequency = frequency.coerceIn(1, 7),
                        tag = tag?.trim().takeUnless { it.isNullOrEmpty() },
                        imageUrl = imageResult.url?.takeUnless { it.isEmpty() }
                    )
                )
                setStatus("OK • create habit${if (imageResult.uploadFailed) " (no image)" else ""}")
                log("create ✅ $name${if (imageResult.uploadFailed) " (image upload failed)" else ""}")
                if (imageResult.uploadFailed) {
                    _state.value = _state.value.copy(error = IMAGE_UPLOAD_WARNING)
                }
                eventBus.emit(AppEventBus.AppEvent.HabitCreated)
                refresh()
                onDone(true)
            } catch (e: HttpException) {
                handleCreateHabitHttpError(e, onDone)
            } catch (e: Exception) {
                // Re-throw CancellationException to respect coroutine cancellation
                if (e is CancellationException) throw e
                handleCreateHabitGenericError(e, onDone)
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
            // Re-throw CancellationException to respect coroutine cancellation
            if (e is CancellationException) throw e
            _state.value = _state.value.copy(error = "Failed to queue check-in")
            setStatus("ERR • check-in enqueue")
        }
    }

    /**
     * Resolves image URL for habit creation.
     * 
     * Image upload failures are intentionally non-blocking - habits can be created
     * without images, and users can add images later. This method never throws
     * exceptions to ensure habit creation always proceeds.
     */
    private suspend fun resolveImageForCreate(
        imageBase64: String?,
        imageMimeType: String?,
        imageUrl: String?
    ): ImageResolutionResult {
        if (!imageBase64.isNullOrBlank()) {
            // Image failures should not prevent habit creation; we log and continue without an image
            val uploadedUrl = imageUploadHelper.uploadImage(appContext, imageBase64, imageMimeType)
            if (uploadedUrl.isNullOrBlank()) {
                Clogger.w("HomeDatabaseViewModel", "Image upload failed during habit creation, continuing without image")
                return ImageResolutionResult(null, true)
            } else {
                return ImageResolutionResult(imageUploadHelper.sanitizeImageUrl(uploadedUrl), false)
            }
        }
        if (!imageUrl.isNullOrBlank()) {
            return ImageResolutionResult(imageUploadHelper.sanitizeImageUrl(imageUrl.trim()), false)
        }
        return ImageResolutionResult(null, false)
    }

    private fun handleCreateHabitHttpError(e: HttpException, onDone: (Boolean) -> Unit) {
        val errorMessage = when {
            e.code() in 500..599 -> "Server error. Habit saved locally and will sync when available."
            e.code() == 401 || e.code() == 403 -> "Authentication error. Please sign in again."
            e.code() == 400 -> "Invalid habit data. Please check your input and try again."
            e.code() == 404 -> "Server not found. Habit saved locally and will sync when available."
            else -> "Failed to create habit: ${e.message() ?: "Unknown error"}"
        }
        setStatus("ERR • create habit")
        _state.value = _state.value.copy(error = errorMessage)
        log("create ❌ HTTP ${e.code()}: ${e.message()}")
        onDone(e.code() in 500..599)
    }

    private fun handleCreateHabitGenericError(e: Exception, onDone: (Boolean) -> Unit) {
        val isNetworkError = e.isNetworkError()
        val errorMessage = if (isNetworkError) {
            "Network error. Habit saved locally and will sync when online."
        } else {
            "Failed to create habit: ${e.message ?: "Unknown error"}"
        }
        setStatus("ERR • create habit")
        _state.value = _state.value.copy(error = errorMessage)
        log("create ❌ ${e.message}")
        onDone(isNetworkError)
    }

    private fun Throwable.isNetworkError(): Boolean {
        return this is java.net.SocketTimeoutException ||
            this is java.net.ConnectException ||
            (this is java.io.IOException && (
                message?.lowercase()?.contains("timeout") == true ||
                    message?.lowercase()?.contains("network") == true ||
                    message?.lowercase()?.contains("connection") == true
                ))
    }

    companion object {
        private const val IMAGE_UPLOAD_WARNING = "Habit created, but image upload failed. You can add an image later."
    }
}
