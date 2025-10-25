package com.lifeops.app.presentation.today

import com.lifeops.app.data.local.entity.Task

/**
 * UI State for the Today Screen
 * 
 * Represents all possible states the Today Screen can be in, including loading,
 * error, and various data states (empty, has tasks, all complete).
 */
data class TodayUiState(
    /**
     * Current date displayed in the header
     * Format: "MMM dd, yyyy" (e.g., "Oct 25, 2025")
     */
    val currentDate: String = "",
    
    /**
     * Tasks grouped by category
     * Key: Category name (e.g., "Fitness", "Home")
     * Value: List of tasks in that category scheduled for today
     */
    val tasksByCategory: Map<String, List<Task>> = emptyMap(),
    
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
    val error: String? = null
)
