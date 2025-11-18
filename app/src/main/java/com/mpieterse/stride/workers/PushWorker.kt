package com.mpieterse.stride.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
//import androidx.work.Result
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.mpieterse.stride.data.repo.CheckInRepository
import com.mpieterse.stride.data.repo.HabitRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

@HiltWorker
class PushWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val habits: HabitRepository,
    private val checkins: CheckInRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = try {
        var anyApplied = false

        // push habit creates first
        anyApplied = habits.pushHabitCreates() || anyApplied
        anyApplied = checkins.pushBatch() || anyApplied

        if (anyApplied) PullWorker.enqueueOnce(applicationContext)
        Result.success()
    } catch (e: HttpException) {
        // surface HTTP details to Debug UI
        val data = workDataOf("error" to "HTTP ${e.code()} ${e.message()}")
        Result.failure(data)
    } catch (e: Exception) {
        // surface message; switch to retry() later if desired
        val data = workDataOf("error" to (e.message ?: e::class.java.simpleName))
        Result.failure(data)
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
