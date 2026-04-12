package com.vdone.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val notes: String?,
    val status: String,       // "todo" | "done"
    val parentId: String?,    // null = root task
    val createdAt: Long,
    val updatedAt: Long,
)
