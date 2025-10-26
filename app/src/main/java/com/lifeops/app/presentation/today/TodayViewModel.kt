package com.lifeops.app.presentation.today

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeops.app.domain.usecase.CompleteTaskUseCase
import com.lifeops.app.domain.usecase.GetTasksDueUseCase
import com.lifeops.app.domain.usecase.ProcessOverdueTasksUseCase
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
            is TodayUiEvent.CompleteTask -> completeTask(event.taskId)
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
     * Toggle task completion status
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
