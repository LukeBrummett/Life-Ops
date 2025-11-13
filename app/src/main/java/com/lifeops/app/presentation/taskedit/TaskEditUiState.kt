package com.lifeops.app.presentation.taskedit

import com.lifeops.app.data.local.entity.ConsumptionMode
import com.lifeops.app.data.local.entity.DayOfWeek
import com.lifeops.app.data.local.entity.IntervalUnit
import com.lifeops.app.data.local.entity.OverdueBehavior
import java.time.LocalDate

/**
 * UI State for Task Edit/Create Screen
 */
data class TaskEditUiState(
    // Screen state
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isCreateMode: Boolean = true, // true for new task, false for edit
    val originalTaskId: String? = null,
    val hasUnsavedChanges: Boolean = false,
    
    // Validation
    val validationErrors: Map<String, String> = emptyMap(),
    val errorMessage: String? = null,
    
    // Basic Information
    val name: String = "",
    val category: String = "",
    val tags: List<String> = emptyList(),
    val description: String = "",
    
    // Available options for dropdowns
    val availableCategories: List<String> = emptyList(),
    val availableTags: List<String> = emptyList(),
    
    // Schedule Configuration
    val intervalUnit: IntervalUnit = IntervalUnit.DAY,
    val intervalQty: Int = 1,
    val specificDaysOfWeek: List<DayOfWeek> = emptyList(),
    val excludedDaysOfWeek: List<DayOfWeek> = emptyList(),
    val excludedDateRanges: List<DateRange> = emptyList(),
    val overdueBehavior: OverdueBehavior = OverdueBehavior.POSTPONE,
    val deleteAfterCompletion: Boolean = false,
    val nextDue: LocalDate = LocalDate.now(),
    
    // Relationships
    val parentTaskId: String? = null,
    val parentTaskName: String? = null,
    val inheritParentSchedule: Boolean = false,
    val childTasks: List<ChildTaskItem> = emptyList(),
    val requiresManualCompletion: Boolean = false,
    val triggeredByTaskIds: List<TaskReference> = emptyList(),
    val triggersTaskIds: List<TaskReference> = emptyList(),
    
    // Available tasks for relationship dropdowns
    val availableTasks: List<TaskReference> = emptyList(),
    
    // Inventory Associations
    val inventoryAssociations: List<InventoryAssociationEdit> = emptyList(),
    
    // Available supplies for dropdown
    val availableSupplies: List<SupplyReference> = emptyList()
)

/**
 * Date range for schedule exclusions
 */
data class DateRange(
    val startDate: LocalDate,
    val endDate: LocalDate
) {
    fun toDisplayString(): String {
        return if (startDate == endDate) {
            startDate.toString()
        } else {
            "$startDate - $endDate"
        }
    }
}

/**
 * Child task with ordering information
 */
data class ChildTaskItem(
    val taskId: String,
    val taskName: String,
    val category: String,
    val order: Int // For drag-to-reorder
)

/**
 * Task reference for dropdowns and lists
 */
data class TaskReference(
    val taskId: String,
    val taskName: String,
    val category: String
)

/**
 * Supply reference for dropdown
 */
data class SupplyReference(
    val supplyId: String,
    val supplyName: String,
    val unit: String
)

/**
 * Inventory association in edit mode
 */
data class InventoryAssociationEdit(
    val supplyId: String,
    val supplyName: String,
    val unit: String,
    val consumptionMode: ConsumptionMode,
    val fixedQuantity: Int? = null,
    val promptedDefaultValue: Int? = null
)

/**
 * Validation error keys
 */
object ValidationError {
    const val NAME_REQUIRED = "name"
    const val NAME_TOO_LONG = "name_length"
    const val CATEGORY_REQUIRED = "category"
    const val INTERVAL_QTY_INVALID = "interval_qty"
    const val SPECIFIC_DAYS_REQUIRED = "specific_days"
    const val CIRCULAR_PARENT = "circular_parent"
    const val CIRCULAR_CHILD = "circular_child"
    const val CIRCULAR_TRIGGER = "circular_trigger"
    const val INVENTORY_FIXED_QTY = "inventory_fixed_qty"
}
