package com.lifeops.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tracks current quantity of a supply item.
 * 
 * Maintains real-time inventory levels that are updated through:
 * - Manual adjustments (+/- buttons)
 * - Task completion (automatic consumption)
 * - Restock operations
 */
@Entity(
    tableName = "inventory",
    foreignKeys = [
        ForeignKey(
            entity = Supply::class,
            parentColumns = ["id"],
            childColumns = ["supplyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["supplyId"], unique = true)
    ]
)
data class Inventory(
    @PrimaryKey
    val supplyId: String,
    
    /** Current on-hand quantity (cannot be negative) */
    val currentQuantity: Int,
    
    /** Timestamp of last update */
    val lastUpdated: Long = System.currentTimeMillis()
) {
    init {
        require(currentQuantity >= 0) { "Current quantity cannot be negative" }
    }
}
