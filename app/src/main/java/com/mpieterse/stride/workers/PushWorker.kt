package com.mpieterse.stride.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.mpieterse.stride.data.repo.CheckInRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

@HiltWorker
class PushWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: CheckInRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = try {
        val pushed = repo.pushBatch() // return count or boolean
        // Optionally chain a small pull when we actually pushed something
        if (pushed) PullWorker.enqueueOnce(applicationContext)
        Result.success()
    } catch (e: HttpException) {
        if (e.code() == 401) Result.failure() else Result.retry()
    } catch (_: Exception) {
        Result.retry()
    }

    companion object {
        const val UNIQUE_NAME = "push-mutations"

        fun enqueueOnce(context: Context) {
            val req = OneTimeWorkRequestBuilder<PushWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_NAME,
                ExistingWorkPolicy.KEEP,
                req
            )
        }
    }
}
