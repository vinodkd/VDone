package com.vdone.data.repository

import android.content.Context
import com.vdone.data.db.TaskDao
import com.vdone.data.db.TaskEntity
import com.vdone.reminder.AlarmScheduler
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class TaskRepository(private val dao: TaskDao, private val context: Context) {

    fun getAllTasks(): Flow<List<TaskEntity>> = dao.getAllTasks()

    fun getFrequencyTasks(): Flow<List<TaskEntity>> = dao.getFrequencyTasks()

    fun getFixedTasks(): Flow<List<TaskEntity>> = dao.getFixedTasks()

    suspend fun getTaskById(id: String): TaskEntity? = dao.getTaskById(id)

    suspend fun getChildren(parentId: String): List<TaskEntity> = dao.getChildren(parentId)

    fun getChildrenFlow(parentId: String): Flow<List<TaskEntity>> = dao.getChildrenFlow(parentId)

    suspend fun createTask(
        title: String,
        notes: String?,
        parentId: String? = null,
        scheduleMode: String = "none",
        frequency: String? = null,
        frequencyTime: Int? = null,
        fixedStart: Long? = null,
    ) {
        createTaskWithId(UUID.randomUUID().toString(), title, notes, parentId, scheduleMode, frequency, frequencyTime, fixedStart)
    }

    suspend fun createTaskWithId(
        id: String,
        title: String,
        notes: String?,
        parentId: String? = null,
        scheduleMode: String = "none",
        frequency: String? = null,
        frequencyTime: Int? = null,
        fixedStart: Long? = null,
    ) {
        val now = System.currentTimeMillis()
        val entity = TaskEntity(
            id = id,
            title = title,
            notes = notes,
            status = "todo",
            parentId = parentId,
            scheduleMode = scheduleMode,
            frequency = frequency,
            frequencyTime = frequencyTime,
            fixedStart = fixedStart,
            lastCompletedAt = null,
            createdAt = now,
            updatedAt = now,
        )
        dao.insert(entity)
        when {
            scheduleMode == "fixed" && fixedStart != null -> AlarmScheduler.schedule(context, entity)
            scheduleMode == "frequency" && frequencyTime != null -> AlarmScheduler.scheduleFrequency(context, entity)
        }
    }

    suspend fun updateTask(task: TaskEntity) {
        val updated = task.copy(updatedAt = System.currentTimeMillis())
        dao.update(updated)
        when {
            updated.scheduleMode == "fixed" && updated.fixedStart != null ->
                AlarmScheduler.schedule(context, updated)
            updated.scheduleMode == "frequency" && updated.frequencyTime != null ->
                AlarmScheduler.scheduleFrequency(context, updated)
            else -> AlarmScheduler.cancel(context, task.id)
        }
    }

    suspend fun toggleStatus(task: TaskEntity) {
        val newStatus = if (task.status == "done") "todo" else "done"
        dao.update(task.copy(status = newStatus, updatedAt = System.currentTimeMillis()))
        if (newStatus == "done") AlarmScheduler.cancel(context, task.id)
    }

    suspend fun completeFrequencyTask(task: TaskEntity) {
        val now = System.currentTimeMillis()
        dao.update(task.copy(status = "todo", lastCompletedAt = now, updatedAt = now))
        if (task.frequencyTime != null) AlarmScheduler.scheduleFrequency(context, task)
    }

    suspend fun deleteTask(task: TaskEntity) {
        AlarmScheduler.cancel(context, task.id)
        dao.deleteChildrenOf(task.id)
        dao.delete(task)
    }
}
