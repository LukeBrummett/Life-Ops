package com.lifeops.app.data.local.dao

import androidx.room.*
import com.lifeops.app.data.local.entity.Inventory
import com.lifeops.app.data.local.entity.Supply
import com.lifeops.app.data.local.entity.TaskSupply
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Supply and Inventory operations.
 * 
 * Provides queries for managing consumable supplies and tracking inventory levels.
 */
@Dao
interface SupplyDao {
    
    // ==================== Supply Queries ====================
    
    /**
     * Observe all supplies ordered by category then name
     */
    @Query("""
        SELECT * FROM supplies 
        ORDER BY category ASC, name ASC
    """)
    fun observeAllSupplies(): Flow<List<Supply>>
    
    /**
     * Get all supplies (one-time query)
     */
    @Query("SELECT * FROM supplies ORDER BY category ASC, name ASC")
    suspend fun getAllSupplies(): List<Supply>
    
    /**
     * Get supply by ID
     */
    @Query("SELECT * FROM supplies WHERE id = :supplyId")
    suspend fun getSupplyById(supplyId: String): Supply?
    
    /**
     * Search supplies by name, category, or tags
     */
    @Query("""
        SELECT * FROM supplies 
        WHERE name LIKE '%' || :query || '%' 
           OR category LIKE '%' || :query || '%'
           OR tags LIKE '%' || :query || '%'
        ORDER BY category ASC, name ASC
    """)
    fun searchSupplies(query: String): Flow<List<Supply>>
    
    /**
     * Get supplies by category
     */
    @Query("SELECT * FROM supplies WHERE category = :category ORDER BY name ASC")
    suspend fun getSuppliesByCategory(category: String): List<Supply>
    
    /**
     * Get all unique categories
     */
    @Query("SELECT DISTINCT category FROM supplies ORDER BY category ASC")
    suspend fun getAllCategories(): List<String>
    
    /**
     * Insert new supply
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(supply: Supply)
    
    /**
     * Update existing supply
     */
    @Update
    suspend fun update(supply: Supply)
    
    /**
     * Delete supply
     */
    @Delete
    suspend fun delete(supply: Supply)
    
    /**
     * Delete supply by ID
     */
    @Query("DELETE FROM supplies WHERE id = :supplyId")
    suspend fun deleteById(supplyId: String)
    
    // ==================== Inventory Queries ====================
    
    /**
     * Observe all inventory levels
     */
    @Query("SELECT * FROM inventory")
    fun observeAllInventory(): Flow<List<Inventory>>
    
    /**
     * Get inventory for specific supply
     */
    @Query("SELECT * FROM inventory WHERE supplyId = :supplyId")
    suspend fun getInventory(supplyId: String): Inventory?
    
    /**
     * Get supplies with inventory below reorder threshold
     */
    @Query("""
        SELECT s.* FROM supplies s
        INNER JOIN inventory i ON s.id = i.supplyId
        WHERE i.currentQuantity < s.reorderThreshold
        ORDER BY s.category ASC, s.name ASC
    """)
    suspend fun getSuppliesNeedingReorder(): List<Supply>
    
    /**
     * Insert or update inventory
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventory(inventory: Inventory)
    
    /**
     * Update inventory quantity
     */
    @Query("""
        UPDATE inventory 
        SET currentQuantity = :quantity, lastUpdated = :timestamp
        WHERE supplyId = :supplyId
    """)
    suspend fun updateInventoryQuantity(supplyId: String, quantity: Int, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Increment inventory quantity
     */
    @Query("""
        UPDATE inventory 
        SET currentQuantity = currentQuantity + :amount, lastUpdated = :timestamp
        WHERE supplyId = :supplyId
    """)
    suspend fun incrementInventory(supplyId: String, amount: Int = 1, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Decrement inventory quantity (will not go below 0)
     */
    @Query("""
        UPDATE inventory 
        SET currentQuantity = MAX(0, currentQuantity - :amount), lastUpdated = :timestamp
        WHERE supplyId = :supplyId
    """)
    suspend fun decrementInventory(supplyId: String, amount: Int = 1, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Get supplies with their current inventory levels (combined query)
     */
    @Transaction
    @Query("""
        SELECT s.*, i.currentQuantity, i.lastUpdated
        FROM supplies s
        LEFT JOIN inventory i ON s.id = i.supplyId
        ORDER BY s.category ASC, s.name ASC
    """)
    fun observeSuppliesWithInventory(): Flow<List<SupplyWithInventory>>
    
    // ==================== TaskSupply Operations ====================
    
    /**
     * Get all task-supply associations for a supply
     */
    @Query("SELECT * FROM task_supplies WHERE supplyId = :supplyId")
    suspend fun getTaskSuppliesForSupply(supplyId: String): List<TaskSupply>
    
    /**
     * Get all task-supply associations for a task
     */
    @Query("SELECT * FROM task_supplies WHERE taskId = :taskId")
    suspend fun getTaskSuppliesForTask(taskId: String): List<TaskSupply>
    
    /**
     * Get count of tasks using a supply
     */
    @Query("SELECT COUNT(*) FROM task_supplies WHERE supplyId = :supplyId")
    suspend fun getTaskCountForSupply(supplyId: String): Int
    
    /**
     * Check if a supply is used by any tasks
     */
    @Query("SELECT EXISTS(SELECT 1 FROM task_supplies WHERE supplyId = :supplyId LIMIT 1)")
    suspend fun isSupplyUsedByTasks(supplyId: String): Boolean
    
    /**
     * Insert task-supply association
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskSupply(taskSupply: TaskSupply)
    
    /**
     * Delete task-supply association
     */
    @Delete
    suspend fun deleteTaskSupply(taskSupply: TaskSupply)
    
    /**
     * Delete all task-supply associations for a task
     */
    @Query("DELETE FROM task_supplies WHERE taskId = :taskId")
    suspend fun deleteTaskSuppliesForTask(taskId: String)
    
    /**
     * Delete all task-supply associations for a supply
     */
    @Query("DELETE FROM task_supplies WHERE supplyId = :supplyId")
    suspend fun deleteTaskSuppliesForSupply(supplyId: String)
}

/**
 * Combined result containing supply and its inventory level
 */
data class SupplyWithInventory(
    @Embedded val supply: Supply,
    val currentQuantity: Int? = 0,
    val lastUpdated: Long? = null
) {
    val needsReorder: Boolean
        get() = (currentQuantity ?: 0) < supply.reorderThreshold
        
    val isWellStocked: Boolean
        get() = (currentQuantity ?: 0) >= supply.reorderTargetQuantity
}

/**
 * Data class representing a supply with its task association count
 */
data class SupplyWithTaskCount(
    @Embedded val supply: Supply,
    val taskCount: Int
)
