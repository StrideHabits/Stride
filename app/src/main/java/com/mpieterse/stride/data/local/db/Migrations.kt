// data/local/db/Migrations.kt
package com.mpieterse.stride.data.local.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1) habits table (idempotent)
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

        // 2) mutations table (idempotent)
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

        // 3) De-dupe check_ins BEFORE adding unique index
        //    Keep the newest row per (habitId, dayKey)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS check_ins_tmp(
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

        db.execSQL("""
            INSERT INTO check_ins_tmp(id, habitId, dayKey, completedAt, deleted, updatedAt, rowVersion, syncState)
            SELECT c.id, c.habitId, c.dayKey, c.completedAt, c.deleted, c.updatedAt, c.rowVersion, c.syncState
            FROM check_ins c
            WHERE NOT EXISTS (
                SELECT 1 FROM check_ins c2
                WHERE c2.habitId = c.habitId AND c2.dayKey = c.dayKey
                  AND (
                        (c2.updatedAt > c.updatedAt) OR
                        (c2.updatedAt = c.updatedAt AND c2.completedAt > c.completedAt) OR
                        (c2.updatedAt = c.updatedAt AND c2.completedAt = c.completedAt AND c2.rowid > c.rowid)
                  )
            )
        """.trimIndent())

        db.execSQL("DELETE FROM check_ins")
        db.execSQL("""
            INSERT INTO check_ins(id, habitId, dayKey, completedAt, deleted, updatedAt, rowVersion, syncState)
            SELECT id, habitId, dayKey, completedAt, deleted, updatedAt, rowVersion, syncState
            FROM check_ins_tmp
        """.trimIndent())
        db.execSQL("DROP TABLE check_ins_tmp")

        // 4) Now the unique index is safe
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_checkins_habit_day ON check_ins(habitId, dayKey)")
    }
}
