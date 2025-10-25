package com.lifeops.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Consumption mode for how a task uses a supply
 */
enum class ConsumptionMode {
    /**
     * Fixed quantity consumed each time task is completed
     * Example: "Replace 2 air filters" always uses 2 filters
     */
    FIXED,
    
    /**
     * User is prompted for quantity when completing task
     * Example: "Clean kitchen" - prompt for how many paper towels used
     */
    PROMPTED,
    
    /**
     * No automatic consumption, user recounts inventory after task
     * Example: "Monthly cleaning" - too many supplies to track individually
     */
    RECOUNT
}

/**
 * Junction table linking tasks to supplies they consume
 * 
 * When a task is completed, the associated supplies are consumed based on their mode:
 * - FIXED: Automatically deduct fixedQuantity
 * - PROMPTED: Ask user how much was used (with default suggestion)
 * - RECOUNT: Create restock task for manual inventory recount
 */
@Entity(
    tableName = "task_supplies",
    primaryKeys = ["taskId", "supplyId"],
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Supply::class,
            parentColumns = ["id"],
            childColumns = ["supplyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["taskId"]),
        Index(value = ["supplyId"])
    ]
)
data class TaskSupply(
    val taskId: String,
    val supplyId: String,
    val consumptionMode: ConsumptionMode,
    val fixedQuantity: Int? = null, // Required if mode is FIXED
    val promptedDefaultValue: Int? = null // Optional default if mode is PROMPTED
)
