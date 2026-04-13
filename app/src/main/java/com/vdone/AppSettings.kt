package com.vdone

import android.content.Context

object AppSettings {
    private const val PREFS_NAME = "vdone_settings"
    private const val KEY_SNOOZE_MINUTES = "snooze_minutes"
    private const val KEY_SHOW_MODE = "show_mode"
    const val DEFAULT_SNOOZE_MINUTES = 10

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSnoozeMinutes(context: Context): Int =
        prefs(context).getInt(KEY_SNOOZE_MINUTES, DEFAULT_SNOOZE_MINUTES)

    fun setSnoozeMinutes(context: Context, minutes: Int) =
        prefs(context).edit().putInt(KEY_SNOOZE_MINUTES, minutes).apply()

    fun isShowMode(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SHOW_MODE, false)

    fun setShowMode(context: Context, enabled: Boolean) =
        prefs(context).edit().putBoolean(KEY_SHOW_MODE, enabled).apply()
}
