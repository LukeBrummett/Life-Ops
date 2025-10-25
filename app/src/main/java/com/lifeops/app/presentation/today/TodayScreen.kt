package com.lifeops.app.presentation.today

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.lifeops.app.presentation.today.components.*
import com.lifeops.app.ui.theme.LifeOpsTheme
import java.time.LocalDate

/**
 * Main Today Screen - displays all tasks due today organized by category
 * 
 * This screen shows:
 * - Header with navigation and filter controls
 * - Loading state while fetching tasks
 * - List of tasks grouped by category
 * - Empty state when no tasks are present
 * - Error state if loading fails
 */
@Composable
fun TodayScreen(
    modifier: Modifier = Modifier,
    onNavigateToAllTasks: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onDateClick: () -> Unit = {},
    onTaskChecked: (Long) -> Unit = {},
    onRetry: () -> Unit = {}
) {
    // TODO: Connect to ViewModel when implemented
    // For now, using static mock data for UI development
    val isLoading = false
    val hasError = false
    val errorMessage = ""
    val showCompleted = false
    val selectedDate = LocalDate.now()
    val tasksByCategory = MockData.tasksByCategory
    
    TodayScreenContent(
        modifier = modifier,
        isLoading = isLoading,
        hasError = hasError,
        errorMessage = errorMessage,
        showCompleted = showCompleted,
        selectedDate = selectedDate,
        tasksByCategory = tasksByCategory,
        onNavigateToAllTasks = onNavigateToAllTasks,
        onNavigateToInventory = onNavigateToInventory,
        onNavigateToSettings = onNavigateToSettings,
        onToggleCompleted = { /* TODO: Toggle show completed */ },
        onDateClick = onDateClick,
        onTaskChecked = onTaskChecked,
        onRetry = onRetry
    )
}

/**
 * Content composable for Today Screen
 * Separated for easier testing and state management
 */
@Composable
private fun TodayScreenContent(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    hasError: Boolean,
    errorMessage: String,
    showCompleted: Boolean,
    selectedDate: LocalDate,
    tasksByCategory: Map<String, List<com.lifeops.app.data.local.entity.Task>>,
    onNavigateToAllTasks: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onToggleCompleted: () -> Unit,
    onDateClick: () -> Unit,
    onTaskChecked: (Long) -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TodayScreenHeader(
                selectedDate = selectedDate,
                showCompleted = showCompleted,
                onNavigateToAllTasks = onNavigateToAllTasks,
                onToggleCompleted = onToggleCompleted,
                onDateClick = onDateClick,
                onNavigateToInventory = onNavigateToInventory,
                onNavigateToSettings = onNavigateToSettings
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    LoadingState()
                }
                hasError -> {
                    ErrorState(
                        errorMessage = errorMessage,
                        onRetry = onRetry
                    )
                }
                tasksByCategory.isEmpty() -> {
                    EmptyState(isAllComplete = false)
                }
                else -> {
                    // Check if all tasks are complete
                    val allTasksComplete = tasksByCategory.values.flatten().all { task ->
                        task.lastCompleted == selectedDate
                    }
                    
                    if (allTasksComplete) {
                        EmptyState(isAllComplete = true)
                    } else {
                        TasksList(
                            tasksByCategory = tasksByCategory,
                            showCompleted = showCompleted,
                            onTaskChecked = onTaskChecked
                        )
                    }
                }
            }
        }
    }
}

// ================================
// Preview Functions
// ================================

@Preview(name = "Today Screen - Has Tasks", showBackground = true)
@Composable
private fun PreviewTodayScreenWithTasks() {
    LifeOpsTheme {
        Surface {
            TodayScreen()
        }
    }
}

@Preview(name = "Today Screen - Loading", showBackground = true)
@Composable
private fun PreviewTodayScreenLoading() {
    LifeOpsTheme {
        Surface {
            TodayScreenContent(
                isLoading = true,
                hasError = false,
                errorMessage = "",
                showCompleted = false,
                selectedDate = LocalDate.now(),
                tasksByCategory = emptyMap(),
                onNavigateToAllTasks = {},
                onNavigateToInventory = {},
                onNavigateToSettings = {},
                onToggleCompleted = {},
                onDateClick = {},
                onTaskChecked = {},
                onRetry = {}
            )
        }
    }
}

@Preview(name = "Today Screen - Empty", showBackground = true)
@Composable
private fun PreviewTodayScreenEmpty() {
    LifeOpsTheme {
        Surface {
            TodayScreenContent(
                isLoading = false,
                hasError = false,
                errorMessage = "",
                showCompleted = false,
                selectedDate = LocalDate.now(),
                tasksByCategory = emptyMap(),
                onNavigateToAllTasks = {},
                onNavigateToInventory = {},
                onNavigateToSettings = {},
                onToggleCompleted = {},
                onDateClick = {},
                onTaskChecked = {},
                onRetry = {}
            )
        }
    }
}

@Preview(name = "Today Screen - Error", showBackground = true)
@Composable
private fun PreviewTodayScreenError() {
    LifeOpsTheme {
        Surface {
            TodayScreenContent(
                isLoading = false,
                hasError = true,
                errorMessage = "Failed to load tasks",
                showCompleted = false,
                selectedDate = LocalDate.now(),
                tasksByCategory = emptyMap(),
                onNavigateToAllTasks = {},
                onNavigateToInventory = {},
                onNavigateToSettings = {},
                onToggleCompleted = {},
                onDateClick = {},
                onTaskChecked = {},
                onRetry = {}
            )
        }
    }
}
