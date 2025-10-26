# Life-Ops Project Overview

## Document Purpose

This document serves as the technical specification and design reference for Life-Ops. It defines expected behaviors, user workflows, and system architecture to ensure the application functions as a reliable cognitive offload tool.

---

## Application Summary

**What it is:**
A personal offline Android application for automating daily task management across all life domains (home, work, personal) through configurable recurring tasks, triggers, and inventory tracking.

**Problem it solves:**
Eliminates the mental overhead of remembering and tracking an overwhelming number of daily tasks. With proper configuration, the app automates what needs attention each day, allowing users to focus on completing visible work and enjoying free time with confidence.

**Core philosophy:**
- **Next-occurrence timeline**: Tasks appear only once in the timeline at their next scheduled date; future views show when tasks will next occur, never duplicate instances
- **Slide-forward persistence**: Missed tasks can either postpone to tomorrow or skip to their next scheduled occurrence based on task configuration; tasks without schedules disappear when skipped
- **Configurable overdue behavior**: Each task can be configured to either "postpone" (slide forward day-by-day) or "skip" (jump to next scheduled occurrence) when not completed
- **Offline-only**: Runs entirely locally with zero network dependencies or sync capabilities
- **Deterministic scheduling**: Tasks appear based on explicit rules and configurations, never algorithmic guessing
- **Configuration over motivation**: Set up task logic once, execute without thinking

---

# User Workflows

## Workflow 1: Exercise Routine with Parent-Child Tasks

**User Goal:** Track completion of structured workout routine with multiple steps

**Scenario:**
John wants to ensure he's getting enough exercise and puts his routine into the application. He sets up a series of tasks that occur every Monday, Wednesday, and Friday in a group.

**Steps:**
1. John opens the app Monday morning
2. He sees his workout routine with a parent task called "Workout"
3. Under the parent, he sees individual tasks in his specified order:
   - "Stretch" (first)
   - "Lift Weights"
   - "Walk on Treadmill"
   - "Stretch" (last)
4. Each task shows either estimated time or difficulty (low/medium/high)
5. As John completes each task, he marks it complete
6. When the last child task is marked complete, the parent "Workout" automatically completes
7. With the checklist now empty, a congratulations message appears in the space where tasks were displayed

**Key Features Demonstrated:**
- Parent-child task grouping
- Custom task ordering within a group
- Recurring schedule (specific days of week)
- Time estimates or difficulty indicators
- Automatic parent completion when all children complete
- Completion celebration

---

## Workflow 2: Flexible Scheduling with Triggers and Conditions

**User Goal:** Manage household tasks with flexible schedules and conditional logic

**Scenario:**
Laura manages cooking, laundry, and plant watering with different schedules and preferences.

**Steps:**

**Part A: Schedule Adjustment**
1. Laura configures laundry to appear every 5 days
2. After using it, she realizes it's too frequent
3. She edits the task and changes it to every 6 days

**Part B: Day-of-Week Avoidance**
1. Laura notices she constantly postpones laundry on Thursdays
2. She edits the task to add a "Never schedule for" condition: Thursdays and Tuesdays
3. When the task would land on those days, it automatically finds the next available date
4. The task now avoids those days entirely in its scheduling

**Part C: Task Triggering**
1. Laura adds two tasks: "Cook Dinner" and "Clean Up After Dinner"
2. She doesn't want both visible at once
3. She configures "Clean Up After Dinner" to be triggered by completing "Cook Dinner"
4. Now when she opens the app, she only sees "Cook Dinner"
5. After completing it, "Clean Up After Dinner" appears immediately

**Part D: Simple Recurring Task**
1. Laura adds "Water Plants" as a basic task
2. She sets it to recur every weekday
3. It appears Monday-Friday as expected

**Key Features Demonstrated:**
- Schedule editing and refinement
- Conditional scheduling ("Never schedule for" conditions for days/dates/months)
- Task postponement behavior
- Task triggering (completion-based spawning)
- Simple recurring tasks
- Multiple interval types

---

## Workflow 3: Comprehensive Inventory Management

**User Goal:** Track household supplies with regular inventory counts and user-initiated shopping/restocking

**Scenario:**
Greg manages household essentials with detailed inventory tracking despite family members not following his system.

**Steps:**

**Part A: Inventory Count Task**
1. Every Tuesday, Greg sees "Take Inventory Count" task
2. He opens the task
3. The app shows a scrollable list with collapsible categories
4. Each category contains inventory items with:
   - Minus button
   - Current count (directly editable)
   - Plus button
   - Check button (locks the item when pressed)
5. Greg works through items, adjusting quantities and checking them off
6. Checked items lock to prevent accidental edits while scrolling
7. When all items in a category are checked, the category auto-collapses
8. After completing all items, the app's inventory tracker is current

**Part B: Task with Prompted Inventory Consumption**
1. Greg has a task "Swap Air Filters"
2. His recent count shows 6 filters in inventory
3. He completes the task
4. The app prompts: "How many air filters did you use?" with a default value of 1
5. He confirms or adjusts the amount used
6. Inventory is automatically decremented

**Part C: Tasks with Fixed Inventory Consumption**
1. Greg has other tasks configured to automatically consume set amounts
2. When he completes these tasks, inventory decrements without prompting
3. No user input required

**Part D: Tasks with Recount Mode**
1. Greg has some tasks where tracking exact consumption during the task is difficult
2. These tasks are configured with "Recount" mode for their inventory items
3. When he completes such a task, instead of decrementing inventory:
4. A "Recount Inventory" task is automatically created
5. This task bundles all items marked for recount
6. He can complete the recount task later when convenient

**Part E: Initiating Shopping Workflow**
1. Greg opens the Inventory Screen
2. He taps the "Go Shopping" button
3. The app shows a checklist of items below their reorder thresholds
4. Items are organized by category with:
   - Item name
   - Current quantity
   - Suggested purchase quantity
   - Checkbox
5. As Greg shops, he checks off items he acquires
6. He unchecks any items he decides not to purchase
7. When finished shopping, he completes the workflow

**Part F: Restock Task**
1. Completing the shopping workflow automatically creates a "Restock Inventory" task
2. The task appears in his Today view (or All Tasks view)
3. Greg opens the restock task
4. He sees only the items he checked off during shopping
5. For each item:
   - Current quantity shown
   - Plus/minus buttons
   - Direct number entry
   - Check button to lock item
6. He updates the quantities for each item he purchased
7. Locked items prevent accidental changes while scrolling
8. Categories auto-collapse when all items in them are checked
9. After updating all quantities, the app prompts: "Add new inventory items?"
10. Greg adds a few new items he bought that weren't on the original list
11. He completes the restock task
12. All inventory quantities are updated

**Key Features Demonstrated:**
- Scheduled inventory count tasks
- Inventory item organization by category with collapsible sections
- Check-to-lock mechanism for safe scrolling
- Prompted inventory consumption with default values
- Fixed/automatic inventory consumption
- Recount mode for difficult-to-track consumption
- Bundled recount tasks (multiple recount items → one task)
- User-initiated shopping workflow (not automatic)
- Dynamic shopping list based on reorder thresholds
- Automatic restock task generation from shopping
- Multiple inventory update methods (+/-, direct entry)
- Adding new inventory items during restock
- Shopping → Restocking workflow chain

---
*
# Screen Specifications

