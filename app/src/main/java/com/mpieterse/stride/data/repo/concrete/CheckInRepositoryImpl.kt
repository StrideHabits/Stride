// app/src/main/java/com/mpieterse/stride/data/repo/concrete/CheckInRepositoryImpl.kt
package com.mpieterse.stride.data.repo.concrete

import android.content.Context
import androidx.room.withTransaction
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.mpieterse.stride.core.utils.checkInId
import com.mpieterse.stride.data.dto.checkins.CheckInCreateDto
import com.mpieterse.stride.data.dto.checkins.CheckInDto
import com.mpieterse.stride.data.local.db.AppDatabase
import com.mpieterse.stride.data.local.entities.CheckInEntity
import com.mpieterse.stride.data.local.entities.MutationEntity
import com.mpieterse.stride.data.local.entities.MutationOp
import com.mpieterse.stride.data.local.entities.MutationState
import com.mpieterse.stride.data.local.entities.SyncState
import com.mpieterse.stride.data.local.entities.TargetType
import com.mpieterse.stride.data.mappers.toDto
import com.mpieterse.stride.data.remote.SummitApiService
import com.mpieterse.stride.data.remote.models.PushItem
import com.mpieterse.stride.data.repo.CheckInRepository
import com.mpieterse.stride.workers.PullWorker
import com.mpieterse.stride.workers.PushWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckInRepositoryImpl @Inject constructor(
    private val api: SummitApiService,
    private val db: AppDatabase,
    @ApplicationContext private val appContext: Context
) : CheckInRepository {

    override fun observeForHabit(habitId: String): Flow<List<CheckInDto>> =
        db.checkIns().byHabit(habitId).map { list -> list.map { it.toDto() } }

    override fun observeById(id: String): Flow<CheckInDto?> =
        db.checkIns().observeEntityById(id).map { it?.toDto() }

    override suspend fun getForHabit(habitId: String, forceRemote: Boolean): List<CheckInDto> =
        db.checkIns().byHabit(habitId).first().map { it.toDto() }

    override suspend fun getById(id: String, forceRemote: Boolean): CheckInDto? =
        db.checkIns().getById(id)?.toDto()

    override suspend fun create(input: CheckInCreateDto): CheckInDto {
        val id = checkInId(input.habitId, input.dayKey)
        db.withTransaction {
            db.checkIns().upsert(
                CheckInEntity(
                    id = id,
                    habitId = input.habitId,
                    dayKey = input.dayKey,
                    completedAt = input.completedAt,
                    deleted = false,
                    updatedAt = "",
                    rowVersion = "",
                    syncState = SyncState.Pending
                )
            )
            db.mutations().insert(
                MutationEntity(
                    requestId   = java.util.UUID.randomUUID().toString(),
                    targetId    = id,
                    targetType  = TargetType.CheckIn,
                    op          = MutationOp.Update,
                    habitId     = input.habitId,
                    dayKey      = input.dayKey,
                    completedAt = input.completedAt,
                    deleted     = false,
                    baseVersion = null
                )
            )
        }
        enqueuePush()
        return CheckInDto(id, input.habitId, input.completedAt, input.dayKey)
    }

    override suspend fun upsertLocal(dto: CheckInDto) {
        db.checkIns().upsert(
            CheckInEntity(
                id = dto.id,
                habitId = dto.habitId,
                dayKey = dto.dayKey,
                completedAt = dto.completedAt,
                deleted = false,
                updatedAt = "",
                rowVersion = "",
                syncState = SyncState.Synced
            )
        )
    }

    override suspend fun delete(id: String) {
        val existing = db.checkIns().getById(id) ?: return
        db.withTransaction {
            db.checkIns().upsert(existing.copy(deleted = true, syncState = SyncState.Pending))
            db.mutations().insert(
                MutationEntity(
                    requestId   = java.util.UUID.randomUUID().toString(),
                    targetId    = id,
                    targetType  = TargetType.CheckIn,
                    op          = MutationOp.Delete,
                    habitId     = existing.habitId,
                    dayKey      = existing.dayKey,
                    completedAt = existing.completedAt,
                    deleted     = true,
                    baseVersion = existing.rowVersion.ifEmpty { null }
                )
            )
        }
        enqueuePush()
    }

    override suspend fun clearLocal() {
        db.checkIns().clearAll()
    }

    override suspend fun pushBatch(): Boolean = pushBatchInternal(100)

    private suspend fun pushBatchInternal(limit: Int): Boolean {
        val batch = db.mutations().nextBatch(limit)
        val checkins = batch.filter { it.targetType == TargetType.CheckIn }
        if (checkins.isEmpty()) return false

        val payload = checkins.map {
            PushItem(
                requestId   = it.requestId,
                id          = it.targetId,
                habitId     = it.habitId!!,
                dayKey      = it.dayKey!!,
                completedAt = it.completedAt!!,
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
                        db.checkIns().upsert(
                            cur.copy(
                                updatedAt = r.updatedAt,
                                rowVersion = r.rowVersion,
                                syncState = if (r.status.equals("applied", true)) SyncState.Synced else SyncState.Failed
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

    override suspend fun pull(): Boolean {
        PullWorker.enqueueOnce(appContext)
        return true
    }

    override suspend fun toggle(habitId: String, dayKey: String, on: Boolean) {
        val id = checkInId(habitId, dayKey)
        val completedAt = dayKey // keep your deterministic value; adjust if you store Instant.now()
        db.withTransaction {
            val current = db.checkIns().getById(id)
            db.checkIns().upsert(
                CheckInEntity(
                    id = id,
                    habitId = habitId,
                    dayKey = dayKey,
                    completedAt = completedAt,
                    deleted = !on,
                    updatedAt = "",
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
                    completedAt = completedAt,
                    deleted     = !on,
                    baseVersion = current?.rowVersion?.ifEmpty { null }
                )
            )
        }
        enqueuePush()
    }

    private fun enqueuePush() {
        val req = OneTimeWorkRequestBuilder<PushWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(appContext)
            .enqueueUniqueWork(PushWorker.UNIQUE_NAME, ExistingWorkPolicy.KEEP, req)
    }
}
