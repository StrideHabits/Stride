// com/mpieterse/stride/workers/PullWorker.kt
package com.mpieterse.stride.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.mpieterse.stride.core.utils.Clogger
import com.mpieterse.stride.data.repo.CheckInRepository
import com.mpieterse.stride.data.repo.HabitRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class PullWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val habits: HabitRepository,
    private val checkins: CheckInRepository
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return try {
            Clogger.d("PullWorker", "Starting pull sync from server")
            
            // Pull habits and check-ins from server
            val habitsSuccess = habits.pull()
            val checkinsSuccess = checkins.pull()
            
            if (habitsSuccess && checkinsSuccess) {
                Clogger.d("PullWorker", "Successfully pulled all data from server")
                Result.success()
            } else {
                Clogger.w("PullWorker", "Pull sync partially failed - habits: $habitsSuccess, checkins: $checkinsSuccess")
                // Return success anyway - partial failures are handled in repositories
                // and we don't want to retry immediately if one fails
                Result.success()
            }
        } catch (e: Exception) {
            Clogger.e("PullWorker", "Failed to pull data from server", e)
            // Return retry to allow WorkManager to retry with backoff
            Result.retry()
        }
    }

    companion object {
        private const val UNIQUE = "pull-sync"
        fun schedulePeriodic(context: Context) {
            val req = PeriodicWorkRequestBuilder<PullWorker>(15, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(UNIQUE, ExistingPeriodicWorkPolicy.KEEP, req)
        }
        fun enqueueOnce(context: Context) {
            val req = OneTimeWorkRequestBuilder<PullWorker>().build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(UNIQUE, ExistingWorkPolicy.KEEP, req)
        }
    }
}
