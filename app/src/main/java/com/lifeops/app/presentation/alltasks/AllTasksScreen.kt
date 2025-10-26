package com.lifeops.app.presentation.alltasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeops.app.presentation.alltasks.components.SearchBar
import com.lifeops.app.presentation.alltasks.components.SortButton
import com.lifeops.app.presentation.alltasks.components.FilterButton
import com.lifeops.app.presentation.alltasks.components.GroupButton

/**
 * All Tasks Screen - Main entry point
 * 
 * Displays all tasks in the system with search, filter, and sort capabilities.
 * Allows navigation to task details and task creation.
 */
@Composable
fun AllTasksScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTaskDetail: (String) -> Unit,
    onNavigateToTaskCreate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AllTasksViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    AllTasksScreenContent(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                is AllTasksUiEvent.NavigateBack -> onNavigateBack()
                is AllTasksUiEvent.NavigateToTaskDetail -> onNavigateToTaskDetail(event.taskId)
                is AllTasksUiEvent.NavigateToTaskCreate -> onNavigateToTaskCreate()
                else -> viewModel.onEvent(event)
            }
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllTasksScreenContent(
    uiState: AllTasksUiState,
    onEvent: (AllTasksUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            AllTasksTopBar(
                isSearchExpanded = uiState.isSearchExpanded,
                isFilterExpanded = uiState.isFilterExpanded,
                onNavigateBack = { onEvent(AllTasksUiEvent.NavigateBack) },
                onToggleSearch = { onEvent(AllTasksUiEvent.ToggleSearch) },
                onToggleFilter = { onEvent(AllTasksUiEvent.ToggleFilter) }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onEvent(AllTasksUiEvent.NavigateToTaskCreate) },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add task") },
                text = { Text("New Task") }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search and filter bar (always visible when not collapsed)
            if (uiState.isSearchExpanded || uiState.isFilterExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Search bar
                    if (uiState.isSearchExpanded) {
                        SearchBar(
                            query = uiState.searchQuery,
                            onQueryChanged = { query ->
                                onEvent(AllTasksUiEvent.SearchQueryChanged(query))
                            }
                        )
                    }
                    
                    // Group / Sort / Filter buttons
                    if (uiState.isFilterExpanded) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GroupButton(
                                currentGroupBy = uiState.groupByOption,
                                onGroupByChange = { groupBy ->
                                    onEvent(AllTasksUiEvent.GroupByChanged(groupBy))
                                },
                                modifier = Modifier.weight(1f)
                            )
                            
                            SortButton(
                                currentSortOption = uiState.sortOption,
                                onSortOptionSelected = { sort ->
                                    onEvent(AllTasksUiEvent.SortChanged(sort))
                                },
                                modifier = Modifier.weight(1f)
                            )
                            
                            FilterButton(
                                filterState = uiState.filterState,
                                onFilterChange = { filter ->
                                    onEvent(AllTasksUiEvent.FilterChanged(filter))
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            // Task list
            when {
                uiState.isLoading -> {
                    LoadingState(modifier = Modifier.fillMaxSize())
                }
                uiState.error != null -> {
                    ErrorState(
                        message = uiState.error,
                        onRetry = { onEvent(AllTasksUiEvent.Refresh) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                uiState.filteredTasks.isEmpty() -> {
                    EmptyState(
                        hasSearchQuery = uiState.searchQuery.isNotEmpty(),
                        hasActiveFilters = hasActiveFilters(uiState.filterState),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    TaskList(
                        tasks = uiState.filteredTasks,
                        currentDate = uiState.currentDate,
                        groupByOption = uiState.groupByOption,
                        onTaskClick = { taskId ->
                            onEvent(AllTasksUiEvent.NavigateToTaskDetail(taskId))
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/**
 * Check if any filters are active (beyond default ACTIVE status)
 */
private fun hasActiveFilters(filter: FilterState): Boolean {
    return filter.statusFilter != StatusFilter.ACTIVE ||
            filter.hasInventory ||
            filter.isParent ||
            filter.isChild ||
            filter.isTriggered ||
            filter.adhocOnly
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllTasksTopBar(
    isSearchExpanded: Boolean,
    isFilterExpanded: Boolean,
    onNavigateBack: () -> Unit,
    onToggleSearch: () -> Unit,
    onToggleFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = { Text("All Tasks") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Navigate back"
                )
            }
        },
        actions = {
            IconButton(onClick = onToggleFilter) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = if (isFilterExpanded) "Hide filters" else "Show filters"
                )
            }
            IconButton(onClick = onToggleSearch) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = if (isSearchExpanded) "Hide search" else "Show search"
                )
            }
        },
        modifier = modifier
    )
}

@Composable
private fun TaskList(
    tasks: List<com.lifeops.app.data.local.entity.Task>,
    currentDate: java.time.LocalDate,
    groupByOption: com.lifeops.app.presentation.alltasks.GroupByOption,
    onTaskClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        when (groupByOption) {
            com.lifeops.app.presentation.alltasks.GroupByOption.RELATIVE_DATE -> {
                // Group tasks by date section with parent-child hierarchy
                val groupedTasks = com.lifeops.app.presentation.alltasks.groupTasksByDate(tasks, currentDate)
                
                groupedTasks.forEach { (section, sectionTaskItems) ->
                    // Section header
                    item(key = "header_$section") {
                        DateSectionHeader(
                            section = section,
                            taskCount = sectionTaskItems.size
                        )
                    }
                    
                    // Tasks in this section
                    items(
                        items = sectionTaskItems,
                        key = { taskItem -> taskItem.task.id }
                    ) { taskItem ->
                        if (taskItem.isParent) {
                            // Display parent with children
                            com.lifeops.app.presentation.alltasks.components.ParentAllTasksItem(
                                parentTask = taskItem.task,
                                childTasks = taskItem.children,
                                onTaskClick = onTaskClick
                            )
                        } else {
                            // Display standalone task
                            com.lifeops.app.presentation.alltasks.components.AllTasksItem(
                                task = taskItem.task,
                                onTaskClick = { onTaskClick(taskItem.task.id) }
                            )
                        }
                    }
                }
            }
            com.lifeops.app.presentation.alltasks.GroupByOption.CATEGORY -> {
                // Group tasks by category with parent-child hierarchy
                val groupedTasks = com.lifeops.app.presentation.alltasks.groupTasksByCategory(tasks)
                
                groupedTasks.forEach { (category, sectionTaskItems) ->
                    // Section header
                    item(key = "header_$category") {
                        CategorySectionHeader(
                            category = category,
                            taskCount = sectionTaskItems.size
                        )
                    }
                    
                    // Tasks in this section
                    items(
                        items = sectionTaskItems,
                        key = { taskItem -> taskItem.task.id }
                    ) { taskItem ->
                        if (taskItem.isParent) {
                            // Display parent with children
                            com.lifeops.app.presentation.alltasks.components.ParentAllTasksItem(
                                parentTask = taskItem.task,
                                childTasks = taskItem.children,
                                onTaskClick = onTaskClick
                            )
                        } else {
                            // Display standalone task
                            com.lifeops.app.presentation.alltasks.components.AllTasksItem(
                                task = taskItem.task,
                                onTaskClick = { onTaskClick(taskItem.task.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateSectionHeader(
    section: com.lifeops.app.presentation.alltasks.DateSection,
    taskCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = section.label(),
            style = MaterialTheme.typography.titleMedium,
            color = getSectionHeaderColor(section)
        )
        
        Text(
            text = "$taskCount",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CategorySectionHeader(
    category: String,
    taskCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (category.isEmpty()) "No Category" else category,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = "$taskCount",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun getSectionHeaderColor(section: com.lifeops.app.presentation.alltasks.DateSection): androidx.compose.ui.graphics.Color {
    return when (section) {
        com.lifeops.app.presentation.alltasks.DateSection.OVERDUE -> MaterialTheme.colorScheme.error
        com.lifeops.app.presentation.alltasks.DateSection.TODAY -> MaterialTheme.colorScheme.primary
        com.lifeops.app.presentation.alltasks.DateSection.TOMORROW -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurface
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚠️",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Error loading tasks",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun EmptyState(
    hasSearchQuery: Boolean,
    hasActiveFilters: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val (emoji, title, message) = when {
            hasSearchQuery -> Triple("🔍", "No Results", "Try different search terms")
            hasActiveFilters -> Triple("🔎", "No Tasks Match Filter", "Adjust your filters to see more tasks")
            else -> Triple("📋", "No Tasks Yet", "Tap the + button to create your first task")
        }
        
        Text(
            text = emoji,
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
