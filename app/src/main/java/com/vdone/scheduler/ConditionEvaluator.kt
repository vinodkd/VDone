package com.vdone.scheduler

import com.vdone.data.db.ConditionEntity
import com.vdone.data.db.TaskEntity

object ConditionEvaluator {

    /**
     * Returns true if all conditions for a task are met.
     * Empty condition list → always met (task has no conditions).
     *
     * after_task_done:  the referenced task is marked done
     * before_task_time: the referenced task's fixedStart is still in the future
     *                   (window closes when the deadline passes)
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
                "after_task_done" -> taskMap[condition.refTaskId]?.status == "done"
                "before_task_time" -> {
                    val fixedStart = taskMap[condition.refTaskId]?.fixedStart
                    fixedStart == null || fixedStart > now
                }
                else -> false
            }
        }
    }
}
