package com.lifeops.app.domain.usecase

import com.lifeops.app.data.local.entity.OverdueBehavior
import com.lifeops.app.data.repository.TaskRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Use case for processing overdue tasks when the date advances
 * 
 * Handles tasks that were not completed on their due date:
 * - Resets completion streaks for all incomplete tasks
 * - POSTPONE: Task remains in Today view (no action needed, naturally slides forward)
 * - SKIP_TO_NEXT: Task's nextDue is advanced to next scheduled occurrence
 * 
 * This should be called when the app detects a date change (e.g., when DateProvider advances)
 */
class ProcessOverdueTasksUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    /**
     * Process all overdue tasks based on their overdueBehavior setting
     * 
     * @param currentDate The current date from DateProvider
     */
    suspend operator fun invoke(currentDate: LocalDate) {
        // Get all overdue tasks (both POSTPONE and SKIP_TO_NEXT)
        val allOverdueTasks = repository.getOverdueTasks(currentDate)
        
        allOverdueTasks.forEach { task ->
            // Skip if task was completed yesterday - streak should continue
            if (task.lastCompleted == currentDate.minusDays(1)) {
                return@forEach
            }
            
            // Reset streak if task wasn't completed yesterday
            if (task.completionStreak > 0) {
                repository.updateTaskStreak(task.id, 0)
            }
            
            // For SKIP_TO_NEXT tasks, also advance nextDue date
            if (task.overdueBehavior == OverdueBehavior.SKIP_TO_NEXT) {
                val nextOccurrence = calculateNextOccurrence(task.nextDue ?: return@forEach, task, currentDate)
                
                if (nextOccurrence != null) {
                    repository.updateTaskNextDue(task.id, nextOccurrence)
                }
            }
        }
    }
    
    /**
     * Calculate the next occurrence of a task from a given start date
     * 
     * @param fromDate The date to calculate from (usually the missed due date)
     * @param task The task to calculate next occurrence for
     * @param currentDate The current date (to ensure we don't calculate past dates)
     * @return The next valid occurrence date, or null if task has no schedule
     */
    private fun calculateNextOccurrence(
        fromDate: LocalDate,
        task: com.lifeops.app.data.local.entity.Task,
        currentDate: LocalDate
    ): LocalDate? {
        // Start from the day after the missed date
        var candidateDate = fromDate.plusDays(1)
        
        // Calculate based on interval
        when (task.intervalUnit) {
            com.lifeops.app.data.local.entity.IntervalUnit.ADHOC -> {
                // ADHOC tasks don't have a next occurrence
                return null
            }
            
            com.lifeops.app.data.local.entity.IntervalUnit.DAY -> {
                // For daily intervals, add the interval quantity
                candidateDate = fromDate.plusDays(task.intervalQty.toLong())
            }
            
            com.lifeops.app.data.local.entity.IntervalUnit.WEEK -> {
                // For weekly intervals
                if (task.specificDaysOfWeek.isNullOrEmpty()) {
                    // Simple weekly interval (e.g., every 2 weeks)
                    candidateDate = fromDate.plusWeeks(task.intervalQty.toLong())
                } else {
                    // Specific days of week (e.g., Monday, Wednesday, Friday)
                    candidateDate = findNextSpecificDayOfWeek(fromDate, task.specificDaysOfWeek)
                }
            }
            
            com.lifeops.app.data.local.entity.IntervalUnit.MONTH -> {
                // For monthly intervals, add the interval quantity
                candidateDate = fromDate.plusMonths(task.intervalQty.toLong())
            }
        }
        
        // Ensure candidate is not before current date
        if (candidateDate.isBefore(currentDate)) {
            candidateDate = currentDate
        }
        
        // Check if candidate falls on an excluded date or day of week
        candidateDate = findNextNonExcludedDate(candidateDate, task)
        
        return candidateDate
    }
    
    /**
     * Find the next occurrence of a specific day of week from a given date
     */
    private fun findNextSpecificDayOfWeek(
        fromDate: LocalDate,
        specificDays: List<com.lifeops.app.data.local.entity.DayOfWeek>
    ): LocalDate {
        var candidate = fromDate.plusDays(1)
        var attempts = 0
        
        while (attempts < 7) {
            val dayOfWeek = candidate.dayOfWeek
            val matchesDay = specificDays.any { it.name == dayOfWeek.name }
            
            if (matchesDay) {
                return candidate
            }
            
            candidate = candidate.plusDays(1)
            attempts++
        }
        
        // Fallback: return one week from original date
        return fromDate.plusWeeks(1)
    }
    
    /**
     * Find the next date that is not excluded
     */
    private fun findNextNonExcludedDate(
        startDate: LocalDate,
        task: com.lifeops.app.data.local.entity.Task
    ): LocalDate {
        var candidate = startDate
        var attempts = 0
        val maxAttempts = 365 // Prevent infinite loop
        
        while (attempts < maxAttempts) {
            val isExcludedDate = task.excludedDates?.contains(candidate) == true
            val isExcludedDayOfWeek = task.excludedDaysOfWeek?.any { 
                it.name == candidate.dayOfWeek.name 
            } == true
            
            if (!isExcludedDate && !isExcludedDayOfWeek) {
                return candidate
            }
            
            candidate = candidate.plusDays(1)
            attempts++
        }
        
        // Fallback: return original candidate if we couldn't find a non-excluded date
        return startDate
    }
}
