// data/repo/CheckInRepository.kt
package com.mpieterse.stride.data.repo

import android.content.Context
import androidx.room.withTransaction
import androidx.work.*
import androidx.work.WorkManager
import com.mpieterse.stride.core.net.safeCall
import com.mpieterse.stride.data.dto.checkins.CheckInCreateDto
import com.mpieterse.stride.data.local.db.AppDatabase
import com.mpieterse.stride.data.local.entities.CheckInEntity
import com.mpieterse.stride.data.local.entities.MutationEntity
import com.mpieterse.stride.data.local.entities.SyncState
import com.mpieterse.stride.data.remote.SummitApiService
import com.mpieterse.stride.data.remote.models.PushItem
import com.mpieterse.stride.workers.PushWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckInRepository @Inject constructor(
    private val api: SummitApiService,
    private val db: AppDatabase,
    @ApplicationContext private val appContext: Context
) {
    // ---- Online endpoints kept for compatibility ----
    suspend fun listOnline() = safeCall { api.getCheckIns() }

    suspend fun createOnline(habitId: String, isoDate: String) = safeCall {
        val day = LocalDate.parse(isoDate.trim())            // yyyy-MM-dd
        val completedAt = ZonedDateTime.of(day.atStartOfDay(), ZoneOffset.UTC)
            .toInstant().toString()
        val dto = CheckInCreateDto(habitId.trim(), completedAt, day.toString())
        api.createCheckIn(dto)
    }

    // ---- Local source of truth + sync ----
    fun observe(habitId: String) = db.checkIns().byHabit(habitId)

    suspend fun toggle(habitId: String, dayKey: String, on: Boolean) {
        val id = UUID.randomUUID().toString()
        val now = Instant.now().toString()
        db.withTransaction {
            db.checkIns().upsert(
                CheckInEntity(
                    id = id,
                    habitId = habitId,
                    dayKey = dayKey,              // "yyyy-MM-dd"
                    completedAt = now,            // ISO-8601
                    deleted = !on,
                    updatedAt = now,              // temp until server reply
                    rowVersion = "",
                    syncState = SyncState.Pending
                )
            )
            db.mutations().insert(
                MutationEntity(
                    requestId = UUID.randomUUID().toString(),
                    checkInId = id,
                    habitId = habitId,
                    dayKey = dayKey,
                    completedAt = now,
                    deleted = !on,
                    baseVersion = null            // create
                )
            )
        }
        enqueuePush()
    }

    suspend fun pushBatch(limit: Int = 100): Boolean {
        val batch = db.mutations().nextBatch(limit)
        if (batch.isEmpty()) return false

        val payload = batch.map {
            PushItem(
                requestId = it.requestId,
                id = it.checkInId,
                habitId = it.habitId,
                dayKey = it.dayKey,
                completedAt = it.completedAt,
                deleted = it.deleted,
                baseVersion = it.baseVersion
            )
        }

        val res = api.syncPush(payload)

        db.withTransaction {
            res.forEach { r ->
                val cur = db.checkIns().getById(r.id) ?: return@forEach
                val state = if (r.status == "applied") SyncState.Synced else SyncState.Failed
                db.checkIns().upsert(
                    cur.copy(
                        updatedAt = r.updatedAt,
                        rowVersion = r.rowVersion,
                        syncState = state
                    )
                )
            }
            db.mutations().delete(batch.map { it.requestId })
        }
        return true
    }

    private fun enqueuePush() {
        val req = OneTimeWorkRequestBuilder<PushWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(appContext).enqueue(req)
    }
}
