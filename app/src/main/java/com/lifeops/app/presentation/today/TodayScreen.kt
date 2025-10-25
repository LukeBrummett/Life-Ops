package com.lifeops.app.presentation.today

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
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
    viewModel: TodayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    TodayScreenContent(
        modifier = modifier,
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

/**
 * Content composable for Today Screen
 * Separated for easier testing and state management
 */
@Composable
private fun TodayScreenContent(
    modifier: Modifier = Modifier,
    uiState: TodayUiState,
    onEvent: (TodayUiEvent) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TodayScreenHeader(
                selectedDate = LocalDate.parse(
                    uiState.currentDate.ifEmpty { LocalDate.now().toString() },
                    java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")
                ),
                showCompleted = uiState.showCompleted,
                onNavigateToAllTasks = { onEvent(TodayUiEvent.NavigateToAllTasks) },
                onToggleCompleted = { onEvent(TodayUiEvent.ToggleShowCompleted) },
                onDateClick = { /* Future: date picker */ },
                onNavigateToInventory = { onEvent(TodayUiEvent.NavigateToInventory) },
                onNavigateToSettings = { onEvent(TodayUiEvent.NavigateToSettings) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingState()
                }
                uiState.error != null -> {
                    ErrorState(
                        errorMessage = uiState.error,
                        onRetry = { onEvent(TodayUiEvent.Refresh) }
                    )
                }
                uiState.tasksByCategory.isEmpty() -> {
                    EmptyState(isAllComplete = false)
                }
                else -> {
                    if (uiState.allTasksComplete && !uiState.showCompleted) {
                        EmptyState(isAllComplete = true)
                    } else {
                        TasksList(
                            tasksByCategory = uiState.tasksByCategory,
                            showCompleted = uiState.showCompleted,
                            onTaskChecked = { taskId -> onEvent(TodayUiEvent.CompleteTask(taskId)) }
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
                uiState = TodayUiState(
                    isLoading = true,
                    currentDate = "Oct 25, 2025"
                ),
                onEvent = {}
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
                uiState = TodayUiState(
                    currentDate = "Oct 25, 2025",
                    tasksByCategory = emptyMap()
                ),
                onEvent = {}
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
                uiState = TodayUiState(
                    currentDate = "Oct 25, 2025",
                    error = "Failed to load tasks"
                ),
                onEvent = {}
            )
        }
    }
}
