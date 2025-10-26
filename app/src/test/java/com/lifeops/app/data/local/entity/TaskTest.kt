package com.lifeops.app.data.local.entity

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for Task entity
 * Tests basic task creation and property validation
 */
class TaskTest {
    
    @Test
    fun `task creation with minimal fields succeeds`() {
        // Given
        val task = Task(
            name = "Test Task",
            category = "Work"
        )
        
        // Then
        assertThat(task.name).isEqualTo("Test Task")
        assertThat(task.category).isEqualTo("Work")
        assertThat(task.active).isTrue()
        assertThat(task.intervalUnit).isEqualTo(IntervalUnit.DAY)
        assertThat(task.intervalQty).isEqualTo(1)
        assertThat(task.completionStreak).isEqualTo(0)
    }
    
    @Test
    fun `task with daily interval every 3 days`() {
        // Given
        val task = Task(
            name = "Take Medication",
            category = "Health",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 3
        )
        
        // Then
        assertThat(task.intervalUnit).isEqualTo(IntervalUnit.DAY)
        assertThat(task.intervalQty).isEqualTo(3)
    }
    
    @Test
    fun `task with specific days of week`() {
        // Given
        val task = Task(
            name = "Workout",
            category = "Health",
            intervalUnit = IntervalUnit.WEEK,
            specificDaysOfWeek = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        )
        
        // Then
        assertThat(task.specificDaysOfWeek).hasSize(3)
        assertThat(task.specificDaysOfWeek).containsExactly(
            DayOfWeek.MONDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.FRIDAY
        )
    }
    
    @Test
    fun `adhoc task has no automatic scheduling`() {
        // Given
        val task = Task(
            name = "Clean Up After Dinner",
            category = "Household",
            intervalUnit = IntervalUnit.ADHOC,
            intervalQty = 0
        )
        
        // Then
        assertThat(task.intervalUnit).isEqualTo(IntervalUnit.ADHOC)
        assertThat(task.intervalQty).isEqualTo(0)
    }
    
    @Test
    fun `task with excluded days`() {
        // Given
        val task = Task(
            name = "Laundry",
            category = "Household",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 5,
            excludedDaysOfWeek = listOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)
        )
        
        // Then
        assertThat(task.excludedDaysOfWeek).containsExactly(
            DayOfWeek.TUESDAY,
            DayOfWeek.THURSDAY
        )
    }
    
    @Test
    fun `task with excluded dates`() {
        // Given
        val excludedDates = listOf(
            LocalDate.of(2025, 12, 25), // Christmas
            LocalDate.of(2025, 1, 1)     // New Year
        )
        val task = Task(
            name = "Work Task",
            category = "Work",
            excludedDates = excludedDates
        )
        
        // Then
        assertThat(task.excludedDates).containsExactlyElementsIn(excludedDates)
    }
    
    @Test
    fun `parent task with multiple children`() {
        // Given - parent task
        val parentTask = Task(
            name = "Clean Bathroom",
            category = "Household",
            requiresManualCompletion = false // Auto-complete when children done
        )
        
        // Then
        assertThat(parentTask.requiresManualCompletion).isFalse()
    }
    
    @Test
    fun `task with single parent`() {
        // Given
        val childTask = Task(
            name = "Clean Shower",
            category = "Household",
            parentTaskIds = listOf("parent-task-1"), // Parent task ID
            childOrder = 1
        )
        
        // Then
        assertThat(childTask.parentTaskIds).containsExactly("parent-task-1")
        assertThat(childTask.childOrder).isEqualTo(1)
    }
    
    @Test
    fun `task with multiple parents`() {
        // Given - As per spec: "Tasks can have multiple parents"
        val childTask = Task(
            name = "Stretch",
            category = "Health",
            parentTaskIds = listOf("parent-task-1", "parent-task-2") // Part of two different workout routines
        )
        
        // Then
        assertThat(childTask.parentTaskIds).hasSize(2)
        assertThat(childTask.parentTaskIds).containsExactly("parent-task-1", "parent-task-2")
    }
    
    @Test
    fun `triggered task configuration`() {
        // Given
        val triggeredTask = Task(
            name = "Clean Up After Dinner",
            category = "Household",
            intervalUnit = IntervalUnit.ADHOC, // Only appears when triggered
            triggeredByTaskIds = listOf("trigger-task-10") // Triggered by "Cook Dinner"
        )
        
        // Then
        assertThat(triggeredTask.intervalUnit).isEqualTo(IntervalUnit.ADHOC)
        assertThat(triggeredTask.triggeredByTaskIds).containsExactly("trigger-task-10")
    }
    
    @Test
    fun `task that triggers other tasks`() {
        // Given
        val task = Task(
            name = "Cook Dinner",
            category = "Household",
            triggersTaskIds = listOf("cleanup-task-20", "cleanup-task-21") // Triggers cleanup tasks
        )
        
        // Then
        assertThat(task.triggersTaskIds).hasSize(2)
        assertThat(task.triggersTaskIds).containsExactly("cleanup-task-20", "cleanup-task-21")
    }
    
    @Test
    fun `task with inventory requirement`() {
        // Given
        val task = Task(
            name = "Change Air Filter",
            category = "Household",
            requiresInventory = true
        )
        
        // Then
        assertThat(task.requiresInventory).isTrue()
    }
    
    @Test
    fun `task with time estimate`() {
        // Given
        val task = Task(
            name = "Morning Routine",
            category = "Personal",
            timeEstimate = 30 // 30 minutes
        )
        
        // Then
        assertThat(task.timeEstimate).isEqualTo(30)
    }
    
    @Test
    fun `task with difficulty indicator`() {
        // Given
        val task = Task(
            name = "Deep Clean Kitchen",
            category = "Household",
            difficulty = Difficulty.HIGH
        )
        
        // Then
        assertThat(task.difficulty).isEqualTo(Difficulty.HIGH)
    }
    
    @Test
    fun `task with tags for searching`() {
        // Given
        val task = Task(
            name = "Weekly Review",
            category = "Work",
            tags = "planning,productivity,important"
        )
        
        // Then
        assertThat(task.tags).isEqualTo("planning,productivity,important")
    }
    
    @Test
    fun `task with next due date and last completed`() {
        // Given
        val nextDue = LocalDate.of(2025, 10, 25)
        val lastCompleted = LocalDate.of(2025, 10, 22)
        
        val task = Task(
            name = "Daily Task",
            category = "Personal",
            nextDue = nextDue,
            lastCompleted = lastCompleted,
            completionStreak = 3
        )
        
        // Then
        assertThat(task.nextDue).isEqualTo(nextDue)
        assertThat(task.lastCompleted).isEqualTo(lastCompleted)
        assertThat(task.completionStreak).isEqualTo(3)
    }
    
    @Test
    fun `archived task is not active`() {
        // Given
        val task = Task(
            name = "Old Task",
            category = "Work",
            active = false
        )
        
        // Then
        assertThat(task.active).isFalse()
    }
}
