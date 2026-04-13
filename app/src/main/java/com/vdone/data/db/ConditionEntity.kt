package com.vdone.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conditions")
data class ConditionEntity(
    @PrimaryKey val id: String,
    val taskId: String,
    val type: String,           // "after_event" | "before_event" | "after_task_done"
    val refTaskId: String?,     // used when type = "after_task_done"
    val eventName: String?,     // used when type = "after_event" | "before_event"
    val offsetSeconds: Long = 0, // for after_event: min seconds after event before task is due
)
