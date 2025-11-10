package com.mpieterse.stride.data.repo

import android.content.Context
import androidx.room.withTransaction
import androidx.work.*
import com.mpieterse.stride.data.dto.habits.HabitCreateDto
import com.mpieterse.stride.data.local.db.AppDatabase
import com.mpieterse.stride.data.local.entities.*
import com.mpieterse.stride.data.remote.SummitApiService
import com.mpieterse.stride.workers.PushWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepository @Inject constructor(
    private val api: SummitApiService,
    private val db: AppDatabase,
    @ApplicationContext private val appContext: Context
) {
    fun observeAll() = db.habits().all()

    suspend fun createLocal(
        name: String,
        frequency: Int = 0,
        tag: String? = null,
        imageUrl: String? = null,
        clientId: String = UUID.randomUUID().toString()
    ) {
        val now = Instant.now().toString()

        db.withTransaction {
            db.habits().upsert(
                HabitEntity(
                    id = clientId,
                    name = name,
                    frequency = frequency,
                    tag = tag,
                    imageUrl = imageUrl,
                    deleted = false,
                    createdAt = now,
                    updatedAt = "",
                    rowVersion = "",
                    syncState = SyncState.Pending
                )
            )
            db.mutations().insert(
                MutationEntity(
                    requestId  = UUID.randomUUID().toString(),
                    targetId   = clientId,
                    targetType = TargetType.Habit,
                    op         = MutationOp.Create,
                    name       = name,
                    frequency  = frequency,
                    tag        = tag,
                    imageUrl   = imageUrl,
                    baseVersion = null
                )
            )
        }
        enqueuePush()
    }

    /**
     * Push pending habit creates. Server allocates a new Guid.
     * We remap local temp id -> server id and update references.
     *
     * Requires DAO helpers:
     *  - HabitDao.hardDelete(id: String)
     *  - HabitDao.upsert(...)
     *  - CheckInDao: @Query("UPDATE check_ins SET habitId=:newId WHERE habitId=:oldId")
     *  - MutationDao: @Query("UPDATE mutations SET habitId=:newId, targetId=CASE WHEN targetType='Habit' AND targetId=:oldId THEN :newId ELSE targetId END WHERE habitId=:oldId OR (targetType='Habit' AND targetId=:oldId)")
     */
    // repos/HabitRepository.kt
    suspend fun pushHabitCreates(limit: Int = 50): Boolean {
        val batch = db.mutations().nextBatch(limit).filter { it.targetType == TargetType.Habit }
        if (batch.isEmpty()) return false

        return try {
            db.withTransaction {
                val applied = mutableListOf<Long>()
                batch.forEach { m ->
                    val resp = api.createHabit(
                        HabitCreateDto(
                            name = m.name ?: error("name"),
                            frequency = m.frequency ?: 0,
                            tag = m.tag,
                            imageUrl = m.imageUrl
                        )
                    )
                    if (!resp.isSuccessful) {
                        val body = resp.errorBody()?.string()?.take(400)
                        throw IllegalStateException("POST /api/habits → ${resp.code()} ${resp.message()} ${body ?: ""}")
                    }
                    val dto = resp.body() ?: error("empty body")
                    db.habits().upsert(
                        HabitEntity(
                            id = dto.id,
                            name = dto.name,
                            frequency = dto.frequency,
                            tag = dto.tag,
                            imageUrl = dto.imageUrl,
                            deleted = false,
                            createdAt = dto.createdAt,
                            updatedAt = dto.updatedAt,
                            rowVersion = "",
                            syncState = SyncState.Synced
                        )
                    )
                    applied += m.localId
                }
                if (applied.isNotEmpty()) db.mutations().markApplied(applied)
            }
            true
        } catch (t: Throwable) {
            db.mutations().mark(batch.map { it.localId }, MutationState.Failed, t.message)
            throw t  // let PushWorker capture and surface it
        }
    }



    private fun AppDatabase.runQuery(sql: String, args: Array<Any?>) {
        this.openHelper.writableDatabase.execSQL(sql, args)
    }

    private fun enqueuePush() {
        val req = OneTimeWorkRequestBuilder<PushWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(appContext).enqueueUniqueWork(PushWorker.UNIQUE_NAME, ExistingWorkPolicy.KEEP, req)
    }
}
