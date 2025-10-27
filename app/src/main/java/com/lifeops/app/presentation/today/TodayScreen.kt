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
import com.lifeops.app.presentation.taskdetail.PromptedInventoryItem
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
    settingsViewModel: com.lifeops.presentation.settings.SettingsViewModel = hiltViewModel(),
    onNavigateToAllTasks: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToTaskDetail: (String) -> Unit = {},
    onNavigateToTaskCreate: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val settingsState by settingsViewModel.uiState.collectAsState()
    
    TodayScreenContent(
        modifier = modifier,
        uiState = uiState,
        debugMode = settingsState.debugMode,
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
    debugMode: Boolean = false,
    onEvent: (TodayUiEvent) -> Unit
) {
    var showDebugMenu by remember { mutableStateOf(false) }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TodayScreenHeader(
                selectedDate = uiState.currentDateValue,
                todayDate = uiState.currentDateValue, // Pass the same date - it IS "today" from DateProvider's perspective
                showCompleted = uiState.showCompleted,
                onNavigateToAllTasks = { onEvent(TodayUiEvent.NavigateToAllTasks) },
                onToggleCompleted = { onEvent(TodayUiEvent.ToggleShowCompleted) },
                onDateClick = { /* Future: date picker */ },
                onNavigateToInventory = { onEvent(TodayUiEvent.NavigateToInventory) },
                onNavigateToSettings = { onEvent(TodayUiEvent.NavigateToSettings) }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onEvent(TodayUiEvent.NavigateToTaskCreate) },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add task") },
                text = { Text("New Task") }
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
                            today = uiState.currentDateValue,
                            showCompleted = uiState.showCompleted,
                            onTaskChecked = { taskId -> onEvent(TodayUiEvent.CompleteTask(taskId)) },
                            onTaskClick = { taskId -> onEvent(TodayUiEvent.NavigateToTaskDetail(taskId)) }
                        )
                    }
                }
            }
            
            // Debug FAB - positioned in bottom-start (only shown when debug mode is enabled)
            if (debugMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
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
                    }
                }
            }
        }
        
        // Inventory consumption prompt dialog
        if (uiState.showInventoryPrompt && uiState.inventoryPromptTaskId != null) {
            InventoryPromptDialog(
                taskName = uiState.inventoryPromptTaskName ?: "",
                inventoryItems = uiState.promptedInventoryItems,
                onConfirm = { consumptions ->
                    onEvent(TodayUiEvent.ConfirmInventoryConsumption(
                        uiState.inventoryPromptTaskId,
                        consumptions
                    ))
                },
                onDismiss = {
                    onEvent(TodayUiEvent.DismissInventoryPrompt)
                }
            )
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

/**
 * Dialog for prompting inventory consumption when completing a task
 */
@Composable
private fun InventoryPromptDialog(
    taskName: String,
    inventoryItems: List<PromptedInventoryItem>,
    onConfirm: (Map<String, Int>) -> Unit,
    onDismiss: () -> Unit
) {
    // Track user input for each inventory item
    val consumptions = remember {
        androidx.compose.runtime.mutableStateMapOf<String, String>().apply {
            inventoryItems.forEach { item ->
                put(item.supplyId, item.defaultValue.toString())
            }
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text("Complete Task: $taskName") 
        },
        text = {
            androidx.compose.foundation.lazy.LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "How much of each supply did you use?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                items(inventoryItems) { item ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = item.supplyName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        )
                        Text(
                            text = "Current stock: ${item.currentQuantity} ${item.unit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = consumptions[item.supplyId] ?: item.defaultValue.toString(),
                            onValueChange = { value ->
                                // Only allow numeric input
                                if (value.isEmpty() || value.all { it.isDigit() }) {
                                    consumptions[item.supplyId] = value
                                }
                            },
                            label = { Text("Quantity used (${item.unit})") },
                            suffix = { Text(item.unit) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Convert string inputs to integers
                    val consumptionMap = consumptions.mapValues { (_, value) ->
                        value.toIntOrNull() ?: 0
                    }
                    onConfirm(consumptionMap)
                }
            ) {
                Text("Complete Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
