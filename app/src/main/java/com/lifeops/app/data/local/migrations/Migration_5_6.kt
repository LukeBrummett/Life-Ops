package com.lifeops.app.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from database version 5 to 6
 * 
 * Changes:
 * - Added deleteAfterCompletion column to tasks table
 *   - Type: INTEGER (SQLite boolean: 0 = false, 1 = true)
 *   - Default: 0 (false) - tasks persist normally
 *   - NOT NULL constraint
 *   - Used for ephemeral/one-time tasks that auto-delete after completion
 */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add deleteAfterCompletion column with default value of 0 (false)
        database.execSQL(
            """
            ALTER TABLE tasks 
            ADD COLUMN deleteAfterCompletion INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )
    }
}
