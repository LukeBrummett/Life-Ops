package com.lifeops.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.lifeops.app.data.local.converter.Converters
import java.time.LocalDate
import java.util.UUID

/**
 * Task entity representing a unit of work with scheduling, relationships, and inventory configuration.
 * 
 * As per Data Model specification:
 * - Tasks can have multiple parents (via parentTaskIds JSON array)
 * - Tasks can be triggered by multiple tasks and trigger multiple tasks
 * - Tasks can have their own schedule OR be ADHOC (trigger-only)
 * - Tasks can consume inventory items with different modes (FIXED, PROMPTED, RECOUNT)
 * 
 * Uses UUID for globally unique task identification to support:
 * - Import/export between different databases
 * - Sharing task lists between users
 * - Conflict-free merging of task collections
 */
@Entity(tableName = "tasks")
@TypeConverters(Converters::class)
data class Task(
    // ============================================
    // Identity & Basic Info
    // ============================================
    
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val name: String,
    
    val category: String,
    
    val tags: String = "", // Comma-separated searchable labels
    
    val description: String = "", // Additional context or instructions
    
    val active: Boolean = true, // Whether task is active or archived
    
    // ============================================
    // Scheduling
    // ============================================
    
    /**
     * Interval unit: "DAY" | "WEEK" | "MONTH" | "ADHOC"
     * ADHOC means no automatic scheduling (trigger-only)
     */
    val intervalUnit: IntervalUnit = IntervalUnit.DAY,
    
    /**
     * Number of units between occurrences (0 for ADHOC)
     */
    val intervalQty: Int = 1,
    
    /**
     * JSON array of days of week for specific-day scheduling
     * e.g., ["MONDAY", "WEDNESDAY", "FRIDAY"]
     * Null or empty if not using specific days
     */
    val specificDaysOfWeek: List<DayOfWeek>? = null,
    
    /**
     * JSON array of excluded dates/date ranges
     * Tasks will never be scheduled on these dates
     * Past dates can be cleaned up periodically or left
     */
    val excludedDates: List<LocalDate>? = null,
    
    /**
     * JSON array of days to never schedule
     * e.g., ["TUESDAY", "THURSDAY"]
     */
    val excludedDaysOfWeek: List<DayOfWeek>? = null,
    
    /**
     * How task behaves when day advances without completion
     * POSTPONE: Slides forward day-by-day (default)
     * SKIP_TO_NEXT: Automatically advances to next scheduled occurrence
     */
    val overdueBehavior: OverdueBehavior = OverdueBehavior.POSTPONE,
    
    /**
     * Whether task should be automatically deleted after completion when day advances
     * Used for one-time, ephemeral tasks that should not persist after being done
     * Deletion happens during end-of-day processing, not immediately on completion
     * Default: false (task persists and follows normal recurrence schedule)
     */
    val deleteAfterCompletion: Boolean = false,
    
    /**
     * Next scheduled date
     * Null for unscheduled ADHOC tasks
     */
    val nextDue: LocalDate? = null,
    
    /**
     * Last completion timestamp
     */
    val lastCompleted: LocalDate? = null,
    
    // ============================================
    // Time Estimation
    // ============================================
    
    /**
     * Minutes (integer) or null
     */
    val timeEstimate: Int? = null,
    
    /**
     * Difficulty indicator: "LOW" | "MEDIUM" | "HIGH" or null
     */
    val difficulty: Difficulty? = null,
    
    // ============================================
    // Relationships
    // ============================================
    
    /**
     * JSON array of parent task IDs
     * Tasks can have multiple parents for UI grouping purposes
     * 
     * Parent-child relationship behavior:
     * - Child tasks are grouped under their parent in the UI
     * - Child tasks still need their own schedule (nextDue) to appear in today's list
     * - ADHOC child tasks (e.g., triggered tasks) only appear when explicitly triggered
     * - Child tasks do NOT automatically inherit their parent's schedule by default
     * 
     * This allows triggered tasks to have a parent for organization while remaining
     * trigger-only (ADHOC) - they appear under the parent when both are due today.
     */
    val parentTaskIds: List<String>? = null,
    
    /**
     * Whether this task should inherit its schedule from its parent task(s)
     * When true, the task appears whenever its parent is due (or when triggered)
     * When false, the task follows its own schedule independently
     * Only relevant for tasks with parents (parentTaskIds not empty)
     * Default: false (tasks maintain independent schedules)
     */
    val inheritParentSchedule: Boolean = false,
    
    /**
     * Whether parent needs manual check-off after all children complete
     * Only relevant for parent tasks
     * If false, parent auto-completes when last child finishes
     */
    val requiresManualCompletion: Boolean = false,
    
    /**
     * Order within parent task
     * Null if not a child task
     * Note: With multiple parents, order may be context-dependent
     */
    val childOrder: Int? = null,
    
    // ============================================
    // Triggers
    // ============================================
    
    /**
     * JSON array of task IDs that trigger this task
     * This task appears when any of these tasks complete
     */
    val triggeredByTaskIds: List<String>? = null,
    
    /**
     * JSON array of task IDs that this task triggers on completion
     */
    val triggersTaskIds: List<String>? = null,
    
    // ============================================
    // Inventory
    // ============================================
    
    /**
     * Whether task consumes inventory items
     * Actual associations stored in TaskSupply junction table
     */
    val requiresInventory: Boolean = false,
    
    // ============================================
    // State Tracking
    // ============================================
    
    /**
     * Current consecutive completion count
     */
    val completionStreak: Int = 0
)

/**
 * Interval unit enumeration
 */
enum class IntervalUnit {
    DAY,
    WEEK,
    MONTH,
    ADHOC  // No automatic scheduling, trigger-only
}

/**
 * Day of week enumeration
 */
enum class DayOfWeek {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY
}

/**
 * Difficulty level enumeration
 */
enum class Difficulty {
    LOW,
    MEDIUM,
    HIGH
}

/**
 * Overdue behavior enumeration
 * Determines what happens when a day passes without completing the task
 */
enum class OverdueBehavior {
    /**
     * Task slides forward day-by-day, remaining in Today view until completed
     * Best for: Daily habits, important recurring tasks, flexible deadlines
     */
    POSTPONE,
    
    /**
     * Task automatically advances to next scheduled occurrence when day changes
     * Best for: Scheduled appointments, optional activities, date-specific tasks
     */
    SKIP_TO_NEXT
}
