package com.lifeops.app.presentation.alltasks

/**
 * UI Events for the All Tasks Screen
 */
sealed interface AllTasksUiEvent {
    /**
     * User changed the search query text
     */
    data class SearchQueryChanged(val query: String) : AllTasksUiEvent
    
    /**
     * User toggled the search bar visibility
     */
    data object ToggleSearch : AllTasksUiEvent
    
    /**
     * User toggled the filter bar visibility
     */
    data object ToggleFilter : AllTasksUiEvent
    
    /**
     * User changed filter settings
     */
    data class FilterChanged(val filter: FilterState) : AllTasksUiEvent
    
    /**
     * User changed group by option
     */
    data class GroupByChanged(val groupBy: GroupByOption) : AllTasksUiEvent
    
    /**
     * User changed sort option
     */
    data class SortChanged(val sort: SortOption) : AllTasksUiEvent
    
    /**
     * User tapped on a task to view details
     */
    data class NavigateToTaskDetail(val taskId: Long) : AllTasksUiEvent
    
    /**
     * User tapped FAB to create new task
     */
    data object NavigateToTaskCreate : AllTasksUiEvent
    
    /**
     * User requested to refresh the task list
     */
    data object Refresh : AllTasksUiEvent
    
    /**
     * User navigated back
     */
    data object NavigateBack : AllTasksUiEvent
}
