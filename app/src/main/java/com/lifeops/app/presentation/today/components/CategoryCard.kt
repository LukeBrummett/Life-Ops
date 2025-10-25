package com.lifeops.app.presentation.today.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lifeops.app.data.local.entity.Task
import com.lifeops.app.presentation.today.MockData
import com.lifeops.app.ui.theme.LifeOpsTheme
import java.time.LocalDate

/**
 * Category card component
 * 
 * Displays:
 * - Category name
 * - Progress indicator (X/Y tasks complete)
 * - List of tasks in this category
 */
@Composable
fun CategoryCard(
    categoryName: String,
    tasks: List<Task>,
    totalTasksInCategory: Int,
    completedTasksInCategory: Int,
    onTaskChecked: (Long) -> Unit,
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
            
            // Task list
            tasks.forEach { task ->
                TaskItem(
                    task = task,
                    isCompleted = isTaskCompleted(task),
                    onCheckedChange = { checked ->
                        onTaskChecked(task.id)
                    }
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

@Preview(name = "Category Card - Mixed Tasks", showBackground = true)
@Composable
private fun PreviewCategoryCardMixed() {
    LifeOpsTheme {
        Surface {
            val tasks = MockData.tasksByCategory["Fitness"] ?: emptyList()
            CategoryCard(
                categoryName = "Fitness",
                tasks = tasks,
                totalTasksInCategory = tasks.size,
                completedTasksInCategory = tasks.count { isTaskCompleted(it) },
                onTaskChecked = {}
            )
        }
    }
}

@Preview(name = "Category Card - All Incomplete", showBackground = true)
@Composable
private fun PreviewCategoryCardAllIncomplete() {
    LifeOpsTheme {
        Surface {
            val tasks = listOf(
                MockData.waterPlants,
                MockData.checkMail,
                MockData.laundry
            )
            CategoryCard(
                categoryName = "Home",
                tasks = tasks,
                totalTasksInCategory = tasks.size,
                completedTasksInCategory = 0,
                onTaskChecked = {}
            )
        }
    }
}

@Preview(name = "Category Card - All Complete", showBackground = true)
@Composable
private fun PreviewCategoryCardAllComplete() {
    LifeOpsTheme {
        Surface {
            val tasks = MockData.tasksByCategory["Work"]?.map { 
                it.copy(lastCompleted = LocalDate.now())
            } ?: emptyList()
            CategoryCard(
                categoryName = "Work",
                tasks = tasks,
                totalTasksInCategory = tasks.size,
                completedTasksInCategory = tasks.size,
                onTaskChecked = {}
            )
        }
    }
}

@Preview(name = "Category Card - Single Task", showBackground = true)
@Composable
private fun PreviewCategoryCardSingleTask() {
    LifeOpsTheme {
        Surface {
            val tasks = listOf(MockData.readBook)
            CategoryCard(
                categoryName = "Reading",
                tasks = tasks,
                totalTasksInCategory = 1,
                completedTasksInCategory = 0,
                onTaskChecked = {}
            )
        }
    }
}
