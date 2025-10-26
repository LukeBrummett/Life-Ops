package com.lifeops.app.presentation.taskedit

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeops.app.data.local.entity.ConsumptionMode
import com.lifeops.app.data.local.entity.DayOfWeek
import com.lifeops.app.data.local.entity.IntervalUnit
import com.lifeops.app.data.local.entity.OverdueBehavior
import com.lifeops.app.domain.usecase.GetTaskDetailsUseCase
import com.lifeops.app.domain.usecase.task.InventoryAssociationRequest
import com.lifeops.app.domain.usecase.task.SaveTaskRequest
import com.lifeops.app.domain.usecase.task.SaveTaskUseCase
import com.lifeops.app.data.repository.TaskRepository
import com.lifeops.app.data.repository.SupplyRepository
import com.lifeops.app.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel for Task Edit/Create Screen
 * 
 * Manages:
 * - Loading existing task for edit mode
 * - Form field updates
 * - Validation
 * - Saving task with relationships
 */
@HiltViewModel
class TaskEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val saveTaskUseCase: SaveTaskUseCase,
    private val getTaskDetailsUseCase: GetTaskDetailsUseCase,
    private val taskRepository: TaskRepository,
    private val supplyRepository: SupplyRepository
) : ViewModel() {
    
    // Extract taskId from navigation args (null for create mode)
    private val taskId: String? = savedStateHandle.get<String>(Screen.TaskDetail.ARG_TASK_ID)
    
    private val _uiState = MutableStateFlow(TaskEditUiState(isCreateMode = taskId == null))
    val uiState: StateFlow<TaskEditUiState> = _uiState.asStateFlow()
    
    private val _events = MutableStateFlow<TaskEditViewModelEvent?>(null)
    val events: StateFlow<TaskEditViewModelEvent?> = _events.asStateFlow()
    
    /**
     * Consume the current event to prevent re-triggering
     */
    fun consumeEvent() {
        _events.value = null
    }
    
    init {
        loadInitialData()
    }
    
    /**
     * Load initial data (task for edit mode, dropdown options)
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Load dropdown options
                loadCategories()
                loadTags()
                loadAvailableTasks()
                loadAvailableSupplies()
                
                // If edit mode, load task data
                if (taskId != null) {
                    loadTaskForEdit(taskId)
                }
                
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                Log.e("TaskEditViewModel", "Error loading initial data", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load data: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Load categories for dropdown
     */
    private suspend fun loadCategories() {
        val result = taskRepository.getAllCategories()
        result.onSuccess { categories ->
            _uiState.update { it.copy(availableCategories = categories) }
        }
    }
    
    /**
     * Load existing tags for autocomplete
     */
    private suspend fun loadTags() {
        val tasks = taskRepository.getAllActiveTasks().getOrNull() ?: emptyList()
        val allTags = tasks
            .mapNotNull { it.tags }
            .filter { it.isNotBlank() }
            .flatMap { it.split(",") }
            .map { it.trim() }
            .distinct()
            .sorted()
        
        _uiState.update { it.copy(availableTags = allTags) }
    }
    
    /**
     * Load available tasks for relationship dropdowns
     */
    private suspend fun loadAvailableTasks() {
        val tasks = taskRepository.getAllActiveTasks().getOrNull() ?: emptyList()
        val taskReferences = tasks
            .filter { it.id != taskId } // Exclude current task
            .map { task ->
                TaskReference(
                    taskId = task.id,
                    taskName = task.name,
                    category = task.category
                )
            }
            .sortedBy { it.taskName }
        
        _uiState.update { it.copy(availableTasks = taskReferences) }
    }
    
    /**
     * Load available supplies for inventory dropdown
     */
    private suspend fun loadAvailableSupplies() {
        val supplies = supplyRepository.getAllSupplies()
        val supplyReferences = supplies.map { supply ->
            SupplyReference(
                supplyId = supply.id,
                supplyName = supply.name,
                unit = supply.unit
            )
        }.sortedBy { it.supplyName }
        
        _uiState.update { it.copy(availableSupplies = supplyReferences) }
    }
    
    /**
     * Load task data for edit mode
     */
    private suspend fun loadTaskForEdit(taskId: String) {
        val result = getTaskDetailsUseCase(taskId)
        
        if (result.isFailure || result.getOrNull() == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Task not found"
                )
            }
            return
        }
        
        val taskDetails = result.getOrNull()!!
        val task = taskDetails.task
        
        // Parse tags
        val tags = task.tags
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
        
        // Map child tasks
        val childTasks = taskDetails.childTasks.mapIndexed { index, childTask ->
            ChildTaskItem(
                taskId = childTask.id,
                taskName = childTask.name,
                category = childTask.category,
                order = index + 1
            )
        }
        
        // Map triggered by tasks
        val triggeredByTasks = taskDetails.triggeredByTasks.map { triggerTask ->
            TaskReference(
                taskId = triggerTask.id,
                taskName = triggerTask.name,
                category = triggerTask.category
            )
        }
        
        // Map triggers tasks
        val triggersTasks = taskDetails.triggersTasks.map { triggeredTask ->
            TaskReference(
                taskId = triggeredTask.id,
                taskName = triggeredTask.name,
                category = triggeredTask.category
            )
        }
        
        // Map inventory associations
        val inventoryAssociations = taskDetails.inventoryAssociations.map { assoc ->
            InventoryAssociationEdit(
                supplyId = assoc.supply.id,
                supplyName = assoc.supply.name,
                unit = assoc.supply.unit,
                consumptionMode = assoc.taskSupply.consumptionMode,
                fixedQuantity = assoc.taskSupply.fixedQuantity,
                promptedDefaultValue = assoc.taskSupply.promptedDefaultValue
            )
        }
        
        _uiState.update {
            it.copy(
                originalTaskId = taskId,
                name = task.name,
                category = task.category,
                tags = tags,
                description = task.description,
                intervalUnit = task.intervalUnit,
                intervalQty = task.intervalQty,
                specificDaysOfWeek = task.specificDaysOfWeek ?: emptyList(),
                excludedDaysOfWeek = task.excludedDaysOfWeek ?: emptyList(),
                overdueBehavior = task.overdueBehavior,
                deleteAfterCompletion = task.deleteAfterCompletion,
                parentTaskId = task.parentTaskIds?.firstOrNull(),
                parentTaskName = taskDetails.parentTask?.name,
                childTasks = childTasks,
                requiresManualCompletion = task.requiresManualCompletion,
                triggeredByTaskIds = triggeredByTasks,
                triggersTaskIds = triggersTasks,
                inventoryAssociations = inventoryAssociations
            )
        }
    }
    
    /**
     * Handle user events
     */
    fun onEvent(event: TaskEditEvent) {
        when (event) {
            // Basic Information
            is TaskEditEvent.UpdateName -> updateName(event.name)
            is TaskEditEvent.UpdateCategory -> updateCategory(event.category)
            is TaskEditEvent.AddTag -> addTag(event.tag)
            is TaskEditEvent.RemoveTag -> removeTag(event.tag)
            is TaskEditEvent.UpdateDescription -> updateDescription(event.description)
            
            // Schedule Configuration
            is TaskEditEvent.UpdateIntervalUnit -> updateIntervalUnit(event.unit)
            is TaskEditEvent.UpdateIntervalQty -> updateIntervalQty(event.qty)
            is TaskEditEvent.ToggleSpecificDay -> toggleSpecificDay(event.day)
            is TaskEditEvent.ToggleExcludedDay -> toggleExcludedDay(event.day)
            is TaskEditEvent.AddExcludedDateRange -> addExcludedDateRange(event.startDate, event.endDate)
            is TaskEditEvent.RemoveExcludedDateRange -> removeExcludedDateRange(event.dateRange)
            is TaskEditEvent.UpdateOverdueBehavior -> updateOverdueBehavior(event.behavior)
            is TaskEditEvent.UpdateDeleteAfterCompletion -> updateDeleteAfterCompletion(event.delete)
            
            // Relationships
            is TaskEditEvent.UpdateParentTask -> updateParentTask(event.taskId)
            is TaskEditEvent.AddChildTask -> addChildTask(event.taskId)
            is TaskEditEvent.RemoveChildTask -> removeChildTask(event.taskId)
            is TaskEditEvent.ReorderChildTasks -> reorderChildTasks(event.fromIndex, event.toIndex)
            is TaskEditEvent.UpdateRequiresManualCompletion -> updateRequiresManualCompletion(event.required)
            is TaskEditEvent.AddTriggeredByTask -> addTriggeredByTask(event.taskId)
            is TaskEditEvent.RemoveTriggeredByTask -> removeTriggeredByTask(event.taskId)
            is TaskEditEvent.AddTriggersTask -> addTriggersTask(event.taskId)
            is TaskEditEvent.RemoveTriggersTask -> removeTriggersTask(event.taskId)
            
            // Inventory
            is TaskEditEvent.AddInventoryAssociation -> addInventoryAssociation(event.supplyId)
            is TaskEditEvent.RemoveInventoryAssociation -> removeInventoryAssociation(event.supplyId)
            is TaskEditEvent.UpdateInventoryConsumptionMode -> updateInventoryMode(event.supplyId, event.mode)
            is TaskEditEvent.UpdateInventoryFixedQuantity -> updateInventoryFixedQty(event.supplyId, event.quantity)
            is TaskEditEvent.UpdateInventoryPromptedDefault -> updateInventoryPromptedDefault(event.supplyId, event.quantity)
            
            // Navigation
            TaskEditEvent.Save -> saveTask()
            TaskEditEvent.Cancel -> handleCancel()
            TaskEditEvent.DismissError -> _uiState.update { it.copy(errorMessage = null, validationErrors = emptyMap()) }
            is TaskEditEvent.NavigateToTaskDetail -> _events.value = TaskEditViewModelEvent.NavigateToDetail(event.taskId)
        }
    }
    
    // Basic Information Updates
    private fun updateName(name: String) {
        _uiState.update { it.copy(name = name, hasUnsavedChanges = true) }
    }
    
    private fun updateCategory(category: String) {
        _uiState.update { it.copy(category = category, hasUnsavedChanges = true) }
    }
    
    private fun addTag(tag: String) {
        if (tag.isBlank()) return
        val currentTags = _uiState.value.tags
        if (!currentTags.contains(tag)) {
            _uiState.update { it.copy(tags = currentTags + tag, hasUnsavedChanges = true) }
        }
    }
    
    private fun removeTag(tag: String) {
        _uiState.update {
            it.copy(
                tags = it.tags.filter { t -> t != tag },
                hasUnsavedChanges = true
            )
        }
    }
    
    private fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description, hasUnsavedChanges = true) }
    }
    
    // Schedule Configuration Updates
    private fun updateIntervalUnit(unit: IntervalUnit) {
        _uiState.update { it.copy(intervalUnit = unit, hasUnsavedChanges = true) }
    }
    
    private fun updateIntervalQty(qty: Int) {
        if (qty > 0) {
            _uiState.update { it.copy(intervalQty = qty, hasUnsavedChanges = true) }
        }
    }
    
    private fun toggleSpecificDay(day: DayOfWeek) {
        _uiState.update { state ->
            val currentDays = state.specificDaysOfWeek
            val newDays = if (currentDays.contains(day)) {
                currentDays - day
            } else {
                currentDays + day
            }
            state.copy(specificDaysOfWeek = newDays, hasUnsavedChanges = true)
        }
    }
    
    private fun toggleExcludedDay(day: DayOfWeek) {
        _uiState.update { state ->
            val currentDays = state.excludedDaysOfWeek
            val newDays = if (currentDays.contains(day)) {
                currentDays - day
            } else {
                currentDays + day
            }
            state.copy(excludedDaysOfWeek = newDays, hasUnsavedChanges = true)
        }
    }
    
    private fun addExcludedDateRange(startDate: LocalDate, endDate: LocalDate) {
        _uiState.update { state ->
            state.copy(
                excludedDateRanges = state.excludedDateRanges + DateRange(startDate, endDate),
                hasUnsavedChanges = true
            )
        }
    }
    
    private fun removeExcludedDateRange(dateRange: DateRange) {
        _uiState.update { state ->
            state.copy(
                excludedDateRanges = state.excludedDateRanges.filter { it != dateRange },
                hasUnsavedChanges = true
            )
        }
    }
    
    private fun updateOverdueBehavior(behavior: OverdueBehavior) {
        _uiState.update { it.copy(overdueBehavior = behavior, hasUnsavedChanges = true) }
    }
    
    private fun updateDeleteAfterCompletion(delete: Boolean) {
        _uiState.update { it.copy(deleteAfterCompletion = delete, hasUnsavedChanges = true) }
    }
    
    // Relationship Updates
    private fun updateParentTask(taskId: String?) {
        viewModelScope.launch {
            val parentName = if (taskId != null) {
                taskRepository.getTaskById(taskId).getOrNull()?.name
            } else null
            
            _uiState.update {
                it.copy(
                    parentTaskId = taskId,
                    parentTaskName = parentName,
                    hasUnsavedChanges = true
                )
            }
        }
    }
    
    private fun addChildTask(taskId: String) {
        viewModelScope.launch {
            val task = taskRepository.getTaskById(taskId).getOrNull() ?: return@launch
            
            _uiState.update { state ->
                if (state.childTasks.any { it.taskId == taskId }) {
                    return@update state // Already added
                }
                
                val newChild = ChildTaskItem(
                    taskId = task.id,
                    taskName = task.name,
                    category = task.category,
                    order = state.childTasks.size + 1
                )
                
                state.copy(
                    childTasks = state.childTasks + newChild,
                    hasUnsavedChanges = true
                )
            }
        }
    }
    
    private fun removeChildTask(taskId: String) {
        _uiState.update { state ->
            val newChildren = state.childTasks
                .filter { it.taskId != taskId }
                .mapIndexed { index, child -> child.copy(order = index + 1) }
            
            state.copy(
                childTasks = newChildren,
                hasUnsavedChanges = true
            )
        }
    }
    
    private fun reorderChildTasks(fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            val mutableList = state.childTasks.toMutableList()
            val item = mutableList.removeAt(fromIndex)
            mutableList.add(toIndex, item)
            
            val reorderedList = mutableList.mapIndexed { index, child ->
                child.copy(order = index + 1)
            }
            
            state.copy(
                childTasks = reorderedList,
                hasUnsavedChanges = true
            )
        }
    }
    
    private fun updateRequiresManualCompletion(required: Boolean) {
        _uiState.update { it.copy(requiresManualCompletion = required, hasUnsavedChanges = true) }
    }
    
    private fun addTriggeredByTask(taskId: String) {
        viewModelScope.launch {
            val task = taskRepository.getTaskById(taskId).getOrNull() ?: return@launch
            
            _uiState.update { state ->
                if (state.triggeredByTaskIds.any { it.taskId == taskId }) {
                    return@update state
                }
                
                val ref = TaskReference(
                    taskId = task.id,
                    taskName = task.name,
                    category = task.category
                )
                
                state.copy(
                    triggeredByTaskIds = state.triggeredByTaskIds + ref,
                    hasUnsavedChanges = true
                )
            }
        }
    }
    
    private fun removeTriggeredByTask(taskId: String) {
        _uiState.update { state ->
            state.copy(
                triggeredByTaskIds = state.triggeredByTaskIds.filter { it.taskId != taskId },
                hasUnsavedChanges = true
            )
        }
    }
    
    private fun addTriggersTask(taskId: String) {
        viewModelScope.launch {
            val task = taskRepository.getTaskById(taskId).getOrNull() ?: return@launch
            
            _uiState.update { state ->
                if (state.triggersTaskIds.any { it.taskId == taskId }) {
                    return@update state
                }
                
                val ref = TaskReference(
                    taskId = task.id,
                    taskName = task.name,
                    category = task.category
                )
                
                state.copy(
                    triggersTaskIds = state.triggersTaskIds + ref,
                    hasUnsavedChanges = true
                )
            }
        }
    }
    
    private fun removeTriggersTask(taskId: String) {
        _uiState.update { state ->
            state.copy(
                triggersTaskIds = state.triggersTaskIds.filter { it.taskId != taskId },
                hasUnsavedChanges = true
            )
        }
    }
    
    // Inventory Updates
    private fun addInventoryAssociation(supplyId: String) {
        val supply = _uiState.value.availableSupplies.find { it.supplyId == supplyId } ?: return
        
        _uiState.update { state ->
            if (state.inventoryAssociations.any { it.supplyId == supplyId }) {
                return@update state
            }
            
            val newAssoc = InventoryAssociationEdit(
                supplyId = supply.supplyId,
                supplyName = supply.supplyName,
                unit = supply.unit,
                consumptionMode = ConsumptionMode.FIXED,
                fixedQuantity = 1
            )
            
            state.copy(
                inventoryAssociations = state.inventoryAssociations + newAssoc,
                hasUnsavedChanges = true
            )
        }
    }
    
    private fun removeInventoryAssociation(supplyId: String) {
        _uiState.update { state ->
            state.copy(
                inventoryAssociations = state.inventoryAssociations.filter { it.supplyId != supplyId },
                hasUnsavedChanges = true
            )
        }
    }
    
    private fun updateInventoryMode(supplyId: String, mode: ConsumptionMode) {
        _uiState.update { state ->
            state.copy(
                inventoryAssociations = state.inventoryAssociations.map { assoc ->
                    if (assoc.supplyId == supplyId) {
                        assoc.copy(consumptionMode = mode)
                    } else {
                        assoc
                    }
                },
                hasUnsavedChanges = true
            )
        }
    }
    
    private fun updateInventoryFixedQty(supplyId: String, quantity: Int) {
        _uiState.update { state ->
            state.copy(
                inventoryAssociations = state.inventoryAssociations.map { assoc ->
                    if (assoc.supplyId == supplyId) {
                        assoc.copy(fixedQuantity = quantity)
                    } else {
                        assoc
                    }
                },
                hasUnsavedChanges = true
            )
        }
    }
    
    private fun updateInventoryPromptedDefault(supplyId: String, quantity: Int) {
        _uiState.update { state ->
            state.copy(
                inventoryAssociations = state.inventoryAssociations.map { assoc ->
                    if (assoc.supplyId == supplyId) {
                        assoc.copy(promptedDefaultValue = quantity)
                    } else {
                        assoc
                    }
                },
                hasUnsavedChanges = true
            )
        }
    }
    
    // Save Task
    private fun saveTask() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSaving = true, validationErrors = emptyMap()) }
                
                val state = _uiState.value
                val isCreateMode = state.isCreateMode
                
                // Build save request
                val request = SaveTaskRequest(
                    taskId = state.originalTaskId,
                    name = state.name,
                    category = state.category,
                    tags = state.tags,
                    description = state.description,
                    intervalUnit = state.intervalUnit,
                    intervalQty = state.intervalQty,
                    specificDaysOfWeek = state.specificDaysOfWeek,
                    excludedDaysOfWeek = state.excludedDaysOfWeek,
                    overdueBehavior = state.overdueBehavior,
                    deleteAfterCompletion = state.deleteAfterCompletion,
                    nextDue = null, // Let use case handle
                    parentTaskId = state.parentTaskId,
                    childTaskIds = state.childTasks.map { it.taskId },
                    requiresManualCompletion = state.requiresManualCompletion,
                    triggeredByTaskIds = state.triggeredByTaskIds.map { it.taskId },
                    triggersTaskIds = state.triggersTaskIds.map { it.taskId },
                    inventoryAssociations = state.inventoryAssociations.map { assoc ->
                        InventoryAssociationRequest(
                            supplyId = assoc.supplyId,
                            consumptionMode = assoc.consumptionMode,
                            fixedQuantity = assoc.fixedQuantity,
                            promptedDefaultValue = assoc.promptedDefaultValue
                        )
                    }
                )
                
                // Save
                val result = saveTaskUseCase(request)
                
                if (result.isSuccess) {
                    val savedTaskId = result.getOrNull()!!
                    _uiState.update { it.copy(isSaving = false, hasUnsavedChanges = false) }
                    _events.value = TaskEditViewModelEvent.NavigateToDetail(savedTaskId, isCreateMode)
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Unknown error"
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("TaskEditViewModel", "Error saving task", e)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Failed to save: ${e.message}"
                    )
                }
            }
        }
    }
    
    // Cancel
    private fun handleCancel() {
        if (_uiState.value.hasUnsavedChanges) {
            _events.value = TaskEditViewModelEvent.ShowUnsavedChangesDialog
        } else {
            _events.value = TaskEditViewModelEvent.NavigateBack
        }
    }
}

/**
 * One-time events from ViewModel
 */
sealed class TaskEditViewModelEvent {
    data class NavigateToDetail(val taskId: String, val isCreateMode: Boolean) : TaskEditViewModelEvent()
    data object NavigateBack : TaskEditViewModelEvent()
    data object ShowUnsavedChangesDialog : TaskEditViewModelEvent()
}
