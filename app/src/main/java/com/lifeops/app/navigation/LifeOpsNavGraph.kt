package com.lifeops.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.lifeops.app.presentation.alltasks.AllTasksScreen
import com.lifeops.app.presentation.inventory.InventoryScreen
import com.lifeops.presentation.settings.SettingsScreen
import com.lifeops.app.presentation.taskcreate.TaskCreateScreen
import com.lifeops.app.presentation.taskdetail.TaskDetailScreen
import com.lifeops.app.presentation.today.TodayScreen

/**
 * Main navigation graph for the Life-Ops app
 * 
 * Sets up all navigation routes and their corresponding screens
 */
@Composable
fun LifeOpsNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Today.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Today Screen (Home)
        composable(Screen.Today.route) {
            TodayScreen(
                onNavigateToAllTasks = { navController.navigate(Screen.AllTasks.route) },
                onNavigateToInventory = { navController.navigate(Screen.Inventory.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToTaskDetail = { taskId -> 
                    navController.navigate(Screen.TaskDetail.createRoute(taskId))
                },
                onNavigateToTaskCreate = {
                    navController.navigate(Screen.TaskCreate.route)
                }
            )
        }
        
        // All Tasks Screen
        composable(Screen.AllTasks.route) {
            AllTasksScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTaskDetail = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId))
                },
                onNavigateToTaskCreate = {
                    navController.navigate(Screen.TaskCreate.route)
                }
            )
        }
        
        // Inventory Screen
        composable(Screen.Inventory.route) {
            InventoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Task Create Screen
        composable(Screen.TaskCreate.route) {
            TaskCreateScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Task Detail Screen (with taskId argument)
        composable(
            route = Screen.TaskDetail.route,
            arguments = listOf(
                navArgument(Screen.TaskDetail.ARG_TASK_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString(Screen.TaskDetail.ARG_TASK_ID) ?: ""
            TaskDetailScreen(
                taskId = taskId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
