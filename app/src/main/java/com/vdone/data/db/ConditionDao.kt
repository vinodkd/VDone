package com.vdone.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConditionDao {
    @Query("SELECT * FROM conditions WHERE taskId = :taskId")
    fun getConditionsForTask(taskId: String): Flow<List<ConditionEntity>>

    @Query("SELECT * FROM conditions WHERE taskId = :taskId")
    suspend fun getConditionsForTaskOnce(taskId: String): List<ConditionEntity>

    @Query("SELECT * FROM conditions WHERE refTaskId = :refTaskId AND type = 'after_task_done'")
    suspend fun getConditionsReferencingTask(refTaskId: String): List<ConditionEntity>

    @Query("SELECT * FROM conditions")
    fun getAllConditions(): Flow<List<ConditionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(condition: ConditionEntity)

    @Query("DELETE FROM conditions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM conditions WHERE taskId = :taskId")
    suspend fun deleteAllForTask(taskId: String)
}
