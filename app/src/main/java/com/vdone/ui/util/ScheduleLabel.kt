package com.vdone.ui.util

import com.vdone.data.db.ConditionEntity
import com.vdone.data.db.TaskEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val WEEKDAYS_MASK = (2..6).fold(0) { acc, dow -> acc or (1 shl (dow - 1)) } // Mon–Fri = 62
private val WEEKENDS_MASK = (1 shl 0) or (1 shl 6)                                  // Sun+Sat  = 65
private val DAY_LABELS = listOf("Su", "M", "T", "W", "Th", "F", "S")

fun scheduleLabel(
    task: TaskEntity,
    conditions: List<ConditionEntity>,
    taskMap: Map<String, TaskEntity>,
): String? = when (task.scheduleMode) {
    "fixed"     -> task.fixedStart?.let { formatFixed(it) }
    "frequency" -> formatFrequency(task)
    "condition" -> conditions.firstOrNull()?.let { formatCondition(it, taskMap) }
    else        -> null
}

private fun formatFixed(ms: Long): String {
    val now = Calendar.getInstance()
    val cal = Calendar.getInstance().apply { timeInMillis = ms }
    val timeFmt = SimpleDateFormat("h:mm a", Locale.getDefault())
    return when {
        cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
        cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) ->
            "Today at ${timeFmt.format(Date(ms))}"
        cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) ->
            SimpleDateFormat("MMM d 'at' h:mm a", Locale.getDefault()).format(Date(ms))
        else ->
            SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()).format(Date(ms))
    }
}

private fun formatFrequency(task: TaskEntity): String {
    val base = when (task.frequency) {
        "daily" -> {
            val days = task.frequencyDays ?: 0
            when {
                days == 0            -> "Daily"
                days == WEEKDAYS_MASK -> "Weekdays"
                days == WEEKENDS_MASK -> "Weekends"
                else -> {
                    val labels = DAY_LABELS.filterIndexed { idx, _ -> days and (1 shl idx) != 0 }
                    "Daily (${labels.joinToString()})"
                }
            }
        }
        "weekly"  -> "Weekly"
        "monthly" -> "Monthly"
        "yearly"  -> "Yearly"
        else      -> return ""
    }
    return if (task.frequencyTime != null)
        "$base at %02d:%02d".format(task.frequencyTime / 60, task.frequencyTime % 60)
    else
        base
}

private fun formatCondition(condition: ConditionEntity, taskMap: Map<String, TaskEntity>): String {
    val refTitle = taskMap[condition.refTaskId]?.title ?: "?"
    val offset = condition.offsetSeconds.let { s ->
        if (s <= 0) "" else {
            val h = s / 3600; val m = (s % 3600) / 60
            when { h > 0 && m > 0 -> " +${h}h ${m}m"; h > 0 -> " +${h}h"; else -> " +${m}m" }
        }
    }
    return when (condition.type) {
        "after_task_done"  -> "After: $refTitle$offset"
        "before_task_time" -> "Before: $refTitle"
        else               -> condition.type
    }
}
