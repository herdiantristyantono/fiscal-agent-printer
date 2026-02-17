package com.android.fiscalagenttixtax.queue

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class PrintRetryWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        return Result.success()
    }
}