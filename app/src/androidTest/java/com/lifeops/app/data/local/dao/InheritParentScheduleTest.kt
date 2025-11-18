package com.lifeops.app.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifeops.app.data.local.LifeOpsDatabase
import com.lifeops.app.data.local.entity.IntervalUnit
import com.lifeops.app.data.local.entity.Task
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * Test for inheritParentSchedule functionality
 * 
 * Verifies that tasks with inheritParentSchedule=true appear when their parent is due,
 * even if they don't have their own schedule.
 */
@RunWith(AndroidJUnit4::class)
class InheritParentScheduleTest {

    private lateinit var database: LifeOpsDatabase
    private lateinit var taskDao: TaskDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LifeOpsDatabase::class.java
        ).build()
        taskDao = database.taskDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun childTaskWithInheritTrueAppearsWhenParentIsDue() = runTest {
        // Given
        val today = LocalDate.of(2025, 11, 13)
        
        // Parent task: Clean Kitchen (scheduled for today)
        val parentTask = Task(
            id = "clean-kitchen",
            name = "Clean Kitchen", 
            category = "Household",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = today // Due today
        )
        
        // Child task with inheritParentSchedule=true and no own schedule (ADHOC)
        val childTask = Task(
            id = "wipe-counters",
            name = "Wipe Counters",
            category = "Household", 
            intervalUnit = IntervalUnit.ADHOC,
            intervalQty = 0,
            nextDue = null, // No own schedule
            parentTaskIds = listOf("clean-kitchen"),
            inheritParentSchedule = true // Should appear with parent
        )
        
        taskDao.insert(parentTask)
        taskDao.insert(childTask)
        
        // When - Get tasks due today
        val tasksDueToday = taskDao.observeTasksDueByDate(today).first()
        
        // Then - Both parent and child should appear
        assertThat(tasksDueToday).hasSize(2)
        assertThat(tasksDueToday.map { it.id }).containsExactly("clean-kitchen", "wipe-counters")
    }

    @Test
    fun childTaskWithInheritFalseDoesNotAppearWhenParentIsDue() = runTest {
        // Given
        val today = LocalDate.of(2025, 11, 13)
        
        // Parent task: Clean Kitchen (scheduled for today)
        val parentTask = Task(
            id = "clean-kitchen",
            name = "Clean Kitchen",
            category = "Household",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = today
        )
        
        // Child task with inheritParentSchedule=false and no own schedule
        val childTask = Task(
            id = "wipe-counters",
            name = "Wipe Counters",
            category = "Household",
            intervalUnit = IntervalUnit.ADHOC,
            intervalQty = 0,
            nextDue = null, // No own schedule
            parentTaskIds = listOf("clean-kitchen"),
            inheritParentSchedule = false // Should NOT appear with parent
        )
        
        taskDao.insert(parentTask)
        taskDao.insert(childTask)
        
        // When - Get tasks due today
        val tasksDueToday = taskDao.observeTasksDueByDate(today).first()
        
        // Then - Only parent should appear, NOT the child
        assertThat(tasksDueToday).hasSize(1)
        assertThat(tasksDueToday[0].id).isEqualTo("clean-kitchen")
        assertThat(tasksDueToday.none { it.id == "wipe-counters" }).isTrue()
    }

    @Test
    fun childTaskWithInheritTrueDoesNotAppearWhenParentNotDue() = runTest {
        // Given
        val today = LocalDate.of(2025, 11, 13)
        val tomorrow = LocalDate.of(2025, 11, 14)
        
        // Parent task: Clean Kitchen (scheduled for tomorrow)
        val parentTask = Task(
            id = "clean-kitchen",
            name = "Clean Kitchen",
            category = "Household",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = tomorrow // Not due today
        )
        
        // Child task with inheritParentSchedule=true
        val childTask = Task(
            id = "wipe-counters",
            name = "Wipe Counters",
            category = "Household",
            intervalUnit = IntervalUnit.ADHOC,
            intervalQty = 0,
            nextDue = null,
            parentTaskIds = listOf("clean-kitchen"),
            inheritParentSchedule = true
        )
        
        taskDao.insert(parentTask)
        taskDao.insert(childTask)
        
        // When - Get tasks due today
        val tasksDueToday = taskDao.observeTasksDueByDate(today).first()
        
        // Then - Neither task should appear
        assertThat(tasksDueToday).isEmpty()
    }

    @Test
    fun childTaskWithOwnScheduleAndInheritTrueAppearsOnEitherDate() = runTest {
        // Given
        val today = LocalDate.of(2025, 11, 13)
        val tomorrow = LocalDate.of(2025, 11, 14)
        
        // Parent task: scheduled for tomorrow
        val parentTask = Task(
            id = "clean-kitchen",
            name = "Clean Kitchen",
            category = "Household",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = tomorrow
        )
        
        // Child task: has own schedule for today AND inherits from parent
        val childTask = Task(
            id = "wipe-counters",
            name = "Wipe Counters",
            category = "Household",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = today, // Own schedule for today
            parentTaskIds = listOf("clean-kitchen"),
            inheritParentSchedule = true // Also inherits from parent
        )
        
        taskDao.insert(parentTask)
        taskDao.insert(childTask)
        
        // When - Get tasks due today
        val tasksDueToday = taskDao.observeTasksDueByDate(today).first()
        
        // Then - Only child appears (has own schedule for today)
        assertThat(tasksDueToday).hasSize(1)
        assertThat(tasksDueToday[0].id).isEqualTo("wipe-counters")
        
        // When - Get tasks due tomorrow
        val tasksDueTomorrow = taskDao.observeTasksDueByDate(tomorrow).first()
        
        // Then - Both appear (parent is due, child still shows because it inherits)
        assertThat(tasksDueTomorrow).hasSize(2)
        assertThat(tasksDueTomorrow.map { it.id }).contains("clean-kitchen")
        assertThat(tasksDueTomorrow.map { it.id }).contains("wipe-counters")
    }

    @Test
    fun scheduledChildWithInheritFalseAppearsEvenWhenParentNotDue() = runTest {
        // Test case for: "A child that is scheduled doesn't appear unless parent appears"
        // This should NOT happen - scheduled children should always appear based on their own schedule
        
        val today = LocalDate.of(2025, 11, 13)
        val tomorrow = LocalDate.of(2025, 11, 14)
        
        // Parent task: NOT due today (scheduled for tomorrow)
        val parentTask = Task(
            id = "clean-kitchen",
            name = "Clean Kitchen",
            category = "Household",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = tomorrow // NOT due today
        )
        
        // Child task: HAS own schedule for today, inheritParentSchedule=false
        val childTask = Task(
            id = "wipe-counters",
            name = "Wipe Counters",
            category = "Household",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = today, // Due today
            parentTaskIds = listOf("clean-kitchen"),
            inheritParentSchedule = false // Should appear based on own schedule
        )
        
        taskDao.insert(parentTask)
        taskDao.insert(childTask)
        
        // When - Get tasks due today
        val tasksDueToday = taskDao.observeTasksDueByDate(today).first()
        
        // Then - Child SHOULD appear even though parent is not due
        assertThat(tasksDueToday).hasSize(1)
        assertThat(tasksDueToday[0].id).isEqualTo("wipe-counters")
    }

    @Test
    fun childTaskWithInheritTrueWorksWithMultipleParents() = runTest {
        // Given
        val today = LocalDate.of(2025, 11, 13)
        val tomorrow = LocalDate.of(2025, 11, 14)
        
        // Parent 1: due today
        val parent1 = Task(
            id = "clean-kitchen",
            name = "Clean Kitchen",
            category = "Household",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = today
        )
        
        // Parent 2: due tomorrow
        val parent2 = Task(
            id = "laundry",
            name = "Laundry",
            category = "Household",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = tomorrow
        )
        
        // Child task with multiple parents and inheritParentSchedule=true
        val childTask = Task(
            id = "fold-towels",
            name = "Fold Towels",
            category = "Household",
            intervalUnit = IntervalUnit.ADHOC,
            intervalQty = 0,
            nextDue = null,
            parentTaskIds = listOf("clean-kitchen", "laundry"),
            inheritParentSchedule = true
        )
        
        taskDao.insert(parent1)
        taskDao.insert(parent2)
        taskDao.insert(childTask)
        
        // When - Get tasks due today
        val tasksDueToday = taskDao.observeTasksDueByDate(today).first()
        
        // Then - Parent 1 and child appear (child inherits from parent 1)
        assertThat(tasksDueToday).hasSize(2)
        assertThat(tasksDueToday.map { it.id }).containsExactly("clean-kitchen", "fold-towels")
        
        // When - Get tasks due tomorrow
        val tasksDueTomorrow = taskDao.observeTasksDueByDate(tomorrow).first()
        
        // Then - Both parents and child appear
        assertThat(tasksDueTomorrow).hasSize(3)
        assertThat(tasksDueTomorrow.map { it.id }).contains("clean-kitchen")
        assertThat(tasksDueTomorrow.map { it.id }).contains("laundry")
        assertThat(tasksDueTomorrow.map { it.id }).contains("fold-towels")
    }
}
