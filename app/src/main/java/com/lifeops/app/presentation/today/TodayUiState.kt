package com.lifeops.app.presentation.today

import com.lifeops.app.data.local.entity.Task
import com.lifeops.app.presentation.taskdetail.PromptedInventoryItem
import java.time.LocalDate

/**
 * Represents a task in the UI, which may be a parent or standalone task
 */
data class TaskItem(
    val task: Task,
    val children: List<Task> = emptyList(),
    val isParent: Boolean = children.isNotEmpty()
)

/**
 * UI State for the Today Screen
 * 
 * Represents all possible states the Today Screen can be in, including loading,
 * error, and various data states (empty, has tasks, all complete).
 */
data class TodayUiState(
    /**
     * Current date as LocalDate (for comparisons and calculations)
     */
    val currentDateValue: LocalDate = LocalDate.now(),
    
    /**
     * Current date displayed in the header
     * Format: "MMM dd, yyyy" (e.g., "Oct 25, 2025")
     */
    val currentDate: String = "",
    
    /**
     * Tasks grouped by category, with parent-child relationships preserved
     * Key: Category name (e.g., "Fitness", "Home")
     * Value: List of task items (parents with children, or standalone tasks)
     */
    val tasksByCategory: Map<String, List<TaskItem>> = emptyMap(),
    
    /**
     * Whether completed tasks should be shown in the UI
     * - true: Show all tasks including completed
     * - false: Hide completed tasks and categories with all tasks complete
     */
    val showCompleted: Boolean = false,
    
    /**
     * Whether all tasks scheduled for today have been completed
     * Used to determine if empty state should show celebration message
     */
    val allTasksComplete: Boolean = false,
    
    /**
     * Whether the screen is currently loading data
     * Shows loading indicator when true
     */
    val isLoading: Boolean = false,
    
    /**
     * Error message if data loading failed
     * null if no error, non-null string shows error state
     */
    val error: String? = null,
    
    /**
     * Whether to show the inventory prompt dialog
     */
    val showInventoryPrompt: Boolean = false,
    
    /**
     * Task ID for which inventory prompt is shown
     */
    val inventoryPromptTaskId: String? = null,
    
    /**
     * Task name for the inventory prompt dialog
     */
    val inventoryPromptTaskName: String? = null,
    
    /**
     * Prompted inventory items for the dialog
     */
    val promptedInventoryItems: List<PromptedInventoryItem> = emptyList()
)
