# Triggered Tasks with Parent Tasks

## Overview

This feature allows triggered tasks (ADHOC tasks that only appear when explicitly triggered) to be grouped under a parent task in the Today view, providing better organization while maintaining trigger-only behavior.

## Use Case

The primary use case is for task workflows where completing one task should trigger related follow-up tasks, and all tasks should be visually grouped together.

**Example: Clean Kitchen Workflow**
1. Parent task: "Clean Kitchen" (scheduled for today)
2. Child task: "Load Dishwasher" (scheduled for today, part of Clean Kitchen)
3. When "Load Dishwasher" completes, it triggers:
   - "Unload Dishwasher" (ADHOC, shows under Clean Kitchen when triggered)
   - "Clean Sink" (ADHOC, shows under Clean Kitchen when triggered)

## How It Works

### Task Configuration

**Parent Task (Clean Kitchen):**
```kotlin
Task(
    name = "Clean Kitchen",
    category = "Household",
    intervalUnit = IntervalUnit.DAY,
    intervalQty = 1,
    nextDue = today,
    requiresManualCompletion = false
)
```

**Triggering Child Task (Load Dishwasher):**
```kotlin
Task(
    name = "Load Dishwasher",
    category = "Household",
    intervalUnit = IntervalUnit.DAY,
    intervalQty = 1,
    nextDue = today,
    parentTaskIds = listOf(cleanKitchenId),
    childOrder = 1,
    triggersTaskIds = listOf(unloadDishwasherId, cleanSinkId)
)
```

**Triggered Child Tasks (Unload Dishwasher, Clean Sink):**
```kotlin
Task(
    name = "Unload Dishwasher",
    category = "Household",
    intervalUnit = IntervalUnit.ADHOC,  // Only appears when triggered
    intervalQty = 0,
    nextDue = null,  // Will be set when triggered
    parentTaskIds = listOf(cleanKitchenId),  // Groups under Clean Kitchen
    childOrder = 2,
    triggeredByTaskIds = listOf(loadDishwasherId)
)
```

### Behavior

1. **Initial State (Before Triggering):**
   - Today view shows: "Clean Kitchen" with child "Load Dishwasher"
   - "Unload Dishwasher" and "Clean Sink" do NOT appear (nextDue is null)

2. **After Completing "Load Dishwasher":**
   - `CompleteTaskUseCase` triggers the related tasks
   - Sets `nextDue = today` for "Unload Dishwasher" and "Clean Sink"
   - Today view now shows: "Clean Kitchen" with children:
     - "Load Dishwasher" (completed)
     - "Unload Dishwasher" (newly triggered)
     - "Clean Sink" (newly triggered)

3. **Key Points:**
   - Triggered tasks ONLY appear when their `nextDue` is set (via trigger)
   - They do NOT automatically appear just because their parent is due
   - They ARE grouped under their parent in the UI when both are due today
   - They maintain ADHOC scheduling (no automatic recurrence)

## Technical Implementation

### Database Query
The `observeTasksDueByDate` query returns tasks where:
```sql
nextDue <= :date OR lastCompleted = :date
```

This means:
- ADHOC tasks with `nextDue = null` do NOT appear
- ADHOC tasks with `nextDue = today` (after being triggered) DO appear

### UI Grouping
The `groupTasksWithHierarchy` method in `TodayViewModel`:
1. Takes all tasks returned by the query
2. Groups tasks by their `parentTaskIds`
3. Sorts children by `childOrder`
4. Returns a hierarchical structure for UI rendering

### Completion Flow
When completing a task with `triggersTaskIds`:
1. `CompleteTaskUseCase.invoke()` is called
2. Updates the completed task's `lastCompleted` and `nextDue`
3. Calls `triggerTasks()` which sets `nextDue = today` for triggered tasks
4. Database triggers a Flow update
5. `TodayViewModel` receives updated task list
6. UI re-renders with newly triggered tasks under their parent

## UI Setup

### Creating a Triggered Task with Parent

1. Create or edit the task you want to be triggered
2. Set **Interval** to "ADHOC" (trigger-only, no automatic schedule)
3. Set **Parent Task** to the desired parent (e.g., "Clean Kitchen")
4. Set **Child Order** to control position within the parent's children
5. In the triggering task, add this task to the **Triggers** list

### Example UI Flow

1. Go to "All Tasks" screen
2. Create parent task "Clean Kitchen" with daily schedule
3. Create child task "Load Dishwasher" with daily schedule and parent "Clean Kitchen"
4. Create triggered task "Unload Dishwasher":
   - Interval: ADHOC
   - Parent: Clean Kitchen
   - Triggered By: Load Dishwasher
5. In "Load Dishwasher" task, add "Unload Dishwasher" to Triggers list

## Benefits

1. **Organization**: Related tasks are visually grouped together
2. **Flexibility**: Triggered tasks only appear when needed, not on every occurrence of the parent
3. **Workflow Support**: Supports complex task dependencies and sequences
4. **No Duplication**: Tasks appear once in the hierarchy, not scattered across categories

## Limitations

1. **Manual Setup**: Requires explicit configuration of parent-child relationships
2. **Single Parent**: While tasks can technically have multiple parents, UI works best with single parent
3. **Same Category**: For best UI experience, parent and children should be in the same category

## Related Features

- **Parent-Child Tasks**: Standard hierarchical task organization
- **Task Triggering**: One task can trigger multiple other tasks on completion
- **ADHOC Scheduling**: Tasks with no automatic schedule, only triggered manually or by other tasks
- **Auto-Completion**: Parent tasks can auto-complete when all children are done
