package com.vdone.data.repository

import android.content.Context
import com.vdone.data.db.ConditionDao
import com.vdone.data.db.TaskDao
import com.vdone.data.db.TaskEntity
import com.vdone.reminder.AlarmScheduler
import com.vdone.scheduler.ConditionEvaluator
import com.vdone.widget.DueTasksWidget
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class TaskRepository(
    private val dao: TaskDao,
    private val conditionDao: ConditionDao,
    private val context: Context,
) {

    fun getAllTasks(): Flow<List<TaskEntity>> = dao.getAllTasks()

    fun getFrequencyTasks(): Flow<List<TaskEntity>> = dao.getFrequencyTasks()

    fun getFixedTasks(): Flow<List<TaskEntity>> = dao.getFixedTasks()

    fun getWaitingTasks(): Flow<List<TaskEntity>> = dao.getWaitingTasks()

    suspend fun getTaskById(id: String): TaskEntity? = dao.getTaskById(id)

    suspend fun getChildren(parentId: String): List<TaskEntity> = dao.getChildren(parentId)

    fun getChildrenFlow(parentId: String): Flow<List<TaskEntity>> = dao.getChildrenFlow(parentId)

    suspend fun createTask(
        title: String,
        notes: String?,
        parentId: String? = null,
        scheduleMode: String = "none",
        frequency: String? = null,
        frequencyDays: Int? = null,
        frequencyTime: Int? = null,
        fixedStart: Long? = null,
        waitingOn: String? = null,
        followUpAt: Long? = null,
        soundUri: String? = null,
    ) {
        createTaskWithId(
            UUID.randomUUID().toString(), title, notes, parentId,
            scheduleMode, frequency, frequencyDays, frequencyTime, fixedStart, waitingOn, followUpAt, soundUri,
        )
    }

    suspend fun createTaskWithId(
        id: String,
        title: String,
        notes: String?,
        parentId: String? = null,
        scheduleMode: String = "none",
        frequency: String? = null,
        frequencyDays: Int? = null,
        frequencyTime: Int? = null,
        fixedStart: Long? = null,
        waitingOn: String? = null,
        followUpAt: Long? = null,
        soundUri: String? = null,
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
            frequencyDays = frequencyDays,
            frequencyTime = frequencyTime,
            fixedStart = fixedStart,
            lastCompletedAt = null,
            lastRemindedAt = null,
            snoozedUntil = null,
            waitingOn = waitingOn,
            followUpAt = followUpAt,
            soundUri = soundUri,
            createdAt = now,
            updatedAt = now,
        )
        dao.insert(entity)
        when {
            scheduleMode == "fixed" && fixedStart != null -> AlarmScheduler.schedule(context, entity)
            scheduleMode == "frequency" && frequencyTime != null -> AlarmScheduler.scheduleFrequency(context, entity)
        }
        if (followUpAt != null) AlarmScheduler.scheduleFollowUp(context, entity)
        DueTasksWidget.refresh(context)
    }

    suspend fun updateTask(task: TaskEntity) {
        // Editing a task invalidates any active snooze — the new schedule takes over.
        val updated = task.copy(updatedAt = System.currentTimeMillis(), snoozedUntil = null)
        dao.update(updated)
        when {
            updated.scheduleMode == "fixed" && updated.fixedStart != null ->
                AlarmScheduler.schedule(context, updated)
            updated.scheduleMode == "frequency" && updated.frequencyTime != null ->
                AlarmScheduler.scheduleFrequency(context, updated)
            else -> AlarmScheduler.cancel(context, task.id)
        }
        if (updated.followUpAt != null) AlarmScheduler.scheduleFollowUp(context, updated)
        else AlarmScheduler.cancelFollowUp(context, task.id)
        DueTasksWidget.refresh(context)
    }

    suspend fun toggleStatus(task: TaskEntity) {
        val now = System.currentTimeMillis()
        val newStatus = if (task.status == "done") "todo" else "done"
        dao.update(task.copy(status = newStatus, updatedAt = now))
        if (newStatus == "done") {
            AlarmScheduler.cancel(context, task.id)
            AlarmScheduler.cancelFollowUp(context, task.id)
            scheduleUnblockedTasks(task.id, now)
        }
        DueTasksWidget.refresh(context)
    }

    suspend fun updateLastRemindedAt(taskId: String, remindedAt: Long) {
        val task = dao.getTaskById(taskId) ?: return
        // Clear snoozedUntil when the alarm actually fires
        dao.update(task.copy(lastRemindedAt = remindedAt, snoozedUntil = null, updatedAt = System.currentTimeMillis()))
    }

    suspend fun setSnooze(taskId: String, until: Long) {
        val task = dao.getTaskById(taskId) ?: return
        dao.update(task.copy(snoozedUntil = until, updatedAt = System.currentTimeMillis()))
    }

    suspend fun clearWaiting(task: TaskEntity) {
        dao.update(task.copy(waitingOn = null, followUpAt = null, updatedAt = System.currentTimeMillis()))
        AlarmScheduler.cancelFollowUp(context, task.id)
    }

    suspend fun completeFrequencyTask(task: TaskEntity) {
        val now = System.currentTimeMillis()
        dao.update(task.copy(status = "todo", lastCompletedAt = now, updatedAt = now))
        if (task.frequencyTime != null) AlarmScheduler.scheduleFrequency(context, task)
        scheduleUnblockedTasks(task.id, now)
    }

    suspend fun skipFrequencyTask(task: TaskEntity) {
        // Advance lastCompletedAt without marking done — suppresses reminder for the current period
        // and reschedules the next occurrence.
        val now = System.currentTimeMillis()
        dao.update(task.copy(lastCompletedAt = now, updatedAt = now))
        if (task.frequencyTime != null) AlarmScheduler.scheduleFrequency(context, task)
        DueTasksWidget.refresh(context)
    }

    // When a task is marked done, schedule alarms for any condition-based tasks that are now unblocked.
    private suspend fun scheduleUnblockedTasks(completedTaskId: String, completedAt: Long) {
        val dependentConditions = conditionDao.getConditionsReferencingTask(completedTaskId)
        if (dependentConditions.isEmpty()) return

        val taskMap = dao.getAllTasksOnce().associateBy { it.id }

        for (condition in dependentConditions) {
            val dependent = taskMap[condition.taskId] ?: continue
            if (dependent.status == "done") continue
            if (dependent.scheduleMode != "condition") continue

            // Check all other conditions for this task are already met
            val allConditions = conditionDao.getConditionsForTaskOnce(dependent.id)
            val otherConditions = allConditions.filter { it.id != condition.id }
            if (!ConditionEvaluator.areAllMet(otherConditions, taskMap, completedAt)) continue

            // Schedule at completedAt + this condition's offset (minimum 2s from now)
            val triggerAt = maxOf(
                completedAt + condition.offsetSeconds * 1000L,
                System.currentTimeMillis() + 2_000L,
            )
            AlarmScheduler.scheduleAt(context, dependent.id, dependent.title, triggerAt, dependent.soundUri)
        }
    }

    suspend fun deleteTask(task: TaskEntity) {
        AlarmScheduler.cancel(context, task.id)
        dao.deleteChildrenOf(task.id)
        dao.delete(task)
    }
}