## Today Screen (Main Screen)

**Purpose:** Primary interface showing tasks scheduled for the current day

**Layout:**

**Header Section:**
- Current date display (center)
- Four icon buttons:
  - All Tasks button (left) - navigates to All Tasks View
  - Show Completed toggle button - filters completed tasks visibility
  - Inventory Management button (right) - navigates to Inventory Screen
  - Application Settings button (far right) - navigates to Settings

**Checklist Section:**
- Tasks organized by category
- Each category shows:
  - Category name
  - Progress indicator (e.g., "3/5 completed")
  - List of tasks within that category
- Category behavior:
  - Disappears when all tasks complete (unless completed filter is toggled on)
  - Collapses/expands to show tasks

**Empty State:**
- When all tasks are completed and categories are hidden
- Displays random congratulations message
- Fills the space where checklist was shown

**Interactions:**
- Tap task to mark complete
- Tap task for more details (navigates to Task Detail Screen)
- Toggle completed filter to show/hide finished tasks
- Navigate to other screens via header buttons

---

## All Tasks View Screen

**Purpose:** View all tasks across the timeline to find and edit tasks scheduled for future dates

**Access:** Dedicated icon button in Today Screen header (leftmost position)

**Layout:**
- Search bar at top
  - Search by task name, category, tags
- Task list ordered by next scheduled date
  - Today's tasks
  - Tomorrow's tasks
  - This week
  - Future tasks
- Each task shows:
  - Task name
  - Next scheduled date
  - Category
  - Quick indicators (has inventory, is triggered, has children, etc.)

**Interactions:**
- Tap task to navigate to Task Detail Screen
- Search to quickly find specific tasks
- Scroll through chronological list

---

## Task Detail Screen

**Purpose:** Display comprehensive technical information about a specific task

**Header:**
- Back button (top left) - returns to previous screen
- Edit button (top right) - switches to Task Edit Screen

**Required Information Display:**
- Task name, category, tags
- Schedule information:
  - Next scheduled date
  - Recurrence pattern (e.g., "Every 3 days", "Every Monday/Wednesday/Friday")
- Completion data:
  - Last completion date
  - Current completion streak
- Relationship information:
  - Parent task (if this is a child)
  - Child tasks (if this is a parent)
  - Triggered by (which task triggers this one)
  - Triggers (which tasks this one triggers)
- Inventory associations:
  - Items consumed
  - Consumption mode (Fixed/Prompted/Recount)
  - Quantities per execution
- Conditional rules:
  - "Never schedule for" conditions (days/dates/months)
  - Any other scheduling conditions
- Task description/notes
- Time estimate or difficulty indicator

**Principle:**
No unanswered questions - all current task configuration must be visible. Historical data (creation date, edit history, average completion rates) is NOT shown. Tasks are representative of their current iteration only.

**Interactions:**
- Edit button (navigates to Task Edit Screen)
- View related tasks (navigate to their detail screens)
- Complete task (if viewing from today)
- Delete task

---

## Task Edit/Create Screen

**Purpose:** User-friendly interface for creating new tasks or editing existing ones with full relationship and configuration options

**Header:**
- Back button / X button (top left) - cancels changes, returns to Task Detail Screen (or Today Screen if creating new task)
- Save button (top right) - saves changes and returns to Task Detail Screen

**Core Fields:**
- Task name
- Category selection/creation
- Tags (searchable, selectable)
- Description/notes
- Time estimate or difficulty

**Schedule Configuration:**
- Recurrence pattern
  - Interval (every X days/weeks/months)
  - Specific days of week (Monday, Wednesday, Friday, etc.)
  - ADHOC (never scheduled, trigger-only)
- Conditional scheduling:
  - "Never schedule for" exclusion rules
    - Exclude specific days of week
    - Exclude specific dates
    - Exclude date ranges
  - When excluded date is hit, automatically finds next available date
- Overdue behavior:
  - **Postpone**: Task slides forward day-by-day when incomplete (default)
  - **Skip to Next**: Task jumps to next scheduled occurrence when a day passes without completion

**Relationship Configuration:**

**Parent-Child Relationships:**
- "Parent Task" field with search
  - Search by name, category, tag
  - Shows existing parent if this is a child
- "Child Tasks" section (only visible if this task is a parent)
  - List of current children
  - Add child with search
  - Remove children
  - Reorder children
- **Manual Completion Toggle** (for parent tasks)
  - Enable: Parent requires manual check-off after all children complete
  - Disable: Parent auto-completes when last child finishes

**Note:** Parent-child relationships are managed from the child's perspective. If editing a parent, you see the children. If editing a child, you see the parent. Connections are made by the user explicitly.

**Trigger Configuration:**
- "Triggered By" field with search
  - Which task completion spawns this task
  - Triggered task appears on the date of triggering task's completion
- "Triggers" section
  - List tasks this task will trigger on completion
  - Add triggers with search
  - Triggered tasks respect their own schedules (schedule updates from trigger date)

**Inventory Configuration:**
- Inventory items consumed by this task
- Search bar for finding inventory items
- For each item:
  - Consumption mode: Fixed, Prompted, Recount
  - Quantity (if Fixed mode)
  - Default value (if Prompted mode)
- Add/remove inventory associations

**Notes:**
- Multiple items with Recount mode will bundle into one "Recount Inventory" task
- Prompted mode can have default values for quick confirmation
- Recount tasks follow the same workflow as Restock tasks

**Search Functionality:**
Throughout this screen, search bars for tasks and inventory should:
- Search by name
- Filter by category
- Filter by tags
- Show relevant results dynamically

**Interactions:**
- Save/Create task
- Cancel (discard changes)
- Delete task (if editing)

---

## Inventory Screen

**Purpose:** Manage all inventory items, their quantities, and initiate shopping workflows

**Header Section:**
- Add Inventory Item button
- "Go Shopping" button - initiates shopping workflow
- Search bar (search by name, category, tag)

**Inventory List:**
- Items organized by category
- Each item shows:
  - Item name
  - Current quantity
  - Plus button (increment)
  - Minus button (decrement)
  - Tags/category indicators
- Sort options:
  - By category
  - By tag
  - By quantity (low to high, high to low)
  - Alphabetical

**Item Details (tap to expand or navigate):**
- Full item information
- Reorder threshold
- Target restock quantity
- Associated tasks (which tasks consume this item)
- Edit/delete options

**Interactions:**
- Quick increment/decrement quantities
- Search for specific items
- Sort and filter list
- Add new inventory items
- Edit existing items
- View which tasks consume each item
- **"Go Shopping" workflow:**
  - Tap "Go Shopping" button
  - See checklist of items below reorder threshold
  - Check off items as you shop
  - Complete shopping to create automatic "Restock Inventory" task

---

## Shopping Workflow Screen

**Purpose:** Grocery list interface for shopping session

**Trigger:** User taps "Go Shopping" button on Inventory Screen

**Layout:**
- List of items below reorder threshold
- Each item shows:
  - Item name
  - Current quantity
  - Suggested purchase quantity (to reach reorder target)
  - Checkbox
- Organized by category (collapsible)

**Interactions:**
- Check off items as you acquire them while shopping
- Uncheck if you decide not to purchase
- Complete shopping workflow
- Upon completion: automatically creates "Restock Inventory" task with checked items

---

## Restock Inventory Task Screen

