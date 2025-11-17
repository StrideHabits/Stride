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
import com.mpieterse.stride.core.utils.Clogger
import com.mpieterse.stride.data.mappers.toEntity
import com.mpieterse.stride.data.repo.CheckInRepository
import com.mpieterse.stride.workers.PushWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
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
        // Filter for CheckIn mutations and ensure required fields are present
        val checkins = batch.filter { 
            it.targetType == TargetType.CheckIn && 
            it.habitId != null && 
            it.dayKey != null && 
            it.completedAt != null
        }
        if (checkins.isEmpty()) return false

        val mutationsByTargetId = checkins.associateBy { it.targetId }
        val payload = checkins.map { mutation ->
            PushItem(
                requestId   = mutation.requestId,
                id          = mutation.targetId,
                habitId     = mutation.habitId!!,
                dayKey      = mutation.dayKey!!,
                completedAt = mutation.completedAt!!,
                deleted     = mutation.deleted,
                baseVersion = mutation.baseVersion
            )
        }

        return try {
            val results = api.syncPush(payload)
            // Room's withTransaction ensures atomicity - if any operation fails, entire transaction rolls back
            try {
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
                        mutationsByTargetId[r.id]?.let { m ->
                            if (r.status.equals("applied", true)) applied += m.localId else failed += m.localId
                        }
                    }
                    if (applied.isNotEmpty()) db.mutations().markApplied(applied)
                    if (failed.isNotEmpty()) db.mutations().mark(failed, MutationState.Failed, "conflict")
                }
                true
            } catch (dbException: Exception) {
                // Transaction failed - Room automatically rolled back, but mark mutations as failed for retry
                Clogger.e("CheckInRepository", "Database transaction failed during push sync, mutations will be retried", dbException)
                db.mutations().mark(checkins.map { it.localId }, MutationState.Failed, "Database error: ${dbException.message}")
                false
            }
        } catch (httpException: retrofit2.HttpException) {
            // HTTP errors - mark mutations as failed for retry
            Clogger.e("CheckInRepository", "HTTP error during push sync (${httpException.code()}): ${httpException.message()}")
            db.mutations().mark(checkins.map { it.localId }, MutationState.Failed, "HTTP ${httpException.code()}: ${httpException.message()}")
            false
        } catch (t: Exception) {
            if (t is CancellationException) throw t
            // Network errors or other exceptions - mark mutations as failed for retry
            Clogger.e("CheckInRepository", "Error during push sync, mutations will be retried", t)
            db.mutations().mark(checkins.map { it.localId }, MutationState.Failed, t.message)
            false
        }
    }

    override suspend fun pull(): Boolean {
        return try {
            Clogger.d("CheckInRepository", "Pulling check-ins from server")
            // Get all check-ins from server (could use syncChanges for incremental sync in future)
            val response = api.getCheckIns()
            
            if (response.isSuccessful) {
                val remoteCheckIns = response.body() ?: emptyList()
                val protectedIds = db.mutations().targetIdsWithStates(
                    TargetType.CheckIn,
                    listOf(MutationState.Pending, MutationState.Failed)
                ).toHashSet()

                val checkInsToSave = remoteCheckIns
                    .filterNot { protectedIds.contains(it.id) }
                    .map { dto -> dto.toEntity(syncState = SyncState.Synced) }

                val skipped = remoteCheckIns.size - checkInsToSave.size

                db.withTransaction {
                    if (checkInsToSave.isNotEmpty()) {
                        db.checkIns().upsertAll(checkInsToSave)
                    }
                }

                if (skipped > 0) {
                    Clogger.w("CheckInRepository", "Skipped $skipped remote check-ins due to local pending mutations")
                }
                Clogger.d("CheckInRepository", "Successfully pulled ${remoteCheckIns.size} check-ins from server")
                true
            } else {
                Clogger.e("CheckInRepository", "Failed to pull check-ins: HTTP ${response.code()} ${response.message()}")
                false
            }
        } catch (e: Exception) {
            Clogger.e("CheckInRepository", "Failed to pull check-ins", e)
            false
        }
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
