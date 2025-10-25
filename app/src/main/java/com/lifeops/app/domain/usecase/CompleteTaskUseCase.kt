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
            // Uncomplete the task
            task.copy(
                lastCompleted = null,
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
