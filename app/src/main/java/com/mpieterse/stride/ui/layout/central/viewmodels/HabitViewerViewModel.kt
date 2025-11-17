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
import com.mpieterse.stride.data.dto.habits.HabitCreateDto
import com.mpieterse.stride.data.dto.habits.HabitDto
import com.mpieterse.stride.data.repo.CheckInRepository
import com.mpieterse.stride.data.repo.HabitRepository
import com.mpieterse.stride.data.repo.concrete.UploadRepository
import com.mpieterse.stride.R
import com.mpieterse.stride.ui.layout.central.components.HabitData
import com.mpieterse.stride.utils.base64ToBitmap
import retrofit2.HttpException
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
import kotlinx.coroutines.delay
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
        val completedDates: List<Int> = emptyList(),
        val selectedYear: Int = LocalDate.now().year,
        val selectedMonth: Int = LocalDate.now().monthValue
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    private var lastHabit: HabitDto? = null
    private var lastImageUrl: String? = null

    private var observeJob: Job? = null

    fun load(habitId: String) {
        _state.update { 
            it.copy(
                loading = true, 
                error = null, 
                habitId = habitId,
                selectedYear = LocalDate.now().year,
                selectedMonth = LocalDate.now().monthValue
            ) 
        }

        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            combine(
                habits.observeById(habitId)
                    .distinctUntilChanged(),
                checkins.observeForHabit(habitId)
                    .map { list -> list } // DTOs already
                    .onStart { emit(emptyList()) }
                    .catch { emit(emptyList()) },
                _state.map { Pair(it.selectedYear, it.selectedMonth) }
                    .distinctUntilChanged()
            ) { habitDto, localCheckins, selectedMonthYear ->
                val habitName = habitDto?.name ?: appContext.getString(R.string.habit_viewer_unknown_habit)
                val display = nameOverrideService.getDisplayName(habitId, habitName)
                val (selectedYear, selectedMonth) = selectedMonthYear
                val completedThisMonth = monthDays(localCheckins, selectedYear, selectedMonth)
                val streak = computeStreakDays(localCheckins)
                Pair(habitDto, Triple(habitName, display, Pair(completedThisMonth, streak)))
            }
                .onStart { _state.update { it.copy(loading = true) } }
                .collect { (habitDto, triple) ->
                    val (habitName, display, pair) = triple
                    val (days, streak) = pair
                    val sanitizedUrl = habitDto?.imageUrl?.let { sanitizeImageUrl(it) }
                    // Preserve existing image if URL hasn't changed, otherwise fetch new one
                    val imageBitmap = sanitizedUrl?.takeIf { it.isNotBlank() }?.let { url ->
                        if (url != lastImageUrl || _state.value.habitImage == null) {
                            fetchBitmap(url)?.also { lastImageUrl = url }
                        } else {
                            // Preserve existing image if URL hasn't changed
                            _state.value.habitImage
                        }
                    } ?: if (sanitizedUrl == null && _state.value.habitImage != null) {
                        // If URL is null but we have an existing image, preserve it (might have been deleted on server)
                        _state.value.habitImage
                    } else {
                        null
                    }

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

    fun navigateMonth(forward: Boolean) {
        val current = LocalDate.of(_state.value.selectedYear, _state.value.selectedMonth, 1)
        val newDate = if (forward) current.plusMonths(1) else current.minusMonths(1)
        _state.update {
            it.copy(selectedYear = newDate.year, selectedMonth = newDate.monthValue)
        }
    }

    fun navigateYear(forward: Boolean) {
        val current = LocalDate.of(_state.value.selectedYear, _state.value.selectedMonth, 1)
        val newDate = if (forward) current.plusYears(1) else current.minusYears(1)
        _state.update {
            it.copy(selectedYear = newDate.year, selectedMonth = newDate.monthValue)
        }
    }

    fun goToToday() {
        val today = LocalDate.now()
        _state.update {
            it.copy(selectedYear = today.year, selectedMonth = today.monthValue)
        }
    }

    fun completeToday(habitId: String) = toggleCheckIn(habitId, LocalDate.now().toString())

    fun toggleCheckIn(habitId: String, isoDate: String) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                // Get current check-ins for this habit (only active ones from observeForHabit)
                val activeCheckIns = checkins.observeForHabit(habitId).firstOrNull().orEmpty()
                val existing = activeCheckIns.firstOrNull { it.dayKey == isoDate }
                // Toggle: if exists and is active, turn off; if doesn't exist, turn on
                // The toggle() method handles marking deleted=false when on, deleted=true when off
                // This preserves check-ins unless user explicitly unchecks them
                val shouldBeOn = existing == null
                checkins.toggle(habitId, isoDate, on = shouldBeOn)
                eventBus.emit(AppEventBus.AppEvent.CheckInCompleted)
                _state.update { it.copy(loading = false) }
            } catch (e: Exception) {
                Clogger.e("HabitViewerViewModel", "Failed to toggle check-in", e)
                _state.update { it.copy(loading = false, error = appContext.getString(R.string.error_write_failed)) }
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
            _state.update { it.copy(loading = true, error = null) }
            
            // Upload image if provided - but don't block update if it fails
            var uploadedUrl: String? = null
            var imageUploadFailed = false
            var imageUploadError: String? = null
            
            if (!updated.imageBase64.isNullOrBlank()) {
                try {
                    // Safe to use imageBase64 here - already checked with isNullOrBlank()
                    val imageBase64 = updated.imageBase64
                    if (imageBase64 != null) {
                        uploadedUrl = uploadImage(imageBase64, updated.imageMimeType)
                    }
                    if (uploadedUrl.isNullOrBlank()) {
                        imageUploadFailed = true
                        imageUploadError = appContext.getString(R.string.error_image_upload_failed)
                        Clogger.w("HabitViewerViewModel", "Image upload failed, continuing with existing image or no image")
                    }
                } catch (e: Exception) {
                    imageUploadFailed = true
                    val isNetworkError = e is java.net.SocketTimeoutException ||
                        e is java.net.ConnectException ||
                        (e is java.io.IOException && (
                            e.message?.lowercase()?.contains("timeout") == true ||
                            e.message?.lowercase()?.contains("network") == true ||
                            e.message?.lowercase()?.contains("connection") == true
                        ))
                    imageUploadError = if (isNetworkError) {
                        appContext.getString(R.string.error_image_upload_network)
                    } else {
                        appContext.getString(R.string.error_image_upload_generic, e.message ?: appContext.getString(R.string.error_unknown))
                    }
                    Clogger.e("HabitViewerViewModel", "Image upload exception, continuing with existing image or no image", e)
                }
            }
            
            // If image upload failed or no new image provided, use existing image URL
            // Only update imageUrl if a new image was uploaded, otherwise preserve existing URL
            val finalImageUrl = when {
                !updated.imageBase64.isNullOrBlank() && !imageUploadFailed -> uploadedUrl
                !updated.imageBase64.isNullOrBlank() && imageUploadFailed -> sanitizeImageUrl(current.imageUrl)
                else -> sanitizeImageUrl(current.imageUrl) // Preserve existing image when no new image is provided
            }
            
            // Update via API (which also caches locally) - always include imageUrl, even if null (to preserve existing)
            val updatedDto = habits.update(
                id = current.id,
                input = HabitCreateDto(
                    name = updated.name.trim(),
                    frequency = updated.frequency.coerceIn(1, 7),
                    tag = updated.tag?.trim().takeUnless { it.isNullOrEmpty() },
                    imageUrl = finalImageUrl // Include even if null - API should handle this
                )
            )
            
            // Update local state
            lastHabit = updatedDto
            lastImageUrl = finalImageUrl
            val updatedBitmap = when {
                !updated.imageBase64.isNullOrBlank() && !imageUploadFailed -> base64ToBitmap(updated.imageBase64)
                !finalImageUrl.isNullOrBlank() -> fetchBitmap(finalImageUrl)
                else -> null
            } ?: _state.value.habitImage
            _state.update { 
                it.copy(
                    habitImage = updatedBitmap,
                    loading = false,
                    displayName = updatedDto.name,
                    error = if (imageUploadFailed && imageUploadError != null) imageUploadError else null
                ) 
            }
            eventBus.emit(AppEventBus.AppEvent.HabitUpdated)
            Clogger.d("HabitViewerViewModel", "Successfully updated habit ${current.id} on server${if (imageUploadFailed) " (image upload failed)" else ""}")
            
            // Show image upload error if it occurred (update state with specific error message)
            if (imageUploadFailed && imageUploadError != null) {
                _state.update { 
                    it.copy(
                        error = imageUploadError,
                        loading = false
                    ) 
                }
                // Clear error after a delay
                launch {
                    delay(5000)
                    _state.update { it.copy(error = null) }
                }
            }
        } catch (e: HttpException) {
            // Handle HTTP errors more gracefully
            val errorMessage = when (e.code()) {
                404 -> appContext.getString(R.string.error_habit_not_found)
                400 -> appContext.getString(R.string.error_habit_invalid_data)
                401, 403 -> appContext.getString(R.string.error_sign_in_again)
                500 -> appContext.getString(R.string.error_server_error)
                else -> appContext.getString(R.string.error_habit_update_failed, e.message() ?: appContext.getString(R.string.error_unknown))
            }
            Clogger.e("HabitViewerViewModel", "Failed to update habit: HTTP ${e.code()}", e)
            _state.update { 
                it.copy(
                    loading = false,
                    error = errorMessage
                ) 
            }
        } catch (e: Exception) {
            Clogger.e("HabitViewerViewModel", "Failed to update habit", e)
            _state.update { 
                it.copy(
                    loading = false,
                    error = appContext.getString(R.string.error_habit_update_failed, e.message ?: appContext.getString(R.string.error_unknown))
                ) 
            }
        }
    }

    private suspend fun fetchBitmap(location: String): Bitmap? =
        withContext(Dispatchers.IO) {
            return@withContext try {
                if (location.startsWith("http", ignoreCase = true)) {
                    // Remote HTTP/HTTPS image (legacy / old data)
                    (URL(location).openConnection()).getInputStream().use { input ->
                        BitmapFactory.decodeStream(input)
                    }
                } else {
                    // Local file path
                    val file = File(location)
                    if (file.exists()) {
                        BitmapFactory.decodeFile(file.absolutePath)
                    } else {
                        Clogger.d("HabitViewerViewModel", "Local image not found at $location")
                        null
                    }
                }
            } catch (e: Exception) {
                if (location.startsWith("http", ignoreCase = true) && e is java.io.FileNotFoundException) {
                    Clogger.d("HabitViewerViewModel", "Remote image not found at $location (likely deleted)")
                } else {
                    Clogger.e("HabitViewerViewModel", "Failed to load image from $location", e)
                }
                null
            }
        }


    private suspend fun uploadImage(base64: String, mimeType: String?): String? =
        withContext(Dispatchers.IO) {
            val extension = when (mimeType) {
                "image/png" -> ".png"
                else -> ".jpg"
            }
            val tempFile = File.createTempFile("habit-", extension, appContext.cacheDir)
            return@withContext try {
                val bytes = Base64.decode(base64, Base64.DEFAULT)
                tempFile.writeBytes(bytes)
                when (val result = uploadRepo.upload(tempFile.path)) {
                    is ApiResult.Ok -> result.data.localPath   // LOCAL PATH NOW
                    is ApiResult.Err -> {
                        Clogger.e("HabitViewerViewModel", "Image save failed: ${result.message}")
                        null
                    }
                }
            } catch (e: Exception) {
                Clogger.e("HabitViewerViewModel", "Image save exception", e)
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

    private fun monthDays(list: List<CheckInDto>, year: Int, month: Int): List<Int> {
        val monthStart = LocalDate.of(year, month, 1)
        return list.asSequence()
            .mapNotNull { runCatching { LocalDate.parse(it.dayKey) }.getOrNull() }
            .filter { it.year == monthStart.year && it.month == monthStart.month }
            .map { it.dayOfMonth }
            .distinct()
            .sorted()
            .toList()
    }

    private fun computeStreakDays(list: List<CheckInDto>): Int {
        // The DAO query already filters out deleted check-ins (WHERE deleted=0)
        // So we don't need to filter here - all items in list are active
        val days = list.asSequence()
            .mapNotNull { runCatching { LocalDate.parse(it.dayKey) }.getOrNull() }
            .toHashSet()
        
        if (days.isEmpty()) return 0
        
        // Get today's date in the system timezone
        val today = LocalDate.now(ZoneId.systemDefault())
        
        // Count consecutive days backwards from today
        // If today is checked, include it. If not, start from yesterday.
        var currentDate = if (days.contains(today)) today else today.minusDays(1)
        var streak = 0
        
        // Count consecutive days backwards until we find a gap
        // This ensures streaks are persistent and only break when user explicitly unchecks
        while (days.contains(currentDate)) {
            streak++
            currentDate = currentDate.minusDays(1)
            // Prevent infinite loop (safety check - shouldn't happen in practice)
            if (streak > 10000) break
        }
        
        return streak
    }
}
