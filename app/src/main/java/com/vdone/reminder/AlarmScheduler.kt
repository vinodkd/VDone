package com.vdone.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.vdone.data.db.TaskEntity

object AlarmScheduler {

    const val EXTRA_TASK_ID = "task_id"
    const val EXTRA_TASK_TITLE = "task_title"

    fun schedule(context: Context, task: TaskEntity) {
        val triggerAt = task.fixedStart ?: return
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) return

        am.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            pendingIntent(context, task.id, task.title),
        )
    }

    fun cancel(context: Context, taskId: String) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent(context, taskId, ""))
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
