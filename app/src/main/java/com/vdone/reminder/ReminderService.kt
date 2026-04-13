package com.vdone.reminder

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.vdone.MainActivity
import com.vdone.VDoneApp

// Foreground service used for M4c full-screen interrupt. Not used for M4b notifications.
class ReminderService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val taskId = intent?.getStringExtra(EXTRA_TASK_ID) ?: run { stopSelf(); return START_NOT_STICKY }
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Task due"

        val openIntent = PendingIntent.getActivity(
            this,
            taskId.hashCode(),
            Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = Notification.Builder(this, VDoneApp.REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Task due")
            .setContentText(taskTitle)
            .setContentIntent(openIntent)
            .setAutoCancel(true)
            .build()

        val notifId = taskId.hashCode().and(0x7FFFFFFF)
        startForeground(notifId, notification)

        // Post separately via NotificationManager so it persists after service stops
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(notifId, notification)

        @Suppress("DEPRECATION")
        stopForeground(false)
        stopSelf(startId)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TASK_TITLE = "task_title"

        fun start(context: Context, taskId: String, taskTitle: String) {
            val intent = Intent(context, ReminderService::class.java).apply {
                putExtra(EXTRA_TASK_ID, taskId)
                putExtra(EXTRA_TASK_TITLE, taskTitle)
            }
            context.startForegroundService(intent)
        }
    }
}
