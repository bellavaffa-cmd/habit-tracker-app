package com.habittracker.app.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/** Schedules (approximate, WorkManager-backed) the "you can smoke now" notification. */
object SmokingReminderScheduler {
    private const val WORK_NAME = "smoke_reminder_work"

    fun scheduleIn(context: Context, delayMillis: Long) {
        if (delayMillis <= 0) {
            cancel(context)
            NotificationHelper.showSmokeReadyNotification(context)
            return
        }
        val request = OneTimeWorkRequestBuilder<SmokeReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
