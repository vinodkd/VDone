package com.vdone.scheduler

import com.vdone.data.db.TaskEntity
import java.util.Calendar

/**
 * Determines whether a frequency-based task is due today.
 * Pure logic — no Android dependencies.
 */
object FrequencyChecker {

    fun isDueToday(task: TaskEntity, now: Long = System.currentTimeMillis()): Boolean {
        // For daily tasks with a days filter, skip days not selected.
        if (task.frequency == "daily") {
            val days = task.frequencyDays ?: 0
            if (days != 0 && !isDayBitSet(days, calOf(now).get(Calendar.DAY_OF_WEEK))) return false
        }

        // Monthly tasks only surface on the 1st of the month
        if (task.frequency == "monthly" && calOf(now).get(Calendar.DAY_OF_MONTH) != 1) return false

        val lastCompleted = task.lastCompletedAt ?: return true  // never done → always due

        return when (task.frequency) {
            "daily"   -> !sameDay(lastCompleted, now)
            "weekly"  -> !sameWeek(lastCompleted, now)
            "monthly" -> !sameMonth(lastCompleted, now)
            "yearly"  -> !sameYear(lastCompleted, now)
            else -> false
        }
    }

    /** True if the Calendar.DAY_OF_WEEK value [dow] is set in the bitmask [days]. */
    fun isDayBitSet(days: Int, dow: Int): Boolean = days and (1 shl (dow - 1)) != 0

    private fun calOf(ms: Long): Calendar = Calendar.getInstance().apply { timeInMillis = ms }

    private fun sameDay(a: Long, b: Long): Boolean {
        val ca = calOf(a); val cb = calOf(b)
        return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR) &&
                ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR)
    }

    private fun sameWeek(a: Long, b: Long): Boolean {
        val ca = calOf(a); val cb = calOf(b)
        return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR) &&
                ca.get(Calendar.WEEK_OF_YEAR) == cb.get(Calendar.WEEK_OF_YEAR)
    }

    private fun sameMonth(a: Long, b: Long): Boolean {
        val ca = calOf(a); val cb = calOf(b)
        return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR) &&
                ca.get(Calendar.MONTH) == cb.get(Calendar.MONTH)
    }

    private fun sameYear(a: Long, b: Long): Boolean =
        calOf(a).get(Calendar.YEAR) == calOf(b).get(Calendar.YEAR)
}
