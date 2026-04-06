package com.example.financecompanion.notifications

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.financecompanion.data.local.prefrences.PreferenceManager
import kotlinx.coroutines.flow.first

class DailyReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val preferenceManager = PreferenceManager(applicationContext)
        val settings = preferenceManager.appSettings.first()
        Log.d(TAG, "Worker started. notificationsEnabled=${settings.notificationsEnabled}")

        if (!settings.notificationsEnabled) {
            Log.d(TAG, "Worker exiting because reminders are disabled")
            return Result.success()
        }

        val shown = ReminderNotificationHelper.showRandomReminder(applicationContext)
        Log.d(TAG, "Reminder notification shown=$shown")
        DailyReminderScheduler.schedule(applicationContext)
        return Result.success()
    }

    companion object {
        private const val TAG = "DailyReminderWorker"
    }
}
