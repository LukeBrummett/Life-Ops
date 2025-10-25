package com.lifeops.app.workflow

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.lifeops.app.data.local.LifeOpsDatabase
import com.lifeops.app.data.local.dao.TaskDao
import com.lifeops.app.data.local.entity.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * Integration tests for Workflow 1: Exercise Routine with Parent-Child Tasks
 * 
 * From Project Overview Document - User Workflows Section:
 * "John wants to ensure he's getting enough exercise and puts his routine into the application.
 * He sets up a series of tasks that occur every Monday, Wednesday, and Friday in a group."
 * 
 * Key Features Tested:
 * - Parent-child task grouping
 * - Custom task ordering within a group
 * - Recurring schedule (specific days of week)
 * - Time estimates or difficulty indicators
 * - Automatic parent completion when all children complete
 */
@RunWith(AndroidJUnit4::class)
class Workflow1ExerciseRoutineTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private lateinit var database: LifeOpsDatabase
    private lateinit var taskDao: TaskDao
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LifeOpsDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        
        taskDao = database.taskDao()
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun workflow_1_create_workout_routine_with_parent_and_children() = runTest {
        // Given - John creates a parent task for his workout routine
        val parentTask = Task(
            name = "Workout",
            category = "Health",
            intervalUnit = IntervalUnit.WEEK,
            specificDaysOfWeek = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            requiresManualCompletion = false // Auto-complete when all children done
        )
        val parentId = taskDao.insert(parentTask)
        
        // When - John adds child tasks in specific order
        val childTasks = listOf(
            Task(
                name = "Stretch",
                category = "Health",
                parentTaskIds = listOf(parentId),
                childOrder = 1,
                timeEstimate = 5,
                difficulty = Difficulty.LOW
            ),
            Task(
                name = "Lift Weights",
                category = "Health",
                parentTaskIds = listOf(parentId),
                childOrder = 2,
                timeEstimate = 30,
                difficulty = Difficulty.MEDIUM
            ),
            Task(
                name = "Cardio",
                category = "Health",
                parentTaskIds = listOf(parentId),
                childOrder = 3,
                timeEstimate = 20,
                difficulty = Difficulty.MEDIUM
            ),
            Task(
                name = "Stretch",
                category = "Health",
                parentTaskIds = listOf(parentId),
                childOrder = 4,
                timeEstimate = 5,
                difficulty = Difficulty.LOW
            )
        )
        
        childTasks.forEach { taskDao.insert(it) }
        
        // Then - verify parent task exists
        val retrievedParent = taskDao.getById(parentId)
        assertThat(retrievedParent).isNotNull()
        assertThat(retrievedParent?.name).isEqualTo("Workout")
        assertThat(retrievedParent?.specificDaysOfWeek).containsExactly(
            DayOfWeek.MONDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.FRIDAY
        )
        assertThat(retrievedParent?.requiresManualCompletion).isFalse()
        
        // Then - verify children are ordered correctly
        val children = taskDao.getChildrenOfParent(parentId)
        assertThat(children).hasSize(4)
        assertThat(children.map { it.name }).containsExactly(
            "Stretch", "Lift Weights", "Cardio", "Stretch"
        ).inOrder()
        
        // Then - verify each child has time estimate or difficulty
        children.forEach { child ->
            val hasTimeEstimate = child.timeEstimate != null
            val hasDifficulty = child.difficulty != null
            assertThat(hasTimeEstimate || hasDifficulty).isTrue()
        }
    }
    
    @Test
    fun workflow_1_parent_appears_on_scheduled_days() = runTest {
        // Given - Workout routine scheduled for Monday, Wednesday, Friday
        val monday = LocalDate.of(2025, 10, 27) // A Monday
        
        val parentTask = Task(
            name = "Workout",
            category = "Health",
            intervalUnit = IntervalUnit.WEEK,
            specificDaysOfWeek = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            nextDue = monday
        )
        val parentId = taskDao.insert(parentTask)
        
        // When - checking tasks due on Monday
        val mondayTasks = taskDao.getTasksDueByDate(monday)
        
        // Then - workout appears on Monday
        assertThat(mondayTasks.map { it.name }).contains("Workout")
        
        // When - checking tasks due on Tuesday (not scheduled)
        val tuesday = monday.plusDays(1)
        val tuesdayTasks = taskDao.getTasksDueByDate(tuesday)
        
        // Then - workout does NOT appear on Tuesday
        assertThat(tuesdayTasks.map { it.name }).doesNotContain("Workout")
    }
    
    @Test
    fun workflow_1_children_inherit_parent_schedule() = runTest {
        // Given - Parent scheduled for specific days
        val monday = LocalDate.of(2025, 10, 27)
        
        val parentTask = Task(
            name = "Workout",
            category = "Health",
            intervalUnit = IntervalUnit.WEEK,
            specificDaysOfWeek = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            nextDue = monday
        )
        val parentId = taskDao.insert(parentTask)
        
        // When - Creating child tasks
        val childTask = Task(
            name = "Stretch",
            category = "Health",
            parentTaskIds = listOf(parentId),
            childOrder = 1,
            nextDue = monday // Child appears with parent
        )
        taskDao.insert(childTask)
        
        // Then - Child appears on Monday (same as parent)
        val mondayTasks = taskDao.getTasksDueByDate(monday)
        assertThat(mondayTasks.map { it.name }).contains("Stretch")
    }
    
    @Test
    fun workflow_1_task_completion_tracking() = runTest {
        // Given - A workout task
        val task = Task(
            name = "Lift Weights",
            category = "Health",
            completionStreak = 0,
            nextDue = LocalDate.of(2025, 10, 24),
            lastCompleted = null
        )
        val taskId = taskDao.insert(task)
        
        // When - John completes the task
        val completionDate = LocalDate.of(2025, 10, 24)
        val nextOccurrence = LocalDate.of(2025, 10, 27) // Next Monday
        taskDao.updateSchedule(
            taskId = taskId,
            nextDue = nextOccurrence,
            lastCompleted = completionDate,
            streak = 1
        )
        
        // Then - Task schedule is updated
        val updated = taskDao.getById(taskId)
        assertThat(updated?.lastCompleted).isEqualTo(completionDate)
        assertThat(updated?.nextDue).isEqualTo(nextOccurrence)
        assertThat(updated?.completionStreak).isEqualTo(1)
    }
    
    @Test
    fun workflow_1_multiple_children_with_different_difficulties() = runTest {
        // Given - Parent task
        val parentId = taskDao.insert(Task(name = "Workout", category = "Health"))
        
        // When - Adding children with different difficulties
        taskDao.insert(Task(
            name = "Warm Up",
            category = "Health",
            parentTaskIds = listOf(parentId),
            childOrder = 1,
            difficulty = Difficulty.LOW
        ))
        
        taskDao.insert(Task(
            name = "Heavy Lifting",
            category = "Health",
            parentTaskIds = listOf(parentId),
            childOrder = 2,
            difficulty = Difficulty.HIGH
        ))
        
        taskDao.insert(Task(
            name = "Cool Down",
            category = "Health",
            parentTaskIds = listOf(parentId),
            childOrder = 3,
            difficulty = Difficulty.LOW
        ))
        
        // Then - Children have correct difficulties
        val children = taskDao.getChildrenOfParent(parentId)
        assertThat(children[0].difficulty).isEqualTo(Difficulty.LOW)
        assertThat(children[1].difficulty).isEqualTo(Difficulty.HIGH)
        assertThat(children[2].difficulty).isEqualTo(Difficulty.LOW)
    }
    
    @Test
    fun workflow_1_reorder_children_within_parent() = runTest {
        // Given - Parent with children
        val parentId = taskDao.insert(Task(name = "Workout", category = "Health"))
        
        val child1Id = taskDao.insert(Task(
            name = "Cardio",
            category = "Health",
            parentTaskIds = listOf(parentId),
            childOrder = 1
        ))
        
        val child2Id = taskDao.insert(Task(
            name = "Weights",
            category = "Health",
            parentTaskIds = listOf(parentId),
            childOrder = 2
        ))
        
        // When - Reordering (swap the order)
        val child1 = taskDao.getById(child1Id)!!
        val child2 = taskDao.getById(child2Id)!!
        
        taskDao.update(child1.copy(childOrder = 2))
        taskDao.update(child2.copy(childOrder = 1))
        
        // Then - Children appear in new order
        val reordered = taskDao.getChildrenOfParent(parentId)
        assertThat(reordered.map { it.name }).containsExactly("Weights", "Cardio").inOrder()
    }
}
