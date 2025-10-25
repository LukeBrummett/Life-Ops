package com.lifeops.app.domain.usecase

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.lifeops.app.data.repository.TaskRepository
import com.lifeops.presentation.settings.export.LocalDateAdapter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Use case for creating automatic backups of all app data.
 * 
 * Features:
 * - Auto-naming with timestamp: backup_YYYY-MM-DD_HH-mm.json
 * - Saves to app-private storage
 * - Automatic retention: keeps only 2 most recent backups
 * - Uses same JSON format as Export
 */
class CreateBackupUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val taskRepository: TaskRepository
) {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
        .setPrettyPrinting()
        .create()

    /**
     * Creates a backup with the given prefix (either "backup" for manual or "auto_backup" for automatic)
     * 
     * @param isAutomatic If true, uses "auto_backup" prefix; otherwise uses "backup" prefix
     * @return Result with filename on success, error message on failure
     */
    suspend fun execute(isAutomatic: Boolean = false): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Get backup directory
            val backupDir = File(context.filesDir, "backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            // Generate filename with timestamp
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"))
            val prefix = if (isAutomatic) "auto_backup" else "backup"
            val filename = "${prefix}_$timestamp.json"
            val backupFile = File(backupDir, filename)

            // Collect all data (same as export)
            val tasks = taskRepository.getAllTasks()

            val exportData = mapOf(
                "version" to 1,
                "exportDate" to LocalDate.now().toString(),
                "tasks" to tasks,
                "supplies" to emptyList<Any>()  // TODO: Add supplies when SupplyRepository exists
            )

            // Write to file
            val json = gson.toJson(exportData)
            backupFile.writeText(json)

            Log.d("CreateBackupUseCase", "Backup created: $filename (${backupFile.length()} bytes)")

            // Clean up old backups (keep only 2 most recent for each type)
            cleanupOldBackups(backupDir, prefix)

            Result.success(filename)
        } catch (e: Exception) {
            Log.e("CreateBackupUseCase", "Backup failed", e)
            Result.failure(e)
        }
    }

    /**
     * Deletes old backups, keeping only the 2 most recent for the given prefix
     */
    private fun cleanupOldBackups(backupDir: File, prefix: String) {
        try {
            val backups = backupDir.listFiles { file ->
                file.name.startsWith(prefix) && file.name.endsWith(".json")
            }?.sortedByDescending { it.lastModified() } ?: return

            // Keep first 2, delete the rest
            backups.drop(2).forEach { oldBackup ->
                val deleted = oldBackup.delete()
                if (deleted) {
                    Log.d("CreateBackupUseCase", "Deleted old backup: ${oldBackup.name}")
                }
            }
        } catch (e: Exception) {
            Log.e("CreateBackupUseCase", "Failed to cleanup old backups", e)
            // Don't fail the backup creation if cleanup fails
        }
    }

    /**
     * Gets the most recent backup filename (manual or automatic)
     * 
     * @return Filename of most recent backup, or null if no backups exist
     */
    fun getLastBackupFilename(): String? {
        val backupDir = File(context.filesDir, "backups")
        if (!backupDir.exists()) return null

        val backups = backupDir.listFiles { file ->
            file.name.endsWith(".json") && 
            (file.name.startsWith("backup_") || file.name.startsWith("auto_backup_"))
        }?.sortedByDescending { it.lastModified() }

        return backups?.firstOrNull()?.name
    }

    /**
     * Gets the most recent manual backup timestamp
     * 
     * @return LocalDateTime of last manual backup, or null if none exists
     */
    fun getLastManualBackupTime(): LocalDateTime? {
        val backupDir = File(context.filesDir, "backups")
        if (!backupDir.exists()) return null

        val manualBackups = backupDir.listFiles { file ->
            file.name.startsWith("backup_") && file.name.endsWith(".json")
        }?.sortedByDescending { it.lastModified() }

        return manualBackups?.firstOrNull()?.let { file ->
            // Parse timestamp from filename: backup_2025-10-25_14-30.json
            val timestampPart = file.name
                .removePrefix("backup_")
                .removeSuffix(".json")
            
            try {
                LocalDateTime.parse(timestampPart, DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"))
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Gets the most recent automatic backup timestamp
     * 
     * @return LocalDateTime of last automatic backup, or null if none exists
     */
    fun getLastAutoBackupTime(): LocalDateTime? {
        val backupDir = File(context.filesDir, "backups")
        if (!backupDir.exists()) return null

        val autoBackups = backupDir.listFiles { file ->
            file.name.startsWith("auto_backup_") && file.name.endsWith(".json")
        }?.sortedByDescending { it.lastModified() }

        return autoBackups?.firstOrNull()?.let { file ->
            // Parse timestamp from filename: auto_backup_2025-10-25_14-30.json
            val timestampPart = file.name
                .removePrefix("auto_backup_")
                .removeSuffix(".json")
            
            try {
                LocalDateTime.parse(timestampPart, DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"))
            } catch (e: Exception) {
                null
            }
        }
    }
}
