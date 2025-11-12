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
 * Test for ADHOC child task behavior in Today view query
 * 
 * This test verifies the fix for the bug where ADHOC child tasks were appearing
 * in the Today view just because their parent task was due, even when the ADHOC
 * task had nextDue = null (not triggered).
 */
@RunWith(AndroidJUnit4::class)
class AdhocChildTaskQueryTest {

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
    fun adhocChildTaskWithNullNextDueShouldNotAppearInTodayView() = runTest {
        // Given
        val today = LocalDate.of(2025, 11, 5)
        
        // Parent task: Clean Kitchen (scheduled for today)
        val parentTask = Task(
            id = "clean-kitchen",
            name = "Clean Kitchen", 
            category = "Household",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = today // Due today
        )
        
        // ADHOC child task: Laundry (not triggered, should NOT appear)
        val adhocChildTask = Task(
            id = "laundry",
            name = "Laundry",
            category = "Household", 
            intervalUnit = IntervalUnit.ADHOC,
            intervalQty = 0,
            nextDue = null, // Not triggered
            parentTaskIds = listOf("clean-kitchen")
        )
        
        taskDao.insert(parentTask)
        taskDao.insert(adhocChildTask)
        
        // When - Get tasks due today
        val tasksDueToday = taskDao.observeTasksDueByDate(today).first()
        
        // Then - Only parent task should appear, NOT the ADHOC child
        assertThat(tasksDueToday).hasSize(1)
        assertThat(tasksDueToday[0].id).isEqualTo("clean-kitchen")
        assertThat(tasksDueToday.none { it.id == "laundry" }).isTrue()
    }

    @Test
    fun adhocChildTaskWithNextDueSetShouldAppearInTodayView() = runTest {
        // Given
        val today = LocalDate.of(2025, 11, 5)
        
        // Parent task: Clean Kitchen (scheduled for today)
        val parentTask = Task(
            id = "clean-kitchen",
            name = "Clean Kitchen",
            category = "Household",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = today
        )
        
        // ADHOC child task: Laundry (triggered, should appear)
        val adhocChildTask = Task(
            id = "laundry", 
            name = "Laundry",
            category = "Household",
            intervalUnit = IntervalUnit.ADHOC,
            intervalQty = 0,
            nextDue = today, // Triggered - set by trigger mechanism
            parentTaskIds = listOf("clean-kitchen")
        )
        
        taskDao.insert(parentTask)
        taskDao.insert(adhocChildTask)
        
        // When - Get tasks due today
        val tasksDueToday = taskDao.observeTasksDueByDate(today).first()
        
        // Then - Both parent and triggered child should appear
        assertThat(tasksDueToday).hasSize(2)
        assertThat(tasksDueToday.map { it.id }).containsExactly("clean-kitchen", "laundry")
    }

    @Test
    fun adhocChildTaskCompletedButNotTriggeredShouldNotAppear() = runTest {
        // Given
        val today = LocalDate.of(2025, 11, 5)
        
        // Parent task: Clean Kitchen (scheduled for today) 
        val parentTask = Task(
            id = "clean-kitchen",
            name = "Clean Kitchen",
            category = "Household", 
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = today
        )
        
        // ADHOC child task: completed today but never triggered (nextDue still null)
        val adhocChildTask = Task(
            id = "laundry",
            name = "Laundry", 
            category = "Household",
            intervalUnit = IntervalUnit.ADHOC,
            intervalQty = 0,
            nextDue = null, // Never triggered
            lastCompleted = today, // But completed today
            parentTaskIds = listOf("clean-kitchen")
        )
        
        taskDao.insert(parentTask)
        taskDao.insert(adhocChildTask)
        
        // When - Get tasks due today
        val tasksDueToday = taskDao.observeTasksDueByDate(today).first()
        
        // Then - Only parent task should appear, NOT the completed but never-triggered ADHOC child
        assertThat(tasksDueToday).hasSize(1)
        assertThat(tasksDueToday[0].id).isEqualTo("clean-kitchen")
        assertThat(tasksDueToday.none { it.id == "laundry" }).isTrue()
    }

    @Test
    fun adhocChildTaskCompletedAndTriggeredShouldAppear() = runTest {
        // Given
        val today = LocalDate.of(2025, 11, 5)
        
        // Parent task: Clean Kitchen (scheduled for today)
        val parentTask = Task(
            id = "clean-kitchen",
            name = "Clean Kitchen",
            category = "Household",
            intervalUnit = IntervalUnit.DAY, 
            intervalQty = 1,
            nextDue = today
        )
        
        // ADHOC child task: was triggered (has nextDue) and completed today
        val adhocChildTask = Task(
            id = "laundry",
            name = "Laundry",
            category = "Household",
            intervalUnit = IntervalUnit.ADHOC,
            intervalQty = 0, 
            nextDue = today, // Was triggered
            lastCompleted = today, // And completed today  
            parentTaskIds = listOf("clean-kitchen")
        )
        
        taskDao.insert(parentTask)
        taskDao.insert(adhocChildTask)
        
        // When - Get tasks due today
        val tasksDueToday = taskDao.observeTasksDueByDate(today).first()
        
        // Then - Both parent and triggered/completed child should appear
        assertThat(tasksDueToday).hasSize(2)
        assertThat(tasksDueToday.map { it.id }).containsExactly("clean-kitchen", "laundry")
    }
}