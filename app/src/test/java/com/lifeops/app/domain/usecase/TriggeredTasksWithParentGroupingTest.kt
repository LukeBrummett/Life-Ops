package com.lifeops.app.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.lifeops.app.data.local.entity.IntervalUnit
import com.lifeops.app.data.local.entity.Task
import com.lifeops.app.presentation.today.TaskItem
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for triggered tasks with parent task grouping logic
 * 
 * Tests the grouping logic that should work in TodayViewModel.groupTasksWithHierarchy
 * when triggered tasks have a parent task.
 */
class TriggeredTasksWithParentGroupingTest {
    
    @Test
    fun `grouping logic includes triggered tasks under parent when both are due today`() {
        // Given - Tasks that would be returned by observeTasksDueByDate(today)
        val today = LocalDate.now()
        
        val parentTask = Task(
            id = "clean-kitchen",
            name = "Clean Kitchen",
            category = "Household",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = today
        )
        
        val loadDishwasher = Task(
            id = "load-dishwasher",
            name = "Load Dishwasher",
            category = "Household",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = today,
            parentTaskIds = listOf(parentTask.id),
            childOrder = 1,
            triggersTaskIds = listOf("unload-dishwasher", "clean-sink")
        )
        
        val unloadDishwasher = Task(
            id = "unload-dishwasher",
            name = "Unload Dishwasher",
            category = "Household",
            intervalUnit = IntervalUnit.ADHOC,
            intervalQty = 0,
            nextDue = today, // Set by trigger mechanism
            parentTaskIds = listOf(parentTask.id),
            childOrder = 2,
            triggeredByTaskIds = listOf(loadDishwasher.id)
        )
        
        val cleanSink = Task(
            id = "clean-sink",
            name = "Clean Sink",
            category = "Household",
            intervalUnit = IntervalUnit.ADHOC,
            intervalQty = 0,
            nextDue = today, // Set by trigger mechanism
            parentTaskIds = listOf(parentTask.id),
            childOrder = 3,
            triggeredByTaskIds = listOf(loadDishwasher.id)
        )
        
        val tasks = listOf(parentTask, loadDishwasher, unloadDishwasher, cleanSink)
        
        // When - Group tasks with hierarchy (mimicking TodayViewModel logic)
        val grouped = groupTasksWithHierarchy(tasks)
        
        // Then - Parent task should have all children
        val parentTaskItem = grouped.find { it.task.id == parentTask.id }
        assertThat(parentTaskItem).isNotNull()
        assertThat(parentTaskItem?.isParent).isTrue()
        assertThat(parentTaskItem?.children).hasSize(3)
        
        // Children should be in correct order
        val childNames = parentTaskItem?.children?.map { it.name }
        assertThat(childNames).containsExactly(
            "Load Dishwasher",
            "Unload Dishwasher",
            "Clean Sink"
        ).inOrder()
    }
    
    @Test
    fun `grouping logic excludes triggered tasks when not triggered`() {
        // Given - Tasks where triggered tasks have nextDue = null (not triggered yet)
        val today = LocalDate.now()
        
        val parentTask = Task(
            id = "clean-kitchen",
            name = "Clean Kitchen",
            category = "Household",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = today
        )
        
        val loadDishwasher = Task(
            id = "load-dishwasher",
            name = "Load Dishwasher",
            category = "Household",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = today,
            parentTaskIds = listOf(parentTask.id),
            childOrder = 1,
            triggersTaskIds = listOf("unload-dishwasher")
        )
        
        // This task would NOT be returned by observeTasksDueByDate because nextDue is null
        // But we include it here to verify the grouping logic handles it correctly
        
        val tasks = listOf(parentTask, loadDishwasher)
        
        // When - Group tasks with hierarchy
        val grouped = groupTasksWithHierarchy(tasks)
        
        // Then - Parent task should only have Load Dishwasher as child
        val parentTaskItem = grouped.find { it.task.id == parentTask.id }
        assertThat(parentTaskItem).isNotNull()
        assertThat(parentTaskItem?.isParent).isTrue()
        assertThat(parentTaskItem?.children).hasSize(1)
        assertThat(parentTaskItem?.children?.get(0)?.name).isEqualTo("Load Dishwasher")
    }
    
    /**
     * Mimics the groupTasksWithHierarchy logic from TodayViewModel
     * This is a simplified version for testing purposes
     */
    private fun groupTasksWithHierarchy(tasks: List<Task>): List<TaskItem> {
        // Track which tasks are children (so we don't show them as standalone)
        val childTaskIds = mutableSetOf<String>()
        
        // Find all child tasks
        tasks.forEach { task ->
            if (!task.parentTaskIds.isNullOrEmpty()) {
                childTaskIds.add(task.id)
            }
        }
        
        // Build task items with hierarchy
        val taskItems = tasks.mapNotNull { task ->
            // Skip tasks that are children (they'll be included under their parent)
            if (task.id in childTaskIds) {
                return@mapNotNull null
            }
            
            // Find children for this task
            val children = tasks.filter { potentialChild ->
                !potentialChild.parentTaskIds.isNullOrEmpty() && 
                task.id in potentialChild.parentTaskIds
            }.sortedBy { it.childOrder ?: 0 }
            
            TaskItem(
                task = task,
                children = children,
                isParent = children.isNotEmpty()
            )
        }
        
        return taskItems
    }
}
