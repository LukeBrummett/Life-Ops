package com.lifeops.app.presentation.today.components

import androidx.compose.foundation.background
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
 * Parent task item with nested children
 * 
 * Displays a parent task with its child tasks indented underneath.
 * The parent shows completion status based on all children being complete.
 */
@Composable
fun ParentTaskItem(
    parentTask: Task,
    childTasks: List<Task>,
    today: LocalDate,
    onTaskChecked: (String) -> Unit,
    onTaskClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allChildrenComplete = childTasks.all { it.lastCompleted == today }
    val parentCompleted = parentTask.lastCompleted == today
    
    // According to spec, parent can auto-complete when all children done
    // or require manual completion if requiresManualCompletion is true
    val isParentComplete = if (parentTask.requiresManualCompletion) {
        parentCompleted
    } else {
        allChildrenComplete
    }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Parent task
        TaskItem(
            task = parentTask,
            isCompleted = isParentComplete,
            onCheckedChange = { _ ->
                // Only allow manual completion if required
                if (parentTask.requiresManualCompletion) {
                    onTaskChecked(parentTask.id)
                }
            },
            onTaskClick = {
                onTaskClick(parentTask.id)
            },
            enabled = parentTask.requiresManualCompletion
        )
        
        // Child tasks (indented)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            childTasks.forEach { childTask ->
                TaskItem(
                    task = childTask,
                    isCompleted = childTask.lastCompleted == today,
                    onCheckedChange = { _ ->
                        onTaskChecked(childTask.id)
                    },
                    onTaskClick = {
                        onTaskClick(childTask.id)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ParentTaskItemPreview() {
    LifeOpsTheme {
        Surface {
            ParentTaskItem(
                parentTask = MockData.parentTask,
                childTasks = MockData.childTasks,
                today = MockData.today,
                onTaskChecked = {},
                onTaskClick = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ParentTaskItemCompletedPreview() {
    LifeOpsTheme {
        Surface {
            val today = MockData.today
            ParentTaskItem(
                parentTask = MockData.parentTask.copy(lastCompleted = today),
                childTasks = MockData.childTasks.map { it.copy(lastCompleted = today) },
                today = today,
                onTaskChecked = {},
                onTaskClick = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
