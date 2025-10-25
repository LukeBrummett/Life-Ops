package com.lifeops.presentation.settings.import_data

import android.content.Context
import android.net.Uri
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.lifeops.app.data.local.entity.Task
import com.lifeops.app.data.repository.TaskRepository
import com.lifeops.presentation.settings.export.LocalDateAdapter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

/**
 * Use case for importing database data from JSON file
 */
class ImportDataUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val taskRepository: TaskRepository
) {
    
    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
        .create()
    
    /**
     * Parse and validate import file
     */
    suspend fun parseAndValidate(uri: Uri): ImportResult = withContext(Dispatchers.IO) {
        try {
            // Read file content
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            } ?: return@withContext ImportResult.Error("Could not read file")
            
            // Parse JSON
            val importData = try {
                gson.fromJson(jsonString, ImportData::class.java)
            } catch (e: JsonSyntaxException) {
                return@withContext ImportResult.Error("Invalid JSON format: ${e.message}")
            }
            
            // Validate data
            when (val validationResult = validateImportData(importData)) {
                is ValidationResult.Invalid -> {
                    return@withContext ImportResult.Error("Validation failed: ${validationResult.errors.joinToString()}")
                }
                ValidationResult.Valid -> {
                    // Check for conflicts
                    detectConflicts(importData.tasks)
                }
            }
        } catch (e: Exception) {
            ImportResult.Error("Import failed: ${e.message}")
        }
    }
    
    /**
     * Execute import with conflict resolution
     */
    suspend fun executeImport(
        tasks: List<Task>,
        conflictResolutions: Map<Long, ConflictResolution> = emptyMap()
    ): ImportResult = withContext(Dispatchers.IO) {
        try {
            var imported = 0
            var skipped = 0
            var replaced = 0
            
            tasks.forEach { task ->
                val resolution = conflictResolutions[task.id] ?: ConflictResolution.SKIP
                
                when (resolution) {
                    ConflictResolution.SKIP -> {
                        skipped++
                    }
                    ConflictResolution.REPLACE -> {
                        taskRepository.updateTask(task)
                        replaced++
                    }
                    ConflictResolution.KEEP_BOTH -> {
                        // Insert with new ID (Room will auto-generate)
                        taskRepository.createTask(task.copy(id = 0))
                        imported++
                    }
                }
            }
            
            ImportResult.Success(
                tasksImported = imported,
                tasksSkipped = skipped,
                tasksReplaced = replaced
            )
        } catch (e: Exception) {
            ImportResult.Error("Import execution failed: ${e.message}")
        }
    }
    
    /**
     * Validate imported data structure
     */
    private fun validateImportData(data: ImportData): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Check version compatibility
        if (data.version != "1.0") {
            errors.add("Unsupported version: ${data.version}")
        }
        
        // Validate tasks
        data.tasks.forEach { task ->
            // Check required fields
            if (task.name.isBlank()) {
                errors.add("Task with ID ${task.id} has empty name")
            }
            
            // Validate relationships
            if (task.parentTaskIds != null) {
                val invalidParents = task.parentTaskIds.filter { parentId ->
                    data.tasks.none { it.id == parentId }
                }
                if (invalidParents.isNotEmpty()) {
                    errors.add("Task '${task.name}' references non-existent parent IDs: $invalidParents")
                }
            }
            
            if (task.triggeredByTaskIds != null) {
                val invalidTriggers = task.triggeredByTaskIds.filter { triggerId ->
                    data.tasks.none { it.id == triggerId }
                }
                if (invalidTriggers.isNotEmpty()) {
                    errors.add("Task '${task.name}' references non-existent trigger IDs: $invalidTriggers")
                }
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    /**
     * Detect conflicts between imported tasks and existing tasks
     */
    private suspend fun detectConflicts(tasks: List<Task>): ImportResult {
        val conflicts = mutableListOf<ImportConflict>()
        
        tasks.forEach { importedTask ->
            val existingTask = taskRepository.getTaskById(importedTask.id).getOrNull()
            
            if (existingTask != null) {
                conflicts.add(
                    ImportConflict(
                        type = ConflictType.DUPLICATE_ID,
                        taskId = importedTask.id,
                        taskName = importedTask.name,
                        existingTask = existingTask,
                        importedTask = importedTask
                    )
                )
            }
        }
        
        return if (conflicts.isEmpty()) {
            // No conflicts - proceed with import
            executeImport(tasks, tasks.associate { it.id to ConflictResolution.KEEP_BOTH })
        } else {
            // Return conflicts for user resolution
            ImportResult.NeedsResolution(conflicts)
        }
    }
}
