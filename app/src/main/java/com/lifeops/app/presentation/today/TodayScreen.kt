package com.lifeops.app.presentation.today

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    viewModel: TodayViewModel = hiltViewModel(),
    onNavigateToAllTasks: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToTaskDetail: (String) -> Unit = {},
    onNavigateToTaskCreate: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    TodayScreenContent(
        modifier = modifier,
        uiState = uiState,
        onEvent = { event ->
            // Handle navigation events directly
            when (event) {
                is TodayUiEvent.NavigateToAllTasks -> onNavigateToAllTasks()
                is TodayUiEvent.NavigateToInventory -> onNavigateToInventory()
                is TodayUiEvent.NavigateToSettings -> onNavigateToSettings()
                is TodayUiEvent.NavigateToTaskDetail -> onNavigateToTaskDetail(event.taskId)
                is TodayUiEvent.NavigateToTaskCreate -> onNavigateToTaskCreate()
                else -> viewModel.onEvent(event) // Pass other events to ViewModel
            }
        }
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
    var showDebugMenu by remember { mutableStateOf(false) }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TodayScreenHeader(
                selectedDate = uiState.currentDateValue,
                showCompleted = uiState.showCompleted,
                onNavigateToAllTasks = { onEvent(TodayUiEvent.NavigateToAllTasks) },
                onToggleCompleted = { onEvent(TodayUiEvent.ToggleShowCompleted) },
                onDateClick = { /* Future: date picker */ },
                onNavigateToInventory = { onEvent(TodayUiEvent.NavigateToInventory) },
                onNavigateToSettings = { onEvent(TodayUiEvent.NavigateToSettings) }
            )
        },
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Debug FAB - bottom left
                Box {
                    SmallFloatingActionButton(
                        onClick = { showDebugMenu = !showDebugMenu },
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    ) {
                        Icon(
                            Icons.Default.BugReport,
                            contentDescription = "Debug date controls"
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showDebugMenu,
                        onDismissRequest = { showDebugMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("+1 Day") },
                            onClick = {
                                onEvent(TodayUiEvent.DebugAdvanceDate(1))
                                showDebugMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("+7 Days") },
                            onClick = {
                                onEvent(TodayUiEvent.DebugAdvanceDate(7))
                                showDebugMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("+30 Days") },
                            onClick = {
                                onEvent(TodayUiEvent.DebugAdvanceDate(30))
                                showDebugMenu = false
                            }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Reset to Today") },
                            onClick = {
                                onEvent(TodayUiEvent.DebugResetDate)
                                showDebugMenu = false
                            }
                        )
                    }
                }
                
                // New Task FAB
                ExtendedFloatingActionButton(
                    onClick = { onEvent(TodayUiEvent.NavigateToTaskCreate) },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add task") },
                    text = { Text("New Task") }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
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
                            onTaskChecked = { taskId -> onEvent(TodayUiEvent.CompleteTask(taskId)) },
                            onTaskClick = { taskId -> onEvent(TodayUiEvent.NavigateToTaskDetail(taskId)) }
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
