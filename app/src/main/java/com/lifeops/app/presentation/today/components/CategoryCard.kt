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
    onTaskChecked: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val completedCount = tasks.count { isTaskCompleted(it) }
    val totalCount = tasks.size
    
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
            CategoryCard(
                categoryName = "Fitness",
                tasks = MockData.tasksByCategory["Fitness"] ?: emptyList(),
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
            CategoryCard(
                categoryName = "Home",
                tasks = listOf(
                    MockData.waterPlants,
                    MockData.checkMail,
                    MockData.laundry
                ),
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
            CategoryCard(
                categoryName = "Work",
                tasks = MockData.tasksByCategory["Work"]?.map { 
                    it.copy(lastCompleted = LocalDate.now())
                } ?: emptyList(),
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
            CategoryCard(
                categoryName = "Reading",
                tasks = listOf(MockData.readBook),
                onTaskChecked = {}
            )
        }
    }
}
