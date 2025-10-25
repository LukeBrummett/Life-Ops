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

/**
 * All Tasks Screen - Main entry point
 * 
 * Displays all tasks in the system with search, filter, and sort capabilities.
 * Allows navigation to task details and task creation.
 */
@Composable
fun AllTasksScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTaskDetail: (Long) -> Unit,
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
            // Search bar (when expanded)
            if (uiState.isSearchExpanded) {
                SearchBar(
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = { query ->
                        onEvent(AllTasksUiEvent.SearchQueryChanged(query))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // Filter and sort bar (when expanded)
            if (uiState.isFilterExpanded) {
                FilterSortBar(
                    filterState = uiState.filterState,
                    groupByOption = uiState.groupByOption,
                    sortOption = uiState.sortOption,
                    onFilterChange = { filter ->
                        onEvent(AllTasksUiEvent.FilterChanged(filter))
                    },
                    onGroupByChange = { groupBy ->
                        onEvent(AllTasksUiEvent.GroupByChanged(groupBy))
                    },
                    onSortChange = { sort ->
                        onEvent(AllTasksUiEvent.SortChanged(sort))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
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
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = { Text("Search tasks...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search")
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search"
                    )
                }
            }
        },
        singleLine = true,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSortBar(
    filterState: FilterState,
    groupByOption: GroupByOption,
    sortOption: SortOption,
    onFilterChange: (FilterState) -> Unit,
    onGroupByChange: (GroupByOption) -> Unit,
    onSortChange: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Status filter chips
            Text(
                text = "Status",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = filterState.statusFilter == StatusFilter.ACTIVE,
                    onClick = { 
                        onFilterChange(filterState.copy(statusFilter = StatusFilter.ACTIVE))
                    },
                    label = { 
                        Text(
                            "Active",
                            style = MaterialTheme.typography.labelSmall
                        ) 
                    },
                    modifier = Modifier.height(28.dp)
                )
                
                FilterChip(
                    selected = filterState.statusFilter == StatusFilter.ALL,
                    onClick = { 
                        onFilterChange(filterState.copy(statusFilter = StatusFilter.ALL))
                    },
                    label = { 
                        Text(
                            "All",
                            style = MaterialTheme.typography.labelSmall
                        ) 
                    },
                    modifier = Modifier.height(28.dp)
                )
                
                FilterChip(
                    selected = filterState.statusFilter == StatusFilter.ARCHIVED,
                    onClick = { 
                        onFilterChange(filterState.copy(statusFilter = StatusFilter.ARCHIVED))
                    },
                    label = { 
                        Text(
                            "Archived",
                            style = MaterialTheme.typography.labelSmall
                        ) 
                    },
                    modifier = Modifier.height(28.dp)
                )
            }
            
            // Attribute filter chips
            Text(
                text = "Attributes",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = filterState.hasInventory,
                    onClick = { 
                        onFilterChange(filterState.copy(hasInventory = !filterState.hasInventory))
                    },
                    label = { 
                        Text(
                            "Inventory",
                            style = MaterialTheme.typography.labelSmall
                        ) 
                    },
                    modifier = Modifier.height(28.dp)
                )
                
                FilterChip(
                    selected = filterState.isParent,
                    onClick = { 
                        onFilterChange(filterState.copy(isParent = !filterState.isParent))
                    },
                    label = { 
                        Text(
                            "Parent",
                            style = MaterialTheme.typography.labelSmall
                        ) 
                    },
                    modifier = Modifier.height(28.dp)
                )
                
                FilterChip(
                    selected = filterState.isChild,
                    onClick = { 
                        onFilterChange(filterState.copy(isChild = !filterState.isChild))
                    },
                    label = { 
                        Text(
                            "Child",
                            style = MaterialTheme.typography.labelSmall
                        ) 
                    },
                    modifier = Modifier.height(28.dp)
                )
                
                FilterChip(
                    selected = filterState.isTriggered,
                    onClick = { 
                        onFilterChange(filterState.copy(isTriggered = !filterState.isTriggered))
                    },
                    label = { 
                        Text(
                            "Triggered",
                            style = MaterialTheme.typography.labelSmall
                        ) 
                    },
                    modifier = Modifier.height(28.dp)
                )
                
                FilterChip(
                    selected = filterState.adhocOnly,
                    onClick = { 
                        onFilterChange(filterState.copy(adhocOnly = !filterState.adhocOnly))
                    },
                    label = { 
                        Text(
                            "Adhoc",
                            style = MaterialTheme.typography.labelSmall
                        ) 
                    },
                    modifier = Modifier.height(28.dp)
                )
            }
            
            // Group By and Sort By dropdowns
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Group By dropdown (left)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Group By",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    GroupByDropdown(
                        selectedGroupBy = groupByOption,
                        onGroupByChange = onGroupByChange
                    )
                }
                
                // Sort By dropdown (right)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sort By",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    SortDropdown(
                        selectedSort = sortOption,
                        onSortChange = onSortChange
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupByDropdown(
    selectedGroupBy: GroupByOption,
    onGroupByChange: (GroupByOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (selectedGroupBy) {
                        GroupByOption.RELATIVE_DATE -> "Date"
                        GroupByOption.CATEGORY -> "Category"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Group by options"
                )
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Date") },
                onClick = {
                    onGroupByChange(GroupByOption.RELATIVE_DATE)
                    expanded = false
                },
                leadingIcon = if (selectedGroupBy == GroupByOption.RELATIVE_DATE) {
                    { Icon(Icons.Default.Search, contentDescription = null) } // TODO: Use check icon
                } else null
            )
            DropdownMenuItem(
                text = { Text("Category") },
                onClick = {
                    onGroupByChange(GroupByOption.CATEGORY)
                    expanded = false
                },
                leadingIcon = if (selectedGroupBy == GroupByOption.CATEGORY) {
                    { Icon(Icons.Default.Search, contentDescription = null) } // TODO: Use check icon
                } else null
            )
        }
    }
}

@Composable
private fun SortDropdown(
    selectedSort: SortOption,
    onSortChange: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (selectedSort) {
                        SortOption.BY_DATE -> "Date"
                        SortOption.BY_CATEGORY -> "Category"
                        SortOption.BY_NAME -> "Name"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Sort options"
                )
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Date") },
                onClick = {
                    onSortChange(SortOption.BY_DATE)
                    expanded = false
                },
                leadingIcon = if (selectedSort == SortOption.BY_DATE) {
                    { Icon(Icons.Default.Search, contentDescription = null) } // TODO: Use check icon
                } else null
            )
            DropdownMenuItem(
                text = { Text("Category") },
                onClick = {
                    onSortChange(SortOption.BY_CATEGORY)
                    expanded = false
                },
                leadingIcon = if (selectedSort == SortOption.BY_CATEGORY) {
                    { Icon(Icons.Default.Search, contentDescription = null) } // TODO: Use check icon
                } else null
            )
            DropdownMenuItem(
                text = { Text("Name") },
                onClick = {
                    onSortChange(SortOption.BY_NAME)
                    expanded = false
                },
                leadingIcon = if (selectedSort == SortOption.BY_NAME) {
                    { Icon(Icons.Default.Search, contentDescription = null) } // TODO: Use check icon
                } else null
            )
        }
    }
}

@Composable
private fun TaskList(
    tasks: List<com.lifeops.app.data.local.entity.Task>,
    groupByOption: com.lifeops.app.presentation.alltasks.GroupByOption,
    onTaskClick: (Long) -> Unit,
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
                val groupedTasks = com.lifeops.app.presentation.alltasks.groupTasksByDate(tasks)
                
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
