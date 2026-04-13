package com.vdone

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.room.Room
import com.vdone.data.db.AppDatabase
import com.vdone.data.db.MIGRATION_1_2
import com.vdone.data.db.MIGRATION_2_3
import com.vdone.data.db.MIGRATION_3_4
import com.vdone.data.db.MIGRATION_4_5
import com.vdone.data.db.MIGRATION_5_6
import com.vdone.data.db.MIGRATION_6_7
import com.vdone.data.repository.ConditionRepository
import com.vdone.data.repository.TaskRepository
import com.vdone.reminder.AlarmScheduler
import com.vdone.scheduler.SchedulerWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class VDoneApp : Application() {

    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "vdone.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
            .build()
    }

    val taskRepository: TaskRepository by lazy {
        TaskRepository(database.taskDao(), this)
    }

    val conditionRepository: ConditionRepository by lazy {
        ConditionRepository(database.conditionDao())
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        SchedulerWorker.schedule(this)
        rescheduleAlarms()
    }

    private fun createNotificationChannel() {
        val nm = getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(REMINDER_CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            "Task Reminders",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Notifications for scheduled tasks"
            enableVibration(true)
        }
        nm.createNotificationChannel(channel)
    }

    private fun rescheduleAlarms() {
        CoroutineScope(Dispatchers.IO).launch {
            val now = System.currentTimeMillis()
            taskRepository.getFixedTasks().first().forEach { task ->
                val fireAt = task.fixedStart ?: return@forEach
                if (fireAt > now) AlarmScheduler.schedule(this@VDoneApp, task)
            }
            taskRepository.getFrequencyTasks().first().forEach { task ->
                if (task.frequencyTime != null && task.status != "done") {
                    AlarmScheduler.scheduleFrequency(this@VDoneApp, task)
                }
            }
        }
    }

    companion object {
        const val REMINDER_CHANNEL_ID = "vdone_reminders"
    }
}
