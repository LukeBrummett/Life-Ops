package com.lifeops.app.presentation.taskedit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeops.app.data.local.entity.ConsumptionMode
import com.lifeops.app.data.local.entity.DayOfWeek
import com.lifeops.app.data.local.entity.IntervalUnit
import com.lifeops.app.data.local.entity.OverdueBehavior

/**
 * Task Edit/Create Screen
 * 
 * Comprehensive editing interface for tasks with:
 * - Basic information (name, category, tags, description)
 * - Schedule configuration (interval, days, exclusions)
 * - Relationships (parent, children, triggers)
 * - Inventory associations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditScreen(
    modifier: Modifier = Modifier,
    viewModel: TaskEditViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToTaskDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val events by viewModel.events.collectAsState()
    
    // State for unsaved changes dialog
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }
    
    // Handle one-time events
    LaunchedEffect(events) {
        when (val event = events) {
            is TaskEditViewModelEvent.NavigateToDetail -> {
                viewModel.consumeEvent()
                onNavigateToTaskDetail(event.taskId)
            }
            is TaskEditViewModelEvent.NavigateBack -> {
                viewModel.consumeEvent()
                onNavigateBack()
            }
            is TaskEditViewModelEvent.ShowUnsavedChangesDialog -> {
                showUnsavedChangesDialog = true
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
                        text = if (uiState.isCreateMode) "New Task" else "Edit Task"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(TaskEditEvent.Cancel) }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cancel"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.onEvent(TaskEditEvent.Save) },
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Save,
                                contentDescription = "Save"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            TaskEditContent(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
        
        // Error snackbar
        uiState.errorMessage?.let { error ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.onEvent(TaskEditEvent.DismissError) }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(error)
            }
        }
    }
    
    // Unsaved changes confirmation dialog
    if (showUnsavedChangesDialog) {
        AlertDialog(
            onDismissRequest = {
                showUnsavedChangesDialog = false
                viewModel.consumeEvent()
            },
            title = { Text("Unsaved Changes") },
            text = { Text("You have unsaved changes. Are you sure you want to discard them?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnsavedChangesDialog = false
                        viewModel.consumeEvent()
                        onNavigateBack()
                    }
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showUnsavedChangesDialog = false
                        viewModel.consumeEvent()
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TaskEditContent(
    uiState: TaskEditUiState,
    onEvent: (TaskEditEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Basic Information Section
        BasicInformationSection(
            name = uiState.name,
            category = uiState.category,
            tags = uiState.tags,
            description = uiState.description,
            availableCategories = uiState.availableCategories,
            availableTags = uiState.availableTags,
            validationErrors = uiState.validationErrors,
            onEvent = onEvent
        )
        
        Divider()
        
        // Schedule Configuration Section
        ScheduleConfigurationSection(
            intervalUnit = uiState.intervalUnit,
            intervalQty = uiState.intervalQty,
            specificDaysOfWeek = uiState.specificDaysOfWeek,
            excludedDaysOfWeek = uiState.excludedDaysOfWeek,
            excludedDateRanges = uiState.excludedDateRanges,
            overdueBehavior = uiState.overdueBehavior,
            deleteAfterCompletion = uiState.deleteAfterCompletion,
            onEvent = onEvent
        )
        
        Divider()
        
        // Relationships Section
        RelationshipsSection(
            parentTaskId = uiState.parentTaskId,
            parentTaskName = uiState.parentTaskName,
            childTasks = uiState.childTasks,
            requiresManualCompletion = uiState.requiresManualCompletion,
            triggeredByTasks = uiState.triggeredByTaskIds,
            triggersTasks = uiState.triggersTaskIds,
            availableTasks = uiState.availableTasks,
            onEvent = onEvent
        )
        
        Divider()
        
        // Inventory Section
        InventorySection(
            inventoryAssociations = uiState.inventoryAssociations,
            availableSupplies = uiState.availableSupplies,
            onEvent = onEvent
        )
    }
}

/**
 * Basic Information Section
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BasicInformationSection(
    name: String,
    category: String,
    tags: List<String>,
    description: String,
    availableCategories: List<String>,
    availableTags: List<String>,
    validationErrors: Map<String, String>,
    onEvent: (TaskEditEvent) -> Unit
) {
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showTagDropdown by remember { mutableStateOf(false) }
    var tagSearchText by remember { mutableStateOf("") }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Basic Information",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        // Task Name
        OutlinedTextField(
            value = name,
            onValueChange = { onEvent(TaskEditEvent.UpdateName(it)) },
            label = { Text("Task Name *") },
            isError = validationErrors.containsKey(ValidationError.NAME_REQUIRED),
            supportingText = {
                validationErrors[ValidationError.NAME_REQUIRED]?.let { Text(it) }
            },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth()
        )
        
        // Category Dropdown
        ExposedDropdownMenuBox(
            expanded = showCategoryDropdown,
            onExpandedChange = { showCategoryDropdown = it }
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = { onEvent(TaskEditEvent.UpdateCategory(it)) },
                label = { Text("Category *") },
                readOnly = false,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = showCategoryDropdown,
                onDismissRequest = { showCategoryDropdown = false }
            ) {
                availableCategories.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat) },
                        onClick = {
                            onEvent(TaskEditEvent.UpdateCategory(cat))
                            showCategoryDropdown = false
                        }
                    )
                }
            }
        }
        
        // Tags
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = tagSearchText,
                onValueChange = { tagSearchText = it },
                label = { Text("Add Tags") },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (tagSearchText.isNotBlank()) {
                                onEvent(TaskEditEvent.AddTag(tagSearchText.trim()))
                                tagSearchText = ""
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Add, "Add tag")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Tag chips
            if (tags.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tags.forEach { tag ->
                        InputChip(
                            selected = false,
                            onClick = { onEvent(TaskEditEvent.RemoveTag(tag)) },
                            label = { Text(tag) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Remove $tag",
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.padding(0.dp)
                        )
                    }
                }
            }
        }
        
        // Description
        OutlinedTextField(
            value = description,
            onValueChange = { onEvent(TaskEditEvent.UpdateDescription(it)) },
            label = { Text("Description / Notes") },
            minLines = 3,
            maxLines = 6,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Schedule Configuration Section
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleConfigurationSection(
    intervalUnit: IntervalUnit,
    intervalQty: Int,
    specificDaysOfWeek: List<DayOfWeek>,
    excludedDaysOfWeek: List<DayOfWeek>,
    excludedDateRanges: List<DateRange>,
    overdueBehavior: OverdueBehavior,
    deleteAfterCompletion: Boolean,
    onEvent: (TaskEditEvent) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Schedule Configuration",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
        }
        
        if (expanded) {
            // Interval and Days of Week Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Interval Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Interval",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    // Interval quantity input and unit dropdown
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Number input
                        OutlinedTextField(
                            value = if (intervalUnit == IntervalUnit.ADHOC) "" else intervalQty.toString(),
                            onValueChange = { newValue ->
                                if (intervalUnit != IntervalUnit.ADHOC) {
                                    newValue.toIntOrNull()?.let { qty ->
                                        if (qty > 0) onEvent(TaskEditEvent.UpdateIntervalQty(qty))
                                    }
                                }
                            },
                            enabled = intervalUnit != IntervalUnit.ADHOC,
                            modifier = Modifier.width(80.dp),
                            singleLine = true
                        )
                        
                        // Unit dropdown
                        var intervalDropdownExpanded by remember { mutableStateOf(false) }
                        
                        ExposedDropdownMenuBox(
                            expanded = intervalDropdownExpanded,
                            onExpandedChange = { intervalDropdownExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = when (intervalUnit) {
                                    IntervalUnit.DAY -> if (intervalQty == 1) "Day" else "Days"
                                    IntervalUnit.WEEK -> if (intervalQty == 1) "Week" else "Weeks"
                                    IntervalUnit.MONTH -> if (intervalQty == 1) "Month" else "Months"
                                    IntervalUnit.ADHOC -> "ADHOC"
                                },
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = intervalDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                singleLine = true
                            )
                            
                            ExposedDropdownMenu(
                                expanded = intervalDropdownExpanded,
                                onDismissRequest = { intervalDropdownExpanded = false }
                            ) {
                                IntervalUnit.entries.forEach { unit ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                when (unit) {
                                                    IntervalUnit.DAY -> if (intervalQty == 1) "Day" else "Days"
                                                    IntervalUnit.WEEK -> if (intervalQty == 1) "Week" else "Weeks"
                                                    IntervalUnit.MONTH -> if (intervalQty == 1) "Month" else "Months"
                                                    IntervalUnit.ADHOC -> "ADHOC"
                                                }
                                            )
                                        },
                                        onClick = {
                                            onEvent(TaskEditEvent.UpdateIntervalUnit(unit))
                                            intervalDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Days of Week Column (only for WEEK interval)
                if (intervalUnit == IntervalUnit.WEEK) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Days of Week",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        // Day toggles: M T W R F S U
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Reorder to show M-U (Monday to Sunday)
                            val orderedDays = listOf(
                                DayOfWeek.MONDAY,
                                DayOfWeek.TUESDAY,
                                DayOfWeek.WEDNESDAY,
                                DayOfWeek.THURSDAY,
                                DayOfWeek.FRIDAY,
                                DayOfWeek.SATURDAY,
                                DayOfWeek.SUNDAY
                            )
                            
                            orderedDays.forEach { day ->
                                FilterChip(
                                    selected = specificDaysOfWeek.contains(day),
                                    onClick = { onEvent(TaskEditEvent.ToggleSpecificDay(day)) },
                                    label = {
                                        Text(
                                            text = when (day) {
                                                DayOfWeek.MONDAY -> "M"
                                                DayOfWeek.TUESDAY -> "T"
                                                DayOfWeek.WEDNESDAY -> "W"
                                                DayOfWeek.THURSDAY -> "R"
                                                DayOfWeek.FRIDAY -> "F"
                                                DayOfWeek.SATURDAY -> "S"
                                                DayOfWeek.SUNDAY -> "U"
                                            }
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
            
            // Never Schedule On - similar to days of week toggle
            Text(
                text = "Never schedule on",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Reorder to show M-U (Monday to Sunday)
                val orderedDays = listOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY,
                    DayOfWeek.SATURDAY,
                    DayOfWeek.SUNDAY
                )
                
                orderedDays.forEach { day ->
                    FilterChip(
                        selected = excludedDaysOfWeek.contains(day),
                        onClick = { onEvent(TaskEditEvent.ToggleExcludedDay(day)) },
                        label = {
                            Text(
                                text = when (day) {
                                    DayOfWeek.MONDAY -> "M"
                                    DayOfWeek.TUESDAY -> "T"
                                    DayOfWeek.WEDNESDAY -> "W"
                                    DayOfWeek.THURSDAY -> "R"
                                    DayOfWeek.FRIDAY -> "F"
                                    DayOfWeek.SATURDAY -> "S"
                                    DayOfWeek.SUNDAY -> "U"
                                }
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Overdue Behavior (not for ADHOC)
            if (intervalUnit != IntervalUnit.ADHOC) {
                Text(
                    text = "When incomplete at end of day:",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OverdueBehavior.entries.forEach { behavior ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = overdueBehavior == behavior,
                                onClick = { onEvent(TaskEditEvent.UpdateOverdueBehavior(behavior)) }
                            )
                            Text(
                                text = when (behavior) {
                                    OverdueBehavior.POSTPONE -> "Postpone (slide to tomorrow)"
                                    OverdueBehavior.SKIP_TO_NEXT -> "Skip to next occurrence"
                                },
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
            
            // Delete After Completion
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = deleteAfterCompletion,
                    onCheckedChange = { onEvent(TaskEditEvent.UpdateDeleteAfterCompletion(it)) }
                )
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text("Delete after completion")
                    Text(
                        text = "(one-time/ephemeral task)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Relationships Section
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RelationshipsSection(
    parentTaskId: String?,
    parentTaskName: String?,
    childTasks: List<ChildTaskItem>,
    requiresManualCompletion: Boolean,
    triggeredByTasks: List<TaskReference>,
    triggersTasks: List<TaskReference>,
    availableTasks: List<TaskReference>,
    onEvent: (TaskEditEvent) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showParentDropdown by remember { mutableStateOf(false) }
    var showChildDropdown by remember { mutableStateOf(false) }
    var showTriggeredByDropdown by remember { mutableStateOf(false) }
    var showTriggersDropdown by remember { mutableStateOf(false) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Relationships",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
        }
        
        if (expanded) {
            // Parent Task
            Text(
                text = "Parent Task",
                style = MaterialTheme.typography.titleMedium
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExposedDropdownMenuBox(
                    expanded = showParentDropdown,
                    onExpandedChange = { showParentDropdown = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = parentTaskName ?: "No parent",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Select Parent") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showParentDropdown)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showParentDropdown,
                        onDismissRequest = { showParentDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("No parent") },
                            onClick = {
                                onEvent(TaskEditEvent.UpdateParentTask(null))
                                showParentDropdown = false
                            }
                        )
                        availableTasks.forEach { task ->
                            DropdownMenuItem(
                                text = { Text("${task.taskName} (${task.category})") },
                                onClick = {
                                    onEvent(TaskEditEvent.UpdateParentTask(task.taskId))
                                    showParentDropdown = false
                                }
                            )
                        }
                    }
                }
                
                if (parentTaskId != null) {
                    IconButton(onClick = { onEvent(TaskEditEvent.UpdateParentTask(null)) }) {
                        Icon(Icons.Filled.Clear, "Clear parent")
                    }
                }
            }
            
            // Child Tasks
            Text(
                text = "Child Tasks",
                style = MaterialTheme.typography.titleMedium
            )
            
            ExposedDropdownMenuBox(
                expanded = showChildDropdown,
                onExpandedChange = { showChildDropdown = it }
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Add Child Task") },
                    trailingIcon = {
                        Icon(Icons.Filled.Add, "Add")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = showChildDropdown,
                    onDismissRequest = { showChildDropdown = false }
                ) {
                    availableTasks
                        .filter { task -> !childTasks.any { it.taskId == task.taskId } }
                        .forEach { task ->
                            DropdownMenuItem(
                                text = { Text("${task.taskName} (${task.category})") },
                                onClick = {
                                    onEvent(TaskEditEvent.AddChildTask(task.taskId))
                                    showChildDropdown = false
                                }
                            )
                        }
                }
            }
            
            // Child Tasks List
            childTasks.forEach { child ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.DragHandle, "Reorder")
                            Text("${child.order}. ${child.taskName}")
                        }
                        IconButton(onClick = { onEvent(TaskEditEvent.RemoveChildTask(child.taskId)) }) {
                            Icon(Icons.Filled.Delete, "Remove")
                        }
                    }
                }
            }
            
            if (childTasks.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = requiresManualCompletion,
                        onCheckedChange = { onEvent(TaskEditEvent.UpdateRequiresManualCompletion(it)) }
                    )
                    Text(
                        text = "Require manual completion (after all children done)",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            
            // Triggered By Tasks
            Text(
                text = "Triggered By",
                style = MaterialTheme.typography.titleMedium
            )
            
            ExposedDropdownMenuBox(
                expanded = showTriggeredByDropdown,
                onExpandedChange = { showTriggeredByDropdown = it }
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Add Trigger Task") },
                    trailingIcon = {
                        Icon(Icons.Filled.Add, "Add")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = showTriggeredByDropdown,
                    onDismissRequest = { showTriggeredByDropdown = false }
                ) {
                    availableTasks
                        .filter { task -> !triggeredByTasks.any { it.taskId == task.taskId } }
                        .forEach { task ->
                            DropdownMenuItem(
                                text = { Text("${task.taskName} (${task.category})") },
                                onClick = {
                                    onEvent(TaskEditEvent.AddTriggeredByTask(task.taskId))
                                    showTriggeredByDropdown = false
                                }
                            )
                        }
                }
            }
            
            triggeredByTasks.forEach { task ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(task.taskName)
                        IconButton(onClick = { onEvent(TaskEditEvent.RemoveTriggeredByTask(task.taskId)) }) {
                            Icon(Icons.Filled.Delete, "Remove")
                        }
                    }
                }
            }
            
            // Triggers Tasks
            Text(
                text = "Triggers",
                style = MaterialTheme.typography.titleMedium
            )
            
            ExposedDropdownMenuBox(
                expanded = showTriggersDropdown,
                onExpandedChange = { showTriggersDropdown = it }
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Add Triggered Task") },
                    trailingIcon = {
                        Icon(Icons.Filled.Add, "Add")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = showTriggersDropdown,
                    onDismissRequest = { showTriggersDropdown = false }
                ) {
                    availableTasks
                        .filter { task -> !triggersTasks.any { it.taskId == task.taskId } }
                        .forEach { task ->
                            DropdownMenuItem(
                                text = { Text("${task.taskName} (${task.category})") },
                                onClick = {
                                    onEvent(TaskEditEvent.AddTriggersTask(task.taskId))
                                    showTriggersDropdown = false
                                }
                            )
                        }
                }
            }
            
            triggersTasks.forEach { task ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(task.taskName)
                        IconButton(onClick = { onEvent(TaskEditEvent.RemoveTriggersTask(task.taskId)) }) {
                            Icon(Icons.Filled.Delete, "Remove")
                        }
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
    inventoryAssociations: List<InventoryAssociationEdit>,
    availableSupplies: List<SupplyReference>,
    onEvent: (TaskEditEvent) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showSupplyDropdown by remember { mutableStateOf(false) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Inventory Consumption",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
        }
        
        if (expanded) {
            // Add Supply Dropdown
            ExposedDropdownMenuBox(
                expanded = showSupplyDropdown,
                onExpandedChange = { showSupplyDropdown = it }
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Add Supply") },
                    trailingIcon = {
                        Icon(Icons.Filled.Add, "Add")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = showSupplyDropdown,
                    onDismissRequest = { showSupplyDropdown = false }
                ) {
                    availableSupplies
                        .filter { supply -> !inventoryAssociations.any { it.supplyId == supply.supplyId } }
                        .forEach { supply ->
                            DropdownMenuItem(
                                text = { Text("${supply.supplyName} (${supply.unit})") },
                                onClick = {
                                    onEvent(TaskEditEvent.AddInventoryAssociation(supply.supplyId))
                                    showSupplyDropdown = false
                                }
                            )
                        }
                }
            }
            
            // Inventory Associations
            inventoryAssociations.forEach { assoc ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📦 ${assoc.supplyName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { onEvent(TaskEditEvent.RemoveInventoryAssociation(assoc.supplyId)) }) {
                                Icon(Icons.Filled.Delete, "Remove")
                            }
                        }
                        
                        // Consumption Mode
                        Text(
                            text = "Consumption Mode:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ConsumptionMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = assoc.consumptionMode == mode,
                                    onClick = { onEvent(TaskEditEvent.UpdateInventoryConsumptionMode(assoc.supplyId, mode)) },
                                    label = { Text(mode.name) }
                                )
                            }
                        }
                        
                        // Mode-specific fields
                        when (assoc.consumptionMode) {
                            ConsumptionMode.FIXED -> {
                                OutlinedTextField(
                                    value = (assoc.fixedQuantity ?: 1).toString(),
                                    onValueChange = { newValue ->
                                        newValue.toIntOrNull()?.let { qty ->
                                            if (qty > 0) {
                                                onEvent(TaskEditEvent.UpdateInventoryFixedQuantity(assoc.supplyId, qty))
                                            }
                                        }
                                    },
                                    label = { Text("Quantity per completion") },
                                    suffix = { Text(assoc.unit) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            ConsumptionMode.PROMPTED -> {
                                OutlinedTextField(
                                    value = (assoc.promptedDefaultValue ?: 0).toString(),
                                    onValueChange = { newValue ->
                                        newValue.toIntOrNull()?.let { qty ->
                                            onEvent(TaskEditEvent.UpdateInventoryPromptedDefault(assoc.supplyId, qty))
                                        }
                                    },
                                    label = { Text("Default quantity (optional)") },
                                    suffix = { Text(assoc.unit) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            ConsumptionMode.RECOUNT -> {
                                Text(
                                    text = "Manual recount after task completion",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
