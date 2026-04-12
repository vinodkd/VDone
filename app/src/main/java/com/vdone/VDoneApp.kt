package com.vdone

import android.app.Application
import androidx.room.Room
import com.vdone.data.db.AppDatabase
import com.vdone.data.db.MIGRATION_1_2
import com.vdone.data.db.MIGRATION_2_3
import com.vdone.data.db.MIGRATION_3_4
import com.vdone.data.repository.TaskRepository
import com.vdone.scheduler.SchedulerWorker

class VDoneApp : Application() {

    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "vdone.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()
    }

    val taskRepository: TaskRepository by lazy {
        TaskRepository(database.taskDao())
    }

    override fun onCreate() {
        super.onCreate()
        SchedulerWorker.schedule(this)
    }
}
