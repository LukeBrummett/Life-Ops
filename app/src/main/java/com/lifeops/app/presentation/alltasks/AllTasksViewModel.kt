package com.lifeops.app.presentation.alltasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeops.app.data.local.entity.Task
import com.lifeops.app.domain.usecase.GetAllTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel for the All Tasks Screen
 * 
 * Manages UI state and handles user events for the All Tasks screen.
 * Fetches all tasks from the database and applies filtering, searching, and sorting.
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class AllTasksViewModel @Inject constructor(
    private val getAllTasksUseCase: GetAllTasksUseCase,
    private val dateProvider: com.lifeops.app.util.DateProvider
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AllTasksUiState())
    val uiState: StateFlow<AllTasksUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    
    init {
        loadAllTasks()
        observeSearchQuery()
    }
    
    /**
     * Handle user events from the UI
     */
    fun onEvent(event: AllTasksUiEvent) {
        when (event) {
            is AllTasksUiEvent.SearchQueryChanged -> {
                _searchQuery.value = event.query
                _uiState.update { it.copy(searchQuery = event.query) }
            }
            is AllTasksUiEvent.ToggleSearch -> toggleSearch()
            is AllTasksUiEvent.ToggleFilter -> toggleFilter()
            is AllTasksUiEvent.FilterChanged -> updateFilter(event.filter)
            is AllTasksUiEvent.GroupByChanged -> updateGroupBy(event.groupBy)
            is AllTasksUiEvent.SortChanged -> updateSort(event.sort)
            is AllTasksUiEvent.NavigateToTaskDetail -> {
                // Navigation handled by screen composable
            }
            is AllTasksUiEvent.NavigateToTaskCreate -> {
                // Navigation handled by screen composable
            }
            is AllTasksUiEvent.Refresh -> loadAllTasks()
            is AllTasksUiEvent.NavigateBack -> {
                // Navigation handled by screen composable
            }
        }
    }
    
    /**
     * Load all tasks from the database
     * Observes changes and updates UI state reactively
     */
    private fun loadAllTasks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            getAllTasksUseCase()
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load tasks"
                        )
                    }
                }
                .collect { tasks ->
                    _uiState.update {
                        it.copy(
                            tasks = tasks,
                            filteredTasks = applyFiltersAndSort(tasks, it.searchQuery, it.filterState, it.groupByOption, it.sortOption),
                            currentDate = dateProvider.now(),
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }
    
    /**
     * Observe search query with debounce to avoid excessive filtering
     */
    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300) // Wait 300ms after user stops typing
                .collect { query ->
                    _uiState.update {
                        it.copy(
                            filteredTasks = applyFiltersAndSort(it.tasks, query, it.filterState, it.groupByOption, it.sortOption)
                        )
                    }
                }
        }
    }
    
    /**
     * Toggle search bar visibility
     */
    private fun toggleSearch() {
        _uiState.update {
            val newExpanded = !it.isSearchExpanded
            it.copy(
                isSearchExpanded = newExpanded,
                searchQuery = if (!newExpanded) "" else it.searchQuery // Clear search when hiding
            )
        }
        if (!_uiState.value.isSearchExpanded) {
            _searchQuery.value = ""
        }
    }
    
    /**
     * Toggle filter bar visibility
     */
    private fun toggleFilter() {
        _uiState.update {
            it.copy(isFilterExpanded = !it.isFilterExpanded)
        }
    }
    
    /**
     * Update filter state and re-filter tasks
     */
    private fun updateFilter(filter: FilterState) {
        _uiState.update {
            it.copy(
                filterState = filter,
                filteredTasks = applyFiltersAndSort(it.tasks, it.searchQuery, filter, it.groupByOption, it.sortOption)
            )
        }
    }
    
    /**
     * Update sort option and re-sort tasks
     */
    private fun updateSort(sort: SortOption) {
        _uiState.update {
            it.copy(
                sortOption = sort,
                filteredTasks = applyFiltersAndSort(it.tasks, it.searchQuery, it.filterState, it.groupByOption, sort)
            )
        }
    }
    
    /**
     * Update group by option and re-group tasks
     */
    private fun updateGroupBy(groupBy: GroupByOption) {
        _uiState.update {
            it.copy(
                groupByOption = groupBy,
                filteredTasks = applyFiltersAndSort(it.tasks, it.searchQuery, it.filterState, groupBy, it.sortOption)
            )
        }
    }
    
    /**
     * Apply filters, search, and sorting to task list
     */
    private fun applyFiltersAndSort(
        tasks: List<Task>,
        query: String,
        filter: FilterState,
        groupBy: GroupByOption,
        sort: SortOption
    ): List<Task> {
        val filtered = tasks
            .filter { matchesStatusFilter(it, filter.statusFilter) }
            .filter { matchesAttributeFilters(it, filter, tasks) }
            .filter { matchesSearch(it, query) }
        
        // If child filter is active, also include the parent tasks
        val withParents = if (filter.isChild) {
            val childTasks = filtered.filter { !it.parentTaskIds.isNullOrEmpty() }
            val parentIds = childTasks.flatMap { it.parentTaskIds ?: emptyList() }.toSet()
            val parentTasks = tasks.filter { it.id in parentIds && it !in filtered }
            (filtered + parentTasks).distinct()
        } else {
            filtered
        }
        
        return sortTasks(withParents, sort)
    }
    
    /**
     * Check if task matches status filter
     */
    private fun matchesStatusFilter(task: Task, statusFilter: StatusFilter): Boolean {
        return when (statusFilter) {
            StatusFilter.ACTIVE -> task.active
            StatusFilter.ALL -> true
            StatusFilter.ARCHIVED -> !task.active
        }
    }
    
    /**
     * Check if task matches attribute filters
     */
    private fun matchesAttributeFilters(task: Task, filter: FilterState, allTasks: List<Task>): Boolean {
        if (filter.hasInventory && !task.requiresInventory) return false
        
        // Check if task is a parent (has children)
        if (filter.isParent) {
            val hasChildren = allTasks.any { potentialChild ->
                !potentialChild.parentTaskIds.isNullOrEmpty() && task.id in potentialChild.parentTaskIds
            }
            if (!hasChildren) return false
        }
        
        // Check if task is a child (has a parent)
        if (filter.isChild && task.parentTaskIds.isNullOrEmpty()) return false
        
        if (filter.isTriggered && task.triggeredByTaskIds.isNullOrEmpty()) return false
        if (filter.adhocOnly && task.intervalUnit != com.lifeops.app.data.local.entity.IntervalUnit.ADHOC) return false
        return true
    }
    
    /**
     * Check if task matches search query
     */
    private fun matchesSearch(task: Task, query: String): Boolean {
        if (query.isBlank()) return true
        
        val normalizedQuery = query.lowercase().trim()
        return task.name.lowercase().contains(normalizedQuery) ||
                task.category.lowercase().contains(normalizedQuery) ||
                task.tags.lowercase().contains(normalizedQuery) ||
                task.description?.lowercase()?.contains(normalizedQuery) == true
    }
    
    /**
     * Sort tasks based on selected option
     */
    private fun sortTasks(tasks: List<Task>, sort: SortOption): List<Task> {
        return when (sort) {
            SortOption.BY_DATE -> tasks.sortedWith(
                compareBy<Task> { it.nextDue == null } // Nulls last
                    .thenBy { it.nextDue }
                    .thenBy { it.name }
            )
            SortOption.BY_CATEGORY -> tasks.sortedWith(
                compareBy<Task> { it.category }
                    .thenBy { it.nextDue == null }
                    .thenBy { it.nextDue }
            )
            SortOption.BY_NAME -> tasks.sortedBy { it.name }
        }
    }
}

