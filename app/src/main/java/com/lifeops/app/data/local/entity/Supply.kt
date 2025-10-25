package com.lifeops.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Represents a consumable supply item that can be tracked and managed.
 * 
 * Supplies define the blueprint for inventory items, including reorder thresholds
 * and target quantities for restocking.
 */
@Entity(tableName = "supplies")
data class Supply(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    /** Display name of the supply */
    val name: String,
    
    /** Category for organizational grouping (e.g., "Household", "Kitchen", "Cleaning") */
    val category: String,
    
    /** Comma-separated searchable labels */
    val tags: String? = null,
    
    /** Unit of measurement (e.g., "count", "oz", "liters", "bottles", "rolls") */
    val unit: String,
    
    /** Quantity below which item appears in shopping list */
    val reorderThreshold: Int,
    
    /** Target quantity after restocking */
    val reorderTargetQuantity: Int,
    
    /** Additional context or instructions */
    val notes: String? = null,
    
    /** Timestamp when supply was created */
    val createdAt: Long = System.currentTimeMillis()
) {
    init {
        require(name.isNotBlank()) { "Supply name cannot be blank" }
        require(reorderThreshold >= 0) { "Reorder threshold must be non-negative" }
        require(reorderTargetQuantity >= reorderThreshold) { 
            "Reorder target ($reorderTargetQuantity) must be >= threshold ($reorderThreshold)" 
        }
    }
}