**Purpose:** Update inventory quantities after shopping

**Trigger:** Automatically created after completing Shopping Workflow OR completing a task with Recount mode inventory

**Layout:**
- List of items from shopping checklist (or recount items)
- Each item shows:
  - Item name
  - Current quantity (before restock)
  - Minus button
  - Quantity input (editable)
  - Plus button
  - Check button (locks item)
- Organized by category (collapsible, auto-collapse when category complete)

**Flow:**
1. User adjusts quantities for each purchased item
2. Check button locks the item (prevents accidental edits while scrolling)
3. Categories auto-collapse when all items checked
4. After all items updated, prompt: "Add new inventory items?"
5. User can add items purchased that weren't on the list
6. Complete task to finalize inventory updates

**Interactions:**
- Increment/decrement quantities with +/- buttons
- Direct number entry
- Lock items with check button (tap again to unlock)
- Add new inventory items
- Complete task to save all changes

---

## Application Settings Screen

**Purpose:** Configure app-wide preferences and data management

**Settings:**
- **Import/Export**
  - Export all data (JSON/CSV)
  - Import tasks and inventory
  - Backup/restore functionality

**Future Settings:**
[Additional settings can be added as needed]

---

## Navigation Flow

```
Today Screen (Main/Home)
├─→ Task Detail Screen
│   ├─→ Task Edit Screen (Edit button)
│   │   └─→ Save → back to Task Detail
│   │   └─→ X/Cancel → back to Task Detail
│   └─→ Back → Today Screen
├─→ All Tasks View Screen [Header button]
│   └─→ Task Detail Screen
├─→ Inventory Screen [Header button]
│   ├─→ Inventory Item Detail/Edit
│   ├─→ Go Shopping Workflow Screen
│   │   └─→ Creates Restock Inventory Task (appears in Today/All Tasks)
│   └─→ Restock Inventory Task Screen (special task type)
│       └─→ Complete → updates inventory, returns to Today
├─→ Application Settings [Header button]
└─→ Task Create Screen (FAB - Floating Add Button)
    └─→ Save → Today Screen or Task Detail
    └─→ Cancel → Today Screen
```

**Notes:**
- Restock Inventory Task is a special screen/workflow (not a regular task detail screen)
- Shopping workflow is initiated from Inventory Screen, not a scheduled task
- Tasks with Recount mode inventory create Restock tasks automatically

---

# Task Configuration System

## Overview

Tasks in Life-Ops are not predefined "types" but rather flexible entities that can be configured with various properties and behaviors. Any task can combine multiple configuration options to create the exact behavior needed.

---

## Core Task Properties

Every task has these fundamental properties:

- **Name** - Task identifier
- **Category** - Organizational grouping (e.g., "Household", "Work", "Health")
- **Tags** - Searchable labels for filtering and organization
- **Description/Notes** - Additional context or instructions
- **Time Estimate** - Expected duration or difficulty indicator (Low/Medium/High)

---

## Scheduling Configuration

**Purpose:** Define when and how often a task appears

### Recurrence Options:

**Interval-Based:**
- Every X days
- Every X weeks
- Every X months

**Specific Days:**
- Specific days of the week (e.g., Monday, Wednesday, Friday)
- Can combine with interval (e.g., every 2 weeks on Mondays)

**ADHOC:**
- No automatic schedule
- Never appears on its own
- Only appears when triggered by another task
- Still maintains schedule tracking (last completed, next due)

### Schedule Exclusions:

**"Never Schedule For" Rules:**
- Exclusion list applied to scheduled tasks
- Exclude specific days of the week
- Exclude specific dates
- Exclude date ranges
- When schedule would land on excluded time, automatically finds next available date
- Only applies if task has a schedule (ADHOC tasks ignore exclusions)
- Works as blacklist, not whitelist

---

## Relationship Configuration

**Purpose:** Create task dependencies and workflows

### Parent-Child Relationships:

**Behavior:**
- Parent task contains multiple child tasks
- Parent completes when ALL children are complete
- Optional manual completion toggle: user can choose whether parent requires manual check-off or auto-completes
- Children can be ordered within the parent
- Children maintain their own schedules independently of parent

**Schedule Inheritance:**
- Children inherit parent's schedule IN ADDITION to their own schedule
- Schedules operate independently and don't override each other
- Each task tracks its own lastCompleted and nextDue dates
- Example: Parent every Tuesday + Child every 3 days = Child appears on its 3-day cycle AND when parent is due
- Child and parent schedules do not interact or offset each other

