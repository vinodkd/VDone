package com.vdone

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.room.Room
import com.vdone.data.db.AppDatabase
import com.vdone.data.db.MIGRATION_1_2
import com.vdone.data.db.MIGRATION_2_3
import com.vdone.data.db.MIGRATION_3_4
import com.vdone.data.db.MIGRATION_4_5
import com.vdone.data.db.MIGRATION_5_6
import com.vdone.data.db.MIGRATION_6_7
import com.vdone.data.db.MIGRATION_7_8
import com.vdone.data.db.MIGRATION_8_9
import com.vdone.data.db.MIGRATION_9_10
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
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
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
        createNotificationChannels()
        SchedulerWorker.schedule(this)
        rescheduleAlarms()
    }

    private fun createNotificationChannels() {
        val nm = getSystemService(NotificationManager::class.java)
        fun make(id: String, name: String, sound: Boolean, vibrate: Boolean) {
            val existing = nm.getNotificationChannel(id)
            // If the channel already exists at IMPORTANCE_HIGH or above, leave it alone
            // (so user-set preferences are preserved).  If it was created at a lower
            // importance (can happen during dev), delete and recreate so full-screen
            // intents work reliably.
            if (existing != null && existing.importance >= NotificationManager.IMPORTANCE_HIGH) return
            if (existing != null) nm.deleteNotificationChannel(id)
            val ch = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifications for scheduled tasks"
                if (sound) {
                    val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    val audioAttr = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                    setSound(alarmUri, audioAttr)
                } else {
                    setSound(null, null)
                }
                enableVibration(vibrate)
            }
            nm.createNotificationChannel(ch)
        }
        // v1 channels used notification stream — delete them so the new alarm-stream
        // channels below take effect on devices that already have the old ones.
        listOf("vdone_sv", "vdone_s", "vdone_v", "vdone_silent").forEach {
            nm.deleteNotificationChannel(it)
        }
        make(CHANNEL_SV,     "Task Reminders",                      sound = true,  vibrate = true)
        make(CHANNEL_S,      "Task Reminders (sound only)",          sound = true,  vibrate = false)
        make(CHANNEL_V,      "Task Reminders (vibration only)",      sound = false, vibrate = true)
        make(CHANNEL_SILENT, "Task Reminders (silent)",              sound = false, vibrate = false)
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
            taskRepository.getWaitingTasks().first().forEach { task ->
                if (task.followUpAt != null && task.followUpAt > now) {
                    AlarmScheduler.scheduleFollowUp(this@VDoneApp, task)
                }
            }
        }
    }

    companion object {
        const val CHANNEL_SV     = "vdone_sv2"
        const val CHANNEL_S      = "vdone_s2"
        const val CHANNEL_V      = "vdone_v2"
        const val CHANNEL_SILENT = "vdone_silent2"
        // legacy constant — kept so any stored references still compile
        const val REMINDER_CHANNEL_ID = CHANNEL_SV

        fun reminderChannel(context: android.content.Context): String {
            val sound   = AppSettings.isReminderSound(context)
            val vibrate = AppSettings.isReminderVibrate(context)
            return when {
                sound && vibrate  -> CHANNEL_SV
                sound             -> CHANNEL_S
                vibrate           -> CHANNEL_V
                else              -> CHANNEL_SILENT
            }
        }
    }
}
