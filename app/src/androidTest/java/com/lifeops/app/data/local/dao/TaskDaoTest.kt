package com.lifeops.app.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.lifeops.app.data.local.LifeOpsDatabase
import com.lifeops.app.data.local.entity.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * Integration tests for TaskDao
 * Tests database operations with in-memory Room database
 * 
 * As per Testing Strategy specification:
 * - Integration Tests: Database + DAO operations
 * - Use in-memory database for fast, isolated tests
 */
@RunWith(AndroidJUnit4::class)
class TaskDaoTest {
    
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
    
    // ============================================
    // Create Operations
    // ============================================
    
    @Test
    fun insertTask_returnsGeneratedId() = runTest {
        // Given
        val task = Task(
            name = "Test Task",
            category = "Work"
        )
        
        // When
        val id = taskDao.insert(task)
        
        // Then
        assertThat(id).isGreaterThan(0)
    }
    
    @Test
    fun insertMultipleTasks_returnsAllIds() = runTest {
        // Given
        val tasks = listOf(
            Task(name = "Task 1", category = "Work"),
            Task(name = "Task 2", category = "Home"),
            Task(name = "Task 3", category = "Health")
        )
        
        // When
        val ids = taskDao.insertAll(tasks)
        
        // Then
        assertThat(ids).hasSize(3)
        assertThat(ids).doesNotContain(0L)
    }
    
    // ============================================
    // Read Operations
    // ============================================
    
