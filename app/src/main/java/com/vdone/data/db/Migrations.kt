package com.vdone.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tasks ADD COLUMN parentId TEXT")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tasks ADD COLUMN scheduleMode TEXT NOT NULL DEFAULT 'none'")
        db.execSQL("ALTER TABLE tasks ADD COLUMN frequency TEXT")
        db.execSQL("ALTER TABLE tasks ADD COLUMN lastCompletedAt INTEGER")
    }
}
