package com.vdone.data.repository

import com.vdone.data.db.ConditionDao
import com.vdone.data.db.ConditionEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ConditionRepository(private val conditionDao: ConditionDao) {

    fun getConditionsForTask(taskId: String): Flow<List<ConditionEntity>> =
        conditionDao.getConditionsForTask(taskId)

    fun getAllConditions(): Flow<List<ConditionEntity>> =
        conditionDao.getAllConditions()

    suspend fun addCondition(
        taskId: String,
        type: String,
        refTaskId: String? = null,
        offsetSeconds: Long = 0,
    ) {
        conditionDao.insert(
            ConditionEntity(
                id = UUID.randomUUID().toString(),
                taskId = taskId,
                type = type,
                refTaskId = refTaskId,
                eventName = null,
                offsetSeconds = offsetSeconds,
            )
        )
    }

    suspend fun deleteCondition(id: String) = conditionDao.deleteById(id)

    suspend fun deleteAllForTask(taskId: String) = conditionDao.deleteAllForTask(taskId)
}
