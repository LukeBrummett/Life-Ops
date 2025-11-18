package com.lifeops.app.presentation.taskedit

import com.lifeops.app.data.local.entity.ConsumptionMode
import com.lifeops.app.data.local.entity.DayOfWeek
import com.lifeops.app.data.local.entity.IntervalUnit
import com.lifeops.app.data.local.entity.OverdueBehavior
import java.time.LocalDate

/**
 * User events for Task Edit/Create Screen
 */
sealed class TaskEditEvent {
    // Basic Information Events
    data class UpdateName(val name: String) : TaskEditEvent()
    data class UpdateCategory(val category: String) : TaskEditEvent()
    data class AddTag(val tag: String) : TaskEditEvent()
    data class RemoveTag(val tag: String) : TaskEditEvent()
    data class UpdateDescription(val description: String) : TaskEditEvent()
    
    // Schedule Configuration Events
    data class UpdateIntervalUnit(val unit: IntervalUnit) : TaskEditEvent()
    data class UpdateIntervalQty(val qty: Int) : TaskEditEvent()
    data class ToggleSpecificDay(val day: DayOfWeek) : TaskEditEvent()
    data class ToggleExcludedDay(val day: DayOfWeek) : TaskEditEvent()
    data class AddExcludedDateRange(val startDate: LocalDate, val endDate: LocalDate) : TaskEditEvent()
    data class RemoveExcludedDateRange(val dateRange: DateRange) : TaskEditEvent()
    data class UpdateOverdueBehavior(val behavior: OverdueBehavior) : TaskEditEvent()
    data class UpdateDeleteAfterCompletion(val delete: Boolean) : TaskEditEvent()
    data class UpdateNextDue(val date: LocalDate) : TaskEditEvent()
    
    // Parent-Child Relationship Events
    data class UpdateParentTask(val taskId: String?) : TaskEditEvent()
    data class UpdateInheritParentSchedule(val inherit: Boolean) : TaskEditEvent()
    data class AddChildTask(val taskId: String) : TaskEditEvent()
    data class RemoveChildTask(val taskId: String) : TaskEditEvent()
    data class ReorderChildTasks(val fromIndex: Int, val toIndex: Int) : TaskEditEvent()
    data class UpdateRequiresManualCompletion(val required: Boolean) : TaskEditEvent()
    
    // Trigger Relationship Events
    data class AddTriggeredByTask(val taskId: String) : TaskEditEvent()
    data class RemoveTriggeredByTask(val taskId: String) : TaskEditEvent()
    data class AddTriggersTask(val taskId: String) : TaskEditEvent()
    data class RemoveTriggersTask(val taskId: String) : TaskEditEvent()
    
    // Inventory Events
    data class AddInventoryAssociation(val supplyId: String) : TaskEditEvent()
    data class RemoveInventoryAssociation(val supplyId: String) : TaskEditEvent()
    data class UpdateInventoryConsumptionMode(val supplyId: String, val mode: ConsumptionMode) : TaskEditEvent()
    data class UpdateInventoryFixedQuantity(val supplyId: String, val quantity: Int) : TaskEditEvent()
    data class UpdateInventoryPromptedDefault(val supplyId: String, val quantity: Int) : TaskEditEvent()
    
    // Navigation Events
    data object Save : TaskEditEvent()
    data object Cancel : TaskEditEvent()
    data object DismissError : TaskEditEvent()
    data class NavigateToTaskDetail(val taskId: String) : TaskEditEvent()
}
