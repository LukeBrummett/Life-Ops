package com.lifeops.app.presentation.alltasks.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.lifeops.app.ui.theme.LifeOpsTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Task item for All Tasks screen
 * 
 * Shows:
 * - Task name with right-aligned category badge
 * - Schedule information (next due date, interval)
 * - Indicator badges (has children, is triggered, requires inventory)
 * - Tags and streak
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AllTasksItem(
    task: Task,
    onTaskClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onTaskClick,
                onLongClick = { /* TODO: Context menu */ }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Task name with right-aligned category badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                
                // Category badge (right-aligned)
                SuggestionChip(
                    onClick = { },
                    label = {
                        Text(
                            text = task.category,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    border = null
                )
            }
            
            // Schedule information
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Next due date
                task.nextDue?.let { nextDue ->
                    Text(
                        text = formatNextDueDate(nextDue),
                        style = MaterialTheme.typography.bodySmall,
                        color = getNextDueDateColor(nextDue)
                    )
                } ?: run {
                    Text(
                        text = "No schedule",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                
                // Interval badge
                if (task.intervalQty > 0 && task.intervalUnit != IntervalUnit.ADHOC) {
                    Text(
                        text = "• ${formatInterval(task.intervalQty, task.intervalUnit)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Indicator badges, tags, and streak
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicator badges
                if (task.parentTaskIds?.isNotEmpty() == true || task.childOrder != null) {
                    Icon(
                        imageVector = Icons.Default.AccountTree,
                        contentDescription = "Parent or child task",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (task.triggeredByTaskIds?.isNotEmpty() == true) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "Triggered task",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
                
                if (task.requiresInventory) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = "Requires inventory",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                
                // Tags
                if (task.tags.isNotBlank()) {
                    val tagList = task.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    tagList.take(3).forEach { tag ->
                        AssistChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = null
                        )
                    }
                    if (tagList.size > 3) {
                        Text(
                            text = "+${tagList.size - 3}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                // Streak
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
 * Parent task item with nested children for All Tasks screen
 */
@Composable
fun ParentAllTasksItem(
    parentTask: Task,
    childTasks: List<Task>,
    onTaskClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // Parent task
        AllTasksItem(
            task = parentTask,
            onTaskClick = { onTaskClick(parentTask.id) }
        )
        
        // Child tasks (indented)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            childTasks.forEach { childTask ->
                AllTasksItem(
                    task = childTask,
                    onTaskClick = { onTaskClick(childTask.id) }
                )
            }
        }
    }
}

/**
 * Formats the next due date relative to today
 */
private fun formatNextDueDate(date: LocalDate): String {
    val today = LocalDate.now()
    val daysUntil = ChronoUnit.DAYS.between(today, date)
    
    return when {
        daysUntil < 0 -> {
            val daysOverdue = -daysUntil
            when (daysOverdue.toInt()) {
                1 -> "Yesterday"
                in 2..6 -> "$daysOverdue days ago"
                else -> date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
            }
        }
        daysUntil == 0L -> "Today"
        daysUntil == 1L -> "Tomorrow"
        daysUntil in 2..6 -> date.format(DateTimeFormatter.ofPattern("EEEE")) // Day of week
        daysUntil in 7..13 -> "Next ${date.format(DateTimeFormatter.ofPattern("EEEE"))}"
        else -> date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    }
}

/**
 * Returns color based on due date urgency
 */
@Composable
private fun getNextDueDateColor(date: LocalDate): androidx.compose.ui.graphics.Color {
    val today = LocalDate.now()
    val daysUntil = ChronoUnit.DAYS.between(today, date)
    
    return when {
        daysUntil < 0 -> MaterialTheme.colorScheme.error // Overdue
        daysUntil == 0L -> MaterialTheme.colorScheme.primary // Today
        daysUntil == 1L -> MaterialTheme.colorScheme.tertiary // Tomorrow
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) // Future
    }
}

/**
 * Formats the interval for display
 */
private fun formatInterval(count: Int, unit: IntervalUnit): String {
    val unitStr = when (unit) {
        IntervalUnit.DAY -> if (count == 1) "day" else "days"
        IntervalUnit.WEEK -> if (count == 1) "week" else "weeks"
        IntervalUnit.MONTH -> if (count == 1) "month" else "months"
        IntervalUnit.ADHOC -> "adhoc"
    }
    return "Every $count $unitStr"
}

// ===== Previews =====

@Preview(showBackground = true)
@Composable
private fun AllTasksItemPreview() {
    LifeOpsTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AllTasksItem(
                task = Task(
                    id = 1,
                    name = "Morning Workout",
                    category = "Health",
                    nextDue = LocalDate.now(),
                    intervalQty = 1,
                    intervalUnit = IntervalUnit.DAY,
                    timeEstimate = 30,
                    difficulty = Difficulty.MEDIUM,
                    completionStreak = 5,
                    parentTaskIds = null,
                    childOrder = null,
                    triggeredByTaskIds = null,
                    requiresInventory = false,
                    tags = "cardio, morning"
                ),
                onTaskClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AllTasksItemWithIndicatorsPreview() {
    LifeOpsTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AllTasksItem(
                task = Task(
                    id = 2,
                    name = "Weekly Planning Session with all team members",
                    category = "Work",
                    nextDue = LocalDate.now().plusDays(3),
                    intervalQty = 1,
                    intervalUnit = IntervalUnit.WEEK,
                    timeEstimate = 120,
                    difficulty = Difficulty.HIGH,
                    completionStreak = 0,
                    childOrder = 1,
                    parentTaskIds = listOf(10),
                    triggeredByTaskIds = listOf(1),
                    requiresInventory = true,
                    tags = "meeting, planning, team, collaboration"
                ),
                onTaskClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AllTasksItemOverduePreview() {
    LifeOpsTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AllTasksItem(
                task = Task(
                    id = 3,
                    name = "Submit Report",
                    category = "Work",
                    nextDue = LocalDate.now().minusDays(2),
                    intervalQty = 0,
                    intervalUnit = IntervalUnit.ADHOC,
                    timeEstimate = 45,
                    difficulty = Difficulty.LOW,
                    completionStreak = 0,
                    parentTaskIds = null,
                    childOrder = null,
                    triggeredByTaskIds = null,
                    requiresInventory = false,
                    tags = "urgent"
                ),
                onTaskClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AllTasksItemCompletedPreview() {
    LifeOpsTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AllTasksItem(
                task = Task(
                    id = 4,
                    name = "Read for 30 minutes",
                    category = "Personal",
                    nextDue = LocalDate.now(),
                    intervalQty = 1,
                    intervalUnit = IntervalUnit.DAY,
                    timeEstimate = 30,
                    difficulty = null,
                    completionStreak = 12,
                    parentTaskIds = null,
                    childOrder = null,
                    triggeredByTaskIds = null,
                    requiresInventory = false,
                    tags = "reading, relaxation"
                ),
                onTaskClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AllTasksItemNoSchedulePreview() {
    LifeOpsTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AllTasksItem(
                task = Task(
                    id = 5,
                    name = "Adhoc task - call plumber",
                    category = "Home",
                    nextDue = null,
                    intervalQty = 0,
                    intervalUnit = IntervalUnit.ADHOC,
                    timeEstimate = null,
                    difficulty = null,
                    completionStreak = 0,
                    parentTaskIds = null,
                    childOrder = null,
                    triggeredByTaskIds = null,
                    requiresInventory = false,
                    tags = ""
                ),
                onTaskClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ParentAllTasksItemPreview() {
    LifeOpsTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ParentAllTasksItem(
                parentTask = Task(
                    id = 100,
                    name = "Complete Morning Routine",
                    category = "Health",
                    nextDue = LocalDate.now(),
                    intervalQty = 1,
                    intervalUnit = IntervalUnit.DAY,
                    requiresManualCompletion = false,
                    tags = "routine"
                ),
                childTasks = listOf(
                    Task(
                        id = 101,
                        name = "Exercise",
                        category = "Health",
                        nextDue = LocalDate.now(),
                        parentTaskIds = listOf(100),
                        childOrder = 1,
                        tags = "cardio"
                    ),
                    Task(
                        id = 102,
                        name = "Meditation",
                        category = "Health",
                        nextDue = LocalDate.now(),
                        parentTaskIds = listOf(100),
                        childOrder = 2,
                        tags = "mindfulness"
                    )
                ),
                onTaskClick = {}
            )
        }
    }
}
