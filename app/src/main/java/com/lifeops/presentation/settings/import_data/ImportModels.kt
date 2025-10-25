package com.lifeops.presentation.settings.import_data

import com.lifeops.app.data.local.entity.Task

/**
 * Import data parsed from JSON file
 */
data class ImportData(
    val version: String,
    val exportDate: String,
    val tasks: List<Task>
)

/**
 * Result of import validation
 */
sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Invalid(val errors: List<String>) : ValidationResult()
}

/**
 * Conflict type when importing data
 */
enum class ConflictType {
    DUPLICATE_ID,
    NEWER_VERSION
}

/**
 * Conflict resolution strategy
 */
enum class ConflictResolution {
    SKIP,           // Don't import conflicting item
    REPLACE,        // Overwrite existing item with imported data
    KEEP_BOTH       // Generate new ID for imported item
}

/**
 * Detected conflict during import
 */
data class ImportConflict(
    val type: ConflictType,
    val taskId: Long,
    val taskName: String,
    val existingTask: Task?,
    val importedTask: Task
)

/**
 * Result of import operation
 */
sealed class ImportResult {
    data class Success(
        val tasksImported: Int,
        val tasksSkipped: Int,
        val tasksReplaced: Int
    ) : ImportResult()
    
    data class NeedsResolution(
        val conflicts: List<ImportConflict>
    ) : ImportResult()
    
    data class Error(val message: String) : ImportResult()
}
