package com.lifeops.app.presentation.alltasks

import com.lifeops.app.data.local.entity.Task

/**
 * UI State for the All Tasks Screen
 */
data class AllTasksUiState(
    /**
     * All tasks from the database
     */
    val tasks: List<Task> = emptyList(),
    
    /**
     * Tasks after applying filters, search, and sorting
     */
    val filteredTasks: List<Task> = emptyList(),
    
    /**
     * Current search query text
     */
    val searchQuery: String = "",
    
    /**
     * Current filter state
     */
    val filterState: FilterState = FilterState(),
    
    /**
     * Current group by option
     */
    val groupByOption: GroupByOption = GroupByOption.RELATIVE_DATE,
    
    /**
     * Current sort option
     */
    val sortOption: SortOption = SortOption.BY_DATE,
    
    /**
     * Whether the screen is currently loading data
     */
    val isLoading: Boolean = false,
    
    /**
     * Error message if data loading failed
     */
    val error: String? = null,
    
    /**
     * Whether the search bar is expanded/visible
     */
    val isSearchExpanded: Boolean = false,
    
    /**
     * Whether the filter bar is expanded/visible
     */
    val isFilterExpanded: Boolean = false
)

/**
 * Filter state for the All Tasks screen
 */
data class FilterState(
    /**
     * Status filter - controls which tasks to show by active state
     */
    val statusFilter: StatusFilter = StatusFilter.ACTIVE,
    
    /**
     * Show only tasks that consume inventory
     */
    val hasInventory: Boolean = false,
    
    /**
     * Show only parent tasks (tasks with children)
     */
    val isParent: Boolean = false,
    
    /**
     * Show only child tasks (tasks with a parent)
     */
    val isChild: Boolean = false,
    
    /**
     * Show only triggered tasks (spawned by other tasks)
     */
    val isTriggered: Boolean = false,
    
    /**
     * Show only ADHOC tasks (no automatic schedule)
     */
    val adhocOnly: Boolean = false
)

/**
 * Status filter options
 */
enum class StatusFilter {
    ACTIVE,   // Show only active tasks (default)
    ALL,      // Show all tasks (active + archived)
    ARCHIVED  // Show only archived tasks
}

/**
 * Group by options for the task list
 */
enum class GroupByOption {
    RELATIVE_DATE,  // Group by relative date (Today, Tomorrow, etc.) - default
    CATEGORY        // Group by category
}

/**
 * Sort options for the task list
 */
enum class SortOption {
    BY_DATE,      // Sort by nextDue date (default)
    BY_CATEGORY,  // Sort by category
    BY_NAME       // Sort alphabetically by name
}

