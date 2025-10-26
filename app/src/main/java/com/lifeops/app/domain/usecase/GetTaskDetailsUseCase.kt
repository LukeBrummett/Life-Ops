package com.lifeops.app.domain.usecase

import com.lifeops.app.data.local.entity.Task
import com.lifeops.app.data.repository.TaskRepository
import javax.inject.Inject

/**
 * Use case for retrieving a single task with all its relationship data
 * 
 * This aggregates:
 * - The task itself
 * - Parent task (if any)
 * - Child tasks (if any)
 * - Tasks that trigger this task
 * - Tasks that this task triggers
 * - Associated inventory items (via task-supply relationships)
 */
class GetTaskDetailsUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    /**
     * Get complete task details including all relationships
     * 
     * @param taskId The ID of the task to retrieve
     * @return TaskDetails containing the task and all related data, or null if task not found
     */
    suspend operator fun invoke(taskId: String): Result<TaskDetails?> {
        return try {
            // Get the main task
            val taskResult = taskRepository.getTaskById(taskId)
            if (taskResult.isFailure || taskResult.getOrNull() == null) {
                return Result.success(null)
            }
            
            val task = taskResult.getOrNull()!!
            
            // Get parent task if exists
            val parentTask = if (!task.parentTaskIds.isNullOrEmpty()) {
                val parentId = task.parentTaskIds.first() // V1: single parent only
                taskRepository.getTaskById(parentId).getOrNull()
            } else {
                null
            }
            
            // Get child tasks
            val childTasks = taskRepository.getChildTasks(taskId).getOrNull() ?: emptyList()
            
            // Get tasks that trigger this task (triggeredBy)
            val triggeredByTasks = taskRepository.getTasksThatTrigger(taskId).getOrNull() ?: emptyList()
            
            // Get tasks that this task triggers
            val triggersTasks = taskRepository.getTriggeredTasks(taskId).getOrNull() ?: emptyList()
            
            // TODO: Get inventory associations when TaskSupply relationship is implemented
            
            Result.success(
                TaskDetails(
                    task = task,
                    parentTask = parentTask,
                    childTasks = childTasks,
                    triggeredByTasks = triggeredByTasks,
                    triggersTasks = triggersTasks
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Aggregated task details with all relationships
 */
data class TaskDetails(
    val task: Task,
    val parentTask: Task? = null,
    val childTasks: List<Task> = emptyList(),
    val triggeredByTasks: List<Task> = emptyList(),
    val triggersTasks: List<Task> = emptyList()
    // TODO: Add inventory associations when TaskSupply relationship is implemented
)
