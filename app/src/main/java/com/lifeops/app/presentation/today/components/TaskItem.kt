package com.lifeops.app.presentation.today.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lifeops.app.data.local.entity.Difficulty
import com.lifeops.app.data.local.entity.IntervalUnit
import com.lifeops.app.data.local.entity.Task
import com.lifeops.app.presentation.today.MockData
import com.lifeops.app.ui.theme.LifeOpsTheme
import java.time.LocalDate

/**
 * Individual task item component
 * 
 * Displays:
 * - Checkbox for completion
 * - Task name (with strike-through when completed)
 * - Metadata: time estimate and difficulty
 * - Completion streak if applicable
 */
@Composable
fun TaskItem(
    task: Task,
    isCompleted: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTaskClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Checkbox
        Checkbox(
            checked = isCompleted,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        )
        
        // Task content (clickable area)
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onTaskClick() },
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Task name
            Text(
                text = task.name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isCompleted) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textDecoration = if (isCompleted) {
                    TextDecoration.LineThrough
                } else {
                    TextDecoration.None
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Metadata row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time estimate
                task.timeEstimate?.let { time ->
                    Text(
                        text = formatTimeEstimate(time),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                // Difficulty
                task.difficulty?.let { difficulty ->
                    Text(
                        text = formatDifficulty(difficulty),
                        style = MaterialTheme.typography.bodySmall,
                        color = getDifficultyColor(difficulty)
                    )
                }
                
                // Streak indicator
                if (task.completionStreak > 0) {
                    Text(
                        text = "🔥 ${task.completionStreak}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Format time estimate in minutes to readable string
 */
private fun formatTimeEstimate(minutes: Int): String {
    return when {
        minutes < 60 -> "${minutes}m"
        minutes % 60 == 0 -> "${minutes / 60}h"
        else -> "${minutes / 60}h ${minutes % 60}m"
    }
}

/**
 * Format difficulty enum to display string
 */
private fun formatDifficulty(difficulty: Difficulty): String {
    return when (difficulty) {
        Difficulty.LOW -> "Easy"
        Difficulty.MEDIUM -> "Medium"
        Difficulty.HIGH -> "Hard"
    }
}

/**
 * Get color for difficulty level
 */
@Composable
private fun getDifficultyColor(difficulty: Difficulty): androidx.compose.ui.graphics.Color {
    return when (difficulty) {
        Difficulty.LOW -> MaterialTheme.colorScheme.tertiary
        Difficulty.MEDIUM -> MaterialTheme.colorScheme.primary
        Difficulty.HIGH -> MaterialTheme.colorScheme.error
    }
}

// ================================
// Preview Functions
// ================================

@Preview(name = "Task Item - Incomplete", showBackground = true)
@Composable
private fun PreviewTaskItemIncomplete() {
    LifeOpsTheme {
        Surface {
            TaskItem(
                task = MockData.morningWorkout,
                isCompleted = false,
                onCheckedChange = {}
            )
        }
    }
}

@Preview(name = "Task Item - Completed", showBackground = true)
@Composable
private fun PreviewTaskItemCompleted() {
    LifeOpsTheme {
        Surface {
            TaskItem(
                task = MockData.stretch,
                isCompleted = true,
                onCheckedChange = {}
            )
        }
    }
}

@Preview(name = "Task Item - With Streak", showBackground = true)
@Composable
private fun PreviewTaskItemWithStreak() {
    LifeOpsTheme {
        Surface {
            TaskItem(
                task = MockData.dailyStandup,
                isCompleted = true,
                onCheckedChange = {}
            )
        }
    }
}

@Preview(name = "Task Item - No Time", showBackground = true)
@Composable
private fun PreviewTaskItemNoTime() {
    LifeOpsTheme {
        Surface {
            TaskItem(
                task = MockData.reviewPRs,
                isCompleted = false,
                onCheckedChange = {}
            )
        }
    }
}

@Preview(name = "Task Item - High Difficulty", showBackground = true)
@Composable
private fun PreviewTaskItemHighDifficulty() {
    LifeOpsTheme {
        Surface {
            TaskItem(
                task = MockData.laundry.copy(difficulty = Difficulty.HIGH),
                isCompleted = false,
                onCheckedChange = {}
            )
        }
    }
}
