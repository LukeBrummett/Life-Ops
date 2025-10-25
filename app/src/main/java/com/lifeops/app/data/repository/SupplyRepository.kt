package com.lifeops.app.data.repository

import com.lifeops.app.data.local.dao.SupplyDao
import com.lifeops.app.data.local.dao.SupplyWithInventory
import com.lifeops.app.data.local.entity.Inventory
import com.lifeops.app.data.local.entity.Supply
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
}
