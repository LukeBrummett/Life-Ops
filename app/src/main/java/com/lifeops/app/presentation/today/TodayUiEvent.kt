package com.lifeops.app.presentation.today

/**
 * UI Events for the Today Screen
 * 
 * Sealed interface representing all possible user interactions and events
 * that can occur on the Today Screen.
 */
sealed interface TodayUiEvent {
    
    /**
     * User tapped checkbox to complete/uncomplete a task
     * @param taskId The ID of the task to toggle completion for
     */
    data class CompleteTask(val taskId: String) : TodayUiEvent
    
    /**
     * User tapped the show/hide completed toggle button
     * Toggles between showing and hiding completed tasks
     */
    data object ToggleShowCompleted : TodayUiEvent
    
    /**
     * User tapped the "All Tasks" button in the header
     * Should navigate to the All Tasks View screen
     */
    data object NavigateToAllTasks : TodayUiEvent
    
    /**
     * User tapped the Inventory button in the header
     * Should navigate to the Inventory Management screen
     */
    data object NavigateToInventory : TodayUiEvent
    
    /**
     * User tapped the Settings button in the header
     * Should navigate to the Settings screen
     */
    data object NavigateToSettings : TodayUiEvent
    
    /**
     * User tapped on a task name (not the checkbox)
     * Should navigate to the Task Detail screen
     * @param taskId The ID of the task to view details for
     */
    data class NavigateToTaskDetail(val taskId: String) : TodayUiEvent
    
    /**
     * User tapped the New Task floating action button
     * Should navigate to the Task Create screen
     */
    data object NavigateToTaskCreate : TodayUiEvent
    
    /**
     * User performed pull-to-refresh or tapped retry button
     * Should reload tasks from the database
     */
    data object Refresh : TodayUiEvent
    
    /**
     * DEBUG: Advance the date by a number of days for testing
     * @param days Number of days to advance (can be negative to go backwards)
     */
    data class DebugAdvanceDate(val days: Int) : TodayUiEvent
}
