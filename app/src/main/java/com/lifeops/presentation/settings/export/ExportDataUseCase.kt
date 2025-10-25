package com.lifeops.presentation.settings.export

import android.content.Context
import android.net.Uri
import com.google.gson.GsonBuilder
import com.lifeops.app.data.local.entity.Task
import com.lifeops.app.data.repository.TaskRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Use case for exporting all database data to JSON file
 */
class ExportDataUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val taskRepository: TaskRepository
) {
    
    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
        .setPrettyPrinting()
        .create()
    
    /**
     * Export all data to the given URI
     */
    suspend fun execute(uri: Uri): ExportResult = withContext(Dispatchers.IO) {
        try {
            // Gather all tasks
            val tasks = taskRepository.getAllTasks()
            
            // Create export object
            val exportData = ExportData(
                version = "1.0",
                exportDate = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                tasks = tasks
            )
            
            // Serialize to JSON
            val json = gson.toJson(exportData)
            
            // Write to file
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray())
            } ?: return@withContext ExportResult.Error("Could not open file for writing")
            
            // Extract filename from URI
            val fileName = getFileNameFromUri(uri)
            
            ExportResult.Success(uri = uri.toString(), fileName = fileName)
        } catch (e: Exception) {
            ExportResult.Error("Export failed: ${e.message}")
        }
    }
    
    private fun getFileNameFromUri(uri: Uri): String {
        return uri.lastPathSegment ?: "export.json"
    }
}
