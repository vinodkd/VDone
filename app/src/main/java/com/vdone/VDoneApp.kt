package com.vdone

import android.app.Application
import androidx.room.Room
import com.vdone.data.db.AppDatabase
import com.vdone.data.db.MIGRATION_1_2
import com.vdone.data.repository.TaskRepository

class VDoneApp : Application() {

    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "vdone.db")
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    val taskRepository: TaskRepository by lazy {
        TaskRepository(database.taskDao())
    }
}
