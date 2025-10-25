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
        
        // Sample data loading disabled - only clearing database
        // Uncomment below to re-enable sample data
        
        /*
        val today = LocalDate.now()
        
        // Pre-generate UUIDs for tasks that need relationships
        val morningWorkoutId = java.util.UUID.randomUUID().toString()
        val warmupStretchId = java.util.UUID.randomUUID().toString()
        val strengthTrainingId = java.util.UUID.randomUUID().toString()
        val cardioId = java.util.UUID.randomUUID().toString()
        val cooldownStretchId = java.util.UUID.randomUUID().toString()
        val cookDinnerId = java.util.UUID.randomUUID().toString()
        val cleanUpDinnerId = java.util.UUID.randomUUID().toString()
        
        // Create sample tasks with pre-assigned UUIDs
        val sampleTasks = listOf(
            // Fitness tasks - Parent/Child example
            Task(
                id = morningWorkoutId,
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
            // Child tasks for Morning Workout
            Task(
                id = warmupStretchId,
                name = "Warm-up Stretch",
                category = "Fitness",
                description = "5 minute stretching routine",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                difficulty = Difficulty.LOW,
                timeEstimate = 5,
                nextDue = today,
                active = true,
                childOrder = 1,
                parentTaskIds = listOf(morningWorkoutId)
            ),
            Task(
                id = strengthTrainingId,
                name = "Strength Training",
                category = "Fitness",
                description = "Weight lifting routine",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                difficulty = Difficulty.MEDIUM,
                timeEstimate = 30,
                nextDue = today,
                active = true,
                childOrder = 2,
                parentTaskIds = listOf(morningWorkoutId)
            ),
            Task(
                id = cardioId,
                name = "Cardio",
                category = "Fitness",
                description = "Running or cycling",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                difficulty = Difficulty.MEDIUM,
                timeEstimate = 20,
                nextDue = today,
                active = true,
                childOrder = 3,
                parentTaskIds = listOf(morningWorkoutId)
            ),
            Task(
                id = cooldownStretchId,
                name = "Cool-down Stretch",
                category = "Fitness",
                description = "Post-workout stretching",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                difficulty = Difficulty.LOW,
                timeEstimate = 5,
                nextDue = today,
                active = true,
                childOrder = 4,
                parentTaskIds = listOf(morningWorkoutId)
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
                id = cookDinnerId,
                name = "Cook Dinner",
                category = "Home",
                description = "Prepare evening meal",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                difficulty = Difficulty.MEDIUM,
                timeEstimate = 45,
                nextDue = today,
                active = true,
                triggersTaskIds = listOf(cleanUpDinnerId)
            ),
            Task(
                id = cleanUpDinnerId,
                name = "Clean Up After Dinner",
                category = "Home",
                description = "Wash dishes and clean kitchen",
                intervalUnit = IntervalUnit.ADHOC,
                intervalQty = 0,
                difficulty = Difficulty.LOW,
                timeEstimate = 20,
                nextDue = null, // ADHOC - only appears when triggered
                active = true,
                triggeredByTaskIds = listOf(cookDinnerId)
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
        
        // Insert all tasks (relationships are already set in the task objects)
        sampleTasks.forEach { task ->
            taskRepository.createTask(task)
        }
        
        // TODO: Add inventory-based task examples once Supply/Inventory entities are implemented
        // Examples to add:
        // - Task with FIXED consumption (e.g., "Brew Coffee" uses 2 filters)
        // - Task with PROMPTED consumption (e.g., "Cook Dinner" prompts for ingredients)
        // - Task with RECOUNT mode (e.g., "Deep Clean Bathroom" recounts cleaning supplies)
        */
    }
}
