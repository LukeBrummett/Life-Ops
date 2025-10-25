package com.lifeops.app.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeops.app.domain.usecase.CompleteTaskUseCase
import com.lifeops.app.domain.usecase.GetTasksDueUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
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
    private val completeTaskUseCase: CompleteTaskUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()
    
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
        }
    }
    
    /**
     * Load tasks due today from the database
     * Observes changes and updates UI state reactively
     */
    private fun loadTasksDueToday() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val today = LocalDate.now()
            val formattedDate = today.format(
                DateTimeFormatter.ofPattern("MMM dd, yyyy")
            )
            
            getTasksDueUseCase(today)
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load tasks"
                        )
                    }
                }
                .collect { tasks ->
                    // Group tasks by category with parent-child relationships
                    val grouped = groupTasksWithHierarchy(tasks)
                    
                    val allComplete = tasks.isNotEmpty() && 
                        tasks.all { it.lastCompleted == today }
                    
                    _uiState.update {
                        it.copy(
                            currentDate = formattedDate,
                            tasksByCategory = grouped,
                            allTasksComplete = allComplete,
                            isLoading = false,
                            error = null
                        )
                    }
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
            val today = LocalDate.now()
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
}
