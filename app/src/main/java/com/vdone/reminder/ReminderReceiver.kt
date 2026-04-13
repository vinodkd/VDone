package com.vdone.reminder

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.vdone.MainActivity
import com.vdone.VDoneApp

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra(AlarmScheduler.EXTRA_TASK_ID) ?: return
        val taskTitle = intent.getStringExtra(AlarmScheduler.EXTRA_TASK_TITLE) ?: "Task due"

        val openIntent = PendingIntent.getActivity(
            context,
            taskId.hashCode(),
            Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, VDoneApp.REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Task due")
            .setContentText(taskTitle)
            .setContentIntent(openIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(taskId.hashCode().and(0x7FFFFFFF), notification)
    }
}
