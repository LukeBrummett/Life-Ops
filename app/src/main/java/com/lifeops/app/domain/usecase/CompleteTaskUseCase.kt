package com.lifeops.app.domain.usecase

import com.lifeops.app.data.local.entity.DayOfWeek
import com.lifeops.app.data.local.entity.IntervalUnit
import com.lifeops.app.data.local.entity.Task
import com.lifeops.app.data.repository.TaskRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Use case for completing a task
 * 
 * Handles all business logic related to task completion:
 * - Updates lastCompleted date
 * - Increments completion streak
 * - Calculates next due date based on interval
 * - Respects specific days of week and excluded dates
 * - Persists changes to database
 * 
 * @param repository The task repository for database access
 */
class CompleteTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    /**
     * Complete a task on the specified date
     * 
     * @param taskId The ID of the task to complete
     * @param completionDate The date the task was completed (usually today)
     */
    suspend operator fun invoke(taskId: Long, completionDate: LocalDate) {
        val taskResult = repository.getTaskById(taskId)
        val task = taskResult.getOrNull() ?: return
        
        // Toggle completion: if already completed today, uncomplete it
        val isAlreadyCompleted = task.lastCompleted == completionDate
        
        val updatedTask = if (isAlreadyCompleted) {
            // Uncomplete the task - move nextDue back to today and decrement streak
            task.copy(
                lastCompleted = null,
                nextDue = completionDate,
                completionStreak = (task.completionStreak - 1).coerceAtLeast(0)
            )
        } else {
            // Complete the task
            val newStreak = if (wasCompletedYesterday(task, completionDate)) {
                task.completionStreak + 1
            } else {
                1 // Reset streak if there was a gap
            }
            
            task.copy(
                lastCompleted = completionDate,
                completionStreak = newStreak,
                nextDue = calculateNextDueDate(task, completionDate)
            )
        }
        
        repository.updateTask(updatedTask)
        
        // Handle parent task auto-completion/uncompletion if this is a child task
        if (!task.parentTaskIds.isNullOrEmpty()) {
            if (!isAlreadyCompleted) {
                // Child was just completed - check if parent should auto-complete
                checkAndCompleteParentTasks(task.parentTaskIds, completionDate)
            } else {
                // Child was just uncompleted - check if parent should auto-uncomplete
                checkAndUncompleteParentTasks(task.parentTaskIds, completionDate)
            }
        }
        
        // Handle triggered tasks if this task was just completed (not uncompleted)
        if (!isAlreadyCompleted && !task.triggersTaskIds.isNullOrEmpty()) {
            triggerTasks(task.triggersTaskIds, completionDate)
        }
    }
    
    /**
     * Check if parent tasks should auto-uncomplete when a child is uncompleted
     * Only auto-uncompletes if requiresManualCompletion is false
     */
    private suspend fun checkAndUncompleteParentTasks(parentTaskIds: List<Long>, completionDate: LocalDate) {
        parentTaskIds.forEach { parentId ->
            val parentTaskResult = repository.getTaskById(parentId)
            val parentTask = parentTaskResult.getOrNull() ?: return@forEach
            
            // Skip if parent requires manual completion
            if (parentTask.requiresManualCompletion) {
                return@forEach
            }
            
            // If parent is currently completed today, uncomplete it
            if (parentTask.lastCompleted == completionDate) {
                val updatedParent = parentTask.copy(
                    lastCompleted = null,
                    nextDue = completionDate,
                    completionStreak = (parentTask.completionStreak - 1).coerceAtLeast(0)
                )
                
                repository.updateTask(updatedParent)
                
                // Recursively uncomplete parent's parents if needed
                if (!parentTask.parentTaskIds.isNullOrEmpty()) {
                    checkAndUncompleteParentTasks(parentTask.parentTaskIds, completionDate)
                }
            }
        }
    }
    
    /**
     * Check if parent tasks should auto-complete when a child is completed
     * Only auto-completes if requiresManualCompletion is false and all children are done
     */
    private suspend fun checkAndCompleteParentTasks(parentTaskIds: List<Long>, completionDate: LocalDate) {
        parentTaskIds.forEach { parentId ->
            val parentTaskResult = repository.getTaskById(parentId)
            val parentTask = parentTaskResult.getOrNull() ?: return@forEach
            
            // Skip if parent requires manual completion
            if (parentTask.requiresManualCompletion) {
                return@forEach
            }
            
            // Get all children of this parent
            val childrenResult = repository.getChildTasks(parentId)
            val children = childrenResult.getOrNull() ?: return@forEach
            
            // Check if all children are completed today
            val allChildrenComplete = children.all { it.lastCompleted == completionDate }
            
            // If all children complete and parent not yet complete, complete the parent
            if (allChildrenComplete && parentTask.lastCompleted != completionDate) {
                val newStreak = if (wasCompletedYesterday(parentTask, completionDate)) {
                    parentTask.completionStreak + 1
                } else {
                    1
                }
                
                val updatedParent = parentTask.copy(
                    lastCompleted = completionDate,
                    completionStreak = newStreak,
                    nextDue = calculateNextDueDate(parentTask, completionDate)
                )
                
                repository.updateTask(updatedParent)
                
                // Recursively check if this parent is also a child of another parent
                if (!parentTask.parentTaskIds.isNullOrEmpty()) {
                    checkAndCompleteParentTasks(parentTask.parentTaskIds, completionDate)
                }
            }
        }
    }
    
    /**
     * Trigger tasks when a task is completed
     * Sets the nextDue date for triggered tasks to the completion date
     */
    private suspend fun triggerTasks(triggeredTaskIds: List<Long>, triggerDate: LocalDate) {
        triggeredTaskIds.forEach { triggeredTaskId ->
            val triggeredTaskResult = repository.getTaskById(triggeredTaskId)
            val triggeredTask = triggeredTaskResult.getOrNull() ?: return@forEach
            
            // Set the triggered task's nextDue to the trigger date so it appears today
            val updatedTriggeredTask = triggeredTask.copy(
                nextDue = triggerDate
            )
            
            repository.updateTask(updatedTriggeredTask)
        }
    }
    
    /**
     * Check if task was completed yesterday (for streak tracking)
     */
    private fun wasCompletedYesterday(task: Task, today: LocalDate): Boolean {
        val lastCompleted = task.lastCompleted ?: return false
        return lastCompleted == today.minusDays(1)
    }
    
    /**
     * Calculate the next due date based on task interval and rules
     */
    private fun calculateNextDueDate(task: Task, completionDate: LocalDate): LocalDate? {
        // ADHOC tasks have no automatic scheduling
        if (task.intervalUnit == IntervalUnit.ADHOC) {
            return null
        }
        
        var candidateDate = when (task.intervalUnit) {
            IntervalUnit.DAY -> completionDate.plusDays(task.intervalQty.toLong())
            IntervalUnit.WEEK -> completionDate.plusWeeks(task.intervalQty.toLong())
            IntervalUnit.MONTH -> completionDate.plusMonths(task.intervalQty.toLong())
            IntervalUnit.ADHOC -> return null
        }
        
        // If specific days of week are set, find the next occurrence of those days
        val specificDays = task.specificDaysOfWeek
        if (!specificDays.isNullOrEmpty()) {
            candidateDate = findNextSpecificDay(candidateDate, specificDays)
        }
        
        // Skip excluded dates and days of week
        candidateDate = skipExcludedDates(
            candidateDate,
            task.excludedDates,
            task.excludedDaysOfWeek
        )
        
        return candidateDate
    }
    
    /**
     * Find the next occurrence of one of the specific days of week
     */
    private fun findNextSpecificDay(
        startDate: LocalDate,
        specificDays: List<DayOfWeek>
    ): LocalDate {
        var candidateDate = startDate
        
        // Search up to 7 days ahead to find a matching day
        for (i in 0..6) {
            val dayOfWeek = candidateDate.dayOfWeek.toCustomDayOfWeek()
            if (dayOfWeek in specificDays) {
                return candidateDate
            }
            candidateDate = candidateDate.plusDays(1)
        }
        
        // Fallback to original date if no match found (shouldn't happen)
        return startDate
    }
    
    /**
     * Skip over excluded dates and excluded days of week
     */
    private fun skipExcludedDates(
        startDate: LocalDate,
        excludedDates: List<LocalDate>?,
        excludedDaysOfWeek: List<DayOfWeek>?
    ): LocalDate {
        var candidateDate = startDate
        
        // Keep advancing until we find a valid date (max 365 days to prevent infinite loop)
        var daysChecked = 0
        while (daysChecked < 365) {
            val isExcludedDate = excludedDates?.contains(candidateDate) == true
            val isExcludedDay = excludedDaysOfWeek?.contains(
                candidateDate.dayOfWeek.toCustomDayOfWeek()
            ) == true
            
            if (!isExcludedDate && !isExcludedDay) {
                return candidateDate
            }
            
            candidateDate = candidateDate.plusDays(1)
            daysChecked++
        }
        
        // Fallback to original date if we can't find a valid date
        return startDate
    }
}

/**
 * Extension function to convert java.time.DayOfWeek to our custom DayOfWeek enum
 */
private fun java.time.DayOfWeek.toCustomDayOfWeek(): DayOfWeek {
    return when (this) {
        java.time.DayOfWeek.MONDAY -> DayOfWeek.MONDAY
        java.time.DayOfWeek.TUESDAY -> DayOfWeek.TUESDAY
        java.time.DayOfWeek.WEDNESDAY -> DayOfWeek.WEDNESDAY
        java.time.DayOfWeek.THURSDAY -> DayOfWeek.THURSDAY
        java.time.DayOfWeek.FRIDAY -> DayOfWeek.FRIDAY
        java.time.DayOfWeek.SATURDAY -> DayOfWeek.SATURDAY
        java.time.DayOfWeek.SUNDAY -> DayOfWeek.SUNDAY
    }
}
