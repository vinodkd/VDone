package com.vdone

import android.content.Context

object AppSettings {
    private const val PREFS_NAME = "vdone_settings"
    private const val KEY_SNOOZE_MINUTES = "snooze_minutes"
    private const val KEY_SHOW_MODE = "show_mode"
    private const val KEY_SUPPRESSED_TASKS = "suppressed_tasks"
    const val DEFAULT_SNOOZE_MINUTES = 10

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSnoozeMinutes(context: Context): Int =
        prefs(context).getInt(KEY_SNOOZE_MINUTES, DEFAULT_SNOOZE_MINUTES)

    fun setSnoozeMinutes(context: Context, minutes: Int) =
        prefs(context).edit().putInt(KEY_SNOOZE_MINUTES, minutes).apply()

    fun isShowMode(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SHOW_MODE, false)

    fun setShowMode(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_SHOW_MODE, enabled).apply()
        if (enabled) clearSuppressedTasks(context) // fresh list each time Show Mode starts
    }

    fun addSuppressedTask(context: Context, taskId: String) {
        val current = getSuppressedTaskIds(context).toMutableSet()
        current.add(taskId)
        prefs(context).edit().putStringSet(KEY_SUPPRESSED_TASKS, current).apply()
    }

    fun getSuppressedTaskIds(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_SUPPRESSED_TASKS, emptySet()) ?: emptySet()

    fun clearSuppressedTasks(context: Context) =
        prefs(context).edit().remove(KEY_SUPPRESSED_TASKS).apply()

    fun isReminderSound(context: Context): Boolean =
        prefs(context).getBoolean("reminder_sound", true)

    fun setReminderSound(context: Context, enabled: Boolean) =
        prefs(context).edit().putBoolean("reminder_sound", enabled).apply()

    fun isReminderVibrate(context: Context): Boolean =
        prefs(context).getBoolean("reminder_vibrate", true)

    fun setReminderVibrate(context: Context, enabled: Boolean) =
        prefs(context).edit().putBoolean("reminder_vibrate", enabled).apply()
}
