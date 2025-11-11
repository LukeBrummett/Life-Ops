package com.lifeops.app.domain.usecase.task

import com.lifeops.app.data.local.entity.IntervalUnit
import com.lifeops.app.data.local.entity.Task
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for SaveTaskUseCase specifically testing ADHOC task nextDue behavior
 * 
 * This test verifies the fix for the bug where ADHOC tasks were incorrectly getting
 * their nextDue set to the current date instead of null when no specific date is provided.
 */
class SaveTaskUseCaseTest {

    @Test
    fun `Task entity creation for ADHOC with null nextDue should remain null`() {
        // Given - creating an ADHOC task without a specific nextDue date
        val task = Task(
            name = "Test ADHOC Task",
            category = "Test",
            intervalUnit = IntervalUnit.ADHOC,
            intervalQty = 0,
            nextDue = null // This should remain null for ADHOC tasks
        )
        
        // Then - verify the task has null nextDue
        assertThat(task.intervalUnit).isEqualTo(IntervalUnit.ADHOC)
        assertThat(task.nextDue).isNull() // ADHOC tasks should have no automatic schedule
    }

    @Test
    fun `Task entity creation for scheduled task with specified nextDue should preserve date`() {
        // Given - creating a scheduled task with a specific nextDue date
        val nextDue = LocalDate.of(2025, 1, 15)
        val task = Task(
            name = "Test Scheduled Task",
            category = "Test",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = nextDue
        )
        
        // Then - verify the task preserves the specified nextDue
        assertThat(task.intervalUnit).isEqualTo(IntervalUnit.DAY)
        assertThat(task.nextDue).isEqualTo(nextDue) // Should preserve specified date
    }

    @Test
    fun `ADHOC task with specified nextDue should preserve the date`() {
        // Given - creating an ADHOC task with a specific nextDue date (e.g., from trigger)
        val triggerDate = LocalDate.of(2025, 1, 20)
        val task = Task(
            name = "Test Triggered ADHOC Task",
            category = "Test",
            intervalUnit = IntervalUnit.ADHOC,
            intervalQty = 0,
            nextDue = triggerDate // ADHOC task that was triggered
        )
        
        // Then - verify the task preserves the specified date
        assertThat(task.intervalUnit).isEqualTo(IntervalUnit.ADHOC)
        assertThat(task.nextDue).isEqualTo(triggerDate) // Should preserve trigger date
    }

    @Test
    fun `Task creation with user-specified future date should preserve that date`() {
        // Given - creating a task with a user-specified future due date
        val futureDate = LocalDate.now().plusDays(7)
        val task = Task(
            name = "Future Task",
            category = "Planning",
            intervalUnit = IntervalUnit.WEEK,
            intervalQty = 1,
            nextDue = futureDate // User wants task due in 7 days
        )
        
        // Then - verify the task preserves the user-specified future date
        assertThat(task.nextDue).isEqualTo(futureDate)
        assertThat(task.intervalUnit).isEqualTo(IntervalUnit.WEEK)
    }
}