**Configuration:**
- Set parent (from child's perspective)
- Add children (from parent's perspective)
- Order children
- Toggle manual completion requirement for parent

### Trigger Relationships:

**Behavior:**
- Completing one task causes another task to immediately appear
- Triggered task appears on the date of the triggering task's completion
- If triggering task is completed from future date (All Tasks View), triggered task appears on that future date
- Triggered tasks update their schedule upon completion (e.g., every-3-days task triggered today will next appear in 3 days)
- Triggers work independently of task schedules - ADHOC and scheduled tasks both trigger normally

**Important:** Incomplete tasks at end of day are postponed (not skipped)

**Configuration:**
- "Triggered By" - which task spawns this one
- "Triggers" - which tasks this one spawns on completion

**Common Patterns:**
- Sequential workflows (Cook Dinner → Clean Up After Dinner)
- Conditional tasks that only appear after certain actions
- Maintenance chains (Change Oil → Reset Oil Light)

---

## Inventory Configuration

**Purpose:** Track consumable resources used by tasks

### Inventory Association:

**Consumption Modes:**

**Fixed Consumption:**
- Automatically deducts set amounts when task completes
- No user prompt needed
- Example: "Brew Coffee" always uses 2 filters

**Prompted Consumption:**
- Asks user for actual amounts used at completion
- Flexible for variable usage
- Can set default value for quick confirmation
- Example: "Cook Dinner" prompts for ingredient amounts (with defaults)

**Recount Mode:**
- Creates a "Recount Inventory" task instead of deducting
- If multiple items have recount mode, they're bundled into one recount task
- Recount task follows restock workflow (item-by-item confirmation)
- Used when actual consumption is difficult to track during task execution

**Multiple Items:**
- A single task can consume multiple inventory items
- Each item can have different consumption mode
- Mixed modes allowed (some fixed, some prompted)

### Shopping Workflow:

**Triggered from Inventory Screen:**
- "Go Shopping" button on Inventory Screen
- Shows items below reorder threshold as checklist
- User checks off items as they shop (like a grocery list)
- Upon completion, automatically creates "Restock Inventory" task

**Restock Task:**
- Created automatically after shopping workflow
- Shows only items that were checked during shopping
- User confirms quantities purchased for each item
- Option to add new inventory items not on the list
- Inventory levels update automatically upon completion

**Note:** Shopping is NOT an automatic task - it's a user-initiated workflow from the Inventory Screen, not triggered by low inventory automatically.

---

## Configuration Combinations

Tasks can combine any of these configurations to create complex behaviors:

**Examples:**

**Simple Recurring Task:**
- Schedule: Every 3 days
- No relationships, no inventory

**Household Maintenance with Supplies:**
- Schedule: Monthly
- Inventory: Fixed consumption (air filter)
- Automatic low inventory detection

**Workout Routine:**
- Parent task with 4 children
- Schedule: Monday/Wednesday/Friday
- Ordered children (stretch → lift → cardio → stretch)
- Time estimates for each

**Triggered Workflow:**
- Task A: "Cook Dinner" (scheduled daily)
- Task B: "Clean Up" (ADHOC, triggered by Task A)
- Task A uses inventory (prompted mode for ingredients)

**Complex Conditional Task:**
- Schedule: Every 6 days
- Never schedule for: Tuesdays, Thursdays
- Inventory: Multiple items with default values (prompted)
- Part of parent group
- Parent requires manual completion

**Shopping and Restocking:**
- User initiates "Go Shopping" from Inventory Screen
- Checks off low-inventory items while shopping
- Automatic "Restock Inventory" task created
- Updates all purchased quantities
- Adds new items discovered during shopping

**Ephemeral One-Time Task:**
- Name: "Pick up dry cleaning"
- Schedule: ADHOC (no recurrence) or set to tomorrow
- Delete after completion: true
- Task appears, user completes it, task auto-deletes at end of day

---

## Task Behavior Rules

### Completion Logic:

**Standard Task:**
- Mark complete → schedule advances to next occurrence
- Inventory consumed (if configured)
- Triggers fire (if configured)

**Parent Task:**
- Cannot be manually completed (unless manual completion toggle is enabled)
- Auto-completes when last child is marked complete (if toggle disabled)
- If manual completion enabled, user must check off parent after all children complete
- All child completions must happen before parent can complete

**Child Task:**
- Can be completed independently
- Contributes to parent completion progress
- Maintains own schedule separate from parent
- Appears based on BOTH its own schedule AND parent's schedule

### Skip vs Postpone:

**Skip:**
- Task disappears until next scheduled occurrence
- Example: Every-3-days task skipped today → reappears in 3 days from original schedule
- ADHOC tasks disappear entirely (no schedule to return to)

**Postpone:**
- Task moves to tomorrow only
- Maintains "overdue" awareness
- Can be postponed repeatedly
- Does not affect future schedule

**Ephemeral/One-Time Tasks:**
- Task marked with `deleteAfterCompletion = true`
- Completes normally with all standard behavior (inventory, triggers, etc.)
- Auto-deletes during end-of-day processing (not immediately on completion)
- Useful for one-off tasks that shouldn't clutter the task list
- Examples: "Pick up dry cleaning", "Call dentist about appointment", "Buy birthday gift"

---

## What This System Enables

**Flexibility:**
- Any task can be simple or complex
- Configurations can be added/removed as needs change
- No predefined limitations on task behavior

**Power:**
- Multi-level hierarchies (parents with children)
- Complex trigger chains
- Sophisticated inventory tracking
- Conditional scheduling logic

**Clarity:**
- Each configuration option has clear behavior
- Combinations are predictable
- No hidden "task types" with special rules

---

# Data Model

## Overview

The data model supports flexible task configuration, relationship tracking, and inventory management through explicit storage of task state and behavior.

---

## Core Entities

### Task

Represents a unit of work with scheduling, relationships, and inventory configuration.

**Identity & Basic Info:**
- `id` - Unique identifier (Primary Key)
- `name` - Task name/title
- `category` - Organizational grouping (e.g., "Household", "Work", "Health")
- `tags` - Comma-separated searchable labels
- `description` - Additional context or instructions
- `active` - Boolean, whether task is active or archived

**Scheduling:**
- `intervalUnit` - "DAY" | "WEEK" | "MONTH" | "ADHOC"
- `intervalQty` - Number of units between occurrences (0 for ADHOC)
- `specificDaysOfWeek` - JSON array of days (e.g., ["MONDAY", "WEDNESDAY", "FRIDAY"])
- `excludedDates` - JSON array of excluded dates/date ranges (past dates can be cleaned up periodically or left)
- `excludedDaysOfWeek` - JSON array of days to never schedule (e.g., ["TUESDAY", "THURSDAY"])
- `overdueBehavior` - "POSTPONE" | "SKIP_TO_NEXT" - How task behaves when day advances without completion
- `deleteAfterCompletion` - Boolean, whether task auto-deletes after completion when day advances (for ephemeral/one-time tasks)
- `nextDue` - Next scheduled date (null for unscheduled ADHOC)
- `lastCompleted` - Last completion timestamp

**Time Estimation:**
- `timeEstimate` - Minutes (integer) or null
- `difficulty` - "LOW" | "MEDIUM" | "HIGH" or null

**Relationships:**
- `parentTaskIds` - JSON array of parent task IDs (can have multiple parents)
- `requiresManualCompletion` - Boolean, whether parent needs manual check-off (only for parent tasks)
- `childOrder` - Integer, order within parent (null if not a child)

**Triggers:**
- `triggeredByTaskIds` - JSON array of task IDs that trigger this task
- `triggersTaskIds` - JSON array of task IDs that this task triggers on completion

**Inventory:**
- `requiresInventory` - Boolean, whether task consumes inventory

**State Tracking:**
- `completionStreak` - Current consecutive completion count

---

### TaskSupply (Junction Table)

Links tasks to inventory items they consume.

- `taskId` - Foreign key to Task (composite PK)
- `supplyId` - Foreign key to Supply (composite PK)
- `consumptionMode` - "FIXED" | "PROMPTED" | "RECOUNT"
- `fixedQuantity` - Amount consumed (used if mode is FIXED)
- `promptedDefaultValue` - Default value for prompt (used if mode is PROMPTED)

---

### Supply

Represents an inventory item that can be tracked and consumed.

- `id` - Unique identifier (Primary Key)
- `name` - Item name
- `category` - Organizational grouping
- `tags` - Comma-separated searchable labels
- `unit` - Unit of measurement (e.g., "count", "oz", "liters")
- `reorderThreshold` - Quantity below which item appears in shopping list
- `reorderTargetQuantity` - Target quantity to restock to
- `notes` - Additional context

---

### Inventory

Tracks current quantities of supplies.

- `supplyId` - Foreign key to Supply (Primary Key)
- `currentQuantity` - Current on-hand amount
- `lastUpdated` - Timestamp of last update

---

### ChecklistItem

Daily snapshot of a task instance when it appears in the checklist.

- `id` - Unique identifier (Primary Key)
- `date` - Date this instance is for
- `taskId` - Foreign key to Task
- `status` - "TODO" | "DONE" | "SKIPPED"
- `completedAt` - Timestamp when marked complete (null if not done)
- `isPostponed` - Boolean, true if task was postponed to tomorrow
- `isSkipped` - Boolean, true if task was skipped to next scheduled occurrence
- `parentChecklistItemId` - Foreign key to parent checklist item (preserves hierarchy)

---

### TaskTrigger (Junction Table)

**REMOVED** - Trigger relationships now stored directly in Task entity as JSON arrays (`triggeredByTaskIds` and `triggersTaskIds`)

---

### TaskLog

Historical record of task completions.

- `id` - Unique identifier (Primary Key)
- `taskId` - Foreign key to Task
- `completedAt` - Timestamp of completion
- `date` - Date for which task was completed
- `inventoryConsumed` - JSON object of supply adjustments made
- `triggeredTasks` - JSON array of task IDs that were triggered
- `notes` - User notes for this completion

---

### RestockTask

Special task type for restocking inventory after shopping.

- `id` - Unique identifier (Primary Key)
- `createdAt` - Timestamp of creation
- `status` - "PENDING" | "COMPLETE"
- `itemsToRestock` - JSON array of {supplyId, suggestedQuantity}
- `completedAt` - Timestamp when restocked (null if pending)

---

## Key Relationships

### Task Hierarchy
```
Task (parent) M:M Task (children via parentTaskIds array)
```
- Tasks can have multiple parents
- Multiple parents means task appears when ANY parent is due
- Each parent-child relationship can have different `childOrder`

### Task Triggers
```
Task M:M Task (via triggeredByTaskIds and triggersTaskIds arrays)
```
- Circular triggers allowed
- Task can be triggered by multiple tasks
- Task can trigger multiple tasks
- Trigger fires when source task completes
- Triggered task appears on completion date of triggering task

### Task-Inventory Association
```
Task M:M Supply (via TaskSupply junction table)
Supply 1:1 Inventory (via supplyId)
```

### Checklist Hierarchy
```
ChecklistItem (parent) 1:M ChecklistItem (children via parentChecklistItemId)
ChecklistItem M:1 Task (task definition)
```

---

## Special Considerations

### ADHOC Tasks
- `intervalUnit` = "ADHOC"
- `intervalQty` = 0
- `nextDue` can be null or set by triggers
- Never auto-schedule
- Appear via triggers OR when parent tasks are due

### Parent-Child Schedule Interaction
- Children have their own `nextDue` and `lastCompleted`
- Children also appear when parent is due
- Both schedules operate independently
- No schedule inheritance or override

### Trigger Date Behavior
- Triggered task's `nextDue` set to triggering task's completion date
- If triggering task completed from future date (All Tasks View), triggered task appears on that future date
- Completing triggered task updates its `nextDue` based on its own schedule

### End-of-Day Postponement
- Incomplete tasks at day's end have `isPostponed` set to true
- Task remains in checklist for next day
- Different from Skip (which advances to next scheduled date)

### Recount Task Creation
- When task with RECOUNT mode inventory completes
- Multiple recount items bundle into single RestockTask
- RestockTask follows same workflow as shopping-generated restock

---

## Data Integrity Rules

1. **Multiple parents allowed** - Tasks can have multiple parents; circular parent-child relationships should be avoided but not strictly prevented
2. **Trigger loops allowed** - Tasks can trigger each other circularly (schedule updates handle this)
3. **Inventory quantities cannot be negative** - Validation on decrement operations
4. **Reorder threshold ≤ target quantity** - Logical constraint on Supply
5. **Child order unique within parent** - No duplicate order values for children of same parent (complex with multiple parents - order may be context-dependent)
6. **Active tasks only in checklist** - Archived tasks don't generate ChecklistItems

---

# Technical Architecture

## Overview

Life-Ops is built as a native Android application using modern Android development practices with a strict **offline-first architecture**. The system follows **Clean Architecture** principles with clear separation between presentation, domain, and data layers, ensuring maintainable, testable, and scalable code.

## Technology Stack

### Core Technologies
- **Platform**: Native Android (Minimum SDK 26 - Android 8.0, Target SDK 34)
- **Language**: Kotlin 1.9.10+
- **UI Framework**: Jetpack Compose with Material Design 3
- **Database**: Room (SQLite) for local persistence
- **Dependency Injection**: Hilt
- **Asynchronous Operations**: Kotlin Coroutines with Flow and StateFlow
- **Build System**: Gradle with Kotlin DSL

### Architecture Pattern
**MVVM (Model-View-ViewModel) + Clean Architecture**

```
┌─────────────────────────────────────────────────────────┐
│                  Presentation Layer                      │
│  ┌─────────────┐  ┌──────────────┐  ┌───────────────┐  │
│  │  Screens    │  │  ViewModels  │  │  Components   │  │
│  │  (Compose)  │←→│  (StateFlow) │←→│  (Reusable)   │  │
│  └─────────────┘  └──────────────┘  └───────────────┘  │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                    Domain Layer                          │
│  ┌─────────────┐  ┌──────────────┐  ┌───────────────┐  │
│  │  Use Cases  │  │   Models     │  │  Scheduler    │  │
│  │  (Business) │  │  (Entities)  │  │  (Logic)      │  │
│  └─────────────┘  └──────────────┘  └───────────────┘  │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                     Data Layer                           │
│  ┌─────────────┐  ┌──────────────┐  ┌───────────────┐  │
│  │    Room     │  │     DAOs     │  │ Repositories  │  │
│  │  Database   │←→│  (Queries)   │←→│  (Abstraction)│  │
│  └─────────────┘  └──────────────┘  └───────────────┘  │
└─────────────────────────────────────────────────────────┘
```

## Layer Responsibilities

### Presentation Layer (`presentation/`)
**Responsibility**: User interface and user interaction

**Components**:
- **Screens**: Full-screen Composables representing app destinations
- **ViewModels**: State management with StateFlow, UI logic coordination
- **UI Components**: Reusable Composable functions for consistent UI elements
- **Navigation**: NavHost-based navigation with type-safe routes

**Key Principles**:
- ViewModels expose single StateFlow per screen
- Composables are stateless where possible
- State hoisting for reusability
- No direct database or repository access

### Domain Layer (`domain/`)
**Responsibility**: Business logic and rules

**Components**:
- **Use Cases**: Single-responsibility business operations
- **Models**: Pure business entities (DTOs for presentation layer)
- **Scheduler**: Deterministic task scheduling logic
- **Business Rules**: Validation, calculations, workflow orchestration

**Key Principles**:
- Use cases have single `execute()` methods
- No Android framework dependencies
- Pure Kotlin business logic
- Testable without UI or database

### Data Layer (`data/`)
**Responsibility**: Data persistence and retrieval

**Components**:
- **Room Database**: SQLite wrapper with compile-time query verification
- **Entities**: Database table definitions with relationships
- **DAOs**: Database access interfaces with queries
- **Repositories**: Abstraction over data sources

**Key Principles**:
- Repository pattern for data access
- Single source of truth (database)
- Suspend functions for async operations
- Type converters for complex types (LocalDate, JSON)

## Data Flow Pattern

### Typical User Action Flow
```
User Interaction (UI)
    ↓
ViewModel receives event
    ↓
ViewModel calls Use Case
    ↓
Use Case executes business logic
    ↓
Use Case calls Repository
    ↓
Repository queries Database (DAO)
    ↓
Database returns data
    ↓
Repository returns Result<Data>
    ↓
Use Case processes/transforms data
    ↓
ViewModel updates StateFlow
    ↓
UI recomposes with new state
```

### State Management Pattern
```kotlin
// ViewModel exposes immutable state
private val _uiState = MutableStateFlow(InitialState())
val uiState: StateFlow<UiState> = _uiState.asStateFlow()

// Updates use immutable copies
_uiState.update { currentState ->
    currentState.copy(
        data = newData,
        isLoading = false
    )
}

// UI observes state
val uiState by viewModel.uiState.collectAsState()
```

## Database Architecture

### Schema Overview
**7 Core Entities**:
1. **Task** - Task definitions with scheduling and relationships
2. **Supply** - Inventory item definitions
3. **TaskSupply** - Junction table linking tasks to supplies
4. **Inventory** - Current quantity tracking
5. **ChecklistItem** - Daily task instance snapshots
6. **TaskLog** - Historical completion records
7. **Param** - System configuration key-value pairs

### Relationship Mapping
```
Task ───┬─── TaskSupply ─── Supply ─── Inventory
        │
        ├─── ChecklistItem (daily instances)
        │
        ├─── TaskLog (history)
        │
        ├─── self-reference (parent/child via parentTaskId)
        │
        └─── self-reference (triggers via triggerTaskId)
```

### Key Indices for Performance
```sql
-- Task lookups
INDEX ON tasks(nextDue)
INDEX ON tasks(parentTaskId)
INDEX ON tasks(triggerTaskId)
INDEX ON tasks(category, priority)

-- Checklist queries
INDEX ON checklist_items(date, status)
INDEX ON checklist_items(taskId, date)

-- Inventory lookups
INDEX ON supplies(name)
INDEX ON task_supplies(taskId, supplyId)
```

## Dependency Injection Structure

### Hilt Module Organization
```
@Module @InstallIn(SingletonComponent::class)
- DatabaseModule (provides Room database, DAOs)

@Module @InstallIn(ViewModelComponent::class)
- RepositoryModule (provides repositories)
- DomainModule (provides use cases, scheduler)

ViewModels:
- @HiltViewModel with @Inject constructor
```

### Dependency Graph
```
MainActivity
    ↓ @AndroidEntryPoint
ViewModels (@HiltViewModel)
    ↓ @Inject
Use Cases
    ↓ @Inject
Repositories
    ↓ @Inject
DAOs (from Database)
    ↓
Room Database (@Singleton)
```

## Offline-First Architecture

### Design Principles
- **No Network Layer**: Application operates entirely offline
- **Local Database**: Single source of truth
- **No Sync**: Data lives on device only (V1)
- **No Authentication**: No user accounts or cloud services

### Data Portability
- **Export**: JSON serialization of complete database state
- **Import**: JSON deserialization with validation and conflict resolution
- **Backup**: User-managed file export/import

### Future Sync Considerations (Post-V1)
- Optional cloud sync as separate module
- Conflict resolution strategy to be defined
- End-to-end encryption for synced data
- Device-to-device sync without cloud intermediary

## Performance Optimization

### Database Optimization
- **Efficient Queries**: JOIN operations optimized with proper indices
- **Lazy Loading**: Load only required data for current view
- **Batch Operations**: Group database writes for efficiency
- **Transaction Management**: Proper use of @Transaction for consistency

### UI Performance
- **LazyColumn**: Efficient list rendering with recycling
- **State Hoisting**: Minimize recomposition scope
- **Remember**: Cache expensive calculations
- **Stable Keys**: Proper key usage for list items

### Memory Management
- **ViewModelScope**: Automatic coroutine cancellation
- **StateFlow**: Efficient state observation
- **Resource Cleanup**: Proper lifecycle awareness
- **Leak Prevention**: Avoid activity/fragment context retention

## Testing Strategy

### Unit Tests
- **ViewModels**: State transitions and business logic
- **Use Cases**: Business rule validation and workflows
- **Repositories**: Data access patterns
- **Utilities**: Helper functions and extensions

### Integration Tests
- **Database**: Room DAO operations with in-memory database
- **End-to-End Flows**: Complete user workflows
- **Repository Integration**: Database + repository testing

### UI Tests
- **Compose Testing**: Screen interactions and state changes
- **Navigation**: Screen transitions and argument passing
- **User Flows**: Complete task lifecycle testing

## Error Handling Strategy

### Layered Error Handling
1. **Repository Level**: Database exceptions wrapped in `Result<T>`
2. **Use Case Level**: Business logic validation with clear error messages
3. **ViewModel Level**: Error state in UI state classes
4. **UI Level**: User-friendly error display

### Logging Strategy
- **Debug Logs**: Data flow tracking for development
- **Error Logs**: Exception details with context
- **Performance Logs**: Timing for optimization
- **User Action Logs**: Workflow tracking for debugging

## Security Considerations

### Data Security
- **Local Storage**: All data stored in app-private directory
- **No Network**: Zero remote data transmission
- **Database Encryption**: Optional for sensitive data (future)
- **No Permissions**: Minimal Android permission requirements

### Privacy
- **No Tracking**: Zero analytics or telemetry
- **No User Accounts**: No personal information collected
- **Offline Operation**: Complete data isolation
- **User Control**: Full data export/import capabilities

---

# Requirements

## Functional Requirements

### Core Task Management
**REQ-1**: The system SHALL support creation, editing, and deletion of tasks  
**REQ-2**: The system SHALL support 7 distinct task types with specific behaviors:
- Basic Task (simple recurring)
- Inventory Associated Task (consumes supplies)
- Triggered Task (appears on completion of another task)
- Group/Parent Task (contains child tasks)
- Child Task (part of a parent task)
- Purchase Task (dynamic shopping list generation)
- Restock Task (inventory restocking workflow)

**REQ-3**: The system SHALL generate a deterministic "Today" checklist based on:
- Task scheduling rules (interval-based)
- Task due dates (nextDue field)
- Overdue tasks (slide-forward behavior)
- Triggered task appearances
- Independent child task schedules

**REQ-4**: The system SHALL support parent-child task hierarchies where:
- Parents complete only when ALL children complete
- Children can inherit parent schedule OR have independent schedules
- Independent children can appear while parent is overdue
- Hierarchy is visually preserved in UI

**REQ-5**: The system SHALL support task relationships:
- Parent-child (hierarchical grouping)
- Triggers (task completion spawns another task)
- Circular triggers are allowed (tasks exist once with updated nextDue)

### Scheduling System
**REQ-6**: The system SHALL support interval-based scheduling:
- Daily intervals (every N days)
- Weekly intervals (every N weeks)
- Monthly intervals (every N months)
- ADHOC (no automatic scheduling, trigger-only)

**REQ-7**: The system SHALL implement slide-forward persistence where:
- Tasks with overdueBehavior="POSTPONE" remain in Today view, sliding forward day-by-day until completed or manually skipped
- Tasks with overdueBehavior="SKIP_TO_NEXT" automatically advance to their next scheduled occurrence when the day changes without completion
- Overdue state is visually indicated (different color/icon)
- No penalties or streak breaks for missed tasks
- Default behavior is POSTPONE for backward compatibility

**REQ-8**: The system SHALL support two skip behaviors:
- **Skip**: Push task to next day only (temporary postponement)
- **Won't Do**: Advance to next scheduled occurrence (full interval skip)

### Inventory Management
**REQ-9**: The system SHALL track consumable supplies with:
- Current quantity (onHandQty)
- Reorder point threshold
- Reorder target quantity
- Unit of measurement
- Purchase URLs

**REQ-10**: The system SHALL support three inventory consumption modes:
- **FIXED**: Automatically decrement by configured amounts
- **PROMPT**: Ask user for actual amounts used at completion
- **RECOUNT**: Create a recount task instead of decrementing

**REQ-11**: The system SHALL generate Purchase Tasks when:
- Inventory levels fall below reorder point
- Task is scheduled (not auto-generated on low inventory)
- Displays dynamic shopping list of items + quantities when due

**REQ-12**: The system SHALL auto-create Restock Tasks when:
- A Purchase Task is completed
- Provides item-by-item restocking checklist
- Updates inventory quantities upon completion

**REQ-13**: Inventory SHALL be advisory only:
- Low inventory NEVER blocks task completion
- Tasks can complete with zero inventory available
- Warnings displayed but not enforced

### User Interface
**REQ-14**: The system SHALL provide a "Today" screen as the main interface showing:
- Tasks due today or overdue
- Grouped by category
- Parent-child hierarchies (expandable)
- Completion status indicators

**REQ-15**: The system SHALL provide advanced filtering:
- By category
- By priority (High/Medium/Low)
- By status (Pending/Completed)
- Special filters (Needs Supplies, Has Triggers, etc.)

**REQ-16**: The system SHALL provide task detail views with:
- Complete scheduling information
- Relationship visualization (parent/child/triggers)
- Inventory requirements
- Completion history

**REQ-17**: The system SHALL provide task editing capabilities for:
- All task properties (name, category, priority, notes)
- Schedule configuration (interval, unit)
- Relationship management (parent/child, triggers)
- Inventory associations (supplies, consumption mode)

**REQ-18**: The system SHALL provide inventory management interface for:
- Viewing current inventory levels
- Manual inventory adjustments
- Supply creation and editing
- Reorder point configuration

### Data Management
**REQ-19**: The system SHALL operate completely offline with:
- No network connectivity required
- All data stored locally in Room database
- Zero remote API dependencies

**REQ-20**: The system SHALL provide data export/import:
- Full database export to JSON format
- Import with validation and conflict resolution
- Support for external task creation via LLM-generated files

---

## Non-Functional Requirements

### Performance
**NFR-1**: Today checklist generation SHALL complete in < 500ms for 500+ tasks  
**NFR-2**: Task completion workflow SHALL complete in < 200ms including inventory processing  
**NFR-3**: UI SHALL maintain 60fps during normal operation  
**NFR-4**: App SHALL handle 200+ active tasks smoothly (target use case: ~17 daily tasks)

### Reliability
**NFR-5**: The system SHALL never lose user data due to crashes  
**NFR-6**: Database operations SHALL use transactions for consistency  
**NFR-7**: The system SHALL handle edge cases gracefully:
- Zero inventory scenarios
- Circular task relationships
- Missing parent/child references
- Invalid date configurations

### Usability
**NFR-8**: Task completion SHALL require ≤ 3 taps from Today screen  
**NFR-9**: New tasks SHALL be creatable in < 30 seconds  
**NFR-10**: Error messages SHALL be user-friendly and actionable  
**NFR-11**: The system SHALL provide clear visual feedback for all actions

### Maintainability
**NFR-12**: Code SHALL follow Clean Architecture principles  
**NFR-13**: Business logic SHALL be testable without UI dependencies  
**NFR-14**: Database schema SHALL support migrations for future changes  
**NFR-15**: Code SHALL include comprehensive inline documentation

### Scalability
**NFR-16**: Architecture SHALL support addition of new task types  
**NFR-17**: Scheduling system SHALL be extensible for future interval types  
**NFR-18**: Inventory system SHALL support future enhancements (categories, locations)

---

## Technical Constraints

### Platform
**CON-1**: Target platform is Android only (no iOS, web, desktop)  
**CON-2**: Minimum Android SDK 26 (Android 8.0)  
**CON-3**: Target Android SDK 34 (latest stable)

### Technology Stack
**CON-4**: UI framework is Jetpack Compose (no XML layouts)  
**CON-5**: Database is Room (SQLite wrapper)  
**CON-6**: Dependency injection is Hilt  
**CON-7**: Programming language is Kotlin (no Java)

### Architecture
**CON-8**: Architecture follows MVVM + Clean Architecture  
**CON-9**: State management uses StateFlow (not LiveData)  
**CON-10**: Navigation uses Jetpack Navigation (NavHost)

### Dependencies
**CON-11**: All dependencies SHALL be open-source  
**CON-12**: No third-party analytics or crash reporting (v1.0)  
**CON-13**: No cloud service dependencies (Firebase, etc.)

---

## Deployment Requirements

### Functionality Completeness
**DEP-1**: All 7 task types must be fully functional  
**DEP-2**: Complete task lifecycle must work: Create → Edit → Complete → Delete  
**DEP-3**: Full inventory management must be operational  
**DEP-4**: Data export/import must be functional

### Quality Assurance
**DEP-5**: App must handle 200+ tasks without performance degradation  
**DEP-6**: No crashes under normal usage scenarios  
**DEP-7**: All critical user flows must be tested end-to-end

### User Experience
**DEP-8**: Clean initial state (no mandatory sample data)  
**DEP-9**: Intuitive navigation and clear information hierarchy  
**DEP-10**: Loading states during Today screen generation

---

## Out of Scope (V1.0)

### Explicitly NOT Included
- **Cloud sync** - Offline-only in v1.0
- **Multi-device support** - Single device only
- **User accounts** - No authentication system
- **Notifications** - No push notification system
- **Widgets** - Home screen widgets deferred to v2.0
- **Backup reminders** - User-initiated export only
- **Advanced analytics** - Basic completion stats only
- **Gamification** - No streaks, points, or achievements
- **Social features** - No sharing or collaboration
- **Calendar integration** - Today-only interface
- **Recurring schedule editor** - Use import/export for bulk edits

---

# Glossary of Terms

## Core Concepts

### ADHOC Task
A task with no automatic scheduling (`intervalUnit = ADHOC`). Never appears on its own in the Today view. Only surfaces when:
- Explicitly spawned/triggered by completing another task
- Manually created by the user

**Example**: "Clean Up After Dinner" (triggered by completing "Cook Dinner")

### Checklist Item
A daily snapshot of a task instance created when a task appears in the Today view. Preserves:
- Task state at time of appearance
- Completion status (TODO/DONE/SKIPPED)
- Original due date for slide tracking
- Parent-child hierarchy relationships

### Child Task
A task that belongs to a parent/group task. Can have two scheduling modes:
- **Inherited Schedule**: Appears when parent appears
- **Independent Schedule**: Has its own interval, can appear solo between parent cycles

### Complete vs Skip vs Won't Do
Three different ways to handle a task in the Today view:
- **Complete**: Mark done, advance schedule normally, process inventory/triggers
- **Skip**: Move to tomorrow only (temporary postponement), preserves original schedule
- **Won't Do**: Advance by full interval (e.g., weekly task skips to next week)

### Consume Mode
How a task affects inventory when completed:
- **FIXED**: Automatically decrement by configured amounts
- **PROMPT**: Ask user for actual amounts used during completion
- **RECOUNT**: Create a recount/restock task instead of decrementing immediately

### Deterministic Scheduling
Scheduling behavior based entirely on explicit rules and stored state, never on algorithms or guessing. Every task appearance can be traced to:
- Configured interval rules
- Due date calculations
- Trigger events
- Parent-child relationships

---

## Task System Terms

### Group Task / Parent Task
A task that contains one or more child tasks. Completion rules:
- Parent completes ONLY when ALL children are complete
- Parent schedule typically applies to all children (unless children have independent schedules)
- Parent inventory consumption and triggers fire only when parent completes

**Example**: "Clean Bathroom" (parent) with children "Clean Shower", "Clean Toilet", "Mop Floor"

### Interval
The time period between task occurrences:
- **Interval Unit**: DAY, WEEK, MONTH, or ADHOC
- **Interval Quantity**: Number of units (e.g., "every 3 days" = intervalQty: 3, intervalUnit: DAY)

### Next Due Date (`nextDue`)
The date when a task should next appear in the Today view. Updated when:
- Task is completed (advances by interval)
- Task is skipped (moves to tomorrow)
- Task is marked "Won't Do" (advances by full interval)
- Triggered task fires (set to current day)

### Priority
Task importance level used for visual ordering and filtering:
- **1 (High)**: Critical tasks
- **2 (Medium)**: Normal tasks (default)
- **3 (Low)**: Nice-to-do tasks

---

## Inventory System Terms

### On-Hand Quantity (`onHandQty`)
Current inventory level for a supply. Can reach zero but never negative. Updated by:
- Task completion (automatic or prompted decrement)
- Manual inventory adjustments
- Restock Task completion

### Purchase Task
A **scheduled task** (not auto-generated) that displays a dynamic shopping list when due. Shows:
- Items below reorder point
- Quantities needed to reach reorder target
- Aggregated list across all low-inventory items

**Important**: User schedules when to review shopping list (e.g., "Purchase Groceries" every Saturday). System does NOT auto-create purchase tasks on low inventory.

### Recount Task
A task that prompts user to manually verify and update inventory counts. Created when:
- A task with RECOUNT mode inventory completes
- Multiple recount items can bundle into one recount task

### Reorder Point
Inventory threshold that determines when an item should appear in a Purchase Task's shopping list. When `onHandQty < reorderPoint`, item shows in shopping list.

### Reorder Target Quantity
The ideal inventory level after restocking. Purchase Task suggests buying enough to reach this quantity: `quantityNeeded = reorderToQty - onHandQty`

### Restock Task
A task **automatically created** when a Purchase Task completes. Provides:
- Item-by-item restocking checklist
- Quantity input for each purchased item
- Inventory update upon task completion

**Workflow**: Purchase Task (shopping) → completion creates → Restock Task (put items away)

### Supply / Inventory
- **Supply**: Definition of a consumable item (name, unit, reorder points, purchase URL)
- **Inventory**: Current quantity tracking for a supply

### Supply Requirement
Specification of how much of a supply a task needs:
- Linked via TaskSupply junction table
- `qtyPerExec`: Quantity consumed per task execution
- Used for automatic inventory decrementing

---

## Scheduling & Workflow Terms

### Circular Trigger
When two or more tasks trigger each other (Task A triggers Task B, Task B triggers Task A). Allowed because:
- Tasks exist once in database with a single `nextDue` field
- Completing Task A updates Task B's `nextDue` to today
- Completing Task B updates Task A's `nextDue` to today
- No infinite loops because nextDue determines appearance, not completion

### Independent Schedule (Child Tasks)
A child task with its own interval separate from the parent. Behavior:
- Child appears on its own schedule even if parent is overdue
- Child completes independently but contributes to parent progress
- Child still displays nested under parent visually

**Example**: "Clean Toilet" (daily) is a child of "Clean Bathroom" (weekly). Toilet cleaning appears daily but still grouped under Bathroom.

### Slide-Forward Persistence
The behavior of tasks when a day passes without completion:

**POSTPONE behavior (default):**
- Task remains in Today view, sliding forward day-by-day
- `nextDue` stays at original due date until completed
- Visual indicator shows overdue status
- Task persists until user completes or manually skips it
- Best for: Daily habits, flexible tasks, important recurring items

**SKIP_TO_NEXT behavior:**
- When day advances without completion, task automatically jumps to next scheduled occurrence
- `nextDue` automatically advances based on task's recurrence pattern
- No manual intervention required
- Task disappears from Today view when day passes
- Best for: Scheduled appointments, optional activities, date-specific tasks

**Visual indicators:**
- Postponed tasks shown with overdue styling (different color/icon)
- No punishment or penalties, just persistence and clarity

### Today View
The main application interface showing only:
- Tasks due today (`nextDue <= today`)
- Overdue tasks (slide-forward)
- Triggered tasks that appeared today
- Independent children whose schedule elapsed

**NOT shown**: Future scheduled tasks, calendar views, planning interfaces

### Trigger / Trigger Task
A task that appears immediately when another task completes:
- Triggering task's `triggerTaskId` points to the triggered task
- On completion, triggered task's `nextDue` is set to current day
- Triggered task can be ADHOC or scheduled
- Incremental triggers: Complete task N times before triggering (via `triggerIncrementBy`)

**Example**: Completing "Cook Dinner" immediately spawns "Clean Up After Dinner" in Today view

---

## UI & Navigation Terms

### Category
User-defined grouping for tasks (e.g., "Cleaning", "Maintenance", "Kitchen"). Used for:
- Visual organization in Today view
- Filtering and searching
- Progress tracking per category

### Filter (Advanced Filtering)
Multi-dimensional task filtering in Today view:
- **Status Filters**: Pending, Completed, Overdue
- **Priority Filters**: High, Medium, Low
- **Special Filters**: Requires Inventory, Has Children, Has Triggers
- **Category Filters**: User-defined categories

### Task Detail Screen
Full-screen view showing comprehensive task information:
- All task properties
- Relationship visualization (parent/child/triggers)
- Inventory requirements
- Scheduling details
- Completion history

### Task Edit Screen
Interface for modifying existing tasks. Supports:
- Changing all task properties
- Reconfiguring schedules
- Managing relationships (parent/child, triggers)
- Adjusting inventory associations

---

## Data & Architecture Terms

### ChecklistItem vs Task
- **Task**: The permanent definition (what needs to be done)
- **ChecklistItem**: A daily instance (snapshot of task when it appeared in Today)

### Clean Architecture
Architectural pattern with three layers:
- **Presentation**: UI (Compose screens, ViewModels)
- **Domain**: Business logic (Use Cases, Scheduling Engine)
- **Data**: Persistence (Room database, Repositories)

### Repository Pattern
Data access abstraction layer. ViewModels never directly access DAOs, always through repositories. Benefits:
- Testability (mock repositories in tests)
- Flexibility (swap data sources)
- Encapsulation (hide database complexity)

### State Hoisting
Compose pattern where:
- Composables are stateless (receive state as parameters)
- State lives in ViewModel
- Composables emit events, ViewModel updates state
- Benefits: Reusability, testability, predictability

### StateFlow
Reactive state management primitive:
- ViewModel exposes immutable `StateFlow<UiState>`
- UI observes with `collectAsState()`
- Updates trigger automatic UI recomposition
- Lifecycle-aware (cancels on destroy)

### Use Case
Single-responsibility business operation. Examples:
- `GenerateToday`: Creates daily checklist
- `CompleteTask`: Handles task completion with all side effects
- `ProcessInventoryConsumption`: Updates inventory levels

---

## System Behavior Terms

### Advisory Inventory
Philosophy that inventory is informative but never blocking:
- Low inventory shows warnings
- Tasks can complete with zero inventory
- User decides whether to proceed or restock first

### Offline-First
Design principle where:
- App functions entirely without network
- All data stored locally
- No cloud dependencies
- Import/export for data portability

### Pull-Only Interaction
User-initiated workflow (opposite of push notifications):
- User opens app when ready
- System never sends reminders or nags (v1.0)
- Today view is always current when opened

### Today-Only Philosophy
Design principle where:
- App surfaces only what's actionable today
- No calendar/planning views
- No future task visibility
- Focus on execution, not planning

---