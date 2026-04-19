package com.vdone.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val notes: String?,
    val status: String,           // "todo" | "done"
    val parentId: String?,        // null = root task
    val scheduleMode: String,     // "none" | "frequency" | "fixed"
    val frequency: String?,       // "daily" | "weekly" | "monthly" | "yearly"
    val frequencyDays: Int?,      // bitmask: bit = 1 shl (Calendar.DAY_OF_WEEK-1); null/0 = every day
    val frequencyTime: Int?,      // minutes from midnight (0-1439); null = anytime
    val fixedStart: Long?,        // unix ms; used when scheduleMode = "fixed"
    val lastCompletedAt: Long?,   // unix ms; null if never completed
    val lastRemindedAt: Long?,    // unix ms; set each time a reminder fires
    val snoozedUntil: Long?,      // unix ms; non-null while alarm is snoozed
    val waitingOn: String?,       // free text; non-null means this task is an open loop
    val followUpAt: Long?,        // unix ms; when to send a follow-up nudge
    val soundUri: String?,      // ringtone URI for alarm; null = system default alarm
    val createdAt: Long,
    val updatedAt: Long,
)
