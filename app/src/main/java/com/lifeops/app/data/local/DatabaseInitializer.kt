package com.lifeops.app.data.local

import com.lifeops.app.data.local.entity.Difficulty
import com.lifeops.app.data.local.entity.IntervalUnit
import com.lifeops.app.data.local.entity.Task
import com.lifeops.app.data.repository.TaskRepository
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Database initialization helper
 * 
 * Provides methods to prepopulate the database with sample tasks
 * for testing and development purposes.
 */
@Singleton
class DatabaseInitializer @Inject constructor(
    private val database: LifeOpsDatabase,
    private val taskRepository: TaskRepository
) {
    
    /**
     * Clear all data and prepopulate with sample tasks
     * TEMPORARILY DISABLED: Only clears data, does not load sample tasks
     */
    suspend fun initializeWithSampleData() {
        // Clear all existing data
        database.clearAllTables()
        
        // TODO: Re-enable sample data loading when needed
        // Sample task creation temporarily disabled for import/export testing
        
        /* COMMENTED OUT - SAMPLE DATA LOADING
        val today = LocalDate.now()
        
        // Track task IDs for parent/child and trigger relationships
        val taskIds = mutableMapOf<String, Long>()
        
        // Create sample tasks
        val sampleTasks = listOf(
            // Fitness tasks - Parent/Child example
            Task(
                name = "Morning Workout",
                category = "Fitness",
                description = "Complete workout routine with stretching",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                difficulty = Difficulty.MEDIUM,
                timeEstimate = 60,
                nextDue = today,
                active = true,
                requiresManualCompletion = false // Auto-completes when all children done
            ),
            // Child tasks for Morning Workout (will be linked after insertion)
            Task(
                name = "Warm-up Stretch",
                category = "Fitness",
                description = "5 minute stretching routine",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                difficulty = Difficulty.LOW,
                timeEstimate = 5,
                nextDue = today,
                active = true,
                childOrder = 1
            ),
            Task(
                name = "Strength Training",
                category = "Fitness",
                description = "Weight lifting routine",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                difficulty = Difficulty.MEDIUM,
                timeEstimate = 30,
                nextDue = today,
                active = true,
                childOrder = 2
            ),
            Task(
                name = "Cardio",
                category = "Fitness",
                description = "Running or cycling",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                difficulty = Difficulty.MEDIUM,
                timeEstimate = 20,
                nextDue = today,
                active = true,
                childOrder = 3
            ),
            Task(
                name = "Cool-down Stretch",
                category = "Fitness",
                description = "Post-workout stretching",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                difficulty = Difficulty.LOW,
                timeEstimate = 5,
                nextDue = today,
                active = true,
                childOrder = 4
            ),
            Task(
                name = "Evening Walk",
                category = "Fitness",
                description = "Walk around the neighborhood",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                difficulty = Difficulty.LOW,
                timeEstimate = 20,
                nextDue = today,
                active = true
            ),
            
            // Home tasks - Trigger example (Cook Dinner -> Clean Up)
            Task(
                name = "Cook Dinner",
                category = "Home",
                description = "Prepare evening meal",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                difficulty = Difficulty.MEDIUM,
                timeEstimate = 45,
                nextDue = today,
                active = true
            ),
            Task(
                name = "Clean Up After Dinner",
                category = "Home",
                description = "Wash dishes and clean kitchen",
                intervalUnit = IntervalUnit.ADHOC,
                intervalQty = 0,
                difficulty = Difficulty.LOW,
                timeEstimate = 20,
                nextDue = null, // ADHOC - only appears when triggered
                active = true
            ),
            Task(
                name = "Water Plants",
                category = "Home",
                description = "Water indoor plants",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 3,
                difficulty = Difficulty.LOW,
                nextDue = today,
                active = true
            ),
            Task(
                name = "Check Mail",
                category = "Home",
                description = "Check mailbox",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                difficulty = Difficulty.LOW,
                nextDue = today,
                active = true
            ),
            Task(
                name = "Laundry",
                category = "Home",
                description = "Wash clothes",
                intervalUnit = IntervalUnit.WEEK,
                intervalQty = 1,
                difficulty = Difficulty.MEDIUM,
                timeEstimate = 60,
                nextDue = today,
                active = true
            ),
            Task(
                name = "Vacuum Living Room",
                category = "Home",
                description = "Vacuum main living areas",
                intervalUnit = IntervalUnit.WEEK,
                intervalQty = 1,
                difficulty = Difficulty.MEDIUM,
                timeEstimate = 20,
                nextDue = today.plusDays(2),
                active = true
            ),
            
            // Work tasks
            Task(
                name = "Daily Standup",
                category = "Work",
                description = "Team standup meeting",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                difficulty = Difficulty.LOW,
                timeEstimate = 15,
                nextDue = today,
                active = true
            ),
            Task(
                name = "Review PRs",
                category = "Work",
                description = "Review open pull requests",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                difficulty = Difficulty.MEDIUM,
                timeEstimate = 30,
                nextDue = today,
                active = true
            ),
            
            // Personal tasks
            Task(
                name = "Read for 30 Minutes",
                category = "Personal",
                description = "Read current book",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                difficulty = Difficulty.MEDIUM,
                timeEstimate = 30,
                nextDue = today,
                active = true
            ),
            Task(
                name = "Journal",
                category = "Personal",
                description = "Daily journaling",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                difficulty = Difficulty.LOW,
                timeEstimate = 15,
                nextDue = today,
                active = true
            ),
            Task(
                name = "Call Family",
                category = "Personal",
                description = "Check in with family",
                intervalUnit = IntervalUnit.WEEK,
                intervalQty = 1,
                difficulty = Difficulty.LOW,
                timeEstimate = 30,
                nextDue = today.plusDays(1),
                active = true
            ),
            
            // Health tasks
            Task(
                name = "Take Vitamins",
                category = "Health",
                description = "Daily vitamins",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                difficulty = Difficulty.LOW,
                nextDue = today,
                active = true
            ),
            Task(
                name = "Drink 8 Glasses of Water",
                category = "Health",
                description = "Stay hydrated throughout the day",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                difficulty = Difficulty.LOW,
                nextDue = today,
                active = true
            )
        )
        
        // Insert all tasks and store their IDs
        sampleTasks.forEach { task ->
            val taskId = taskRepository.createTask(task).getOrThrow()
            
            // Map task names to IDs for relationship setup
            when (task.name) {
                "Morning Workout" -> taskIds["morningWorkout"] = taskId
                "Warm-up Stretch" -> taskIds["warmupStretch"] = taskId
                "Strength Training" -> taskIds["strengthTraining"] = taskId
                "Cardio" -> taskIds["cardio"] = taskId
                "Cool-down Stretch" -> taskIds["cooldownStretch"] = taskId
                "Cook Dinner" -> taskIds["cookDinner"] = taskId
                "Clean Up After Dinner" -> taskIds["cleanUpDinner"] = taskId
            }
        }
        
        // Set up parent-child relationships
        // Morning Workout (parent) -> Warm-up, Strength, Cardio, Cool-down (children)
        val morningWorkoutId = taskIds["morningWorkout"]!!
        taskRepository.updateTask(
            taskRepository.getTaskById(taskIds["warmupStretch"]!!).getOrThrow()!!.copy(
                parentTaskIds = listOf(morningWorkoutId)
            )
        )
        taskRepository.updateTask(
            taskRepository.getTaskById(taskIds["strengthTraining"]!!).getOrThrow()!!.copy(
                parentTaskIds = listOf(morningWorkoutId)
            )
        )
        taskRepository.updateTask(
            taskRepository.getTaskById(taskIds["cardio"]!!).getOrThrow()!!.copy(
                parentTaskIds = listOf(morningWorkoutId)
            )
        )
        taskRepository.updateTask(
            taskRepository.getTaskById(taskIds["cooldownStretch"]!!).getOrThrow()!!.copy(
                parentTaskIds = listOf(morningWorkoutId)
            )
        )
        
        // Set up trigger relationship
        // Cook Dinner (trigger) -> Clean Up After Dinner (triggered)
        val cookDinnerId = taskIds["cookDinner"]!!
        val cleanUpDinnerId = taskIds["cleanUpDinner"]!!
        
        taskRepository.updateTask(
            taskRepository.getTaskById(cookDinnerId).getOrThrow()!!.copy(
                triggersTaskIds = listOf(cleanUpDinnerId)
            )
        )
        taskRepository.updateTask(
            taskRepository.getTaskById(cleanUpDinnerId).getOrThrow()!!.copy(
                triggeredByTaskIds = listOf(cookDinnerId)
            )
        )
        
        // TODO: Add inventory-based task examples once Supply/Inventory entities are implemented
        // Examples to add:
        // - Task with FIXED consumption (e.g., "Brew Coffee" uses 2 filters)
        // - Task with PROMPTED consumption (e.g., "Cook Dinner" prompts for ingredients)
        // - Task with RECOUNT mode (e.g., "Deep Clean Bathroom" recounts cleaning supplies)
        */
    }
}
