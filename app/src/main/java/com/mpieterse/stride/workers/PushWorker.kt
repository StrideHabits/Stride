package com.mpieterse.stride.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.mpieterse.stride.data.repo.CheckInRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class PushWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: CheckInRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = try {
        repo.pushBatch()
        Result.success()
    } catch (e: Exception) {
        Result.retry()
    }

    companion object {
        fun enqueueOnce(context: Context) {
            val req = OneTimeWorkRequestBuilder<PushWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueue(req)
        }
    }
}
