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
     */
    suspend fun initializeWithSampleData() {
        // Clear all existing data
        database.clearAllTables()
        
        val today = LocalDate.now()
        
        // Create sample tasks
        val sampleTasks = listOf(
            // Fitness tasks
            Task(
                name = "Morning Workout",
                category = "Fitness",
                description = "30 minute workout routine",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                difficulty = Difficulty.MEDIUM,
                timeEstimate = 30,
                nextDue = today,
                active = true
            ),
            Task(
                name = "Stretch",
                category = "Fitness",
                description = "Morning stretching routine",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                difficulty = Difficulty.LOW,
                timeEstimate = 10,
                nextDue = today,
                active = true
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
            
            // Home tasks
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
        
        // Insert all tasks
        sampleTasks.forEach { task ->
            taskRepository.createTask(task)
        }
    }
}
