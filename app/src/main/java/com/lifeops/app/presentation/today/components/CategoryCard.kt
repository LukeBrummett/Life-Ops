package com.lifeops.app.presentation.today.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
 * Category card component
 * 
 * Displays:
 * - Category name
 * - Progress indicator (X/Y tasks complete)
 * - List of tasks in this category (with parent-child hierarchy)
 */
@Composable
fun CategoryCard(
    categoryName: String,
    taskItems: List<TaskItem>,
    totalTasksInCategory: Int,
    completedTasksInCategory: Int,
    onTaskChecked: (Long) -> Unit,
    onTaskClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val completedCount = completedTasksInCategory
    val totalCount = totalTasksInCategory
    
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Category header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Category name
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Progress indicator
                Text(
                    text = "$completedCount/$totalCount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (completedCount == totalCount && totalCount > 0) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    }
                )
            }
            
            // Progress bar
            if (totalCount > 0) {
                LinearProgressIndicator(
                    progress = completedCount.toFloat() / totalCount.toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Task list with parent-child hierarchy
            taskItems.forEach { taskItem ->
                if (taskItem.isParent) {
                    // Parent task with children
                    ParentTaskItem(
                        parentTask = taskItem.task,
                        childTasks = taskItem.children,
                        onTaskChecked = onTaskChecked,
                        onTaskClick = onTaskClick
                    )
                } else {
                    // Standalone task
                    com.lifeops.app.presentation.today.components.TaskItem(
                        task = taskItem.task,
                        isCompleted = com.lifeops.app.presentation.today.components.isTaskCompleted(taskItem.task),
                        onCheckedChange = { _ ->
                            onTaskChecked(taskItem.task.id)
                        },
                        onTaskClick = {
                            onTaskClick(taskItem.task.id)
                        }
                    )
                }
            }
        }
    }
}

// ================================
// Preview Functions
// ================================

@Preview(name = "Category Card - Mixed Tasks", showBackground = true)
@Composable
private fun PreviewCategoryCardMixed() {
    LifeOpsTheme {
        Surface {
            val taskItems = MockData.tasksByCategory["Fitness"] ?: emptyList()
            val totalTasks = taskItems.sumOf { if (it.isParent) it.children.size else 1 }
            val completedTasks = taskItems.sumOf { item ->
                if (item.isParent) {
                    item.children.count { com.lifeops.app.presentation.today.components.isTaskCompleted(it) }
                } else {
                    if (com.lifeops.app.presentation.today.components.isTaskCompleted(item.task)) 1 else 0
                }
            }
            CategoryCard(
                categoryName = "Fitness",
                taskItems = taskItems,
                totalTasksInCategory = totalTasks,
                completedTasksInCategory = completedTasks,
                onTaskChecked = {},
                onTaskClick = {}
            )
        }
    }
}

@Preview(name = "Category Card - All Incomplete", showBackground = true)
@Composable
private fun PreviewCategoryCardAllIncomplete() {
    LifeOpsTheme {
        Surface {
            val taskItems = listOf(
                TaskItem(MockData.waterPlants),
                TaskItem(MockData.checkMail),
                TaskItem(MockData.laundry)
            )
            CategoryCard(
                categoryName = "Home",
                taskItems = taskItems,
                totalTasksInCategory = taskItems.size,
                completedTasksInCategory = 0,
                onTaskChecked = {},
                onTaskClick = {}
            )
        }
    }
}

@Preview(name = "Category Card - All Complete", showBackground = true)
@Composable
private fun PreviewCategoryCardAllComplete() {
    LifeOpsTheme {
        Surface {
            val today = LocalDate.now()
            val taskItems = MockData.tasksByCategory["Work"]?.map { item ->
                if (item.isParent) {
                    item.copy(
                        task = item.task.copy(lastCompleted = today),
                        children = item.children.map { it.copy(lastCompleted = today) }
                    )
                } else {
                    item.copy(task = item.task.copy(lastCompleted = today))
                }
            } ?: emptyList()
            val totalTasks = taskItems.size
            CategoryCard(
                categoryName = "Work",
                taskItems = taskItems,
                totalTasksInCategory = totalTasks,
                completedTasksInCategory = totalTasks,
                onTaskChecked = {},
                onTaskClick = {}
            )
        }
    }
}

@Preview(name = "Category Card - Single Task", showBackground = true)
@Composable
private fun PreviewCategoryCardSingleTask() {
    LifeOpsTheme {
        Surface {
            val taskItems = listOf(TaskItem(MockData.readBook))
            CategoryCard(
                categoryName = "Reading",
                taskItems = taskItems,
                totalTasksInCategory = 1,
                completedTasksInCategory = 0,
                onTaskChecked = {},
                onTaskClick = {}
            )
        }
    }
}
