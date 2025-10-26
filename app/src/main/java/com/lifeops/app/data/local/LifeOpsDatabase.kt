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
 * As per Technical Architecture specification:
 * - SQLite via Room for local persistence
 * - Type converters for complex types (LocalDate, JSON arrays)
 * - Destructive migration during development (pre-1.0 release)
 */
@Database(
    entities = [
        Task::class,
        Supply::class,
        Inventory::class,
        TaskSupply::class
        // Additional entities will be added: ChecklistItem, TaskLog, RestockTask
    ],
    version = 1,
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
