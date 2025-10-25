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
import com.lifeops.app.ui.theme.LifeOpsTheme
import java.time.LocalDate

/**
 * Tasks list component
 * 
 * Displays:
 * - LazyColumn of CategoryCards
 * - Filtered based on showCompleted state
 * - Groups tasks by category
 */
@Composable
fun TasksList(
    tasksByCategory: Map<String, List<Task>>,
    showCompleted: Boolean,
    onTaskChecked: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter tasks based on showCompleted setting
    val filteredTasksByCategory = if (showCompleted) {
        tasksByCategory
    } else {
        tasksByCategory.mapValues { (_, tasks) ->
            tasks.filter { !isTaskCompleted(it) }
        }.filterValues { it.isNotEmpty() }
    }
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        filteredTasksByCategory.forEach { (category, filteredTasks) ->
            // Get the original (unfiltered) task list for this category to calculate progress
            val allTasksInCategory = tasksByCategory[category] ?: emptyList()
            val completedCount = allTasksInCategory.count { isTaskCompleted(it) }
            val totalCount = allTasksInCategory.size
            
            item(key = category) {
                CategoryCard(
                    categoryName = category,
                    tasks = filteredTasks,
                    totalTasksInCategory = totalCount,
                    completedTasksInCategory = completedCount,
                    onTaskChecked = onTaskChecked
                )
            }
        }
    }
}

/**
 * Determine if a task is completed based on lastCompleted date
 */
private fun isTaskCompleted(task: Task): Boolean {
    val today = LocalDate.now()
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
            showCompleted = true,
            onTaskChecked = {}
        )
    }
}

@Preview(name = "Tasks List - Hide Completed", showBackground = true)
@Composable
private fun PreviewTasksListHideCompleted() {
    LifeOpsTheme {
        TasksList(
            tasksByCategory = MockData.tasksByCategory,
            showCompleted = false,
            onTaskChecked = {}
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
            showCompleted = true,
            onTaskChecked = {}
        )
    }
}