/**
 * Date section for grouping tasks
 */
enum class DateSection {
    OVERDUE,
    TODAY,
    TOMORROW,
    THIS_WEEK,
    NEXT_WEEK,
    LATER,
    NO_DATE
}

/**
 * Get the section label for display
 */
fun DateSection.label(): String {
    return when (this) {
        DateSection.OVERDUE -> "Overdue"
        DateSection.TODAY -> "Today"
        DateSection.TOMORROW -> "Tomorrow"
        DateSection.THIS_WEEK -> "This Week"
        DateSection.NEXT_WEEK -> "Next Week"
        DateSection.LATER -> "Later"
        DateSection.NO_DATE -> "No Schedule"
    }
}

/**
 * Determine which section a task belongs to based on its next due date
 */
fun getDateSection(task: Task, today: LocalDate): DateSection {
    val nextDue = task.nextDue ?: return DateSection.NO_DATE
    
    val daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, nextDue)
    
    return when {
        daysUntil < 0 -> DateSection.OVERDUE
        daysUntil == 0L -> DateSection.TODAY
        daysUntil == 1L -> DateSection.TOMORROW
        daysUntil in 2..6 -> DateSection.THIS_WEEK
        daysUntil in 7..13 -> DateSection.NEXT_WEEK
        else -> DateSection.LATER
    }
}

