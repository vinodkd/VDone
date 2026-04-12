package com.vdone.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: String): TaskEntity?

    @Query("SELECT * FROM tasks WHERE parentId = :parentId ORDER BY createdAt ASC")
    suspend fun getChildren(parentId: String): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE parentId = :parentId ORDER BY createdAt ASC")
    fun getChildrenFlow(parentId: String): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE parentId = :parentId")
    suspend fun deleteChildrenOf(parentId: String)

    @Query("SELECT * FROM tasks WHERE scheduleMode = 'frequency' AND parentId IS NULL ORDER BY createdAt ASC")
    fun getFrequencyTasks(): Flow<List<TaskEntity>>
}
