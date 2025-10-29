package com.mpieterse.stride.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.mpieterse.stride.data.local.SyncPrefs
import com.mpieterse.stride.data.local.db.AppDatabase
import com.mpieterse.stride.data.local.entities.CheckInEntity
import com.mpieterse.stride.data.remote.SummitApiService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import androidx.room.withTransaction

@HiltWorker
class PullWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val api: SummitApiService,
    private val db: AppDatabase
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            var since = SyncPrefs.getSince(applicationContext)
            do {
                val page = api.syncChanges(since)
                db.withTransaction {
                    page.items.forEach { c ->
                        db.checkIns().upsert(
                            CheckInEntity(
                                id = c.id, habitId = c.habitId, dayKey = c.dayKey,
                                completedAt = c.completedAt, deleted = c.deleted,
                                updatedAt = c.updatedAt, rowVersion = c.rowVersion
                            )
                        )
                    }
                }
                since = page.nextSince
                SyncPrefs.setSince(applicationContext, since)
            } while (page.hasMore)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        fun schedulePeriodic(context: Context) {
            val req = PeriodicWorkRequestBuilder<PullWorker>(15, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                ).build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork("pull-checkins", ExistingPeriodicWorkPolicy.UPDATE, req)
        }
    }
}
