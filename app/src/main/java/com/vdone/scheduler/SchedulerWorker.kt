package com.vdone.scheduler

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.vdone.VDoneApp
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class SchedulerWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = (applicationContext as VDoneApp).taskRepository
        val frequencyTasks = repository.getFrequencyTasks().first()

        frequencyTasks.forEach { task ->
            // Reset status to "todo" if a new period has started since last completion
            if (task.lastCompletedAt != null && !FrequencyChecker.isDueToday(task)) return@forEach
            // Nothing to do here yet — reminders come in M4/M5.
            // Worker presence ensures WorkManager is scheduled and ready.
        }

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "vdone_scheduler"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<SchedulerWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES,
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