    @Test
    fun getById_returnsCorrectTask() = runTest {
        // Given
        val task = Task(name = "Test Task", category = "Work")
        val id = taskDao.insert(task)
        
        // When
        val retrieved = taskDao.getById(id)
        
        // Then
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.name).isEqualTo("Test Task")
        assertThat(retrieved?.category).isEqualTo("Work")
    }
    
    @Test
    fun getById_nonExistentTask_returnsNull() = runTest {
        // When
        val retrieved = taskDao.getById(999L)
        
        // Then
        assertThat(retrieved).isNull()
    }
    
    @Test
    fun getAllActive_returnsOnlyActiveTasks() = runTest {
        // Given
        taskDao.insert(Task(name = "Active 1", category = "Work", active = true))
        taskDao.insert(Task(name = "Active 2", category = "Home", active = true))
        taskDao.insert(Task(name = "Archived", category = "Work", active = false))
        
        // When
        val activeTasks = taskDao.getAllActive()
        
        // Then
        assertThat(activeTasks).hasSize(2)
        assertThat(activeTasks.map { it.name }).containsExactly("Active 1", "Active 2")
    }
    
    @Test
    fun observeAllActive_emitsUpdates() = runTest {
        // Given - initial state
        val task = Task(name = "Task 1", category = "Work")
        taskDao.insert(task)
        
        // When - collect initial value
        val initial = taskDao.observeAllActive().first()
        
        // Then
        assertThat(initial).hasSize(1)
        
        // When - insert another task
        taskDao.insert(Task(name = "Task 2", category = "Home"))
        val updated = taskDao.observeAllActive().first()
        
        // Then
        assertThat(updated).hasSize(2)
    }
    
    @Test
    fun getTasksDueByDate_returnsDueAndOverdueTasks() = runTest {
        // Given
        val today = LocalDate.of(2025, 10, 24)
        val yesterday = today.minusDays(1)
        val tomorrow = today.plusDays(1)
        
        taskDao.insert(Task(name = "Overdue", category = "Work", nextDue = yesterday))
        taskDao.insert(Task(name = "Due Today", category = "Work", nextDue = today))
        taskDao.insert(Task(name = "Future", category = "Work", nextDue = tomorrow))
        taskDao.insert(Task(name = "No Due Date", category = "Work", nextDue = null))
        
        // When
        val dueTasks = taskDao.getTasksDueByDate(today)
        
        // Then
        assertThat(dueTasks).hasSize(2)
        assertThat(dueTasks.map { it.name }).containsExactly("Overdue", "Due Today")
    }
    
    @Test
    fun getByCategory_returnsTasksInCategory() = runTest {
        // Given
        taskDao.insert(Task(name = "Work 1", category = "Work"))
        taskDao.insert(Task(name = "Work 2", category = "Work"))
        taskDao.insert(Task(name = "Home 1", category = "Home"))
        
        // When
        val workTasks = taskDao.getByCategory("Work")
        
        // Then
        assertThat(workTasks).hasSize(2)
        assertThat(workTasks.map { it.name }).containsExactly("Work 1", "Work 2")
    }
    
    @Test
    fun getChildrenOfParent_returnsChildrenOrderedByChildOrder() = runTest {
        // Given
        val parentId = taskDao.insert(Task(name = "Parent", category = "Work"))
        
        taskDao.insert(Task(name = "Child 3", category = "Work", parentTaskIds = listOf(parentId), childOrder = 3))
        taskDao.insert(Task(name = "Child 1", category = "Work", parentTaskIds = listOf(parentId), childOrder = 1))
        taskDao.insert(Task(name = "Child 2", category = "Work", parentTaskIds = listOf(parentId), childOrder = 2))
        taskDao.insert(Task(name = "Other", category = "Work")) // Not a child
        
        // When
        val children = taskDao.getChildrenOfParent(parentId)
        
        // Then
        assertThat(children).hasSize(3)
        assertThat(children.map { it.name }).containsExactly("Child 1", "Child 2", "Child 3").inOrder()
    }
    
    @Test
    fun search_findsByName() = runTest {
        // Given
        taskDao.insert(Task(name = "Clean Kitchen", category = "Home"))
        taskDao.insert(Task(name = "Clean Bathroom", category = "Home"))
        taskDao.insert(Task(name = "Cook Dinner", category = "Home"))
        
        // When
        val results = taskDao.search("Clean")
        
        // Then
        assertThat(results).hasSize(2)
        assertThat(results.map { it.name }).containsExactly("Clean Bathroom", "Clean Kitchen")
    }
    
    @Test
    fun search_findsByCategory() = runTest {
        // Given
        taskDao.insert(Task(name = "Task 1", category = "Health"))
        taskDao.insert(Task(name = "Task 2", category = "Health Fitness"))
        taskDao.insert(Task(name = "Task 3", category = "Work"))
        
        // When
        val results = taskDao.search("Health")
        
        // Then
        assertThat(results).hasSize(2)
    }
    
    @Test
    fun search_findsByTags() = runTest {
        // Given
        taskDao.insert(Task(name = "Task 1", category = "Work", tags = "urgent,important"))
        taskDao.insert(Task(name = "Task 2", category = "Work", tags = "important"))
        taskDao.insert(Task(name = "Task 3", category = "Work", tags = "routine"))
        
        // When
        val results = taskDao.search("urgent")
        
        // Then
        assertThat(results).hasSize(1)
        assertThat(results.first().name).isEqualTo("Task 1")
    }
    
    @Test
    fun getAllOrderedByNextDue_sortsCorrectly() = runTest {
        // Given
        val today = LocalDate.of(2025, 10, 24)
        
        taskDao.insert(Task(name = "Future", category = "Work", nextDue = today.plusDays(5)))
        taskDao.insert(Task(name = "Tomorrow", category = "Work", nextDue = today.plusDays(1)))
        taskDao.insert(Task(name = "No Date", category = "Work", nextDue = null))
        taskDao.insert(Task(name = "Today", category = "Work", nextDue = today))
        
        // When
        val ordered = taskDao.getAllOrderedByNextDue()
        
        // Then
        assertThat(ordered.map { it.name }).containsExactly(
            "Today", "Tomorrow", "Future", "No Date"
        ).inOrder()
    }
    
    @Test
    fun getAllCategories_returnsUniqueCategories() = runTest {
        // Given
        taskDao.insert(Task(name = "Task 1", category = "Work"))
        taskDao.insert(Task(name = "Task 2", category = "Home"))
        taskDao.insert(Task(name = "Task 3", category = "Work"))
        taskDao.insert(Task(name = "Task 4", category = "Health"))
        
        // When
        val categories = taskDao.getAllCategories()
        
        // Then
        assertThat(categories).hasSize(3)
        assertThat(categories).containsExactly("Health", "Home", "Work")
    }
    
    // ============================================
    // Update Operations
    // ============================================
    
    @Test
    fun updateTask_modifiesExistingTask() = runTest {
        // Given
        val task = Task(name = "Original", category = "Work")
        val id = taskDao.insert(task)
        
        // When
        val updated = task.copy(id = id, name = "Updated")
        taskDao.update(updated)
        
        // Then
        val retrieved = taskDao.getById(id)
        assertThat(retrieved?.name).isEqualTo("Updated")
    }
    
    @Test
    fun updateSchedule_updatesDateFields() = runTest {
        // Given
        val task = Task(name = "Task", category = "Work")
        val id = taskDao.insert(task)
        
        val newNextDue = LocalDate.of(2025, 10, 27)
        val newLastCompleted = LocalDate.of(2025, 10, 24)
        
        // When
        taskDao.updateSchedule(id, newNextDue, newLastCompleted, 5)
        
        // Then
        val retrieved = taskDao.getById(id)
        assertThat(retrieved?.nextDue).isEqualTo(newNextDue)
        assertThat(retrieved?.lastCompleted).isEqualTo(newLastCompleted)
        assertThat(retrieved?.completionStreak).isEqualTo(5)
    }
    
    @Test
    fun archiveTask_setsActiveToFalse() = runTest {
        // Given
        val task = Task(name = "Task", category = "Work", active = true)
        val id = taskDao.insert(task)
        
        // When
        taskDao.archive(id)
        
        // Then
        val retrieved = taskDao.getById(id)
        assertThat(retrieved?.active).isFalse()
        
        // And archived tasks don't appear in active list
        val activeTasks = taskDao.getAllActive()
        assertThat(activeTasks).isEmpty()
    }
    
    @Test
    fun restoreTask_setsActiveToTrue() = runTest {
        // Given
        val task = Task(name = "Task", category = "Work", active = false)
        val id = taskDao.insert(task)
        
        // When
        taskDao.restore(id)
        
        // Then
        val retrieved = taskDao.getById(id)
        assertThat(retrieved?.active).isTrue()
        
        // And restored tasks appear in active list
        val activeTasks = taskDao.getAllActive()
        assertThat(activeTasks).hasSize(1)
    }
    
    // ============================================
    // Delete Operations
    // ============================================
    
    @Test
    fun deleteById_removesTask() = runTest {
        // Given
        val task = Task(name = "Task", category = "Work")
        val id = taskDao.insert(task)
        
        // When
        taskDao.deleteById(id)
        
        // Then
        val retrieved = taskDao.getById(id)
        assertThat(retrieved).isNull()
    }
    
    @Test
    fun deleteAllArchived_removesOnlyArchivedTasks() = runTest {
        // Given
        taskDao.insert(Task(name = "Active 1", category = "Work", active = true))
        taskDao.insert(Task(name = "Archived 1", category = "Work", active = false))
        taskDao.insert(Task(name = "Archived 2", category = "Work", active = false))
        
        // When
        taskDao.deleteAllArchived()
        
        // Then
        val allTasks = taskDao.getAllActive()
        assertThat(allTasks).hasSize(1)
        assertThat(allTasks.first().name).isEqualTo("Active 1")
    }
    
    // ============================================
    // Utility Operations
    // ============================================
    
    @Test
    fun exists_returnsTrueForExistingTask() = runTest {
        // Given
        val task = Task(name = "Task", category = "Work")
        val id = taskDao.insert(task)
        
        // When
        val exists = taskDao.exists(id)
        
        // Then
        assertThat(exists).isTrue()
    }
    
    @Test
    fun exists_returnsFalseForNonExistentTask() = runTest {
        // When
        val exists = taskDao.exists(999L)
        
        // Then
        assertThat(exists).isFalse()
    }
    
    @Test
    fun getActiveTaskCount_returnsCorrectCount() = runTest {
        // Given
        taskDao.insert(Task(name = "Active 1", category = "Work", active = true))
        taskDao.insert(Task(name = "Active 2", category = "Work", active = true))
        taskDao.insert(Task(name = "Archived", category = "Work", active = false))
        
        // When
        val count = taskDao.getActiveTaskCount()
        
        // Then
        assertThat(count).isEqualTo(2)
    }
}
