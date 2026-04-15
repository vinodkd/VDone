package com.vdone.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.vdone.data.db.TaskEntity
import java.util.Calendar

// Opens the app's task list — shown when the user taps the status-bar alarm icon.
private fun showIntent(context: Context): PendingIntent =
    PendingIntent.getActivity(
        context, 0,
        context.packageManager.getLaunchIntentForPackage(context.packageName),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

object AlarmScheduler {

    const val EXTRA_TASK_ID = "task_id"
    const val EXTRA_TASK_TITLE = "task_title"
    const val EXTRA_WAITING_ON = "waiting_on"
    const val EXTRA_IS_FOLLOWUP = "is_followup"

    fun schedule(context: Context, task: TaskEntity) {
        val triggerAt = task.fixedStart ?: return
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) return

        am.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerAt, showIntent(context)),
            pendingIntent(context, task.id, task.title),
        )
    }

    fun scheduleFrequency(context: Context, task: TaskEntity) {
        val minuteOfDay = task.frequencyTime ?: return
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, minuteOfDay / 60)
            set(Calendar.MINUTE, minuteOfDay % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= now) cal.add(Calendar.DAY_OF_YEAR, 1)
        scheduleAt(context, task.id, task.title, cal.timeInMillis)
    }

    fun scheduleAt(context: Context, taskId: String, taskTitle: String, triggerAt: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) return
        am.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerAt, showIntent(context)),
            pendingIntent(context, taskId, taskTitle),
        )
    }

    fun cancel(context: Context, taskId: String) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent(context, taskId, ""))
    }

    fun scheduleFollowUp(context: Context, task: TaskEntity) {
        val triggerAt = task.followUpAt ?: return
        if (triggerAt <= System.currentTimeMillis()) return
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) return
        am.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            followUpPendingIntent(context, task.id, task.title, task.waitingOn ?: ""),
        )
    }

    fun cancelFollowUp(context: Context, taskId: String) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(followUpPendingIntent(context, taskId, "", ""))
    }

    private fun followUpPendingIntent(
        context: Context, taskId: String, taskTitle: String, waitingOn: String,
    ): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, taskTitle)
            putExtra(EXTRA_WAITING_ON, waitingOn)
            putExtra(EXTRA_IS_FOLLOWUP, true)
        }
        return PendingIntent.getBroadcast(
            context,
            taskId.hashCode() xor 0x2000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    fun pendingIntent(context: Context, taskId: String, taskTitle: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, taskTitle)
        }
        return PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
