package com.vdone.scheduler

import com.vdone.data.db.ConditionEntity
import com.vdone.data.db.TaskEntity
import java.util.Calendar

object ConditionEvaluator {

    /**
     * Returns true if all conditions for a task are met.
     * Empty condition list → always met (task has no conditions).
     *
     * after_task_done:  the referenced task is marked done, OR is a frequency task
     *                   that was completed today (status resets to "todo" after completion,
     *                   so we use lastCompletedAt to detect same-day completion).
     * before_task_time: the referenced task's fixedStart is still in the future
     *                   (window closes when the deadline passes).
     *                   If the ref task has no fixedStart, condition is considered open.
     */
    fun areAllMet(
        conditions: List<ConditionEntity>,
        taskMap: Map<String, TaskEntity>,
        now: Long = System.currentTimeMillis(),
    ): Boolean {
        if (conditions.isEmpty()) return true
        return conditions.all { condition ->
            when (condition.type) {
                "after_task_done" -> {
                    val ref = taskMap[condition.refTaskId] ?: return@all false
                    val completedAt: Long? = when {
                        ref.status == "done"           -> ref.updatedAt
                        isToday(ref.lastCompletedAt, now) -> ref.lastCompletedAt
                        else                           -> null
                    }
                    completedAt != null &&
                        (condition.offsetSeconds <= 0 || now >= completedAt + condition.offsetSeconds * 1000L)
                }
                "before_task_time" -> {
                    val fixedStart = taskMap[condition.refTaskId]?.fixedStart
                    fixedStart == null || fixedStart > now
                }
                else -> false
            }
        }
    }

    private fun isToday(timestampMs: Long?, now: Long): Boolean {
        if (timestampMs == null) return false
        val ref = Calendar.getInstance().apply { timeInMillis = timestampMs }
        val today = Calendar.getInstance().apply { timeInMillis = now }
        return ref.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            ref.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    }
}