/**
 * Data class to represent a task item with optional children
 */
data class TaskItem(
    val task: Task,
    val children: List<Task> = emptyList(),
    val isParent: Boolean = false
)

/**
 * Group tasks by date section with parent-child hierarchy
 */
fun groupTasksByDate(tasks: List<Task>, today: LocalDate): Map<DateSection, List<TaskItem>> {
    // First, organize tasks with hierarchy
    val taskItems = groupTasksWithHierarchy(tasks)
    
    // Then group by date section
    return taskItems.groupBy { getDateSection(it.task, today) }
        .toSortedMap(compareBy { 
            // Order: OVERDUE, TODAY, TOMORROW, THIS_WEEK, NEXT_WEEK, LATER, NO_DATE
            when (it) {
                DateSection.OVERDUE -> 0
                DateSection.TODAY -> 1
                DateSection.TOMORROW -> 2
                DateSection.THIS_WEEK -> 3
                DateSection.NEXT_WEEK -> 4
                DateSection.LATER -> 5
                DateSection.NO_DATE -> 6
            }
        })
}

/**
 * Group tasks by category with parent-child hierarchy
 */
fun groupTasksByCategory(tasks: List<Task>): Map<String, List<TaskItem>> {
    // First, organize tasks with hierarchy
    val taskItems = groupTasksWithHierarchy(tasks)
    
    // Then group by category (sorted alphabetically, with empty/null categories last)
    return taskItems.groupBy { it.task.category ?: "" }
        .toSortedMap(compareBy<String> { it.isEmpty() }.thenBy { it })
}

/**
 * Group tasks with parent-child hierarchy
 * - Parents appear with their children nested
 * - Children are shown under their parents
 * - Standalone tasks appear normally
 */
private fun groupTasksWithHierarchy(tasks: List<Task>): List<TaskItem> {
    // Track which tasks are children (so we don't show them as standalone)
    val childTaskIds = mutableSetOf<String>()
    
    // Find all child tasks
    tasks.forEach { task ->
        if (!task.parentTaskIds.isNullOrEmpty()) {
            childTaskIds.add(task.id)
        }
    }
    
    // Build task items with hierarchy
    return tasks.mapNotNull { task ->
        // Skip tasks that are children (they'll be included under their parent)
        if (task.id in childTaskIds) {
            return@mapNotNull null
        }
        
        // Find children for this task
        val children = tasks.filter { potentialChild ->
            !potentialChild.parentTaskIds.isNullOrEmpty() && 
            task.id in potentialChild.parentTaskIds
        }.sortedBy { it.childOrder ?: 0 }
        
        TaskItem(
            task = task,
            children = children,
            isParent = children.isNotEmpty()
        )
    }
}
