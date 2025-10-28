package com.lifeops.app.presentation.taskdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel


/**
 * Task Detail Screen - comprehensive read-only view of a task
 * 
 * Displays all task information including:
 * - Basic properties (name, category, tags, priority, description)
 * - Schedule and recurrence
 * - Completion data
 * - Parent-child relationships
 * - Trigger relationships
 * - Inventory associations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    modifier: Modifier = Modifier,
    viewModel: TaskDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit = {},
    onNavigateToTask: (String) -> Unit = {},
    onNavigateToInventory: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val navigationEvent by viewModel.navigationEvent.collectAsState()
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // Handle navigation events
    LaunchedEffect(navigationEvent) {
        when (navigationEvent) {
            is TaskDetailNavigationEvent.NavigateBack -> {
                onNavigateBack()
                viewModel.consumeNavigationEvent()
            }
            null -> { /* No event */ }
        }
    }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.task?.name ?: "Task Detail",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { uiState.task?.let { onNavigateToEdit(it.id) } },
                        enabled = uiState.task != null
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit task"
                        )
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        enabled = uiState.task != null && !uiState.isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete task"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = uiState.errorMessage ?: "Unknown error",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = onNavigateBack) {
                            Text("Go Back")
                        }
                    }
                }
            }
            
            uiState.task != null -> {
                TaskDetailContent(
                    uiState = uiState,
                    onEvent = viewModel::onEvent,
                    onNavigateToTask = onNavigateToTask,
                    onNavigateToInventory = onNavigateToInventory,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        uiState.task?.let { task ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Task?") },
                text = {
                    Text(
                        "Are you sure you want to delete '${task.name}'? This action cannot be undone."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.onEvent(TaskDetailEvent.DeleteTask(task.id))
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
    
    // Inventory consumption prompt dialog
    if (uiState.showInventoryPrompt) {
        uiState.task?.let { task ->
            InventoryPromptDialog(
                taskName = task.name,
                inventoryItems = uiState.promptedInventoryItems,
                onConfirm = { consumptions ->
                    viewModel.onEvent(TaskDetailEvent.ConfirmInventoryConsumption(task.id, consumptions))
                },
                onDismiss = {
                    viewModel.onEvent(TaskDetailEvent.DismissInventoryPrompt)
                }
            )
        }
    }
}

/**
 * Main content area showing all task details
 */
@Composable
private fun TaskDetailContent(
    uiState: TaskDetailUiState,
    onEvent: (TaskDetailEvent) -> Unit,
    onNavigateToTask: (String) -> Unit,
    onNavigateToInventory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val task = uiState.task ?: return
    
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Basic Information Section
        item {
            BasicInformationSection(task = task)
        }
        
        // Schedule Information Section
        item {
            ScheduleInformationSection(
                task = task,
                scheduleDescription = uiState.scheduleDescription,
                excludedDaysText = uiState.excludedDaysText,
                excludedDatesText = uiState.excludedDatesText
            )
        }
        
        // Completion Data Section
        item {
            CompletionDataSection(
                task = task,
                statusDescription = uiState.statusDescription,
                lastCompletedRelative = uiState.lastCompletedRelative
            )
        }
        
        // Parent Task Section (if exists)
        if (uiState.parentTask != null) {
            item {
                ParentTaskSection(
                    parentTask = uiState.parentTask,
                    onClick = onNavigateToTask
                )
            }
        }
        
        // Child Tasks Section (if exists)
        if (uiState.childTasks.isNotEmpty()) {
            item {
                ChildTasksSection(
                    childTasks = uiState.childTasks,
                    requiresManualCompletion = task.requiresManualCompletion,
                    onClick = onNavigateToTask
                )
            }
        }
        
        // Triggered By Section (if exists)
        if (uiState.triggeredByTasks.isNotEmpty()) {
            item {
                TriggeredBySection(
                    tasks = uiState.triggeredByTasks,
                    onClick = onNavigateToTask
                )
            }
        }
        
        // Triggers Section (if exists)
        if (uiState.triggersTasks.isNotEmpty()) {
            item {
                TriggersSection(
                    tasks = uiState.triggersTasks,
                    onClick = onNavigateToTask
                )
            }
        }
        
        // Inventory Section (if exists)
        if (uiState.inventoryItems.isNotEmpty()) {
            item {
                InventorySection(
                    items = uiState.inventoryItems,
                    onClick = onNavigateToInventory
                )
            }
        }
        
        // Actions Section
        item {
            ActionsSection(
                canComplete = uiState.canComplete,
                onCompleteTask = { onEvent(TaskDetailEvent.CompleteTask(task.id)) }
            )
        }
        
        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ========== Section Composables ==========

/**
 * Basic Information Section
 */
@Composable
private fun BasicInformationSection(task: com.lifeops.app.data.local.entity.Task) {
    DetailSection(title = "Basic Information") {
        // Task Name
        Text(
            text = task.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Category
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Category,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = task.category,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Tags (if any) - TODO: Implement tags display when we finalize tag format
        // Currently tags is a comma-separated string, not JSON array
        
        // Description (if any)
        if (!task.description.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Schedule Information Section
 */
@Composable
private fun ScheduleInformationSection(
    task: com.lifeops.app.data.local.entity.Task,
    scheduleDescription: String,
    excludedDaysText: String?,
    excludedDatesText: String?
) {
    DetailSection(title = "Schedule") {
        // Next Due Date (if scheduled)
        if (task.nextDue != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Next due: ${task.nextDue}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Recurrence Pattern
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Repeat,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = scheduleDescription,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        // Overdue Behavior (if not ADHOC)
        if (task.intervalUnit != com.lifeops.app.data.local.entity.IntervalUnit.ADHOC) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = when (task.overdueBehavior) {
                        com.lifeops.app.data.local.entity.OverdueBehavior.POSTPONE -> "Postpone day-by-day"
                        com.lifeops.app.data.local.entity.OverdueBehavior.SKIP_TO_NEXT -> "Skip to next occurrence"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // Excluded Days (if any)
        if (!excludedDaysText.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.EventBusy,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Never schedules on: $excludedDaysText",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Ephemeral Task Indicator
        if (task.deleteAfterCompletion) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteSweep,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = "⏳ Ephemeral task - will be deleted after completion",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

/**
 * Completion Data Section
 */
@Composable
private fun CompletionDataSection(
    task: com.lifeops.app.data.local.entity.Task,
    statusDescription: String,
    lastCompletedRelative: String
) {
    DetailSection(title = "Completion Data") {
        // Last Completed
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Last completed: $lastCompletedRelative",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Current Streak
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.LocalFireDepartment,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (task.completionStreak > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Current streak: ${task.completionStreak}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Status
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Status: $statusDescription",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Parent Task Section
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParentTaskSection(
    parentTask: TaskSummary,
    onClick: (String) -> Unit
) {
    DetailSection(title = "Parent Task") {
        Card(
            onClick = { onClick(parentTask.taskId) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = parentTask.taskName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${parentTask.category} • ${parentTask.scheduleSummary}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Child Tasks Section
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChildTasksSection(
    childTasks: List<ChildTaskDisplay>,
    requiresManualCompletion: Boolean,
    onClick: (String) -> Unit
) {
    DetailSection(title = "Child Tasks (${childTasks.size})") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            childTasks.forEach { child ->
                Card(
                    onClick = { onClick(child.taskId) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${child.order}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = child.taskName,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "${child.category} • ${child.scheduleSummary}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            if (requiresManualCompletion) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.TouchApp,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "Requires manual completion (after all children done)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

/**
 * Triggered By Section
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TriggeredBySection(
    tasks: List<TaskSummary>,
    onClick: (String) -> Unit
) {
    DetailSection(title = "Triggered By") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "This task appears when completing:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            tasks.forEach { task ->
                Card(
                    onClick = { onClick(task.taskId) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = task.taskName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${task.category} • ${task.scheduleSummary}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Triggers Section
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TriggersSection(
    tasks: List<TaskSummary>,
    onClick: (String) -> Unit
) {
    DetailSection(title = "Triggers") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Completing this task triggers:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            tasks.forEach { task ->
                Card(
                    onClick = { onClick(task.taskId) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = task.taskName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${task.category} • ${task.scheduleSummary}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Inventory Section
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InventorySection(
    items: List<InventoryItemDisplay>,
    onClick: (String) -> Unit
) {
    DetailSection(title = "Inventory Consumption") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEach { item ->
                Card(
                    onClick = { onClick(item.supplyId) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📦 ${item.supplyName}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            if (item.isLowStock) {
                                Text(
                                    text = "⚠️ Low",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "${item.consumptionMode} • ${item.modeDetails}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Stock: ${item.currentStock}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (item.isLowStock) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Actions Section
 */
@Composable
private fun ActionsSection(
    canComplete: Boolean,
    onCompleteTask: () -> Unit
) {
    DetailSection(title = "Actions") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onCompleteTask,
                enabled = canComplete,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Complete Task")
            }
            
            if (!canComplete) {
                Text(
                    text = "Task can only be completed when due or overdue",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

/**
 * Reusable section container with title
 */
@Composable
private fun DetailSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
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
    // Track consumption quantities for each inventory item
    val consumptions = remember {
        mutableStateMapOf<String, Int>().apply {
            inventoryItems.forEach { item ->
                put(item.supplyId, item.defaultValue)
            }
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text("Complete Task: $taskName") 
        },
        text = {
            LazyColumn(
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
                    val consumption = consumptions[item.supplyId] ?: item.defaultValue
                    
                    // Single card with all content on one row
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Item name
                            Text(
                                text = item.supplyName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            
                            // Consumption amount
                            Text(
                                text = consumption.toString(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            
                            // Plus/Minus controls
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Minus button
                                FilledIconButton(
                                    onClick = {
                                        val current = consumptions[item.supplyId] ?: item.defaultValue
                                        if (current > 0) {
                                            consumptions[item.supplyId] = current - 1
                                        }
                                    },
                                    enabled = consumption > 0,
                                    modifier = Modifier.size(36.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Decrease",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                
                                // Plus button
                                FilledIconButton(
                                    onClick = {
                                        val current = consumptions[item.supplyId] ?: item.defaultValue
                                        consumptions[item.supplyId] = current + 1
                                    },
                                    modifier = Modifier.size(36.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Increase",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(consumptions.toMap())
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
