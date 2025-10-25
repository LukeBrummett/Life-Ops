package com.lifeops.app.data.local.dao

import androidx.room.*
import com.lifeops.app.data.local.entity.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Data Access Object for Task entity
 * Provides database operations for task management
 */
@Dao
interface TaskDao {
    
    // ============================================
    // Create
    // ============================================
    
    /**
     * Insert a new task
     * @return The ID of the inserted task
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long
    
    /**
     * Insert multiple tasks
     * @return List of inserted task IDs
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<Task>)
    
    // ============================================
    // Read
    // ============================================
    
    /**
     * Get a task by ID
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getById(taskId: String): Task?
    
    /**
     * Observe a task by ID (reactive)
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun observeById(taskId: String): Flow<Task?>
    
    /**
     * Get all tasks (including archived) for export/backup
     */
    @Query("SELECT * FROM tasks ORDER BY id")
    suspend fun getAll(): List<Task>
    
    /**
     * Get all active tasks
     */
    @Query("SELECT * FROM tasks WHERE active = 1 ORDER BY category, name")
    suspend fun getAllActive(): List<Task>
    
    /**
     * Observe all active tasks (reactive)
     */
    @Query("SELECT * FROM tasks WHERE active = 1 ORDER BY category, name")
    fun observeAllActive(): Flow<List<Task>>
    
    /**
     * Get tasks due today or overdue
     * Used for generating Today checklist
     */
    @Query("SELECT * FROM tasks WHERE active = 1 AND nextDue <= :date ORDER BY nextDue, category, name")
    suspend fun getTasksDueByDate(date: LocalDate): List<Task>
    
    /**
     * Observe tasks due today or overdue (reactive)
     * Includes tasks completed today even if nextDue has moved forward
     * Used for Today screen with reactive updates
     */
    @Query("SELECT * FROM tasks WHERE active = 1 AND (nextDue <= :date OR lastCompleted = :date) ORDER BY nextDue, category, name")
    fun observeTasksDueByDate(date: LocalDate): Flow<List<Task>>
    
    /**
     * Get tasks by category
     */
    @Query("SELECT * FROM tasks WHERE active = 1 AND category = :category ORDER BY name")
    suspend fun getByCategory(category: String): List<Task>
    
    /**
     * Get tasks with specific parent
     */
    @Query("SELECT * FROM tasks WHERE active = 1 AND parentTaskIds LIKE '%' || :parentId || '%' ORDER BY childOrder")
    suspend fun getChildrenOfParent(parentId: String): List<Task>
    
    /**
     * Get all parent tasks (tasks that have children)
     * Note: This checks if any other task has this task as parent
     */
    @Query("SELECT * FROM tasks WHERE active = 1 AND id IN (SELECT DISTINCT json_each.value FROM tasks, json_each(tasks.parentTaskIds) WHERE tasks.active = 1)")
    suspend fun getAllParentTasks(): List<Task>
    
    /**
     * Get tasks triggered by a specific task
     */
    @Query("SELECT * FROM tasks WHERE active = 1 AND triggeredByTaskIds LIKE '%' || :taskId || '%'")
    suspend fun getTasksTriggeredBy(taskId: String): List<Task>
    
    /**
     * Get ADHOC tasks (no automatic scheduling)
     */
    @Query("SELECT * FROM tasks WHERE active = 1 AND intervalUnit = 'ADHOC' ORDER BY name")
    suspend fun getAdhocTasks(): List<Task>
    
    /**
     * Search tasks by name, category, or tags
     */
    @Query("""
        SELECT * FROM tasks 
        WHERE active = 1 
        AND (name LIKE '%' || :query || '%' 
             OR category LIKE '%' || :query || '%' 
             OR tags LIKE '%' || :query || '%')
        ORDER BY category, name
    """)
    suspend fun search(query: String): List<Task>
    
    /**
     * Get all tasks ordered by next due date
     * Used for "All Tasks View" screen
     */
    @Query("""
        SELECT * FROM tasks 
        WHERE active = 1 
        ORDER BY 
            CASE WHEN nextDue IS NULL THEN 1 ELSE 0 END,
            nextDue ASC,
            category,
            name
    """)
    suspend fun getAllOrderedByNextDue(): List<Task>
    
    /**
     * Observe all tasks ordered by next due date (reactive)
     */
    @Query("""
        SELECT * FROM tasks 
        WHERE active = 1 
        ORDER BY 
            CASE WHEN nextDue IS NULL THEN 1 ELSE 0 END,
            nextDue ASC,
            category,
            name
    """)
    fun observeAllOrderedByNextDue(): Flow<List<Task>>
    
    // ============================================
    // Update
    // ============================================
    
    /**
     * Update an existing task
     */
    @Update
    suspend fun update(task: Task)
    
    /**
     * Update multiple tasks
     */
    @Update
    suspend fun updateAll(tasks: List<Task>)
    
    /**
     * Update task's next due date and last completed date
     * Used after task completion
     */
    @Query("UPDATE tasks SET nextDue = :nextDue, lastCompleted = :lastCompleted, completionStreak = :streak WHERE id = :taskId")
    suspend fun updateSchedule(taskId: String, nextDue: LocalDate?, lastCompleted: LocalDate, streak: Int)
    
    /**
     * Archive a task (soft delete)
     */
    @Query("UPDATE tasks SET active = 0 WHERE id = :taskId")
    suspend fun archive(taskId: String)
    
    /**
     * Restore an archived task
     */
    @Query("UPDATE tasks SET active = 1 WHERE id = :taskId")
    suspend fun restore(taskId: String)
    
    // ============================================
    // Delete
    // ============================================
    
    /**
     * Delete a task permanently
     * Note: Should cascade delete related TaskSupply entries
     */
    @Delete
    suspend fun delete(task: Task)
    
    /**
     * Delete task by ID
     */
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteById(taskId: String)
    
    /**
     * Delete all archived tasks
     */
    @Query("DELETE FROM tasks WHERE active = 0")
    suspend fun deleteAllArchived()
    
    // ============================================
    // Utility Queries
    // ============================================
    
    /**
     * Get count of active tasks
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE active = 1")
    suspend fun getActiveTaskCount(): Int
    
    /**
     * Get all unique categories
     */
    @Query("SELECT DISTINCT category FROM tasks WHERE active = 1 ORDER BY category")
    suspend fun getAllCategories(): List<String>
    
    /**
     * Check if task exists
     */
    @Query("SELECT EXISTS(SELECT 1 FROM tasks WHERE id = :taskId)")
    suspend fun exists(taskId: Long): Boolean
}
