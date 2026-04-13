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

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tasks ADD COLUMN fixedStart INTEGER")
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tasks ADD COLUMN snoozedUntil INTEGER")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tasks ADD COLUMN lastRemindedAt INTEGER")
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tasks ADD COLUMN waitingOn TEXT")
        db.execSQL("ALTER TABLE tasks ADD COLUMN followUpAt INTEGER")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tasks ADD COLUMN frequencyTime INTEGER")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // EventEntity removed from Room entities; events table remains in SQLite but is no longer managed.
        // No schema changes needed — Room's identity hash updates automatically with the version bump.
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `conditions` (
                `id` TEXT NOT NULL,
                `taskId` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `refTaskId` TEXT,
                `eventName` TEXT,
                `offsetSeconds` INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `events` (
                `name` TEXT NOT NULL,
                `lastOccurredAt` INTEGER,
                PRIMARY KEY(`name`)
            )
        """.trimIndent())
        // Seed default events
        listOf("Wake up", "Breakfast", "Lunch", "Dinner", "Bed time").forEach { name ->
            db.execSQL("INSERT INTO `events` (`name`, `lastOccurredAt`) VALUES ('$name', NULL)")
        }
    }
}
