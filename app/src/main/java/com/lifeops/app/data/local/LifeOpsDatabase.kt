package com.lifeops.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lifeops.app.data.local.converter.Converters
import com.lifeops.app.data.local.dao.SupplyDao
import com.lifeops.app.data.local.dao.TaskDao
import com.lifeops.app.data.local.entity.Inventory
import com.lifeops.app.data.local.entity.Supply
import com.lifeops.app.data.local.entity.Task
import com.lifeops.app.data.local.entity.TaskSupply

/**
 * Life-Ops Room Database
 * 
 * Single source of truth for all application data
 * Completely offline, no network dependencies
 * 
 * Version History:
 * - v1: Initial schema with Long task IDs
 * - v2: Migrated to UUID (String) task IDs for conflict-free sharing
 * - v3: Added Supply and Inventory entities for inventory management
 * - v4: Added TaskSupply junction table for task-inventory integration
 * 
 * As per Technical Architecture specification:
 * - SQLite via Room for local persistence
 * - Type converters for complex types (LocalDate, JSON arrays)
 * - Database migrations for future schema changes
 */
@Database(
    entities = [
        Task::class,
        Supply::class,
        Inventory::class,
        TaskSupply::class
        // Additional entities will be added: ChecklistItem, TaskLog, RestockTask
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class LifeOpsDatabase : RoomDatabase() {
    
    abstract fun taskDao(): TaskDao
    abstract fun supplyDao(): SupplyDao
    // Future DAOs will be added here
    
    companion object {
        const val DATABASE_NAME = "lifeops_db"
    }
}
