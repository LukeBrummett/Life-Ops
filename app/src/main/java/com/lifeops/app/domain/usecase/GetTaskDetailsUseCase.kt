package com.lifeops.app.domain.usecase

import com.lifeops.app.data.local.entity.Inventory
import com.lifeops.app.data.local.entity.Supply
import com.lifeops.app.data.local.entity.Task
import com.lifeops.app.data.local.entity.TaskSupply
import com.lifeops.app.data.repository.SupplyRepository
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
    private val taskRepository: TaskRepository,
    private val supplyRepository: SupplyRepository
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
            
            // Get inventory associations
            val inventoryAssociations = getInventoryAssociations(taskId)
            
            Result.success(
                TaskDetails(
                    task = task,
                    parentTask = parentTask,
                    childTasks = childTasks,
                    triggeredByTasks = triggeredByTasks,
                    triggersTasks = triggersTasks,
                    inventoryAssociations = inventoryAssociations
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get inventory associations with supply and stock details
     */
    private suspend fun getInventoryAssociations(taskId: String): List<InventoryAssociation> {
        val taskSupplies = supplyRepository.getTaskSuppliesForTask(taskId)
        
        return taskSupplies.mapNotNull { taskSupply ->
            val supply = supplyRepository.getSupplyById(taskSupply.supplyId)
            val inventory = supplyRepository.getInventory(taskSupply.supplyId)
            
            if (supply != null) {
                InventoryAssociation(
                    taskSupply = taskSupply,
                    supply = supply,
                    inventory = inventory
                )
            } else {
                null
            }
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
    val triggersTasks: List<Task> = emptyList(),
    val inventoryAssociations: List<InventoryAssociation> = emptyList()
)

/**
 * Complete inventory association with supply and stock data
 */
data class InventoryAssociation(
    val taskSupply: TaskSupply,
    val supply: Supply,
    val inventory: Inventory?
)
