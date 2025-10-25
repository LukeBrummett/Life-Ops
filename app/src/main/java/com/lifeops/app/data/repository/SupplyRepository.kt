package com.lifeops.app.data.repository

import com.lifeops.app.data.local.dao.SupplyDao
import com.lifeops.app.data.local.dao.SupplyWithInventory
import com.lifeops.app.data.local.entity.Inventory
import com.lifeops.app.data.local.entity.Supply
import com.lifeops.app.data.local.entity.TaskSupply
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Supply and Inventory data operations.
 * 
 * Provides abstraction layer between data sources and domain layer,
 * following Repository pattern from Clean Architecture.
 */
@Singleton
class SupplyRepository @Inject constructor(
    private val supplyDao: SupplyDao
) {
    
    // ==================== Supply Operations ====================
    
    /**
     * Observe all supplies with their inventory levels
     */
    fun observeSuppliesWithInventory(): Flow<List<SupplyWithInventory>> {
        return supplyDao.observeSuppliesWithInventory()
    }
    
    /**
     * Observe all supplies
     */
    fun observeAllSupplies(): Flow<List<Supply>> {
        return supplyDao.observeAllSupplies()
    }
    
    /**
     * Get all supplies (one-time)
     */
    suspend fun getAllSupplies(): List<Supply> {
        return supplyDao.getAllSupplies()
    }
    
    /**
     * Get supply by ID
     */
    suspend fun getSupplyById(id: String): Supply? {
        return supplyDao.getSupplyById(id)
    }
    
    /**
     * Search supplies by query
     */
    fun searchSupplies(query: String): Flow<List<Supply>> {
        return supplyDao.searchSupplies(query)
    }
    
    /**
     * Get all unique categories
     */
    suspend fun getAllCategories(): List<String> {
        return supplyDao.getAllCategories()
    }
    
    /**
     * Create new supply with initial inventory
     */
    suspend fun createSupply(supply: Supply, initialQuantity: Int = 0): Result<Unit> {
        return try {
            supplyDao.insert(supply)
            
            // Create initial inventory entry
            supplyDao.insertInventory(
                Inventory(
                    supplyId = supply.id,
                    currentQuantity = initialQuantity
                )
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update existing supply
     */
    suspend fun updateSupply(supply: Supply): Result<Unit> {
        return try {
            supplyDao.update(supply)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete supply (will cascade delete inventory)
     */
    suspend fun deleteSupply(supply: Supply): Result<Unit> {
        return try {
            supplyDao.delete(supply)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete supply by ID
     */
    suspend fun deleteSupplyById(id: String): Result<Unit> {
        return try {
            supplyDao.deleteById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== Inventory Operations ====================
    
    /**
     * Observe all inventory levels
     */
    fun observeAllInventory(): Flow<List<Inventory>> {
        return supplyDao.observeAllInventory()
    }
    
    /**
     * Get inventory for specific supply
     */
    suspend fun getInventory(supplyId: String): Inventory? {
        return supplyDao.getInventory(supplyId)
    }
    
    /**
     * Get supplies needing reorder (below threshold)
     */
    suspend fun getSuppliesNeedingReorder(): List<Supply> {
        return supplyDao.getSuppliesNeedingReorder()
    }
    
    /**
     * Update inventory quantity
     */
    suspend fun updateInventoryQuantity(supplyId: String, quantity: Int): Result<Unit> {
        return try {
            require(quantity >= 0) { "Quantity cannot be negative" }
            supplyDao.updateInventoryQuantity(supplyId, quantity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Increment inventory quantity
     */
    suspend fun incrementInventory(supplyId: String, amount: Int = 1): Result<Unit> {
        return try {
            supplyDao.incrementInventory(supplyId, amount)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Decrement inventory quantity (won't go below 0)
     */
    suspend fun decrementInventory(supplyId: String, amount: Int = 1): Result<Unit> {
        return try {
            supplyDao.decrementInventory(supplyId, amount)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== Task-Supply Association Operations ====================
    
    /**
     * Get all task-supply associations for a supply
     */
    suspend fun getTaskSuppliesForSupply(supplyId: String): List<TaskSupply> {
        return supplyDao.getTaskSuppliesForSupply(supplyId)
    }
    
    /**
     * Get all task-supply associations for a task
     */
    suspend fun getTaskSuppliesForTask(taskId: String): List<TaskSupply> {
        return supplyDao.getTaskSuppliesForTask(taskId)
    }
    
    /**
     * Get count of tasks using a supply
     */
    suspend fun getTaskCountForSupply(supplyId: String): Int {
        return supplyDao.getTaskCountForSupply(supplyId)
    }
    
    /**
     * Check if a supply is used by any tasks
     */
    suspend fun isSupplyUsedByTasks(supplyId: String): Boolean {
        return supplyDao.isSupplyUsedByTasks(supplyId)
    }
    
    /**
     * Add task-supply association
     */
    suspend fun addTaskSupply(taskSupply: TaskSupply): Result<Unit> {
        return try {
            supplyDao.insertTaskSupply(taskSupply)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Remove task-supply association
     */
    suspend fun removeTaskSupply(taskSupply: TaskSupply): Result<Unit> {
        return try {
            supplyDao.deleteTaskSupply(taskSupply)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Remove all task-supply associations for a task
     */
    suspend fun removeAllTaskSuppliesForTask(taskId: String): Result<Unit> {
        return try {
            supplyDao.deleteTaskSuppliesForTask(taskId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
