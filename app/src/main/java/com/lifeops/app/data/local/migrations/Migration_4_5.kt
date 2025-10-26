package com.lifeops.app.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from database version 4 to 5
 * 
 * Changes:
 * - Added overdueBehavior column to tasks table
 *   - Type: TEXT (stores "POSTPONE" or "SKIP_TO_NEXT")
 *   - Default: "POSTPONE" for backward compatibility
 *   - NOT NULL constraint
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add overdueBehavior column with default value of POSTPONE
        database.execSQL(
            """
            ALTER TABLE tasks 
            ADD COLUMN overdueBehavior TEXT NOT NULL DEFAULT 'POSTPONE'
            """.trimIndent()
        )
    }
}
