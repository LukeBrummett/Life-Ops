package com.lifeops.app.presentation.today.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lifeops.app.data.local.entity.Task
import com.lifeops.app.presentation.today.MockData
import com.lifeops.app.presentation.today.TaskItem
import com.lifeops.app.ui.theme.LifeOpsTheme
import java.time.LocalDate

/**
 * Tasks list component
 * 
 * Displays:
 * - LazyColumn of CategoryCards
 * - Filtered based on showCompleted state
 * - Groups tasks by category with parent-child hierarchy
 */
@Composable
fun TasksList(
    tasksByCategory: Map<String, List<TaskItem>>,
    today: LocalDate, // The current "today" date from DateProvider (may be offset for testing)
    showCompleted: Boolean,
    onTaskChecked: (String) -> Unit,
    onTaskClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter task items based on showCompleted setting
    val filteredTasksByCategory = if (showCompleted) {
        tasksByCategory
    } else {
        tasksByCategory.mapValues { (_, taskItems) ->
            taskItems.mapNotNull { taskItem ->
                if (taskItem.isParent) {
                    // For parent tasks, filter children
                    val incompleteChildren = taskItem.children.filter { !isTaskCompleted(it, today) }
                    val parentCompleted = isTaskCompleted(taskItem.task, today)
                    
                    // Show parent if parent incomplete OR has incomplete children
                    if (!parentCompleted || incompleteChildren.isNotEmpty()) {
                        if (incompleteChildren.isNotEmpty()) {
                            // Show parent with only incomplete children
                            taskItem.copy(children = incompleteChildren)
                        } else {
                            // Show parent with all children (for manual completion)
                            taskItem
                        }
                    } else {
                        null
                    }
                } else {
                    // For standalone tasks, only show if incomplete
                    if (!isTaskCompleted(taskItem.task, today)) taskItem else null
                }
            }
        }.filterValues { it.isNotEmpty() }
    }
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        filteredTasksByCategory.forEach { (category, filteredTaskItems) ->
            // Calculate total tasks and completed count (including all children)
            val allTaskItemsInCategory = tasksByCategory[category] ?: emptyList()
            val totalCount = allTaskItemsInCategory.sumOf { 
                if (it.isParent) it.children.size else 1 
            }
            val completedCount = allTaskItemsInCategory.sumOf { taskItem ->
                if (taskItem.isParent) {
                    taskItem.children.count { isTaskCompleted(it, today) }
                } else {
                    if (isTaskCompleted(taskItem.task, today)) 1 else 0
                }
            }
            
            item(key = category) {
                CategoryCard(
                    categoryName = category,
                    taskItems = filteredTaskItems,
                    today = today,
                    totalTasksInCategory = totalCount,
                    completedTasksInCategory = completedCount,
                    onTaskChecked = onTaskChecked,
                    onTaskClick = onTaskClick
                )
            }
        }
    }
}

/**
 * Determine if a task is completed based on lastCompleted date
 * @param task The task to check
 * @param today The current "today" date from DateProvider (may be offset for testing)
 */
fun isTaskCompleted(task: Task, today: LocalDate): Boolean {
    return task.lastCompleted == today
}

// ================================
// Preview Functions
// ================================

@Preview(name = "Tasks List - All Tasks", showBackground = true)
@Composable
private fun PreviewTasksListAll() {
    LifeOpsTheme {
        TasksList(
            tasksByCategory = MockData.tasksByCategory,
            today = MockData.today,
            showCompleted = true,
            onTaskChecked = {},
            onTaskClick = {}
        )
    }
}

@Preview(name = "Tasks List - Hide Completed", showBackground = true)
@Composable
private fun PreviewTasksListHideCompleted() {
    LifeOpsTheme {
        TasksList(
            tasksByCategory = MockData.tasksByCategory,
            today = MockData.today,
            showCompleted = false,
            onTaskChecked = {},
            onTaskClick = {}
        )
    }
}

@Preview(name = "Tasks List - Single Category", showBackground = true)
@Composable
private fun PreviewTasksListSingleCategory() {
    LifeOpsTheme {
        TasksList(
            tasksByCategory = mapOf(
                "Fitness" to (MockData.tasksByCategory["Fitness"] ?: emptyList())
            ),
            today = MockData.today,
            showCompleted = true,
            onTaskChecked = {},
            onTaskClick = {}
        )
    }
}
