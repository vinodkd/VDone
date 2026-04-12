package com.vdone.data.repository

import com.vdone.data.db.TaskDao
import com.vdone.data.db.TaskEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class TaskRepository(private val dao: TaskDao) {

    fun getAllTasks(): Flow<List<TaskEntity>> = dao.getAllTasks()

    fun getFrequencyTasks(): Flow<List<TaskEntity>> = dao.getFrequencyTasks()

    suspend fun getTaskById(id: String): TaskEntity? = dao.getTaskById(id)

    suspend fun getChildren(parentId: String): List<TaskEntity> = dao.getChildren(parentId)

    fun getChildrenFlow(parentId: String): Flow<List<TaskEntity>> = dao.getChildrenFlow(parentId)

    suspend fun createTask(
        title: String,
        notes: String?,
        parentId: String? = null,
        scheduleMode: String = "none",
        frequency: String? = null,
    ) {
        val now = System.currentTimeMillis()
        dao.insert(
            TaskEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                notes = notes,
                status = "todo",
                parentId = parentId,
                scheduleMode = scheduleMode,
                frequency = frequency,
                lastCompletedAt = null,
                createdAt = now,
                updatedAt = now,
            )
        )
    }

    suspend fun updateTask(task: TaskEntity) {
        dao.update(task.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleStatus(task: TaskEntity) {
        val newStatus = if (task.status == "done") "todo" else "done"
        dao.update(task.copy(status = newStatus, updatedAt = System.currentTimeMillis()))
    }

    suspend fun completeFrequencyTask(task: TaskEntity) {
        val now = System.currentTimeMillis()
        dao.update(
            task.copy(
                status = "todo",   // immediately resets for next period
                lastCompletedAt = now,
                updatedAt = now,
            )
        )
    }

    suspend fun deleteTask(task: TaskEntity) {
        dao.deleteChildrenOf(task.id)
        dao.delete(task)
    }
}
