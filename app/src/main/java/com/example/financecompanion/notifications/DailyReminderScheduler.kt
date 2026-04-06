package com.example.financecompanion.notifications

import android.content.Context
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.financecompanion.dev.DeveloperOptions
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.random.Random

object DailyReminderScheduler {
    private const val UNIQUE_WORK_NAME = "daily_finance_reminder"
    private const val TEST_WORK_NAME = "daily_finance_reminder_test"
    private const val START_HOUR = 9
    private const val END_HOUR = 21
    private const val TAG = "DailyReminderScheduler"

    fun schedule(context: Context) {
        ReminderNotificationHelper.ensureChannel(context)
        val delay = calculateDelayMillis()
        val request = OneTimeWorkRequestBuilder<DailyReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            if (DeveloperOptions.useFastReminderSchedule) {
                ExistingWorkPolicy.REPLACE
            } else {
                ExistingWorkPolicy.KEEP
            },
            request
        )
        Log.d(TAG, "Reminder work enqueued with delayMillis=$delay")
    }

    fun triggerNow(context: Context) {
        ReminderNotificationHelper.ensureChannel(context)
        val request = OneTimeWorkRequestBuilder<DailyReminderWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            TEST_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
        Log.d(TAG, "Immediate reminder test work enqueued")
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
        WorkManager.getInstance(context).cancelUniqueWork(TEST_WORK_NAME)
        Log.d(TAG, "Reminder work cancelled")
    }

    private fun calculateDelayMillis(nowMillis: Long = System.currentTimeMillis()): Long {
        if (DeveloperOptions.useFastReminderSchedule) {
            return Random.nextLong(
                TimeUnit.MINUTES.toMillis(1),
                TimeUnit.MINUTES.toMillis(2) + 1
            )
        }

        val now = Calendar.getInstance().apply {
            timeInMillis = nowMillis
        }
        val earliest = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            add(Calendar.MINUTE, 5)
        }
        val target = Calendar.getInstance().apply {
            timeInMillis = nowMillis
        }

        val windowStart = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, START_HOUR)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val windowEnd = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, END_HOUR)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (now.after(windowEnd)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
            target.set(Calendar.HOUR_OF_DAY, START_HOUR)
            target.set(Calendar.MINUTE, 0)
        } else {
            target.timeInMillis = maxOf(windowStart.timeInMillis, earliest.timeInMillis)
        }

        val targetDayEnd = Calendar.getInstance().apply {
            timeInMillis = target.timeInMillis
            set(Calendar.HOUR_OF_DAY, END_HOUR)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val startMillis = target.timeInMillis
        val endMillis = maxOf(targetDayEnd.timeInMillis, startMillis + TimeUnit.MINUTES.toMillis(1))
        val randomMillis = if (endMillis > startMillis) {
            Random.nextLong(startMillis, endMillis)
        } else {
            startMillis
        }

        return (randomMillis - nowMillis).coerceAtLeast(TimeUnit.MINUTES.toMillis(1))
    }
}
