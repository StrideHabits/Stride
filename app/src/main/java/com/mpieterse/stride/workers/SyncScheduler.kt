package com.mpieterse.stride.workers

import android.content.Context

object SyncScheduler {
    fun onLocalWrite(context: Context) {
        PushWorker.enqueueOnce(context) // push first, pull will chain if needed
    }
}
