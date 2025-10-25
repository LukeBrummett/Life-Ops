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
                    val grouped = tasks.groupBy { it.category }
                        .toSortedMap()
                    
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
     * Toggle task completion status
     * Uses CompleteTaskUseCase to handle all business logic
     */
    private fun completeTask(taskId: Long) {
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
