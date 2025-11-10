package com.mpieterse.stride.data.repo

import android.content.Context
import androidx.room.withTransaction
import androidx.work.*
import com.mpieterse.stride.core.utils.checkInId
import com.mpieterse.stride.data.local.db.AppDatabase
import com.mpieterse.stride.data.local.entities.*
import com.mpieterse.stride.data.remote.SummitApiService
import com.mpieterse.stride.data.remote.models.PushItem
import com.mpieterse.stride.workers.PullWorker
import com.mpieterse.stride.workers.PushWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckInRepository @Inject constructor(
    private val api: SummitApiService,
    private val db: AppDatabase,
    @ApplicationContext private val appContext: Context
) {
    fun observe(habitId: String) = db.checkIns().byHabit(habitId)

    suspend fun toggle(habitId: String, dayKey: String, on: Boolean) {
        val id = checkInId(habitId, dayKey)        // deterministic, idempotent
        val now = Instant.now().toString()         // ISO-8601 Z

        db.withTransaction {
            val current = db.checkIns().getById(id)
            db.checkIns().upsert(
                CheckInEntity(
                    id = id,
                    habitId = habitId,
                    dayKey = dayKey,
                    completedAt = now,
                    deleted = !on,
                    updatedAt = "",                // server sets
                    rowVersion = current?.rowVersion ?: "",
                    syncState = SyncState.Pending
                )
            )
            db.mutations().insert(
                MutationEntity(
                    requestId   = java.util.UUID.randomUUID().toString(),
                    targetId    = id,
                    targetType  = TargetType.CheckIn,
                    op          = if (on) MutationOp.Update else MutationOp.Delete,
                    habitId     = habitId,
                    dayKey      = dayKey,
                    completedAt = now,            // never blank
                    deleted     = !on,
                    baseVersion = current?.rowVersion?.ifEmpty { null }
                )
            )
        }
        enqueuePush()
    }

    // Called by PushWorker
    suspend fun pushBatch(limit: Int = 100): Boolean {
        val batch = db.mutations().nextBatch(limit)
        if (batch.isEmpty()) return false

        val checkins = batch.filter { it.targetType == TargetType.CheckIn }
        if (checkins.isEmpty()) return false

        val payload = checkins.map {
            PushItem(
                requestId   = it.requestId,
                id          = it.targetId,
                habitId     = it.habitId ?: error("habitId required"),
                dayKey      = it.dayKey ?: error("dayKey required"),
                completedAt = it.completedAt ?: error("completedAt required"),
                deleted     = it.deleted,
                baseVersion = it.baseVersion
            )
        }

        return try {
            val results = api.syncPush(payload)
            db.withTransaction {
                val applied = mutableListOf<Long>()
                val failed  = mutableListOf<Long>()

                results.forEach { r ->
                    db.checkIns().getById(r.id)?.let { cur ->
                        val state = if (r.status.equals("applied", true)) SyncState.Synced else SyncState.Failed
                        db.checkIns().upsert(
                            cur.copy(
                                updatedAt = r.updatedAt,
                                rowVersion = r.rowVersion,
                                syncState = state
                            )
                        )
                    }
                    checkins.find { it.targetId == r.id }?.let { m ->
                        if (r.status.equals("applied", true)) applied += m.localId else failed += m.localId
                    }
                }
                if (applied.isNotEmpty()) db.mutations().markApplied(applied)
                if (failed.isNotEmpty()) db.mutations().mark(failed, MutationState.Failed, "conflict")
            }
            true
        } catch (t: Throwable) {
            db.mutations().mark(checkins.map { it.localId }, MutationState.Failed, t.message)
            false
        }
    }

    private fun enqueuePush() {
        val req = OneTimeWorkRequestBuilder<PushWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(appContext).enqueueUniqueWork(PushWorker.UNIQUE_NAME, ExistingWorkPolicy.KEEP, req)
    }

    fun pullOnce() = PullWorker.enqueueOnce(appContext)
}
