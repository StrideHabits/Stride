package com.mpieterse.stride.core.services

import android.content.Context
import android.util.Base64
import com.mpieterse.stride.core.net.ApiResult
import com.mpieterse.stride.core.utils.Clogger
import com.mpieterse.stride.data.dto.uploads.UploadResponse
import com.mpieterse.stride.data.local.PendingHabit
import com.mpieterse.stride.data.local.PendingHabitsStore
import com.mpieterse.stride.data.local.TokenStore
import com.mpieterse.stride.data.repo.HabitRepository
import com.mpieterse.stride.data.repo.UploadRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@Singleton
class HabitSyncManager @Inject constructor(
    private val pendingHabitsStore: PendingHabitsStore,
    private val habitRepository: HabitRepository,
    private val tokenStore: TokenStore,
    private val eventBus: AppEventBus,
    private val uploadRepository: UploadRepository,
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "HabitSyncManager"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun start() {
        scope.launch {
            tokenStore.tokenFlow.collectLatest { token ->
                if (!token.isNullOrBlank()) {
                    syncPendingHabits()
                }
            }
        }
    }

    private suspend fun syncPendingHabits() {
        val pending = pendingHabitsStore.getAll()
        if (pending.isEmpty()) return

        Clogger.d(TAG, "Syncing ${pending.size} pending habits")
        var anySynced = false

        for (habit in pending) {
            val imageUrlResult = resolveImageUrl(habit)
            when (imageUrlResult) {
                is ApiResult.Err -> {
                    Clogger.w(TAG, "Image upload failed for '${habit.name}': ${imageUrlResult.message}")
                    // If upload failed due to auth, stop and retry later.
                    if (imageUrlResult.code == 401 || imageUrlResult.code == 403) return
                    // For other errors, leave habit in queue and retry on next sync.
                    continue
                }
                is ApiResult.Ok -> {
                    val result = runCatching {
                        habitRepository.create(
                            habit.name,
                            habit.frequency,
                            habit.tag,
                            imageUrlResult.data
                        )
                    }.getOrElse { error ->
                        Clogger.e(TAG, "Failed to sync habit '${habit.name}'", error)
                        return
                    }

                    when (result) {
                        is ApiResult.Ok<*> -> {
                            pendingHabitsStore.remove(habit.id)
                            anySynced = true
                            Clogger.d(TAG, "Synced pending habit '${habit.name}'")
                        }
                        is ApiResult.Err -> {
                            Clogger.w(TAG, "Sync failed (${result.code}): ${result.message}")
                            if (result.code != null && result.code in 400..499 && result.code !in listOf(429, 401, 403)) {
                                pendingHabitsStore.remove(habit.id)
                                Clogger.w(TAG, "Dropped pending habit '${habit.name}' due to client error ${result.code}")
                            }
                            if (result.code == 401 || result.code == 403) {
                                return
                            }
                        }
                    }
                }
            }
        }

        if (anySynced) {
            eventBus.emit(AppEventBus.AppEvent.HabitCreated)
        }
    }

    private suspend fun resolveImageUrl(habit: PendingHabit): ApiResult<String?> {
        if (!habit.imageDataBase64.isNullOrBlank()) {
            val file = createTempImageFile(habit) ?: return ApiResult.Err(null, "Failed to prepare image")
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
        return ApiResult.Ok(habit.imageUrl)
    }

    private fun createTempImageFile(habit: PendingHabit): File? {
        return runCatching {
            val bytes = Base64.decode(habit.imageDataBase64, Base64.DEFAULT)
            val fileName = habit.imageFileName ?: "${UUID.randomUUID()}.jpg"
            val safeName = if (fileName.contains(".")) fileName else "$fileName.jpg"
            val file = File(context.cacheDir, "pending_habit_$safeName")
            FileOutputStream(file).use { it.write(bytes) }
            file
        }.getOrElse {
            Clogger.e(TAG, "Failed to create temp file for '${habit.name}'", it)
            null
        }
    }
}
