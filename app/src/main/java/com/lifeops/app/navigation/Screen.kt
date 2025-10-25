package com.lifeops.app.navigation

/**
 * Sealed class representing all screens in the app for type-safe navigation
 */
sealed class Screen(val route: String) {
    /**
     * Today screen - the home screen showing tasks due today
     */
    data object Today : Screen("today")
    
    /**
     * All Tasks screen - view and manage all tasks
     */
    data object AllTasks : Screen("all_tasks")
    
    /**
     * Inventory screen - manage task inventory
     */
    data object Inventory : Screen("inventory")
    
    /**
     * Settings screen - app settings and preferences
     */
    data object Settings : Screen("settings")
    
    /**
     * Task Create screen - create a new task
     */
    data object TaskCreate : Screen("task_create")
    
    /**
     * Task Detail screen - view/edit a specific task
     * Route includes taskId as a parameter: task_detail/{taskId}
     */
    data object TaskDetail : Screen("task_detail/{taskId}") {
        fun createRoute(taskId: Long): String = "task_detail/$taskId"
        const val ARG_TASK_ID = "taskId"
    }
}
