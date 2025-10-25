package com.lifeops.app.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeops.app.data.local.entity.Difficulty
import com.lifeops.app.data.local.entity.IntervalUnit
import com.lifeops.app.data.local.entity.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * ViewModel for the Today Screen
 * 
 * Manages UI state and handles user events for the Today Screen.
 * Currently uses mock data - will be connected to repository in Phase 3.
 */
@HiltViewModel
class TodayViewModel @Inject constructor(
    // TODO Phase 3: Inject GetTasksDueUseCase
    // TODO Phase 3: Inject CompleteTaskUseCase
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
     * Load tasks due today
     * TODO Phase 3: Replace with GetTasksDueUseCase
     */
    private fun loadTasksDueToday() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val today = LocalDate.now()
                val formattedDate = today.format(
                    DateTimeFormatter.ofPattern("MMM dd, yyyy")
                )
                
                // TODO Phase 3: Replace with real data from repository
                val tasks = getMockTasks()
                
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
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }
    
    /**
     * Toggle task completion status
     * TODO Phase 3: Replace with CompleteTaskUseCase
     */
    private fun completeTask(taskId: Long) {
        viewModelScope.launch {
            val today = LocalDate.now()
            
            // Update the task in the current state
            val updatedCategories = _uiState.value.tasksByCategory.mapValues { (_, tasks) ->
                tasks.map { task ->
                    if (task.id == taskId) {
                        // Toggle completion
                        task.copy(
                            lastCompleted = if (task.lastCompleted == today) null else today,
                            completionStreak = if (task.lastCompleted == today) {
                                (task.completionStreak - 1).coerceAtLeast(0)
                            } else {
                                task.completionStreak + 1
                            }
                        )
                    } else {
                        task
                    }
                }
            }
            
            val allComplete = updatedCategories.values.flatten().let { allTasks ->
                allTasks.isNotEmpty() && allTasks.all { it.lastCompleted == today }
            }
            
            _uiState.update {
                it.copy(
                    tasksByCategory = updatedCategories,
                    allTasksComplete = allComplete
                )
            }
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
     * Mock data for development
     * TODO Phase 3: Remove when connecting to repository
     */
    private fun getMockTasks(): List<Task> {
        val today = LocalDate.now()
        
        return listOf(
            Task(
                id = 1,
                name = "Morning Workout",
                category = "Fitness",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                difficulty = Difficulty.MEDIUM,
                timeEstimate = 30,
                nextDue = today,
                lastCompleted = null,
                completionStreak = 0,
                active = true
            ),
            Task(
                id = 2,
                name = "Stretch",
                category = "Fitness",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                difficulty = Difficulty.LOW,
                timeEstimate = 10,
                nextDue = today,
                lastCompleted = today,
                completionStreak = 5,
                active = true
            ),
            Task(
                id = 3,
                name = "Walk",
                category = "Fitness",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                difficulty = Difficulty.LOW,
                timeEstimate = 20,
                nextDue = today,
                lastCompleted = today,
                completionStreak = 3,
                active = true
            ),
            Task(
                id = 4,
                name = "Water Plants",
                category = "Home",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 3,
                difficulty = Difficulty.LOW,
                nextDue = today,
                lastCompleted = null,
                completionStreak = 0,
                active = true
            ),
            Task(
                id = 5,
                name = "Check Mail",
                category = "Home",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                difficulty = Difficulty.LOW,
                nextDue = today,
                lastCompleted = null,
                completionStreak = 0,
                active = true
            ),
            Task(
                id = 6,
                name = "Daily Standup",
                category = "Work",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                difficulty = Difficulty.LOW,
                timeEstimate = 15,
                nextDue = today,
                lastCompleted = today,
                completionStreak = 10,
                active = true
            ),
            Task(
                id = 7,
                name = "Read for 30 Minutes",
                category = "Personal",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                difficulty = Difficulty.MEDIUM,
                timeEstimate = 30,
                nextDue = today,
                lastCompleted = null,
                completionStreak = 0,
                active = true
            )
        )
    }
}
