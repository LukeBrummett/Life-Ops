package com.lifeops.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lifeops.app.data.local.converter.Converters
import com.lifeops.app.data.local.dao.TaskDao
import com.lifeops.app.data.local.entity.Task

/**
 * Life-Ops Room Database
 * 
 * Single source of truth for all application data
 * Completely offline, no network dependencies
 * 
 * As per Technical Architecture specification:
 * - SQLite via Room for local persistence
 * - Type converters for complex types (LocalDate, JSON arrays)
 * - Database migrations for future schema changes
 */
@Database(
    entities = [
        Task::class
        // Additional entities will be added: Supply, TaskSupply, Inventory, ChecklistItem, TaskLog, RestockTask
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class LifeOpsDatabase : RoomDatabase() {
    
    abstract fun taskDao(): TaskDao
    // Future DAOs will be added here
    
    companion object {
        const val DATABASE_NAME = "lifeops_db"
    }
}
