package com.lifeops.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.lifeops.app.presentation.alltasks.AllTasksScreen
import com.lifeops.app.presentation.taskedit.TaskEditScreen
import com.lifeops.presentation.inventory.InventoryScreen
import com.lifeops.presentation.inventory.restock.RestockScreen
import com.lifeops.presentation.settings.SettingsScreen
import com.lifeops.presentation.supplyedit.SupplyEditScreen
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
                    navController.navigate(Screen.TaskEdit.createRoute())
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
                    navController.navigate(Screen.TaskEdit.createRoute())
                }
            )
        }
        
        // Inventory Screen
        composable(Screen.Inventory.route) {
            InventoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSupplyEdit = { supplyId ->
                    navController.navigate(Screen.SupplyEdit.createRoute(supplyId))
                },
                onNavigateToRestock = { supplyIds ->
                    navController.navigate(Screen.Restock.createRoute(supplyIds))
                }
            )
        }
        
        // Supply Edit Screen
        composable(
            route = Screen.SupplyEdit.route,
            arguments = listOf(
                navArgument(Screen.SupplyEdit.ARG_SUPPLY_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            SupplyEditScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Restock Screen
        composable(
            route = Screen.Restock.route,
            arguments = listOf(
                navArgument(Screen.Restock.ARG_SUPPLY_IDS) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val supplyIdsString = backStackEntry.arguments?.getString(Screen.Restock.ARG_SUPPLY_IDS) ?: ""
            val supplyIds = supplyIdsString.split(",").filter { it.isNotBlank() }
            
            // Get the inventory screen's back stack entry to access its ViewModel
            val inventoryEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Inventory.route)
            }
            val inventoryViewModel: com.lifeops.presentation.inventory.InventoryViewModel = androidx.hilt.navigation.compose.hiltViewModel(inventoryEntry)
            
            RestockScreen(
                supplyIds = supplyIds,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSupplyEdit = { supplyId ->
                    navController.navigate(Screen.SupplyEdit.createRoute(supplyId))
                },
                onRestockCompleted = {
                    inventoryViewModel.clearPendingRestock()
                }
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
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { editTaskId ->
                    navController.navigate(Screen.TaskEdit.createRoute(editTaskId))
                },
                onNavigateToTask = { relatedTaskId ->
                    // Navigate to another task's detail screen
                    navController.navigate(Screen.TaskDetail.createRoute(relatedTaskId))
                },
                onNavigateToInventory = { supplyId ->
                    // Navigate to inventory (supply edit or inventory screen)
                    navController.navigate(Screen.SupplyEdit.createRoute(supplyId))
                }
            )
        }
        
        // Task Edit Screen (with optional taskId argument)
        composable(
            route = Screen.TaskEdit.route,
            arguments = listOf(
                navArgument(Screen.TaskEdit.ARG_TASK_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            TaskEditScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTaskDetail = { taskId ->
                    // Need to pop both TaskEdit AND the old TaskDetail
                    // Pop twice: once for TaskEdit, once for old TaskDetail
                    navController.popBackStack()
                    navController.popBackStack()
                    // Now navigate fresh to TaskDetail with updated data
                    navController.navigate(Screen.TaskDetail.createRoute(taskId))
                }
            )
        }
    }
}
