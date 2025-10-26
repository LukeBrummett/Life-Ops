package com.lifeops.app.data.local

import com.lifeops.app.data.local.entity.ConsumptionMode
import com.lifeops.app.data.local.entity.DayOfWeek
import com.lifeops.app.data.local.entity.Difficulty
import com.lifeops.app.data.local.entity.Inventory
import com.lifeops.app.data.local.entity.IntervalUnit
import com.lifeops.app.data.local.entity.Supply
import com.lifeops.app.data.local.entity.Task
import com.lifeops.app.data.local.entity.TaskSupply
import com.lifeops.app.data.repository.SupplyRepository
import com.lifeops.app.data.repository.TaskRepository
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Database initialization helper
 * 
 * Provides realistic sample data matching the user workflows from Project Overview:
 * - Workflow 1: Exercise Routine with Parent-Child Tasks
 * - Workflow 2: Flexible Scheduling with Triggers and Conditions
 * - Workflow 3: Comprehensive Inventory Management
 */
@Singleton
class DatabaseInitializer @Inject constructor(
    private val database: LifeOpsDatabase,
    private val taskRepository: TaskRepository,
    private val supplyRepository: SupplyRepository
) {
    
    /**
     * Clear all data and prepopulate with comprehensive sample data
     * matching the three main workflows from Project Overview
     */
    suspend fun initializeWithSampleData() {
        // Clear all existing data
        database.clearAllTables()
        
        // Load comprehensive sample data
        val today = LocalDate.now()
        
        // Pre-generate UUIDs for tasks that need relationships
        val workoutParentId = java.util.UUID.randomUUID().toString()
        val stretchFirstId = java.util.UUID.randomUUID().toString()
        val pushUpsId = java.util.UUID.randomUUID().toString()
        val squatsId = java.util.UUID.randomUUID().toString()
        val planksId = java.util.UUID.randomUUID().toString()
        val stretchLastId = java.util.UUID.randomUUID().toString()
        
        val cookDinnerId = java.util.UUID.randomUUID().toString()
        val cleanUpDinnerId = java.util.UUID.randomUUID().toString()
        
        val cleanBathroomId = java.util.UUID.randomUUID().toString()
        val cleanShowerId = java.util.UUID.randomUUID().toString()
        val cleanToiletId = java.util.UUID.randomUUID().toString()
        val mopFloorId = java.util.UUID.randomUUID().toString()
        
        val swapAirFiltersId = java.util.UUID.randomUUID().toString()
        val monthlyCleaningId = java.util.UUID.randomUUID().toString()
        
        // ===========================================
        // WORKFLOW 1: Exercise Routine (Parent-Child)
        // ===========================================
        val workflowOneTasks = listOf(
            // Parent Task - Workout (Monday, Wednesday, Friday)
            Task(
                id = workoutParentId,
                name = "Workout",
                category = "Fitness",
                description = "Complete structured workout routine",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                specificDaysOfWeek = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                difficulty = Difficulty.MEDIUM,
                timeEstimate = 45,
                nextDue = getNextDayOfWeek(today, listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)),
                active = true,
                requiresManualCompletion = false // Auto-completes when all children done
            ),
            
            // Child 1: Stretch (first)
            Task(
                id = stretchFirstId,
                name = "Stretch",
                category = "Fitness",
                description = "Warm-up stretching routine",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                specificDaysOfWeek = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                difficulty = Difficulty.LOW,
                timeEstimate = 5,
                nextDue = getNextDayOfWeek(today, listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)),
                active = true,
                childOrder = 1,
                parentTaskIds = listOf(workoutParentId)
            ),
            
            // Child 2: Push-ups
            Task(
                id = pushUpsId,
                name = "Push-ups",
                category = "Fitness",
                description = "3 sets of push-ups",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                specificDaysOfWeek = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                difficulty = Difficulty.MEDIUM,
                timeEstimate = 10,
                nextDue = getNextDayOfWeek(today, listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)),
                active = true,
                childOrder = 2,
                parentTaskIds = listOf(workoutParentId)
            ),
            
            // Child 3: Squats
            Task(
                id = squatsId,
                name = "Squats",
                category = "Fitness",
                description = "3 sets of squats",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                specificDaysOfWeek = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                difficulty = Difficulty.MEDIUM,
                timeEstimate = 10,
                nextDue = getNextDayOfWeek(today, listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)),
                active = true,
                childOrder = 3,
                parentTaskIds = listOf(workoutParentId)
            ),
            
            // Child 4: Planks
            Task(
                id = planksId,
                name = "Planks",
                category = "Fitness",
                description = "Hold plank for 60 seconds x 3",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                specificDaysOfWeek = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                difficulty = Difficulty.MEDIUM,
                timeEstimate = 5,
                nextDue = getNextDayOfWeek(today, listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)),
                active = true,
                childOrder = 4,
                parentTaskIds = listOf(workoutParentId)
            ),
            
            // Child 5: Stretch (last)
            Task(
                id = stretchLastId,
                name = "Stretch",
                category = "Fitness",
                description = "Cool-down stretching routine",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                specificDaysOfWeek = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                difficulty = Difficulty.LOW,
                timeEstimate = 10,
                nextDue = getNextDayOfWeek(today, listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)),
                active = true,
                childOrder = 5,
                parentTaskIds = listOf(workoutParentId)
            )
        )
        
        // ===========================================
        // WORKFLOW 2: Flexible Scheduling
        // ===========================================
        val workflowTwoTasks = listOf(
            // Part A & B: Laundry (every 6 days, never on Tuesday/Thursday)
            Task(
                name = "Laundry",
                category = "Household",
                description = "Wash and dry clothes",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 6,
                excludedDaysOfWeek = listOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
                difficulty = Difficulty.MEDIUM,
                timeEstimate = 90,
                nextDue = findNextValidDate(today, 6, listOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)),
                active = true
            ),
            
            // Part C: Task Triggering (Cook Dinner triggers Clean Up)
            Task(
                id = cookDinnerId,
                name = "Cook Dinner",
                category = "Household",
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
                category = "Household",
                description = "Wash dishes and clean kitchen",
                intervalUnit = IntervalUnit.ADHOC,
                intervalQty = 0,
                difficulty = Difficulty.LOW,
                timeEstimate = 20,
                nextDue = null, // ADHOC - only appears when triggered
                active = true,
                triggeredByTaskIds = listOf(cookDinnerId)
            ),
            
            // Part D: Simple Recurring (Water Plants - every weekday)
            Task(
                name = "Water Plants",
                category = "Household",
                description = "Water indoor plants",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                specificDaysOfWeek = listOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY
                ),
                difficulty = Difficulty.LOW,
                timeEstimate = 10,
                nextDue = getNextWeekday(today),
                active = true
            )
        )
        
        // ===========================================
        // WORKFLOW 3: Comprehensive Inventory
        // ===========================================
        
        // First, create inventory supplies
        val supplies = createWorkflowThreeSupplies()
        supplies.forEach { supply ->
            supplyRepository.createSupply(supply.first, supply.second)
        }
        
        // Get supply IDs for task associations
        val airFilterSupplyId = supplies.find { it.first.name == "Air Filters" }!!.first.id
        val paperTowelSupplyId = supplies.find { it.first.name == "Paper Towels" }!!.first.id
        val toiletBowlCleanerSupplyId = supplies.find { it.first.name == "Toilet Bowl Cleaner" }!!.first.id
        val glassCleanerSupplyId = supplies.find { it.first.name == "Glass Cleaner" }!!.first.id
        val floorCleanerSupplyId = supplies.find { it.first.name == "Floor Cleaner" }!!.first.id
        
        val workflowThreeTasks = listOf(
            // Part A: Inventory Count Task (every Tuesday)
            Task(
                name = "Take Inventory Count",
                category = "Household",
                description = "Count all household supplies",
                intervalUnit = IntervalUnit.WEEK,
                intervalQty = 1,
                specificDaysOfWeek = listOf(DayOfWeek.TUESDAY),
                difficulty = Difficulty.MEDIUM,
                timeEstimate = 30,
                nextDue = getNextDayOfWeek(today, listOf(DayOfWeek.TUESDAY)),
                active = true
            ),
            
            // Part B: Task with PROMPTED consumption
            Task(
                id = swapAirFiltersId,
                name = "Swap Air Filters",
                category = "Household",
                description = "Replace HVAC air filters",
                intervalUnit = IntervalUnit.MONTH,
                intervalQty = 3,
                difficulty = Difficulty.MEDIUM,
                timeEstimate = 15,
                nextDue = today.plusMonths(3),
                active = true,
                requiresInventory = true
            ),
            
            // Part D: Task with RECOUNT mode (creates recount task)
            Task(
                id = monthlyCleaningId,
                name = "Monthly Deep Clean",
                category = "Household",
                description = "Thorough cleaning of entire house",
                intervalUnit = IntervalUnit.MONTH,
                intervalQty = 1,
                difficulty = Difficulty.HIGH,
                timeEstimate = 180,
                nextDue = today.plusMonths(1),
                active = true,
                requiresInventory = true
            ),
            
            // Bathroom cleaning with FIXED consumption
            Task(
                id = cleanBathroomId,
                name = "Clean Bathroom",
                category = "Household",
                description = "Complete bathroom cleaning routine",
                intervalUnit = IntervalUnit.WEEK,
                intervalQty = 1,
                difficulty = Difficulty.MEDIUM,
                timeEstimate = 45,
                nextDue = today.plusDays(3),
                active = true,
                requiresManualCompletion = false,
                requiresInventory = true
            ),
            Task(
                id = cleanShowerId,
                name = "Clean Shower",
                category = "Household",
                description = "Scrub shower walls and floor",
                intervalUnit = IntervalUnit.WEEK,
                intervalQty = 1,
                difficulty = Difficulty.MEDIUM,
                timeEstimate = 15,
                nextDue = today.plusDays(3),
                active = true,
                childOrder = 1,
                parentTaskIds = listOf(cleanBathroomId),
                requiresInventory = true
            ),
            Task(
                id = cleanToiletId,
                name = "Clean Toilet",
                category = "Household",
                description = "Clean and disinfect toilet",
                intervalUnit = IntervalUnit.WEEK,
                intervalQty = 1,
                difficulty = Difficulty.LOW,
                timeEstimate = 10,
                nextDue = today.plusDays(3),
                active = true,
                childOrder = 2,
                parentTaskIds = listOf(cleanBathroomId),
                requiresInventory = true
            ),
            Task(
                id = mopFloorId,
                name = "Mop Floor",
                category = "Household",
                description = "Mop bathroom floor",
                intervalUnit = IntervalUnit.WEEK,
                intervalQty = 1,
                difficulty = Difficulty.LOW,
                timeEstimate = 10,
                nextDue = today.plusDays(3),
                active = true,
                childOrder = 3,
                parentTaskIds = listOf(cleanBathroomId),
                requiresInventory = true
            )
        )
        
        // Additional realistic tasks
        val additionalTasks = listOf(
            Task(
                name = "Morning Meditation",
                category = "Personal",
                description = "10 minutes of mindfulness meditation",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                difficulty = Difficulty.LOW,
                timeEstimate = 10,
                nextDue = today,
                active = true
            ),
            Task(
                name = "Check Email",
                category = "Work",
                description = "Review and respond to emails",
                intervalUnit = IntervalUnit.DAY,
                intervalQty = 1,
                difficulty = Difficulty.LOW,
                timeEstimate = 20,
                nextDue = today,
                active = true
            ),
            Task(
                name = "Grocery Shopping",
                category = "Household",
                description = "Weekly grocery shopping trip",
                intervalUnit = IntervalUnit.WEEK,
                intervalQty = 1,
                specificDaysOfWeek = listOf(DayOfWeek.SATURDAY),
                difficulty = Difficulty.MEDIUM,
                timeEstimate = 60,
                nextDue = getNextDayOfWeek(today, listOf(DayOfWeek.SATURDAY)),
                active = true
            ),
            Task(
                name = "Take Out Trash",
                category = "Household",
                description = "Take trash to curb for pickup",
                intervalUnit = IntervalUnit.WEEK,
                intervalQty = 1,
                specificDaysOfWeek = listOf(DayOfWeek.WEDNESDAY),
                difficulty = Difficulty.LOW,
                timeEstimate = 5,
                nextDue = getNextDayOfWeek(today, listOf(DayOfWeek.WEDNESDAY)),
                active = true
            ),
            
            // Ephemeral tasks - these will auto-delete after completion
            Task(
                name = "Pick up dry cleaning",
                category = "Errands",
                description = "Pick up shirts from dry cleaner on Main St",
                intervalUnit = IntervalUnit.ADHOC,
                intervalQty = 0,
                difficulty = Difficulty.LOW,
                timeEstimate = 15,
                nextDue = today,
                active = true,
                deleteAfterCompletion = true
            ),
            Task(
                name = "Call dentist about appointment",
                category = "Personal",
                description = "Schedule 6-month checkup",
                intervalUnit = IntervalUnit.ADHOC,
                intervalQty = 0,
                difficulty = Difficulty.LOW,
                timeEstimate = 5,
                nextDue = today,
                active = true,
                deleteAfterCompletion = true
            )
        )
        
        // Insert all tasks
        val allTasks = workflowOneTasks + workflowTwoTasks + workflowThreeTasks + additionalTasks
        allTasks.forEach { task ->
            taskRepository.createTask(task)
        }
        
        // Create task-supply associations
        createTaskSupplyAssociations(
            airFilterSupplyId,
            paperTowelSupplyId,
            toiletBowlCleanerSupplyId,
            glassCleanerSupplyId,
            floorCleanerSupplyId,
            swapAirFiltersId,
            monthlyCleaningId,
            cleanShowerId,
            cleanToiletId,
            mopFloorId
        )
    }
    
    /**
     * Create supplies for Workflow 3 with realistic categories and quantities
     */
    private fun createWorkflowThreeSupplies(): List<Pair<Supply, Int>> {
        return listOf(
            // Household supplies
            Pair(
                Supply(
                    id = java.util.UUID.randomUUID().toString(),
                    name = "Air Filters",
                    category = "Household",
                    tags = "hvac, filters",
                    unit = "count",
                    reorderThreshold = 3,
                    reorderTargetQuantity = 10,
                    notes = "20x25x1 size"
                ),
                6 // current quantity
            ),
            Pair(
                Supply(
                    id = java.util.UUID.randomUUID().toString(),
                    name = "Trash Bags",
                    category = "Household",
                    tags = "bags, kitchen",
                    unit = "count",
                    reorderThreshold = 10,
                    reorderTargetQuantity = 30,
                    notes = "13 gallon kitchen bags"
                ),
                25 // current quantity
            ),
            
            // Kitchen supplies
            Pair(
                Supply(
                    id = java.util.UUID.randomUUID().toString(),
                    name = "Coffee Filters",
                    category = "Kitchen",
                    tags = "coffee, filters",
                    unit = "count",
                    reorderThreshold = 5,
                    reorderTargetQuantity = 20,
                    notes = "#4 cone filters"
                ),
                1 // current quantity - BELOW THRESHOLD
            ),
            Pair(
                Supply(
                    id = java.util.UUID.randomUUID().toString(),
                    name = "Dish Soap",
                    category = "Kitchen",
                    tags = "cleaning, dishes",
                    unit = "count",
                    reorderThreshold = 1,
                    reorderTargetQuantity = 3,
                    notes = "Dawn Ultra"
                ),
                2 // current quantity
            ),
            Pair(
                Supply(
                    id = java.util.UUID.randomUUID().toString(),
                    name = "Paper Towels",
                    category = "Kitchen",
                    tags = "cleaning, paper",
                    unit = "rolls",
                    reorderThreshold = 4,
                    reorderTargetQuantity = 12,
                    notes = "Bounty select-a-size"
                ),
                8 // current quantity
            ),
            
            // Cleaning supplies
            Pair(
                Supply(
                    id = java.util.UUID.randomUUID().toString(),
                    name = "All-Purpose Cleaner",
                    category = "Cleaning",
                    tags = "cleaning, spray",
                    unit = "bottles",
                    reorderThreshold = 1,
                    reorderTargetQuantity = 3,
                    notes = "Lysol multi-surface"
                ),
                2 // current quantity
            ),
            Pair(
                Supply(
                    id = java.util.UUID.randomUUID().toString(),
                    name = "Toilet Bowl Cleaner",
                    category = "Cleaning",
                    tags = "cleaning, bathroom",
                    unit = "bottles",
                    reorderThreshold = 1,
                    reorderTargetQuantity = 2,
                    notes = "Clorox toilet bowl cleaner"
                ),
                1 // current quantity
            ),
            Pair(
                Supply(
                    id = java.util.UUID.randomUUID().toString(),
                    name = "Glass Cleaner",
                    category = "Cleaning",
                    tags = "cleaning, windows",
                    unit = "bottles",
                    reorderThreshold = 1,
                    reorderTargetQuantity = 2,
                    notes = "Windex"
                ),
                1 // current quantity
            ),
            Pair(
                Supply(
                    id = java.util.UUID.randomUUID().toString(),
                    name = "Floor Cleaner",
                    category = "Cleaning",
                    tags = "cleaning, floors",
                    unit = "bottles",
                    reorderThreshold = 1,
                    reorderTargetQuantity = 2,
                    notes = "Swiffer WetJet solution"
                ),
                1 // current quantity
            ),
            Pair(
                Supply(
                    id = java.util.UUID.randomUUID().toString(),
                    name = "Sponges",
                    category = "Cleaning",
                    tags = "cleaning, kitchen",
                    unit = "count",
                    reorderThreshold = 3,
                    reorderTargetQuantity = 10,
                    notes = "Scrub Daddy"
                ),
                2 // current quantity - BELOW THRESHOLD
            )
        )
    }
    
    /**
     * Create task-supply associations for Workflow 3
     */
    private suspend fun createTaskSupplyAssociations(
        airFilterSupplyId: String,
        paperTowelSupplyId: String,
        toiletBowlCleanerSupplyId: String,
        glassCleanerSupplyId: String,
        floorCleanerSupplyId: String,
        swapAirFiltersId: String,
        monthlyCleaningId: String,
        cleanShowerId: String,
        cleanToiletId: String,
        mopFloorId: String
    ) {
        val associations = listOf(
            // Swap Air Filters - PROMPTED consumption (asks how many filters used)
            TaskSupply(
                taskId = swapAirFiltersId,
                supplyId = airFilterSupplyId,
                consumptionMode = ConsumptionMode.PROMPTED,
                promptedDefaultValue = 2
            ),
            
            // Monthly Deep Clean - RECOUNT mode (creates restock task)
            TaskSupply(
                taskId = monthlyCleaningId,
                supplyId = paperTowelSupplyId,
                consumptionMode = ConsumptionMode.RECOUNT
            ),
            TaskSupply(
                taskId = monthlyCleaningId,
                supplyId = toiletBowlCleanerSupplyId,
                consumptionMode = ConsumptionMode.RECOUNT
            ),
            TaskSupply(
                taskId = monthlyCleaningId,
                supplyId = glassCleanerSupplyId,
                consumptionMode = ConsumptionMode.RECOUNT
            ),
            TaskSupply(
                taskId = monthlyCleaningId,
                supplyId = floorCleanerSupplyId,
                consumptionMode = ConsumptionMode.RECOUNT
            ),
            
            // Clean Shower - FIXED consumption
            TaskSupply(
                taskId = cleanShowerId,
                supplyId = glassCleanerSupplyId,
                consumptionMode = ConsumptionMode.FIXED,
                fixedQuantity = 1
            ),
            
            // Clean Toilet - FIXED consumption
            TaskSupply(
                taskId = cleanToiletId,
                supplyId = toiletBowlCleanerSupplyId,
                consumptionMode = ConsumptionMode.FIXED,
                fixedQuantity = 1
            ),
            
            // Mop Floor - FIXED consumption
            TaskSupply(
                taskId = mopFloorId,
                supplyId = floorCleanerSupplyId,
                consumptionMode = ConsumptionMode.FIXED,
                fixedQuantity = 1
            )
        )
        
        associations.forEach { association ->
            supplyRepository.addTaskSupply(association)
        }
    }
    
    /**
     * Helper: Get next occurrence of specific days of week
     */
    private fun getNextDayOfWeek(from: LocalDate, daysOfWeek: List<DayOfWeek>): LocalDate {
        var date = from
        while (!daysOfWeek.contains(date.dayOfWeek.toCustomDayOfWeek())) {
            date = date.plusDays(1)
        }
        return date
    }
    
    /**
     * Helper: Get next weekday (Monday-Friday)
     */
    private fun getNextWeekday(from: LocalDate): LocalDate {
        val weekdays = listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        )
        return getNextDayOfWeek(from, weekdays)
    }
    
    /**
     * Helper: Find next valid date considering interval and excluded days
     */
    private fun findNextValidDate(from: LocalDate, intervalDays: Int, excludedDays: List<DayOfWeek>): LocalDate {
        var date = from.plusDays(intervalDays.toLong())
        while (excludedDays.contains(date.dayOfWeek.toCustomDayOfWeek())) {
            date = date.plusDays(1)
        }
        return date
    }
    
    /**
     * Helper: Convert java.time.DayOfWeek to custom DayOfWeek enum
     */
    private fun java.time.DayOfWeek.toCustomDayOfWeek(): DayOfWeek {
        return when (this) {
            java.time.DayOfWeek.MONDAY -> DayOfWeek.MONDAY
            java.time.DayOfWeek.TUESDAY -> DayOfWeek.TUESDAY
            java.time.DayOfWeek.WEDNESDAY -> DayOfWeek.WEDNESDAY
            java.time.DayOfWeek.THURSDAY -> DayOfWeek.THURSDAY
            java.time.DayOfWeek.FRIDAY -> DayOfWeek.FRIDAY
            java.time.DayOfWeek.SATURDAY -> DayOfWeek.SATURDAY
            java.time.DayOfWeek.SUNDAY -> DayOfWeek.SUNDAY
        }
    }
}
