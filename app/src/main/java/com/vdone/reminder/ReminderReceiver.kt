package com.vdone.reminder

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.vdone.AppSettings
import com.vdone.VDoneApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId    = intent.getStringExtra(AlarmScheduler.EXTRA_TASK_ID)    ?: return
        val taskTitle = intent.getStringExtra(AlarmScheduler.EXTRA_TASK_TITLE) ?: "Task due"

        if (AppSettings.isShowMode(context)) return

        val isFollowUp = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_FOLLOWUP, false)

        if (isFollowUp) {
            // Follow-up reminders are plain notifications — no full-screen, no audio loop.
            val waitingOn = intent.getStringExtra(AlarmScheduler.EXTRA_WAITING_ON)
            val channel   = VDoneApp.reminderChannel(context)
            val openPi = PendingIntent.getActivity(
                context, taskId.hashCode(),
                Intent(context, ReminderActivity::class.java).apply {
                    putExtra(AlarmScheduler.EXTRA_TASK_ID, taskId)
                    putExtra(AlarmScheduler.EXTRA_TASK_TITLE, taskTitle)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val notification = NotificationCompat.Builder(context, channel)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("Follow-up reminder")
                .setContentText(
                    if (!waitingOn.isNullOrBlank()) "$taskTitle — waiting on $waitingOn"
                    else taskTitle
                )
                .setContentIntent(openPi)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            NotificationManagerCompat.from(context)
                .notify(taskId.hashCode().and(0x7FFFFFFF), notification)

            val pendingResult = goAsync()
            val repo = (context.applicationContext as VDoneApp).taskRepository
            CoroutineScope(Dispatchers.IO).launch {
                try { repo.updateLastRemindedAt(taskId, System.currentTimeMillis()) }
                finally { pendingResult.finish() }
            }
        } else {
            // Regular alarm — hand off to ReminderService which owns audio + notification.
            val soundUri = intent.getStringExtra(AlarmScheduler.EXTRA_SOUND_URI)
            ReminderService.start(context, taskId, taskTitle, soundUri)
        }
    }
}
