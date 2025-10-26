package com.lifeops.app.presentation.taskdetail

/**
 * User events for the Task Detail Screen
 */
sealed class TaskDetailEvent {
    /**
     * Navigate to the Task Edit screen for this task
     */
    data class NavigateToEdit(val taskId: String) : TaskDetailEvent()
    
    /**
     * Complete this task (only available if due today or overdue)
     */
    data class CompleteTask(val taskId: String) : TaskDetailEvent()
    
    /**
     * Delete this task (with confirmation)
     */
    data class DeleteTask(val taskId: String) : TaskDetailEvent()
    
    /**
     * Navigate to another task's detail screen
     */
    data class NavigateToTask(val taskId: String) : TaskDetailEvent()
    
    /**
     * Navigate to an inventory item's detail screen
     */
    data class NavigateToInventory(val supplyId: String) : TaskDetailEvent()
    
    /**
     * Skip this task to tomorrow (if due)
     */
    data class SkipToTomorrow(val taskId: String) : TaskDetailEvent()
    
    /**
     * Mark as "Won't Do" (skip to next occurrence)
     */
    data class WontDo(val taskId: String) : TaskDetailEvent()
    
    /**
     * Navigate to All Tasks screen (showing this task)
     */
    data class ViewInAllTasks(val taskId: String) : TaskDetailEvent()
    
    /**
     * Dismiss error message
     */
    object DismissError : TaskDetailEvent()
}
