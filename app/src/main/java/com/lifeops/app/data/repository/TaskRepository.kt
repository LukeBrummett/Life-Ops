package com.lifeops.app.data.repository

import com.lifeops.app.data.local.dao.TaskDao
import com.lifeops.app.data.local.entity.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Task data operations
 * 
 * As per Clean Architecture specification:
 * - Abstraction over data sources (currently just Room database)
 * - Single source of truth
 * - Provides clean API to domain layer
 * - Handles data transformations if needed
 */
@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {
    
    // ============================================
    // Create
    // ============================================
    
    /**
     * Create a new task
     * @return The ID of the created task
     */
    suspend fun createTask(task: Task): Result<String> {
        return try {
            taskDao.insert(task)
            Result.success(task.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create multiple tasks
     * @return List of created task IDs
     */
    suspend fun createTasks(tasks: List<Task>): Result<List<String>> {
        return try {
            taskDao.insertAll(tasks)
            Result.success(tasks.map { it.id })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ============================================
    // Read
    // ============================================
    
    /**
     * Get a task by ID
     */
    suspend fun getTaskById(taskId: String): Result<Task?> {
        return try {
            val task = taskDao.getById(taskId)
            Result.success(task)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Observe a task by ID (reactive)
     */
    fun observeTask(taskId: String): Flow<Task?> {
        return taskDao.observeById(taskId)
    }
    
    /**
     * Get all tasks (including archived) for export/backup
     */
    suspend fun getAllTasks(): List<Task> {
        return taskDao.getAll()
    }
    
    /**
     * Get all active tasks
     */
    suspend fun getAllActiveTasks(): Result<List<Task>> {
        return try {
            val tasks = taskDao.getAllActive()
            Result.success(tasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Observe all active tasks (reactive)
     */
    fun observeAllActiveTasks(): Flow<List<Task>> {
        return taskDao.observeAllActive()
    }
    
    /**
     * Get tasks due on or before a specific date
     * Used for Today checklist generation
     */
    suspend fun getTasksDueByDate(date: LocalDate): Result<List<Task>> {
        return try {
            val tasks = taskDao.getTasksDueByDate(date)
            Result.success(tasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Observe tasks due on or before a specific date (reactive)
     * Used for Today screen with reactive updates
     */
    fun observeTasksDueByDate(date: LocalDate): Flow<List<Task>> {
        return taskDao.observeTasksDueByDate(date)
    }
    
    /**
     * Observe all active tasks ordered by next due date (reactive)
     * Used for All Tasks screen
     */
    fun observeAllOrderedByNextDue(): Flow<List<Task>> {
        return taskDao.observeAllOrderedByNextDue()
    }
    
    /**
     * Get tasks by category
     */
    suspend fun getTasksByCategory(category: String): Result<List<Task>> {
        return try {
            val tasks = taskDao.getByCategory(category)
            Result.success(tasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get child tasks of a parent
     */
    suspend fun getChildTasks(parentId: String): Result<List<Task>> {
        return try {
            val tasks = taskDao.getChildrenOfParent(parentId)
            Result.success(tasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get tasks triggered by a specific task
     */
    suspend fun getTriggeredTasks(taskId: String): Result<List<Task>> {
        return try {
            val tasks = taskDao.getTasksTriggeredBy(taskId)
            Result.success(tasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Search tasks by query string
     */
    suspend fun searchTasks(query: String): Result<List<Task>> {
        return try {
            val tasks = taskDao.search(query)
            Result.success(tasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all tasks ordered by next due date
     * Used for All Tasks View screen
     */
    suspend fun getAllTasksOrderedByDueDate(): Result<List<Task>> {
        return try {
            val tasks = taskDao.getAllOrderedByNextDue()
            Result.success(tasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Observe all tasks ordered by next due date (reactive)
     */
    fun observeAllTasksOrderedByDueDate(): Flow<List<Task>> {
        return taskDao.observeAllOrderedByNextDue()
    }
    
    /**
     * Get all unique categories
     */
    suspend fun getAllCategories(): Result<List<String>> {
        return try {
            val categories = taskDao.getAllCategories()
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ============================================
    // Update
    // ============================================
    
    /**
     * Update an existing task
     */
    suspend fun updateTask(task: Task): Result<Unit> {
        return try {
            taskDao.update(task)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update task schedule after completion
     */
    suspend fun updateTaskSchedule(
        taskId: String,
        nextDue: LocalDate?,
        lastCompleted: LocalDate,
        streak: Int
    ): Result<Unit> {
        return try {
            taskDao.updateSchedule(taskId, nextDue, lastCompleted, streak)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Archive a task (soft delete)
     */
    suspend fun archiveTask(taskId: String): Result<Unit> {
        return try {
            taskDao.archive(taskId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Restore an archived task
     */
    suspend fun restoreTask(taskId: String): Result<Unit> {
        return try {
            taskDao.restore(taskId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ============================================
    // Delete
    // ============================================
    
    /**
     * Delete a task permanently
     */
    suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            taskDao.deleteById(taskId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if a task exists
     */
    suspend fun taskExists(taskId: Long): Result<Boolean> {
        return try {
            val exists = taskDao.exists(taskId)
            Result.success(exists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
