package com.lifeops.app.presentation.today

import com.lifeops.app.data.local.entity.Difficulty
import com.lifeops.app.data.local.entity.IntervalUnit
import com.lifeops.app.data.local.entity.Task
import java.time.LocalDate

/**
 * Mock data for Today Screen previews and testing
 */
object MockData {
    
    val today: LocalDate = LocalDate.now()
    val yesterday: LocalDate = today.minusDays(1)
    
    // Fitness Tasks
    val morningWorkout = Task(
        id = 1,
        name = "Morning Workout",
        category = "Fitness",
        description = "Complete full body workout routine",
        intervalUnit = IntervalUnit.WEEK,
        intervalQty = 1,
        nextDue = today,
        lastCompleted = null,
        timeEstimate = 30,
        difficulty = Difficulty.MEDIUM,
        active = true
    )
    
    val stretch = Task(
        id = 2,
        name = "Stretch",
        category = "Fitness",
        description = "Morning stretching routine",
        intervalUnit = IntervalUnit.DAY,
        intervalQty = 1,
        nextDue = today,
        lastCompleted = today,
        timeEstimate = 10,
        difficulty = Difficulty.LOW,
        active = true,
        completionStreak = 5
    )
    
    val walk = Task(
        id = 3,
        name = "Walk",
        category = "Fitness",
        description = "30 minute walk around the neighborhood",
        intervalUnit = IntervalUnit.DAY,
        intervalQty = 1,
        nextDue = today,
        lastCompleted = today,
        timeEstimate = 20,
        difficulty = Difficulty.LOW,
        active = true,
        completionStreak = 3
    )
    
    // Home Tasks
    val waterPlants = Task(
        id = 4,
        name = "Water Plants",
        category = "Home",
        description = "Water all indoor plants",
        intervalUnit = IntervalUnit.DAY,
        intervalQty = 3,
        nextDue = today,
        lastCompleted = null,
        timeEstimate = 15,
        difficulty = Difficulty.LOW,
        active = true
    )
    
    val checkMail = Task(
        id = 5,
        name = "Check Mail",
        category = "Home",
        description = "Check and sort mail",
        intervalUnit = IntervalUnit.DAY,
        intervalQty = 1,
        nextDue = today,
        lastCompleted = null,
        timeEstimate = 5,
        difficulty = Difficulty.LOW,
        active = true
    )
    
    val laundry = Task(
        id = 6,
        name = "Do Laundry",
        category = "Home",
        description = "Wash, dry, and fold clothes",
        intervalUnit = IntervalUnit.WEEK,
        intervalQty = 1,
        nextDue = today,
        lastCompleted = null,
        timeEstimate = 60,
        difficulty = Difficulty.MEDIUM,
        active = true
    )
    
    // Work Tasks
    val dailyStandup = Task(
        id = 7,
        name = "Daily Standup",
        category = "Work",
        description = "Team standup meeting",
        intervalUnit = IntervalUnit.DAY,
        intervalQty = 1,
        nextDue = today,
        lastCompleted = today,
        timeEstimate = 15,
        difficulty = Difficulty.LOW,
        active = true,
        completionStreak = 12
    )
    
    val reviewPRs = Task(
        id = 8,
        name = "Review Pull Requests",
        category = "Work",
        description = "Review and approve pending PRs",
        intervalUnit = IntervalUnit.DAY,
        intervalQty = 1,
        nextDue = today,
        lastCompleted = null,
        difficulty = Difficulty.MEDIUM,
        active = true
    )
    
    // Personal Tasks
    val readBook = Task(
        id = 9,
        name = "Read Book",
        category = "Personal",
        description = "Read for 30 minutes",
        intervalUnit = IntervalUnit.DAY,
        intervalQty = 1,
        nextDue = today,
        lastCompleted = null,
        timeEstimate = 30,
        difficulty = Difficulty.LOW,
        active = true
    )
    
    val meditation = Task(
        id = 10,
        name = "Meditation",
        category = "Personal",
        description = "Morning meditation",
        intervalUnit = IntervalUnit.DAY,
        intervalQty = 1,
        nextDue = today,
        lastCompleted = today,
        timeEstimate = 10,
        difficulty = Difficulty.LOW,
        active = true,
        completionStreak = 7
    )
    
    // Parent/Child example
    val parentTask = Task(
        id = 11,
        name = "Complete Workout Routine",
        category = "Fitness",
        description = "Full workout with warm-up, exercise, and cool-down",
        intervalUnit = IntervalUnit.DAY,
        intervalQty = 1,
        nextDue = today,
        lastCompleted = null,
        timeEstimate = 60,
        difficulty = Difficulty.MEDIUM,
        active = true,
        requiresManualCompletion = false
    )
    
    val childTasks = listOf(
        Task(
            id = 12,
            name = "Warm-up Stretch",
            category = "Fitness",
            description = "5 minute stretching",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = today,
            lastCompleted = null,
            timeEstimate = 5,
            difficulty = Difficulty.LOW,
            active = true,
            parentTaskIds = listOf(11),
            childOrder = 1
        ),
        Task(
            id = 13,
            name = "Strength Training",
            category = "Fitness",
            description = "Weight lifting",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = today,
            lastCompleted = null,
            timeEstimate = 30,
            difficulty = Difficulty.MEDIUM,
            active = true,
            parentTaskIds = listOf(11),
            childOrder = 2
        ),
        Task(
            id = 14,
            name = "Cool-down Stretch",
            category = "Fitness",
            description = "Post-workout stretching",
            intervalUnit = IntervalUnit.DAY,
            intervalQty = 1,
            nextDue = today,
            lastCompleted = null,
            timeEstimate = 5,
            difficulty = Difficulty.LOW,
            active = true,
            parentTaskIds = listOf(11),
            childOrder = 3
        )
    )
    
    // All tasks by category
    val tasksByCategory: Map<String, List<TaskItem>> = mapOf(
        "Fitness" to listOf(
            TaskItem(morningWorkout),
            TaskItem(stretch),
            TaskItem(walk),
            TaskItem(parentTask, childTasks, true)
        ),
        "Home" to listOf(
            TaskItem(waterPlants),
            TaskItem(checkMail),
            TaskItem(laundry)
        ),
        "Work" to listOf(
            TaskItem(dailyStandup),
            TaskItem(reviewPRs)
        ),
        "Personal" to listOf(
            TaskItem(readBook),
            TaskItem(meditation)
        )
    )
    
    // Tasks with all incomplete (for testing)
    val allIncompleteTasks = listOf(
        morningWorkout,
        waterPlants,
        checkMail,
        laundry,
        reviewPRs,
        readBook
    )
    
    // Tasks with all complete (for testing)
    val allCompleteTasks = listOf(
        stretch.copy(lastCompleted = today),
        walk.copy(lastCompleted = today),
        dailyStandup.copy(lastCompleted = today),
        meditation.copy(lastCompleted = today)
    )
    
    // Empty task list (for testing)
    val emptyTasks = emptyList<Task>()
}
