package com.habittracker.app.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class SmokeReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        NotificationHelper.showSmokeReadyNotification(applicationContext)
        return Result.success()
    }
}
