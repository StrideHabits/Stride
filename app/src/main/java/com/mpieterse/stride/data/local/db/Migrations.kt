package com.mpieterse.stride.data.local.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // habits (new table)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS habits(
                id TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                frequency INTEGER NOT NULL,
                tag TEXT,
                imageUrl TEXT,
                deleted INTEGER NOT NULL DEFAULT 0,
                createdAt TEXT NOT NULL DEFAULT '',
                updatedAt TEXT NOT NULL DEFAULT '',
                rowVersion TEXT NOT NULL DEFAULT '',
                syncState TEXT NOT NULL DEFAULT 'Synced'
            )
        """.trimIndent())

        // check_ins: ensure table exists (older v1 already had it), then indices
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS check_ins(
                id TEXT NOT NULL PRIMARY KEY,
                habitId TEXT NOT NULL,
                dayKey TEXT NOT NULL,
                completedAt TEXT NOT NULL,
                deleted INTEGER NOT NULL,
                updatedAt TEXT NOT NULL,
                rowVersion TEXT NOT NULL,
                syncState TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_checkins_habit_day ON check_ins(habitId, dayKey)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_check_ins_habitId ON check_ins(habitId)")

        // mutations (guard for clean installs)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS mutations(
                localId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                requestId TEXT NOT NULL,
                targetId TEXT NOT NULL,
                targetType TEXT NOT NULL,
                op TEXT NOT NULL,
                name TEXT,
                frequency INTEGER,
                tag TEXT,
                imageUrl TEXT,
                habitId TEXT,
                dayKey TEXT,
                completedAt TEXT,
                deleted INTEGER NOT NULL DEFAULT 0,
                baseVersion TEXT,
                state TEXT NOT NULL,
                attemptCount INTEGER NOT NULL DEFAULT 0,
                createdAtMs INTEGER NOT NULL,
                lastError TEXT
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_mutations_state_time ON mutations(state, createdAtMs)")
    }
}
