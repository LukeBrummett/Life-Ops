package com.lifeops.app.domain.usecase.task

import com.lifeops.app.data.local.entity.ConsumptionMode
import com.lifeops.app.data.local.entity.Task
import com.lifeops.app.data.local.entity.TaskSupply
import com.lifeops.app.data.repository.SupplyRepository
import com.lifeops.app.data.repository.TaskRepository
import com.lifeops.app.util.DateProvider
import java.util.UUID
import javax.inject.Inject

/**
 * Use case for saving a task (create or update)
 * 
 * Handles:
 * - Task validation
 * - Task entity save
 * - Relationship updates (parent/child, triggers)
 * - Inventory associations
 * - Circular dependency detection
 */
class SaveTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val supplyRepository: SupplyRepository,
    private val dateProvider: DateProvider
) {
    /**
     * Save task with all relationships
     * 
     * @param request SaveTaskRequest containing all task data
     * @return Result with taskId on success or error message
     */
    suspend operator fun invoke(request: SaveTaskRequest): Result<String> {
        return try {
            // Validate request
            validateRequest(request)?.let { error ->
                return Result.failure(Exception(error))
            }
            
            // Check for circular dependencies
            if (request.taskId != null) {
                checkCircularDependencies(request)?.let { error ->
                    return Result.failure(Exception(error))
                }
            }
            
            // Create or get task ID
            val taskId = request.taskId ?: UUID.randomUUID().toString()
            
            // Build task entity
            val task = Task(
                id = taskId,
                name = request.name,
                category = request.category,
                tags = request.tags.joinToString(","),
                description = request.description.takeIf { it.isNotBlank() } ?: "",
                intervalUnit = request.intervalUnit,
                intervalQty = request.intervalQty,
                specificDaysOfWeek = request.specificDaysOfWeek.takeIf { it.isNotEmpty() },
                excludedDaysOfWeek = request.excludedDaysOfWeek.takeIf { it.isNotEmpty() },
                overdueBehavior = request.overdueBehavior,
                deleteAfterCompletion = request.deleteAfterCompletion,
                parentTaskIds = request.parentTaskId?.let { listOf(it) },
                requiresManualCompletion = request.requiresManualCompletion,
                triggeredByTaskIds = request.triggeredByTaskIds.takeIf { it.isNotEmpty() },
                triggersTaskIds = request.triggersTaskIds.takeIf { it.isNotEmpty() },
                nextDue = when {
                    request.nextDue != null -> request.nextDue // Use provided date if specified
                    request.intervalUnit == com.lifeops.app.data.local.entity.IntervalUnit.ADHOC -> null // ADHOC tasks have no automatic schedule
                    else -> dateProvider.now() // Set initial due date for scheduled tasks
                },
                active = true
            )
            
            // Save task
            val saveResult = if (request.taskId != null) {
                taskRepository.updateTask(task)
            } else {
                taskRepository.createTask(task)
            }
            
            if (saveResult.isFailure) {
                return Result.failure(saveResult.exceptionOrNull() ?: Exception("Failed to save task"))
            }
            
            // Update child task relationships (update their parentTaskIds)
            updateChildRelationships(taskId, request.childTaskIds)
            
            // Update trigger relationships (bidirectional sync)
            updateTriggerRelationships(taskId, request.triggeredByTaskIds, request.triggersTaskIds)
            
            // Save inventory associations
            saveInventoryAssociations(taskId, request.inventoryAssociations)
            
            Result.success(taskId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Validate the save request
     */
    private fun validateRequest(request: SaveTaskRequest): String? {
        // Required fields
        if (request.name.isBlank()) {
            return "Task name is required"
        }
        
        if (request.name.length > 100) {
            return "Task name must be 100 characters or less"
        }
        
        if (request.category.isBlank()) {
            return "Category is required"
        }
        
        // Interval validation (ADHOC tasks can have intervalQty = 0)
        if (request.intervalUnit != com.lifeops.app.data.local.entity.IntervalUnit.ADHOC && 
            request.intervalQty < 1) {
            return "Interval quantity must be at least 1"
        }
        
        // Inventory validation
        request.inventoryAssociations.forEach { association ->
            if (association.consumptionMode == ConsumptionMode.FIXED && 
                (association.fixedQuantity == null || association.fixedQuantity <= 0)) {
                return "Fixed consumption mode requires a positive quantity"
            }
        }
        
        return null
    }
    
    /**
     * Check for circular dependencies in relationships
     */
    private suspend fun checkCircularDependencies(request: SaveTaskRequest): String? {
        val taskId = request.taskId ?: return null
        
        // Check parent relationship (would this task become its own ancestor?)
        request.parentTaskId?.let { parentId ->
            if (parentId == taskId) {
                return "Task cannot be its own parent"
            }
            
            // Check if any child would become a parent
            if (request.childTaskIds.contains(parentId)) {
                return "A task cannot be both a parent and child"
            }
            
            // TODO: Check for deeper circular hierarchies (grandparent, etc.)
        }
        
        // Check child relationships
        request.childTaskIds.forEach { childId ->
            if (childId == taskId) {
                return "Task cannot be its own child"
            }
        }
        
        // Check trigger relationships
        request.triggeredByTaskIds.forEach { triggerId ->
            if (triggerId == taskId) {
                return "Task cannot trigger itself"
            }
        }
        
        request.triggersTaskIds.forEach { triggerId ->
            if (triggerId == taskId) {
                return "Task cannot trigger itself"
            }
        }
        
        return null
    }
    
    /**
     * Update child tasks to reference this task as parent
     */
    private suspend fun updateChildRelationships(parentId: String, childIds: List<String>) {
        // Get all tasks that currently have this task as parent
        val currentChildren = taskRepository.getChildTasks(parentId).getOrNull() ?: emptyList()
        
        // Remove parent from tasks no longer children
        val removedChildren = currentChildren.filter { !childIds.contains(it.id) }
        removedChildren.forEach { child ->
            val updatedParents = child.parentTaskIds?.filter { it != parentId } ?: emptyList()
            taskRepository.updateTask(child.copy(parentTaskIds = updatedParents.takeIf { it.isNotEmpty() }))
        }
        
        // Add parent to new children
        val newChildren = childIds.filter { childId -> 
            !currentChildren.any { it.id == childId }
        }
        
        newChildren.forEach { childId ->
            taskRepository.getTaskById(childId).getOrNull()?.let { child ->
                val updatedParents = (child.parentTaskIds ?: emptyList()) + parentId
                taskRepository.updateTask(child.copy(parentTaskIds = updatedParents))
            }
        }
    }
    
    /**
     * Update trigger relationships bidirectionally
     * 
     * This ensures that:
     * - If this task is triggered by task X, then X's triggersTaskIds includes this task
     * - If this task triggers task Y, then Y's triggeredByTaskIds includes this task
     */
    private suspend fun updateTriggerRelationships(
        taskId: String,
        triggeredByTaskIds: List<String>,
        triggersTaskIds: List<String>
    ) {
        // Update "triggered by" relationships
        // Get tasks that currently trigger this task
        val currentTriggeringTasks = taskRepository.getTasksThatTrigger(taskId).getOrNull() ?: emptyList()
        
        // Remove this task from tasks that no longer trigger it
        val removedTriggers = currentTriggeringTasks.filter { !triggeredByTaskIds.contains(it.id) }
        removedTriggers.forEach { triggerTask ->
            val updatedTriggers = triggerTask.triggersTaskIds?.filter { it != taskId } ?: emptyList()
            taskRepository.updateTask(triggerTask.copy(triggersTaskIds = updatedTriggers.takeIf { it.isNotEmpty() }))
        }
        
        // Add this task to new triggering tasks
        val newTriggers = triggeredByTaskIds.filter { triggerId ->
            !currentTriggeringTasks.any { it.id == triggerId }
        }
        newTriggers.forEach { triggerId ->
            taskRepository.getTaskById(triggerId).getOrNull()?.let { triggerTask ->
                val updatedTriggers = (triggerTask.triggersTaskIds ?: emptyList()) + taskId
                taskRepository.updateTask(triggerTask.copy(triggersTaskIds = updatedTriggers))
            }
        }
        
        // Update "triggers" relationships
        // Get tasks that this task currently triggers
        val currentTriggeredTasks = taskRepository.getTriggeredTasks(taskId).getOrNull() ?: emptyList()
        
        // Remove this task from tasks it no longer triggers
        val removedTriggered = currentTriggeredTasks.filter { !triggersTaskIds.contains(it.id) }
        removedTriggered.forEach { triggeredTask ->
            val updatedTriggeredBy = triggeredTask.triggeredByTaskIds?.filter { it != taskId } ?: emptyList()
            taskRepository.updateTask(triggeredTask.copy(triggeredByTaskIds = updatedTriggeredBy.takeIf { it.isNotEmpty() }))
        }
        
        // Add this task to new triggered tasks
        val newTriggered = triggersTaskIds.filter { triggeredId ->
            !currentTriggeredTasks.any { it.id == triggeredId }
        }
        newTriggered.forEach { triggeredId ->
            taskRepository.getTaskById(triggeredId).getOrNull()?.let { triggeredTask ->
                val updatedTriggeredBy = (triggeredTask.triggeredByTaskIds ?: emptyList()) + taskId
                taskRepository.updateTask(triggeredTask.copy(triggeredByTaskIds = updatedTriggeredBy))
            }
        }
    }
    
    /**
     * Save inventory associations
     */
    private suspend fun saveInventoryAssociations(
        taskId: String,
        associations: List<InventoryAssociationRequest>
    ) {
        // Get current associations
        val currentAssociations = supplyRepository.getTaskSuppliesForTask(taskId)
        
        // Remove associations no longer present
        val removedSupplyIds = currentAssociations
            .map { it.supplyId }
            .filter { supplyId -> !associations.any { it.supplyId == supplyId } }
        
        removedSupplyIds.forEach { supplyId ->
            currentAssociations.find { it.supplyId == supplyId }?.let { taskSupply ->
                supplyRepository.removeTaskSupply(taskSupply)
            }
        }
        
        // Insert or update associations
        associations.forEach { association ->
            val taskSupply = TaskSupply(
                taskId = taskId,
                supplyId = association.supplyId,
                consumptionMode = association.consumptionMode,
                fixedQuantity = association.fixedQuantity,
                promptedDefaultValue = association.promptedDefaultValue
            )
            supplyRepository.addTaskSupply(taskSupply)
        }
    }
}

/**
 * Request object for saving a task
 */
data class SaveTaskRequest(
    val taskId: String? = null, // null for create, ID for update
    
    // Basic info
    val name: String,
    val category: String,
    val tags: List<String>,
    val description: String,
    
    // Schedule
    val intervalUnit: com.lifeops.app.data.local.entity.IntervalUnit,
    val intervalQty: Int,
    val specificDaysOfWeek: List<com.lifeops.app.data.local.entity.DayOfWeek>,
    val excludedDaysOfWeek: List<com.lifeops.app.data.local.entity.DayOfWeek>,
    val overdueBehavior: com.lifeops.app.data.local.entity.OverdueBehavior,
    val deleteAfterCompletion: Boolean,
    val nextDue: java.time.LocalDate?,
    
    // Relationships
    val parentTaskId: String?,
    val childTaskIds: List<String>,
    val requiresManualCompletion: Boolean,
    val triggeredByTaskIds: List<String>,
    val triggersTaskIds: List<String>,
    
    // Inventory
    val inventoryAssociations: List<InventoryAssociationRequest>
)

/**
 * Inventory association request
 */
data class InventoryAssociationRequest(
    val supplyId: String,
    val consumptionMode: ConsumptionMode,
    val fixedQuantity: Int?,
    val promptedDefaultValue: Int?
)
