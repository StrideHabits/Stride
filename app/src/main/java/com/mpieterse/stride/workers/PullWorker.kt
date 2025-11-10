package com.mpieterse.stride.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.room.withTransaction
import androidx.work.*
import com.mpieterse.stride.data.local.SyncPrefs
import com.mpieterse.stride.data.local.db.AppDatabase
import com.mpieterse.stride.data.local.entities.CheckInEntity
import com.mpieterse.stride.data.remote.SummitApiService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

@HiltWorker
class PullWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val api: SummitApiService,
    private val db: AppDatabase
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        var since: String? = SyncPrefs.getSince(applicationContext)
        try {
            var hasMore = true
            while (hasMore) {
                val page = api.syncChanges(since)

                db.withTransaction {
                    // bulk upsert
                    page.items.forEach { c ->
                        db.checkIns().upsert(
                            CheckInEntity(
                                id = c.id,
                                habitId = c.habitId,
                                dayKey = c.dayKey,
                                completedAt = c.completedAt,
                                deleted = c.deleted,
                                updatedAt = c.updatedAt,
                                rowVersion = c.rowVersion
                            )
                        )
                    }
                }

                // advance cursor to last item’s UpdatedAt; keep if server returns null
                since = page.nextSince ?: since
                SyncPrefs.setSince(applicationContext, since)
                hasMore = page.hasMore
            }
            Result.success()
        } catch (e: HttpException) {
            if (e.code() == 401) Result.failure() else Result.retry()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val UNIQUE_NAME = "pull-checkins"

        fun schedulePeriodic(context: Context) {
            val req = PeriodicWorkRequestBuilder<PullWorker>(15, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                req
            )
        }

        fun enqueueOnce(context: Context) {
            val req = OneTimeWorkRequestBuilder<PullWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_NAME + "-once",
                ExistingWorkPolicy.KEEP,
                req
            )
        }
    }
}
