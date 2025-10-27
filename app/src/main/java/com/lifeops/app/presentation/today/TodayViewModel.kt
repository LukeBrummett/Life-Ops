package com.lifeops.app.presentation.today

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeops.app.data.local.entity.ConsumptionMode
import com.lifeops.app.domain.usecase.CompleteTaskUseCase
import com.lifeops.app.domain.usecase.GetTaskDetailsUseCase
import com.lifeops.app.domain.usecase.GetTasksDueUseCase
import com.lifeops.app.domain.usecase.ProcessOverdueTasksUseCase
import com.lifeops.app.presentation.taskdetail.PromptedInventoryItem
import com.lifeops.app.util.DateProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * ViewModel for the Today Screen
 * 
 * Manages UI state and handles user events for the Today Screen.
 * Fetches tasks from the database and handles task completion.
 */
@HiltViewModel
class TodayViewModel @Inject constructor(
    private val getTasksDueUseCase: GetTasksDueUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val processOverdueTasksUseCase: ProcessOverdueTasksUseCase,
    private val getTaskDetailsUseCase: GetTaskDetailsUseCase,
    private val supplyRepository: com.lifeops.app.data.repository.SupplyRepository,
    private val dateProvider: DateProvider
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()
    
    private var loadTasksJob: Job? = null
    
    init {
        loadTasksDueToday()
    }
    
    /**
     * Handle user events from the UI
     */
    fun onEvent(event: TodayUiEvent) {
        when (event) {
            is TodayUiEvent.CompleteTask -> prepareCompleteTask(event.taskId)
            is TodayUiEvent.ToggleShowCompleted -> toggleShowCompleted()
            is TodayUiEvent.NavigateToAllTasks -> {
                // Navigation handled by MainActivity/NavHost in Phase 4
            }
            is TodayUiEvent.NavigateToInventory -> {
                // Navigation handled by MainActivity/NavHost in Phase 4
            }
            is TodayUiEvent.NavigateToSettings -> {
                // Navigation handled by MainActivity/NavHost in Phase 4
            }
            is TodayUiEvent.NavigateToTaskDetail -> {
                // Navigation handled by MainActivity/NavHost in Phase 4
            }
            is TodayUiEvent.NavigateToTaskCreate -> {
                // Navigation handled by TodayScreen
            }
            is TodayUiEvent.Refresh -> loadTasksDueToday()
            is TodayUiEvent.DebugAdvanceDate -> advanceDebugDate(event.days)
            is TodayUiEvent.DismissInventoryPrompt -> dismissInventoryPrompt()
            is TodayUiEvent.ConfirmInventoryConsumption -> confirmInventoryConsumption(event.taskId, event.consumptions)
        }
    }
    
    /**
     * Load tasks due today from the database
     * Observes changes and updates UI state reactively
     */
    private fun loadTasksDueToday() {
        // Cancel previous job to avoid multiple simultaneous collectors
        loadTasksJob?.cancel()
        
        loadTasksJob = viewModelScope.launch {
            Log.d("TodayViewModel", "loadTasksDueToday() called")
            _uiState.update { it.copy(isLoading = true) }
            
            val today = dateProvider.now()
            val formattedDate = today.format(
                DateTimeFormatter.ofPattern("MMM dd, yyyy")
            )
            
            Log.d("TodayViewModel", "Loading tasks for date: $today ($formattedDate)")
            
            getTasksDueUseCase(today)
                .catch { exception ->
                    Log.e("TodayViewModel", "Error loading tasks", exception)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load tasks"
                        )
                    }
                }
                .collect { tasks ->
                    Log.d("TodayViewModel", "Received ${tasks.size} tasks from use case")
                    tasks.forEach { task ->
                        Log.d("TodayViewModel", "  - Task: ${task.name}, lastCompleted: ${task.lastCompleted}, category: ${task.category}")
                    }
                    
                    // Group tasks by category with parent-child relationships
                    val grouped = groupTasksWithHierarchy(tasks)
                    
                    Log.d("TodayViewModel", "Grouped into ${grouped.size} categories")
                    grouped.forEach { (category, items) ->
                        Log.d("TodayViewModel", "  - Category '$category': ${items.size} items")
                    }
                    
                    val allComplete = tasks.isNotEmpty() && 
                        tasks.all { it.lastCompleted == today }
                    
                    Log.d("TodayViewModel", "All complete check: $allComplete (today: $today)")
                    
                    _uiState.update {
                        it.copy(
                            currentDateValue = today,
                            currentDate = formattedDate,
                            tasksByCategory = grouped,
                            allTasksComplete = allComplete,
                            isLoading = false,
                            error = null
                        )
                    }
                    
                    Log.d("TodayViewModel", "UI state updated - isLoading: false, categories: ${grouped.size}, allComplete: $allComplete")
                }
        }
    }
    
    /**
     * Group tasks by category, organizing parent-child relationships
     * - Parents appear with their children nested
     * - Children are shown under their parents
     * - Standalone tasks appear normally
     */
    private fun groupTasksWithHierarchy(tasks: List<com.lifeops.app.data.local.entity.Task>): Map<String, List<TaskItem>> {
        // Create a map of task ID to task for quick lookup
        val taskMap = tasks.associateBy { it.id }
        
        // Track which tasks are children (so we don't show them as standalone)
        val childTaskIds = mutableSetOf<String>()
        
        // Find all child tasks
        tasks.forEach { task ->
            if (!task.parentTaskIds.isNullOrEmpty()) {
                childTaskIds.add(task.id)
            }
        }
        
        // Build task items with hierarchy
        val taskItems = tasks.mapNotNull { task ->
            // Skip tasks that are children (they'll be included under their parent)
            if (task.id in childTaskIds) {
                return@mapNotNull null
            }
            
            // Find children for this task
            val children = tasks.filter { potentialChild ->
                !potentialChild.parentTaskIds.isNullOrEmpty() && 
                task.id in potentialChild.parentTaskIds
            }.sortedBy { it.childOrder ?: 0 }
            
            TaskItem(
                task = task,
                children = children,
                isParent = children.isNotEmpty()
            )
        }
        
        // Group by category and sort
        return taskItems.groupBy { it.task.category }
            .toSortedMap()
    }
    
    /**
     * Prepare to complete a task - check for prompted inventory items first
     */
    private fun prepareCompleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                // Get task details to check inventory associations
                val result = getTaskDetailsUseCase(taskId)
                val taskDetails = result.getOrNull()
                
                if (taskDetails == null) {
                    Log.e("TodayViewModel", "Failed to load task details for $taskId")
                    return@launch
                }
                
                // Find all prompted inventory items
                val promptedItems = taskDetails.inventoryAssociations
                    .filter { it.taskSupply.consumptionMode == ConsumptionMode.PROMPTED }
                    .map { association ->
                        PromptedInventoryItem(
                            supplyId = association.supply.id,
                            supplyName = association.supply.name,
                            unit = association.supply.unit,
                            defaultValue = association.taskSupply.promptedDefaultValue ?: 1,
                            currentQuantity = association.inventory?.currentQuantity ?: 0
                        )
                    }
                
                // If there are prompted items, show the dialog
                if (promptedItems.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            showInventoryPrompt = true,
                            inventoryPromptTaskId = taskId,
                            inventoryPromptTaskName = taskDetails.task.name,
                            promptedInventoryItems = promptedItems
                        )
                    }
                } else {
                    // No prompted items, complete immediately with FIXED items
                    completeTaskWithInventory(taskId, emptyMap())
                }
            } catch (e: Exception) {
                Log.e("TodayViewModel", "Error preparing task completion for $taskId", e)
            }
        }
    }
    
    /**
     * Dismiss the inventory prompt dialog
     */
    private fun dismissInventoryPrompt() {
        _uiState.update {
            it.copy(
                showInventoryPrompt = false,
                inventoryPromptTaskId = null,
                inventoryPromptTaskName = null,
                promptedInventoryItems = emptyList()
            )
        }
    }
    
    /**
     * Confirm inventory consumption and complete the task
     */
    private fun confirmInventoryConsumption(taskId: String, consumptions: Map<String, Int>) {
        viewModelScope.launch {
            try {
                // Dismiss dialog first
                _uiState.update {
                    it.copy(
                        showInventoryPrompt = false,
                        inventoryPromptTaskId = null,
                        inventoryPromptTaskName = null,
                        promptedInventoryItems = emptyList()
                    )
                }
                
                // Complete task with inventory consumption
                completeTaskWithInventory(taskId, consumptions)
            } catch (e: Exception) {
                Log.e("TodayViewModel", "Error confirming inventory consumption", e)
            }
        }
    }
    
    /**
     * Complete the task and handle inventory consumption
     */
    private suspend fun completeTaskWithInventory(taskId: String, promptedConsumptions: Map<String, Int>) {
        try {
            val currentDate = dateProvider.now()
            
            // Get task details for inventory associations
            val result = getTaskDetailsUseCase(taskId)
            val taskDetails = result.getOrNull() ?: return
            
            // Process all inventory consumption
            taskDetails.inventoryAssociations.forEach { association ->
                when (association.taskSupply.consumptionMode) {
                    ConsumptionMode.FIXED -> {
                        // Consume fixed quantity
                        val quantity = association.taskSupply.fixedQuantity ?: 0
                        if (quantity > 0) {
                            supplyRepository.decrementInventory(association.supply.id, quantity)
                        }
                    }
                    ConsumptionMode.PROMPTED -> {
                        // Consume prompted quantity (from user input)
                        val quantity = promptedConsumptions[association.supply.id] ?: 0
                        if (quantity > 0) {
                            supplyRepository.decrementInventory(association.supply.id, quantity)
                        }
                    }
                    ConsumptionMode.RECOUNT -> {
                        // No automatic consumption for recount mode
                    }
                }
            }
            
            // Complete the task
            completeTaskUseCase(taskId, currentDate)
            // UI will update automatically via Flow from getTasksDueUseCase
        } catch (e: Exception) {
            Log.e("TodayViewModel", "Error completing task with inventory", e)
        }
    }
    
    /**
     * Toggle task completion status (legacy - now handled by prepareCompleteTask)
     * Uses CompleteTaskUseCase to handle all business logic
     */
    private fun completeTask(taskId: String) {
        viewModelScope.launch {
            val today = dateProvider.now()
            completeTaskUseCase(taskId, today)
            // UI will update automatically via Flow from getTasksDueUseCase
        }
    }
    
    /**
     * Toggle visibility of completed tasks
     */
    private fun toggleShowCompleted() {
        _uiState.update { 
            it.copy(showCompleted = !it.showCompleted) 
        }
    }
    
    /**
     * DEBUG: Advance the date by specified number of days
     */
    private fun advanceDebugDate(days: Int) {
        Log.d("TodayViewModel", "advanceDebugDate($days) called")
        
        viewModelScope.launch {
            // Calculate the new date we're advancing to
            val currentDate = dateProvider.now()
            val newDate = currentDate.plusDays(days.toLong())
            
            // Process overdue tasks based on the NEW date we're advancing to
            processOverdueTasksUseCase(newDate)
            Log.d("TodayViewModel", "Processed overdue tasks before advancing date")
            
            // Then advance the date
            dateProvider.advanceDebugDate(days)
            val actualNewDate = dateProvider.now()
            Log.d("TodayViewModel", "New debug date: $actualNewDate (offset: ${dateProvider.getDebugOffset()})")
            
            // Finally, reload tasks for the new date
            loadTasksDueToday()
        }
    }
}
