package com.lifeops.app.presentation.taskdetail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeops.app.data.local.entity.ConsumptionMode
import com.lifeops.app.data.local.entity.DayOfWeek
import com.lifeops.app.data.local.entity.IntervalUnit
import com.lifeops.app.data.local.entity.Task
import com.lifeops.app.domain.usecase.CompleteTaskUseCase
import com.lifeops.app.domain.usecase.GetTaskDetailsUseCase
import com.lifeops.app.domain.usecase.InventoryAssociation
import com.lifeops.app.domain.usecase.TaskDetails
import com.lifeops.app.util.DateProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * ViewModel for the Task Detail Screen
 * 
 * Loads and displays comprehensive task information including:
 * - Basic task properties
 * - Schedule and recurrence
 * - Completion history
 * - Parent-child relationships
 * - Trigger relationships
 * - Inventory associations
 */
@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTaskDetailsUseCase: GetTaskDetailsUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val taskRepository: com.lifeops.app.data.repository.TaskRepository,
    private val dateProvider: DateProvider
) : ViewModel() {
    
    private val taskId: String = checkNotNull(savedStateHandle["taskId"]) {
        "TaskDetailViewModel requires taskId parameter"
    }
    
    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()
    
    private val _navigationEvent = MutableStateFlow<TaskDetailNavigationEvent?>(null)
    val navigationEvent: StateFlow<TaskDetailNavigationEvent?> = _navigationEvent.asStateFlow()
    
    fun consumeNavigationEvent() {
        _navigationEvent.value = null
    }
    
    init {
        loadTaskDetails()
    }
    
    /**
     * Handle user events from the UI
     */
    fun onEvent(event: TaskDetailEvent) {
        when (event) {
            is TaskDetailEvent.CompleteTask -> completeTask()
            is TaskDetailEvent.DeleteTask -> deleteTask()
            is TaskDetailEvent.DismissError -> dismissError()
            // Navigation events handled by composable
            is TaskDetailEvent.NavigateToEdit -> {}
            is TaskDetailEvent.NavigateToTask -> {}
            is TaskDetailEvent.NavigateToInventory -> {}
            is TaskDetailEvent.SkipToTomorrow -> {}
            is TaskDetailEvent.WontDo -> {}
            is TaskDetailEvent.ViewInAllTasks -> {}
        }
    }
    
    /**
     * Load task details and all relationships
     */
    private fun loadTaskDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val result = getTaskDetailsUseCase(taskId)
                
                if (result.isFailure) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to load task: ${result.exceptionOrNull()?.message}"
                        )
                    }
                    return@launch
                }
                
                val taskDetails = result.getOrNull()
                if (taskDetails == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Task not found"
                        )
                    }
                    return@launch
                }
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        task = taskDetails.task,
                        scheduleDescription = formatScheduleDescription(taskDetails.task),
                        statusDescription = formatStatusDescription(taskDetails.task),
                        lastCompletedRelative = formatLastCompletedRelative(taskDetails.task),
                        excludedDaysText = formatExcludedDays(taskDetails.task),
                        excludedDatesText = formatExcludedDates(taskDetails.task),
                        parentTask = taskDetails.parentTask?.let { formatTaskSummary(it) },
                        childTasks = taskDetails.childTasks.mapIndexed { index, task ->
                            ChildTaskDisplay(
                                taskId = task.id,
                                taskName = task.name,
                                category = task.category,
                                order = index + 1,
                                scheduleSummary = formatScheduleSummary(task)
                            )
                        },
                        triggeredByTasks = taskDetails.triggeredByTasks.map { formatTaskSummary(it) },
                        triggersTasks = taskDetails.triggersTasks.map { formatTaskSummary(it) },
                        inventoryItems = taskDetails.inventoryAssociations.map { formatInventoryItem(it) },
                        canComplete = canCompleteTask(taskDetails.task),
                        canDelete = true
                    )
                }
            } catch (e: Exception) {
                Log.e("TaskDetailViewModel", "Error loading task details", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Unexpected error: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Complete the current task
     */
    private fun completeTask() {
        viewModelScope.launch {
            try {
                val currentDate = dateProvider.now()
                completeTaskUseCase(taskId, currentDate)
                // Reload to get updated completion data
                loadTaskDetails()
            } catch (e: Exception) {
                Log.e("TaskDetailViewModel", "Error completing task", e)
                _uiState.update {
                    it.copy(errorMessage = "Failed to complete task: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Delete the current task
     * TODO: Implement delete use case
     */
    private fun deleteTask() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val result = taskRepository.deleteTask(taskId)
                
                if (result.isSuccess) {
                    // Navigate back after successful deletion
                    _navigationEvent.value = TaskDetailNavigationEvent.NavigateBack
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to delete task: ${result.exceptionOrNull()?.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("TaskDetailViewModel", "Error deleting task", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to delete task: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Dismiss error message
     */
    private fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    // ========== Formatting Helpers ==========
    
    /**
     * Format schedule description in plain English
     */
    private fun formatScheduleDescription(task: Task): String {
        return when (task.intervalUnit) {
            IntervalUnit.ADHOC -> "No automatic schedule (trigger-only)"
            IntervalUnit.DAY -> {
                val every = if (task.intervalQty == 1) "day" else "${task.intervalQty} days"
                "Every $every"
            }
            IntervalUnit.WEEK -> {
                val every = if (task.intervalQty == 1) "week" else "${task.intervalQty} weeks"
                val days = formatSpecificDays(task.specificDaysOfWeek)
                if (days.isNotEmpty()) {
                    "Every $every on $days"
                } else {
                    "Every $every"
                }
            }
            IntervalUnit.MONTH -> {
                val every = if (task.intervalQty == 1) "month" else "${task.intervalQty} months"
                "Every $every"
            }
        }
    }
    
    /**
     * Format specific days of week
     */
    private fun formatSpecificDays(days: List<DayOfWeek>?): String {
        if (days.isNullOrEmpty()) return ""
        
        return when (days.size) {
            0 -> ""
            1 -> days[0].name
            2 -> "${days[0].name} and ${days[1].name}"
            else -> days.dropLast(1).joinToString(", ") { it.name } + ", and ${days.last().name}"
        }
    }
    
    /**
     * Format task status description
     */
    private fun formatStatusDescription(task: Task): String {
        val today = dateProvider.now()
        
        return when {
            task.nextDue == null -> "Not yet scheduled"
            task.nextDue.isBefore(today) -> {
                val daysOverdue = ChronoUnit.DAYS.between(task.nextDue, today)
                "Overdue by $daysOverdue day${if (daysOverdue > 1) "s" else ""}"
            }
            task.nextDue.isEqual(today) -> "Due today"
            else -> "Not yet due"
        }
    }
    
    /**
     * Format last completed date as relative time
     */
    private fun formatLastCompletedRelative(task: Task): String {
        val lastCompleted = task.lastCompleted ?: return "Never completed"
        val today = dateProvider.now()
        val daysAgo = ChronoUnit.DAYS.between(lastCompleted, today)
        
        return when (daysAgo) {
            0L -> "Today"
            1L -> "Yesterday"
            else -> "$daysAgo days ago"
        }
    }
    
    /**
     * Format excluded days of week
     */
    private fun formatExcludedDays(task: Task): String? {
        val days = formatSpecificDays(task.excludedDaysOfWeek)
        return if (days.isNotEmpty()) days else null
    }
    
    /**
     * Format excluded date ranges
     */
    private fun formatExcludedDates(task: Task): String? {
        // TODO: Implement when we have date range support
        return null
    }
    
    /**
     * Create a task summary for display
     */
    private fun formatTaskSummary(task: Task): TaskSummary {
        return TaskSummary(
            taskId = task.id,
            taskName = task.name,
            category = task.category,
            scheduleSummary = formatScheduleSummary(task)
        )
    }
    
    /**
     * Format short schedule summary for related tasks
     */
    private fun formatScheduleSummary(task: Task): String {
        return when (task.intervalUnit) {
            IntervalUnit.ADHOC -> "ADHOC (trigger-only)"
            IntervalUnit.DAY -> {
                if (task.intervalQty == 1) "Daily" else "Every ${task.intervalQty} days"
            }
            IntervalUnit.WEEK -> {
                val days = formatSpecificDays(task.specificDaysOfWeek)
                if (days.isNotEmpty()) {
                    days
                } else if (task.intervalQty == 1) {
                    "Weekly"
                } else {
                    "Every ${task.intervalQty} weeks"
                }
            }
            IntervalUnit.MONTH -> {
                if (task.intervalQty == 1) "Monthly" else "Every ${task.intervalQty} months"
            }
        }
    }
    
    /**
     * Format inventory association for display
     */
    private fun formatInventoryItem(association: InventoryAssociation): InventoryItemDisplay {
        val taskSupply = association.taskSupply
        val supply = association.supply
        val inventory = association.inventory
        
        // Format consumption mode
        val consumptionMode = when (taskSupply.consumptionMode) {
            ConsumptionMode.FIXED -> "Fixed"
            ConsumptionMode.PROMPTED -> "Prompted"
            ConsumptionMode.RECOUNT -> "Recount"
        }
        
        // Format mode details
        val modeDetails = when (taskSupply.consumptionMode) {
            ConsumptionMode.FIXED -> {
                val qty = taskSupply.fixedQuantity ?: 0
                "$qty ${supply.unit} per completion"
            }
            ConsumptionMode.PROMPTED -> {
                taskSupply.promptedDefaultValue?.let { default ->
                    "default: $default ${supply.unit}"
                } ?: "prompt for quantity"
            }
            ConsumptionMode.RECOUNT -> "manual recount"
        }
        
        // Format current stock
        val currentStock = if (inventory != null) {
            "${inventory.currentQuantity} ${supply.unit}"
        } else {
            "Not tracked"
        }
        
        // Determine if low stock
        val isLowStock = inventory?.let { inv ->
            inv.currentQuantity <= supply.reorderThreshold
        } ?: false
        
        return InventoryItemDisplay(
            supplyId = supply.id,
            supplyName = supply.name,
            consumptionMode = consumptionMode,
            modeDetails = modeDetails,
            currentStock = currentStock,
            isLowStock = isLowStock
        )
    }
    
    /**
     * Determine if task can be completed
     */
    private fun canCompleteTask(task: Task): Boolean {
        val today = dateProvider.now()
        return task.nextDue != null && !task.nextDue.isAfter(today)
    }
}

/**
 * One-time navigation events
 */
sealed class TaskDetailNavigationEvent {
    data object NavigateBack : TaskDetailNavigationEvent()
}
