// app/src/main/java/com/mpieterse/stride/ui/layout/central/viewmodels/HabitViewerViewModel.kt
package com.mpieterse.stride.ui.layout.central.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpieterse.stride.core.net.ApiResult
import com.mpieterse.stride.core.services.AppEventBus
import com.mpieterse.stride.core.services.HabitNameOverrideService
import com.mpieterse.stride.core.utils.Clogger
import com.mpieterse.stride.core.utils.checkInId
import com.mpieterse.stride.data.dto.checkins.CheckInCreateDto
import com.mpieterse.stride.data.dto.checkins.CheckInDto
import com.mpieterse.stride.data.dto.habits.HabitDto
import com.mpieterse.stride.data.repo.CheckInRepository
import com.mpieterse.stride.data.repo.HabitRepository
import com.mpieterse.stride.data.repo.concrete.UploadRepository
import com.mpieterse.stride.ui.layout.central.components.HabitData
import com.mpieterse.stride.utils.base64ToBitmap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.net.URL
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class HabitViewerViewModel @Inject constructor(
    private val habits: HabitRepository,
    private val checkins: CheckInRepository,
    private val nameOverrideService: HabitNameOverrideService,
    private val eventBus: AppEventBus,
    private val uploadRepo: UploadRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    data class UiState(
        val loading: Boolean = true,
        val error: String? = null,
        val habitId: String = "",
        val habitName: String = "",
        val displayName: String = "",
        val frequency: Int = 0,
        val tag: String? = null,
        val habitImage: Bitmap? = null,
        val streakDays: Int = 0,
        val completedDates: List<Int> = emptyList()
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    private var lastHabit: HabitDto? = null
    private var lastImageUrl: String? = null

    private var observeJob: Job? = null

    fun load(habitId: String) {
        _state.update { it.copy(loading = true, error = null, habitId = habitId) }

        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            combine(
                habits.observeById(habitId)
                    .distinctUntilChanged(),
                checkins.observeForHabit(habitId)
                    .map { list -> list } // DTOs already
                    .onStart { emit(emptyList()) }
                    .catch { emit(emptyList()) }
            ) { habitDto, localCheckins ->
                val habitName = habitDto?.name ?: "(Unknown habit)"
                val display = nameOverrideService.getDisplayName(habitId, habitName)
                val completedThisMonth = monthDays(localCheckins)
                val streak = computeStreakDays(localCheckins)
                Pair(habitDto, Triple(habitName, display, Pair(completedThisMonth, streak)))
            }
                .onStart { _state.update { it.copy(loading = true) } }
                .collect { (habitDto, triple) ->
                    val (habitName, display, pair) = triple
                    val (days, streak) = pair
                    val sanitizedUrl = habitDto?.imageUrl?.let { sanitizeImageUrl(it) }
                    val imageBitmap = sanitizedUrl?.takeIf { it.isNotBlank() }?.let { url ->
                        if (url != lastImageUrl || _state.value.habitImage == null) {
                            fetchBitmap(url)?.also { lastImageUrl = url }
                        } else {
                            _state.value.habitImage
                        }
                    } ?: _state.value.habitImage

                    lastHabit = habitDto
                    _state.update {
                        it.copy(
                            loading = false,
                            habitName = habitName,
                            displayName = display,
                            frequency = habitDto?.frequency ?: 0,
                            tag = habitDto?.tag,
                            habitImage = imageBitmap,
                            completedDates = days,
                            streakDays = streak
                        )
                    }
                }
        }
    }

    fun completeToday(habitId: String) = toggleCheckIn(habitId, LocalDate.now().toString())

    fun toggleCheckIn(habitId: String, isoDate: String) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val local = checkins.observeForHabit(habitId).firstOrNull().orEmpty()
                val existing = local.firstOrNull { it.dayKey == isoDate }
                if (existing == null) {
                    // create
                    checkins.create(
                        CheckInCreateDto(
                            habitId = habitId,
                            dayKey = isoDate,
                            completedAt = "${isoDate}T00:00:00Z"
                        )
                    )
                } else {
                    // delete by deterministic id
                    val id = existing.id.ifEmpty { checkInId(habitId, isoDate) }
                    checkins.delete(id)
                }
                eventBus.emit(AppEventBus.AppEvent.CheckInCompleted)
                _state.update { it.copy(loading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = "Write failed") }
            }
        }
    }

    fun updateLocalName(newName: String) {
        val id = _state.value.habitId
        if (id.isEmpty()) return
        nameOverrideService.updateHabitName(id, newName)
        _state.update { it.copy(displayName = newName) }
        viewModelScope.launch { eventBus.emit(AppEventBus.AppEvent.HabitNameChanged) }
    }

    fun updateHabitDetails(updated: HabitData) = viewModelScope.launch {
        val current = lastHabit ?: return@launch
        try {
            val uploadedUrl = if (!updated.imageBase64.isNullOrBlank()) {
                uploadImage(updated.imageBase64!!, updated.imageMimeType)
            } else {
                sanitizeImageUrl(current.imageUrl)
            }
            if (!updated.imageBase64.isNullOrBlank() && uploadedUrl.isNullOrBlank()) {
                throw IllegalStateException("Image upload failed")
            }
            val dto = current.copy(
                name = updated.name,
                frequency = updated.frequency.coerceIn(1, 7),
                tag = updated.tag,
                imageUrl = uploadedUrl
            )
            habits.upsertLocal(dto)
            lastHabit = dto
            lastImageUrl = uploadedUrl
            val updatedBitmap = when {
                !updated.imageBase64.isNullOrBlank() -> base64ToBitmap(updated.imageBase64)
                !uploadedUrl.isNullOrBlank() -> fetchBitmap(uploadedUrl)
                else -> null
            } ?: _state.value.habitImage
            _state.update { it.copy(habitImage = updatedBitmap) }
            eventBus.emit(AppEventBus.AppEvent.HabitUpdated)
        } catch (e: Exception) {
            Clogger.e("HabitViewerViewModel", "Failed to update habit", e)
            _state.update { it.copy(error = "Failed to update habit") }
        }
    }

    private suspend fun fetchBitmap(url: String): Bitmap? = withContext(Dispatchers.IO) {
        return@withContext try {
            (URL(url).openConnection()).getInputStream().use { input ->
                BitmapFactory.decodeStream(input)
            }
        } catch (e: Exception) {
            Clogger.e("HabitViewerViewModel", "Failed to load image from $url", e)
            null
        }
    }

    private suspend fun uploadImage(base64: String, mimeType: String?): String? = withContext(Dispatchers.IO) {
        val extension = when (mimeType) {
            "image/png" -> ".png"
            else -> ".jpg"
        }
        val tempFile = File.createTempFile("habit-", extension, appContext.cacheDir)
        return@withContext try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            tempFile.writeBytes(bytes)
            when (val result = uploadRepo.upload(tempFile.path)) {
                is ApiResult.Ok -> sanitizeImageUrl(result.data.url ?: result.data.path)
                is ApiResult.Err -> {
                    Clogger.e("HabitViewerViewModel", "Image upload failed: ${result.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Clogger.e("HabitViewerViewModel", "Image upload exception", e)
            null
        } finally {
            tempFile.delete()
        }
    }

    private fun sanitizeImageUrl(url: String?): String? {
        return url?.let {
            if (it.startsWith("http://", ignoreCase = true)) {
                "https://${it.removePrefix("http://")}"
            } else it
        }
    }

    // Helpers

    private fun monthDays(list: List<CheckInDto>): List<Int> {
        val today = LocalDate.now()
        val monthStart = today.withDayOfMonth(1)
        return list.asSequence()
            .mapNotNull { runCatching { LocalDate.parse(it.dayKey) }.getOrNull() }
            .filter { it.year == monthStart.year && it.month == monthStart.month }
            .map { it.dayOfMonth }
            .distinct()
            .sorted()
            .toList()
    }

    private fun computeStreakDays(list: List<CheckInDto>): Int {
        val days = list.asSequence()
            .mapNotNull { runCatching { LocalDate.parse(it.dayKey) }.getOrNull() }
            .toHashSet()
        var d = LocalDate.now(ZoneId.systemDefault())
        var streak = 0
        while (days.contains(d)) {
            streak++
            d = d.minusDays(1)
        }
        return streak
    }
}
