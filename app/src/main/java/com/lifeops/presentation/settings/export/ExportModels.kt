package com.lifeops.presentation.settings.export

import com.lifeops.app.data.local.entity.Task
import java.time.LocalDateTime

/**
 * Root export object containing all database data
 */
data class ExportData(
    val version: String = "1.0",
    val exportDate: String, // ISO-8601 format
    val tasks: List<Task>
    // Future: supplies, inventory, checklistItems, taskLogs will be added here
)

/**
 * Result of export operation
 */
sealed class ExportResult {
    data class Success(val uri: String, val fileName: String) : ExportResult()
    data class Error(val message: String) : ExportResult()
}
