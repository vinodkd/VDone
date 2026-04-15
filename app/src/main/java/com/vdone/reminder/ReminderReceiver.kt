package com.vdone.reminder

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.vdone.AppSettings
import com.vdone.MainActivity
import com.vdone.VDoneApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra(AlarmScheduler.EXTRA_TASK_ID) ?: return
        val taskTitle = intent.getStringExtra(AlarmScheduler.EXTRA_TASK_TITLE) ?: "Task due"

        // In show mode all alarms are silently suppressed
        if (AppSettings.isShowMode(context)) return

        val isFollowUp = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_FOLLOWUP, false)
        val waitingOn = intent.getStringExtra(AlarmScheduler.EXTRA_WAITING_ON)
        val channel = VDoneApp.reminderChannel(context)

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openIntent = PendingIntent.getActivity(
            context,
            taskId.hashCode(),
            Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = if (isFollowUp) {
            NotificationCompat.Builder(context, channel)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("Follow-up reminder")
                .setContentText(
                    if (!waitingOn.isNullOrBlank()) "$taskTitle — waiting on $waitingOn"
                    else taskTitle
                )
                .setContentIntent(openIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
        } else {
            val builder = NotificationCompat.Builder(context, channel)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("Task due")
                .setContentText(taskTitle)
                .setContentIntent(openIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
            // Always set the full-screen intent. On Android 14+, the OS silently
            // ignores it if the permission isn't granted — unless the alarm was
            // scheduled via setAlarmClock(), in which case the OS allows it
            // regardless. Skipping setFullScreenIntent entirely prevents that
            // exception from ever firing.
            val fullScreenIntent = PendingIntent.getActivity(
                context,
                taskId.hashCode() xor 0x1000,
                Intent(context, ReminderActivity::class.java).apply {
                    putExtra(AlarmScheduler.EXTRA_TASK_ID, taskId)
                    putExtra(AlarmScheduler.EXTRA_TASK_TITLE, taskTitle)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            builder.setFullScreenIntent(fullScreenIntent, true)
            builder.build()
        }

        nm.notify(taskId.hashCode().and(0x7FFFFFFF), notification)

        // Record when this reminder fired (async, no need to hold up the receiver)
        if (!isFollowUp) {
            val pendingResult = goAsync()
            val repo = (context.applicationContext as VDoneApp).taskRepository
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    repo.updateLastRemindedAt(taskId, System.currentTimeMillis())
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
