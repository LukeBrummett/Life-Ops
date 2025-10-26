package com.lifeops.app.presentation.taskdetail

import com.lifeops.app.data.local.entity.Task

/**
 * UI State for the Task Detail Screen
 * 
 * Represents all information needed to display a task in read-only detail view,
 * including basic info, schedule, completion data, relationships, and inventory.
 */
data class TaskDetailUiState(
    val isLoading: Boolean = true,
    val task: Task? = null,
    val errorMessage: String? = null,
    
    // Formatted display values
    val scheduleDescription: String = "",
    val statusDescription: String = "",
    val lastCompletedRelative: String = "",
    val excludedDaysText: String? = null,
    val excludedDatesText: String? = null,
    
    // Relationships
    val parentTask: TaskSummary? = null,
    val childTasks: List<ChildTaskDisplay> = emptyList(),
    val triggeredByTasks: List<TaskSummary> = emptyList(),
    val triggersTasks: List<TaskSummary> = emptyList(),
    
    // Inventory associations
    val inventoryItems: List<InventoryItemDisplay> = emptyList(),
    
    // Action availability
    val canComplete: Boolean = false,
    val canDelete: Boolean = true
)

/**
 * Summary information for a related task
 */
data class TaskSummary(
    val taskId: String,
    val taskName: String,
    val category: String,
    val scheduleSummary: String
)

/**
 * Display information for a child task
 */
data class ChildTaskDisplay(
    val taskId: String,
    val taskName: String,
    val category: String,
    val order: Int,
    val scheduleSummary: String
)

/**
 * Display information for an inventory item consumed by the task
 */
data class InventoryItemDisplay(
    val supplyId: String,
    val supplyName: String,
    val consumptionMode: String, // "Fixed", "Prompted", "Recount"
    val modeDetails: String, // e.g., "2 per execution", "default: 30g", ""
    val currentStock: String, // e.g., "8 filters"
    val isLowStock: Boolean = false
)
