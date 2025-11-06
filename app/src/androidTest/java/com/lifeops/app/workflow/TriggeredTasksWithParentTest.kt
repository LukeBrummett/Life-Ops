package com.lifeops.app.workflow

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.lifeops.app.data.local.LifeOpsDatabase
import com.lifeops.app.data.local.dao.TaskDao
import com.lifeops.app.data.local.entity.*
import com.lifeops.app.data.repository.TaskRepository
import com.lifeops.app.domain.usecase.CompleteTaskUseCase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * Integration tests for Triggered Tasks with Parent Tasks feature
 * 
 * Tests the scenario where:
 * - A parent task has child tasks
 * - Child tasks can trigger additional tasks
 * - Triggered tasks should appear under the parent when both are due today
 * - Triggered tasks should NOT be scheduled by the parent's schedule
 */
@RunWith(AndroidJUnit4::class)
class TriggeredTasksWithParentTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private lateinit var database: LifeOpsDatabase
    private lateinit var taskDao: TaskDao
    private lateinit var taskRepository: TaskRepository
    private lateinit var completeTaskUseCase: CompleteTaskUseCase
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LifeOpsDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        
        taskDao = database.taskDao()
        taskRepository = TaskRepository(taskDao)
        completeTaskUseCase = CompleteTaskUseCase(taskRepository)
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun triggered_tasks_with_parent_appear_under_parent_when_triggered() = runTest {
        // Given - Setup the "Clean Kitchen" workflow
        val today = LocalDate.now()
        
        // Parent task: Clean Kitchen (scheduled for today)
        val parentTask = Task(
            id = "clean-kitchen-id",
            name = "Clean Kitchen",
            category = "Household",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = today,
            requiresManualCompletion = false
        )
        taskDao.insert(parentTask)
        
        // Child task: Load Dishwasher (scheduled for today, part of parent)
        val loadDishwasherTask = Task(
            id = "load-dishwasher-id",
            name = "Load Dishwasher",
            category = "Household",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = today,
            parentTaskIds = listOf(parentTask.id),
            childOrder = 1,
            triggersTaskIds = listOf("unload-dishwasher-id", "clean-sink-id")
        )
        taskDao.insert(loadDishwasherTask)
        
        // Triggered task 1: Unload Dishwasher (ADHOC, triggered by Load Dishwasher, part of parent)
        val unloadDishwasherTask = Task(
            id = "unload-dishwasher-id",
            name = "Unload Dishwasher",
            category = "Household",
            intervalUnit = IntervalUnit.ADHOC,
            intervalQty = 0,
            nextDue = null, // Will be set when triggered
            parentTaskIds = listOf(parentTask.id), // Same parent as triggering task
            childOrder = 2, // Appears after Load Dishwasher in parent's child list
            triggeredByTaskIds = listOf(loadDishwasherTask.id)
        )
        taskDao.insert(unloadDishwasherTask)
        
        // Triggered task 2: Clean Sink (ADHOC, triggered by Load Dishwasher, part of parent)
        val cleanSinkTask = Task(
            id = "clean-sink-id",
            name = "Clean Sink",
            category = "Household",
            intervalUnit = IntervalUnit.ADHOC,
            intervalQty = 0,
            nextDue = null, // Will be set when triggered
            parentTaskIds = listOf(parentTask.id), // Same parent as triggering task
            childOrder = 3, // Appears after Unload Dishwasher in parent's child list
            triggeredByTaskIds = listOf(loadDishwasherTask.id)
        )
        taskDao.insert(cleanSinkTask)
        
        // Verify initial state - only parent and Load Dishwasher are due today
        val tasksBeforeTrigger = taskDao.observeTasksDueByDate(today)
        var tasksList = mutableListOf<Task>()
        tasksBeforeTrigger.collect { tasks ->
            tasksList = tasks.toMutableList()
            return@collect
        }
        
        assertThat(tasksList).hasSize(2) // Parent and Load Dishwasher
        assertThat(tasksList.map { it.name }).containsExactly(
            "Clean Kitchen",
            "Load Dishwasher"
        )
        
        // When - User completes "Load Dishwasher" task
        completeTaskUseCase(loadDishwasherTask.id, today)
        
        // Then - Triggered tasks should now be due today
        val tasksAfterTrigger = taskDao.getTasksDueByDate(today)
        
        // Should have parent, Load Dishwasher (completed), and 2 triggered tasks
        assertThat(tasksAfterTrigger).hasSize(4)
        assertThat(tasksAfterTrigger.map { it.name }).containsExactly(
            "Clean Kitchen",
            "Load Dishwasher",
            "Unload Dishwasher",
            "Clean Sink"
        )
        
        // Verify triggered tasks have nextDue set to today
        val unloadAfterTrigger = tasksAfterTrigger.find { it.id == "unload-dishwasher-id" }
        assertThat(unloadAfterTrigger?.nextDue).isEqualTo(today)
        
        val cleanSinkAfterTrigger = tasksAfterTrigger.find { it.id == "clean-sink-id" }
        assertThat(cleanSinkAfterTrigger?.nextDue).isEqualTo(today)
        
        // Verify triggered tasks have the correct parent
        assertThat(unloadAfterTrigger?.parentTaskIds).containsExactly(parentTask.id)
        assertThat(cleanSinkAfterTrigger?.parentTaskIds).containsExactly(parentTask.id)
        
        // Verify child tasks can be retrieved
        val childrenOfParent = taskDao.getChildrenOfParent(parentTask.id)
        assertThat(childrenOfParent).hasSize(3) // Load, Unload, Clean Sink
        assertThat(childrenOfParent.map { it.name }).containsExactly(
            "Load Dishwasher",
            "Unload Dishwasher",
            "Clean Sink"
        )
    }
    
    @Test
    fun triggered_tasks_with_parent_do_not_appear_when_not_triggered() = runTest {
        // Given - Setup the same workflow but don't complete the triggering task
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        
        // Parent task: Clean Kitchen (scheduled for tomorrow, not today)
        val parentTask = Task(
            id = "clean-kitchen-id",
            name = "Clean Kitchen",
            category = "Household",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = tomorrow,
            requiresManualCompletion = false
        )
        taskDao.insert(parentTask)
        
        // Triggered task: Unload Dishwasher (ADHOC, NOT triggered yet, part of parent)
        val unloadDishwasherTask = Task(
            id = "unload-dishwasher-id",
            name = "Unload Dishwasher",
            category = "Household",
            intervalUnit = IntervalUnit.ADHOC,
            intervalQty = 0,
            nextDue = null, // Not triggered
            parentTaskIds = listOf(parentTask.id),
            childOrder = 2,
            triggeredByTaskIds = listOf("some-trigger-task-id")
        )
        taskDao.insert(unloadDishwasherTask)
        
        // When - Check tasks due today
        val tasksDueToday = taskDao.getTasksDueByDate(today)
        
        // Then - Triggered task should NOT appear because:
        // 1. It's ADHOC (no automatic schedule)
        // 2. It hasn't been triggered (nextDue is null)
        // 3. Even though it has a parent, the parent is due tomorrow, not today
        assertThat(tasksDueToday).isEmpty()
        
        // Verify parent is due tomorrow
        val tasksDueTomorrow = taskDao.getTasksDueByDate(tomorrow)
        assertThat(tasksDueTomorrow).hasSize(1)
        assertThat(tasksDueTomorrow[0].name).isEqualTo("Clean Kitchen")
    }
    
    @Test
    fun triggered_tasks_with_parent_are_included_when_both_parent_and_triggered_task_are_due() = runTest {
        // Given - Parent is due today AND triggered task was triggered (nextDue = today)
        val today = LocalDate.now()
        
        val parentTask = Task(
            id = "clean-kitchen-id",
            name = "Clean Kitchen",
            category = "Household",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = today
        )
        taskDao.insert(parentTask)
        
        val triggeredTask = Task(
            id = "unload-dishwasher-id",
            name = "Unload Dishwasher",
            category = "Household",
            intervalUnit = IntervalUnit.ADHOC,
            intervalQty = 0,
            nextDue = today, // Already triggered
            parentTaskIds = listOf(parentTask.id),
            childOrder = 1
        )
        taskDao.insert(triggeredTask)
        
        // When - Get tasks due today
        val tasksDueToday = taskDao.getTasksDueByDate(today)
        
        // Then - Both should appear
        assertThat(tasksDueToday).hasSize(2)
        assertThat(tasksDueToday.map { it.name }).containsExactly(
            "Clean Kitchen",
            "Unload Dishwasher"
        )
    }
}
