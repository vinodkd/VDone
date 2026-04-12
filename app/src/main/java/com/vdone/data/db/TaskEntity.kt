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
    val fixedStart: Long?,        // unix ms; used when scheduleMode = "fixed"
    val lastCompletedAt: Long?,   // unix ms; null if never completed
    val createdAt: Long,
    val updatedAt: Long,
)
