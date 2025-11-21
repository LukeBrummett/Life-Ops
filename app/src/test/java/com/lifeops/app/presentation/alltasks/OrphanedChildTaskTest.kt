package com.lifeops.app.presentation.alltasks

import com.google.common.truth.Truth.assertThat
import com.lifeops.app.data.local.entity.IntervalUnit
import com.lifeops.app.data.local.entity.Task
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for orphaned child tasks in All Tasks view
 * 
 * Tests the display logic for child tasks when their parent task is deleted.
 * 
 * Note: In production, when a parent is deleted, the TaskRepository automatically
 * removes the parent ID from child tasks' parentTaskIds lists. However, these tests
 * verify that the display logic can handle edge cases where orphaned references might
 * still exist (e.g., direct database manipulation, data import scenarios).
 * 
 * The groupTasksWithHierarchy() function should show child tasks with non-existent
 * parent IDs as standalone tasks rather than hiding them completely.
 */
class OrphanedChildTaskTest {
    
    @Test
    fun `orphaned child task appears as standalone when parent is deleted`() {
        // Given - A child task whose parent no longer exists
        val today = LocalDate.now()
        
        val orphanedChild = Task(
            id = "child-task",
            name = "Orphaned Child Task",
            category = "Household",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = today,
            parentTaskIds = listOf("deleted-parent-id"), // Parent was deleted
            childOrder = 1
        )
        
        val standaloneTask = Task(
            id = "standalone",
            name = "Standalone Task",
            category = "Work",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = today
        )
        
        val tasks = listOf(orphanedChild, standaloneTask)
        
        // When - Group tasks with hierarchy
        val grouped = groupTasksWithHierarchy(tasks)
        
        // Then - Both tasks should appear as standalone (no children)
        assertThat(grouped).hasSize(2)
        
        val orphanedTaskItem = grouped.find { it.task.id == "child-task" }
        assertThat(orphanedTaskItem).isNotNull()
        assertThat(orphanedTaskItem?.task?.name).isEqualTo("Orphaned Child Task")
        assertThat(orphanedTaskItem?.isParent).isFalse()
        assertThat(orphanedTaskItem?.children).isEmpty()
        
        val standaloneTaskItem = grouped.find { it.task.id == "standalone" }
        assertThat(standaloneTaskItem).isNotNull()
        assertThat(standaloneTaskItem?.task?.name).isEqualTo("Standalone Task")
        assertThat(standaloneTaskItem?.isParent).isFalse()
        assertThat(standaloneTaskItem?.children).isEmpty()
    }
    
    @Test
    fun `child task with existing parent is grouped correctly`() {
        // Given - A parent and child task that both exist
        val today = LocalDate.now()
        
        val parentTask = Task(
            id = "parent-task",
            name = "Parent Task",
            category = "Household",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = today
        )
        
        val childTask = Task(
            id = "child-task",
            name = "Child Task",
            category = "Household",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = today,
            parentTaskIds = listOf(parentTask.id),
            childOrder = 1
        )
        
        val tasks = listOf(parentTask, childTask)
        
        // When - Group tasks with hierarchy
        val grouped = groupTasksWithHierarchy(tasks)
        
        // Then - Only parent should appear in the top level, with child nested
        assertThat(grouped).hasSize(1)
        
        val parentTaskItem = grouped[0]
        assertThat(parentTaskItem.task.id).isEqualTo("parent-task")
        assertThat(parentTaskItem.isParent).isTrue()
        assertThat(parentTaskItem.children).hasSize(1)
        assertThat(parentTaskItem.children[0].id).isEqualTo("child-task")
    }
    
    @Test
    fun `child with multiple parents appears under first existing parent`() {
        // Given - A child with multiple parents, one deleted
        val today = LocalDate.now()
        
        val parentTask = Task(
            id = "parent-task",
            name = "Existing Parent",
            category = "Household",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = today
        )
        
        val childTask = Task(
            id = "child-task",
            name = "Child with Multiple Parents",
            category = "Household",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = today,
            parentTaskIds = listOf("deleted-parent-id", parentTask.id), // First parent deleted, second exists
            childOrder = 1
        )
        
        val tasks = listOf(parentTask, childTask)
        
        // When - Group tasks with hierarchy
        val grouped = groupTasksWithHierarchy(tasks)
        
        // Then - Child should be grouped under the existing parent
        assertThat(grouped).hasSize(1)
        
        val parentTaskItem = grouped[0]
        assertThat(parentTaskItem.task.id).isEqualTo("parent-task")
        assertThat(parentTaskItem.isParent).isTrue()
        assertThat(parentTaskItem.children).hasSize(1)
        assertThat(parentTaskItem.children[0].id).isEqualTo("child-task")
    }
    
    @Test
    fun `multiple orphaned children from same deleted parent all appear as standalone`() {
        // Given - Multiple child tasks whose parent was deleted
        val today = LocalDate.now()
        
        val deletedParentId = "deleted-parent-id"
        
        val child1 = Task(
            id = "child-1",
            name = "Orphaned Child 1",
            category = "Household",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = today,
            parentTaskIds = listOf(deletedParentId),
            childOrder = 1
        )
        
        val child2 = Task(
            id = "child-2",
            name = "Orphaned Child 2",
            category = "Household",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = today,
            parentTaskIds = listOf(deletedParentId),
            childOrder = 2
        )
        
        val tasks = listOf(child1, child2)
        
        // When - Group tasks with hierarchy
        val grouped = groupTasksWithHierarchy(tasks)
        
        // Then - Both orphaned children should appear as standalone tasks
        assertThat(grouped).hasSize(2)
        
        val names = grouped.map { it.task.name }
        assertThat(names).containsExactly("Orphaned Child 1", "Orphaned Child 2")
        
        // Both should be standalone (not parents)
        grouped.forEach { taskItem ->
            assertThat(taskItem.isParent).isFalse()
            assertThat(taskItem.children).isEmpty()
        }
    }
    
    /**
     * Mimics the groupTasksWithHierarchy logic from AllTasksViewModel
     * This is the FIXED version that checks if parents actually exist
     */
    private fun groupTasksWithHierarchy(tasks: List<Task>): List<TaskItem> {
        // Create a set of all task IDs for quick parent existence check
        val taskIds = tasks.map { it.id }.toSet()
        
        // Track which tasks are children whose parents exist (so we don't show them as standalone)
        val childTaskIds = mutableSetOf<String>()
        
        // Find child tasks that have at least one existing parent
        tasks.forEach { task ->
            val parentIds = task.parentTaskIds
            if (!parentIds.isNullOrEmpty() && 
                parentIds.any { parentId -> parentId in taskIds }) {
                childTaskIds.add(task.id)
            }
        }
        
        // Build task items with hierarchy
        return tasks.mapNotNull { task ->
            // Skip tasks that are children with existing parents (they'll be included under their parent)
            if (task.id in childTaskIds) {
                return@mapNotNull null
            }
            
            // Find children for this task
            val children = tasks.filter { potentialChild ->
                val childParentIds = potentialChild.parentTaskIds
                !childParentIds.isNullOrEmpty() && 
                task.id in childParentIds
            }.sortedBy { it.childOrder ?: 0 }
            
            TaskItem(
                task = task,
                children = children,
                isParent = children.isNotEmpty()
            )
        }
    }
}
