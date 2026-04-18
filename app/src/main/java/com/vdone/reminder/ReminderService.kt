package com.vdone.reminder

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.vdone.VDoneApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Persistent foreground service that owns the alarm audio and notification for the duration
 * of an alarm. Stays running until ACTION_DISMISS or ACTION_SNOOZE is received from
 * ReminderActivity. This keeps the notification alive and the audio looping on OEM devices
 * that would otherwise kill a transient notification immediately.
 */
class ReminderService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            ACTION_DISMISS, ACTION_SNOOZE -> {
                // ReminderActivity handled the user action; just clean up and stop.
                stop()
                return START_NOT_STICKY
            }
        }

        val taskId    = intent?.getStringExtra(EXTRA_TASK_ID)    ?: run { stopSelf(); return START_NOT_STICKY }
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE)  ?: "Task due"

        acquireWakeLock()
        startAudio()
        postNotification(taskId, taskTitle)
        launchAlarmScreen(taskId, taskTitle)

        // Record when this reminder fired
        val repo = (applicationContext as VDoneApp).taskRepository
        CoroutineScope(Dispatchers.IO).launch {
            repo.updateLastRemindedAt(taskId, System.currentTimeMillis())
        }

        return START_STICKY   // restart if killed by OEM; audio + wakelock re-acquired
    }

    private fun postNotification(taskId: String, taskTitle: String) {
        val channel = VDoneApp.reminderChannel(this)

        // Tapping notification or full-screen → ReminderActivity
        val activityIntent = Intent(this, ReminderActivity::class.java).apply {
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, taskTitle)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                     Intent.FLAG_ACTIVITY_CLEAR_TOP or
                     Intent.FLAG_ACTIVITY_NO_USER_ACTION)
        }
        val contentPi = PendingIntent.getActivity(
            this, taskId.hashCode(),
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val fullScreenPi = PendingIntent.getActivity(
            this, taskId.hashCode() xor 0x1000,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, channel)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Task due")
            .setContentText(taskTitle)
            .setContentIntent(contentPi)
            .setFullScreenIntent(fullScreenPi, true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)        // FLAG_ONGOING_EVENT + FLAG_NO_CLEAR: can't be swiped away
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)  // show on lock screen
            .build()

        startForeground(taskId.hashCode().and(0x7FFFFFFF), notification)
    }

    private fun launchAlarmScreen(taskId: String, taskTitle: String) {
        startActivity(
            Intent(this, ReminderActivity::class.java).apply {
                putExtra(EXTRA_TASK_ID, taskId)
                putExtra(EXTRA_TASK_TITLE, taskTitle)
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION
                )
            }
        )
    }

    private fun startAudio() {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(applicationContext, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            } catch (_: Exception) {
                // If alarm URI fails, continue without audio rather than crash
            }
        }
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "VDone:AlarmWakeLock").apply {
            acquire(10 * 60 * 1000L)   // max 10 min; released on dismiss
        }
    }

    private fun stop() {
        mediaPlayer?.runCatching { stop(); release() }
        mediaPlayer = null
        wakeLock?.runCatching { if (isHeld) release() }
        wakeLock = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.runCatching { stop(); release() }
        wakeLock?.runCatching { if (isHeld) release() }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val EXTRA_TASK_ID    = "task_id"
        const val EXTRA_TASK_TITLE = "task_title"
        const val ACTION_DISMISS   = "com.vdone.ALARM_DISMISS"
        const val ACTION_SNOOZE    = "com.vdone.ALARM_SNOOZE"

        fun start(context: Context, taskId: String, taskTitle: String) {
            context.startForegroundService(
                Intent(context, ReminderService::class.java).apply {
                    putExtra(EXTRA_TASK_ID, taskId)
                    putExtra(EXTRA_TASK_TITLE, taskTitle)
                }
            )
        }

        fun dismiss(context: Context) {
            context.startService(
                Intent(context, ReminderService::class.java).setAction(ACTION_DISMISS)
            )
        }

        fun snooze(context: Context) {
            context.startService(
                Intent(context, ReminderService::class.java).setAction(ACTION_SNOOZE)
            )
        }
    }
}
