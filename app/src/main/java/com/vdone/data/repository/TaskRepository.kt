package com.vdone.data.repository

import com.vdone.data.db.TaskDao
import com.vdone.data.db.TaskEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class TaskRepository(private val dao: TaskDao) {

    fun getAllTasks(): Flow<List<TaskEntity>> = dao.getAllTasks()

    suspend fun getTaskById(id: String): TaskEntity? = dao.getTaskById(id)

    suspend fun getChildren(parentId: String): List<TaskEntity> = dao.getChildren(parentId)

    suspend fun createTask(title: String, notes: String?, parentId: String? = null) {
        val now = System.currentTimeMillis()
        dao.insert(
            TaskEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                notes = notes,
                status = "todo",
                parentId = parentId,
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

    suspend fun deleteTask(task: TaskEntity) {
        dao.deleteChildrenOf(task.id)
        dao.delete(task)
    }
}
