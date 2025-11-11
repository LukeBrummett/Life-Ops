# Task Edit Design

## Overview

The Task Detail and Task Edit/Create screens are the most complex interfaces in Life-Ops, providing comprehensive access to all task configuration options. These screens must expose the full flexibility of the task system while maintaining usability.

**Key Principle**: No unanswered questions - all current task configuration must be visible and editable.

---

## Screen Pair Architecture

### Two-Screen Pattern

**Task Detail Screen** (Read-Only View)
- Purpose: Display all task information in organized, scannable format
- Access: From Today screen, All Tasks screen
- Navigation: Edit button → Task Edit Screen

**Task Edit/Create Screen** (Editing Interface)
- Purpose: Modify task configuration with full relationship management
- Access: Edit button from Task Detail, FAB from Today screen
- Navigation: Save → Task Detail, Cancel → Previous screen

**Relationship**:
```
Today Screen → Task Detail Screen ⟷ Task Edit Screen
All Tasks   → Task Detail Screen ⟷ Task Edit Screen
Any Screen  → Task Create Screen → Task Detail Screen (after save)
```

---

## Task Detail Screen Specification

### Purpose
Display comprehensive technical information about a specific task in read-only format.

### Layout Structure

#### Header
```
┌─────────────────────────────────────────────────┐
│ ←  [Task Name]                         ✏️ Edit  │
└─────────────────────────────────────────────────┘
```
- Back button (top left): Returns to previous screen
- Task name (center/left)
- Edit button (top right): Navigates to Task Edit Screen

#### Content Sections

##### 1. Basic Information
**Always Visible:**
- Task name (emphasized)
- Category (with icon/color indicator)
- Tags (chips, scrollable if many)
- Priority (High/Medium/Low with visual indicator)
- Description/notes (expandable if long)

##### 2. Schedule Information
**Display Logic:**
- If task has schedule (not ADHOC):
  - Next scheduled date (prominent)
  - Recurrence pattern in plain English:
    - "Every 3 days"
    - "Every Monday, Wednesday, Friday"
    - "Every 2 weeks on Tuesday"
    - "Monthly on the 15th"
  - Overdue behavior: "Postpone day-by-day" or "Skip to next occurrence"
- If ADHOC:
  - "No automatic schedule (trigger-only)"
  - Last triggered date (if ever triggered)

**Schedule Exclusions (if configured):**
- "Never schedules on: Tuesday, Thursday"
- "Excluded dates: Dec 24-26, Jan 1"

**Task Lifecycle (if ephemeral):**
- "⏳ Ephemeral task - will be deleted after completion"
- Only shown when `deleteAfterCompletion = true`
- Indicates task will auto-delete during end-of-day processing after being marked complete

##### 3. Completion Data
**Always Show:**
- Last completed: Date and relative time ("3 days ago")
- Current streak: Number of consecutive completions
- Status: "Not yet due" | "Due today" | "Overdue by X days"

**Historical Context (Optional V2):**
- Total completions
- Average completion rate
- Streak history

##### 4. Relationship Information

**Parent Task (if child):**
```
┌─────────────────────────────────────┐
│ Part of: [Parent Task Name]        │
│ Order: 2 of 4 children              │
└─────────────────────────────────────┘
```
- Tappable to navigate to parent's detail screen
- Shows position in parent's child order

**Child Tasks (if parent):**
```
┌─────────────────────────────────────┐
│ Contains 4 child tasks:             │
│ 1. [Child Task A]                   │
│ 2. [Child Task B]                   │
│ 3. [Child Task C]                   │
│ 4. [Child Task D]                   │
│                                     │
│ ☐ Requires manual completion       │
└─────────────────────────────────────┘
```
- Numbered list in execution order
- Each child tappable (navigate to detail)
- Manual completion toggle status shown

**Triggered By:**
```
┌─────────────────────────────────────┐
│ Triggered by completing:            │
│ • [Task Name A]                     │
│ • [Task Name B]                     │
└─────────────────────────────────────┘
```
- List of tasks that spawn this one
- Each tappable to navigate

**Triggers:**
```
┌─────────────────────────────────────┐
│ Completing this triggers:           │
│ • [Task Name X]                     │
│ • [Task Name Y]                     │
└─────────────────────────────────────┘
```
- List of tasks spawned by this one
- Each tappable to navigate

##### 5. Inventory Associations

**If task consumes inventory:**
```
┌─────────────────────────────────────────────────┐
│ Consumes Inventory:                             │
│                                                 │
│ 📦 Coffee Filters                               │
│    Mode: Fixed (2 per execution)                │
│    Current stock: 8 filters                     │
│                                                 │
│ 📦 Ground Coffee                                │
│    Mode: Prompted (default: 30g)                │
│    Current stock: 450g                          │
│                                                 │
│ 📦 Milk                                         │
│    Mode: Recount                                │
│    Current stock: 2 cartons                     │
└─────────────────────────────────────────────────┘
```

**For each inventory item:**
- Item name (tappable → inventory detail)
- Consumption mode with details:
  - Fixed: Quantity consumed
  - Prompted: Default value for prompt
  - Recount: Creates recount task
- Current inventory level (color-coded if low)
- Warning if below reorder threshold

##### 6. Time Estimation
- Time estimate: "30 minutes" or "1 hour 15 min"
- OR Difficulty: Low/Medium/High with visual indicator
- Only show if configured (not required)

#### Actions Section (Bottom)

**Primary Actions:**
- Complete Task (only if due today or overdue)
- Edit Task (navigates to edit screen)
- Delete Task (confirmation dialog)

**Secondary Actions:**
- Skip to Tomorrow (if due)
- Won't Do (skip to next occurrence)
- View in All Tasks

### Visual Design Principles

**Scannable Layout:**
- Clear section headers
- Whitespace between sections
- Icons for quick recognition
- Color coding for status/priority

**Information Hierarchy:**
- Most important info at top (name, schedule, next due)
- Relationships and details in middle
- Actions at bottom

**Interactive Elements:**
- All related tasks are tappable (navigate to detail)
- All inventory items are tappable (navigate to inventory)
- Clear tap targets, minimum 48dp

**No Empty States:**
- Only show sections that have data
- "No child tasks" → hide section entirely
- "No inventory" → hide section entirely

---

## Task Edit/Create Screen Specification

### Purpose
User-friendly interface for creating new tasks or editing existing ones with full relationship and configuration options.

### Layout Structure

#### Header
```
┌─────────────────────────────────────────────────┐
│ ✕  [New Task | Edit: Task Name]        💾 Save │
└─────────────────────────────────────────────────┘
```
- X/Back button (top left): Cancel changes, confirmation if dirty
- Screen title: "New Task" or "Edit: [Task Name]"
- Save button (top right): Validates and saves

#### Content Sections (Scrollable)

The edit screen is organized into collapsible/expandable sections for easier navigation.

##### Section 1: Basic Information (Always Expanded)

**Task Name** (Required)
```
┌─────────────────────────────────────┐
│ Task Name                           │
│ ┌─────────────────────────────────┐ │
│ │ [Text Input]                    │ │
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```
- Text field, auto-focus on create
- Required field validation
- Character limit (e.g., 100 chars)

**Category**
```
┌─────────────────────────────────────┐
│ Category                            │
│ ┌─────────────────────────────────┐ │
│ │ [Dropdown/Selector]     [+New] │ │
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```
- Dropdown of existing categories
- "Add New Category" option (inline creation)
- Default to "Uncategorized" if none selected

**Tags**
```
┌─────────────────────────────────────┐
│ Tags                                │
│ ┌─────────────────────────────────┐ │
│ │ [Search/Add Tags]               │ │
│ └─────────────────────────────────┘ │
│ [Tag1 ×] [Tag2 ×] [Tag3 ×]         │
└─────────────────────────────────────┘
```
- Search existing tags or create new
- Chips display selected tags with remove button
- Auto-complete suggestions

**Priority**
```
┌─────────────────────────────────────┐
│ Priority                            │
│ ( ) High  (•) Medium  ( ) Low      │
└─────────────────────────────────────┘
```
- Radio buttons
- Default: Medium

**Description**
```
┌─────────────────────────────────────┐
│ Description / Notes                 │
│ ┌─────────────────────────────────┐ │
│ │ [Multi-line Text Input]         │ │
│ │                                 │ │
│ │                                 │ │
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```
- Multi-line text field
- Optional
- Character limit (e.g., 500 chars)

**Next Due Date**
```
┌─────────────────────────────────────┐
│ Next Due Date                       │
│ ┌─────────────────────────────────┐ │
│ │ [Date Picker]              📅   │ │
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```
- Date picker for selecting when task should first be due
- Defaults to today's date
- Only shown for scheduled tasks (hidden for ADHOC)
- Allows users to create tasks for future dates without needing to complete them to push the date forward

##### Section 2: Schedule Configuration (Collapsible)

**Recurrence Pattern**

**Interval Type Selection:**
```
┌─────────────────────────────────────┐
│ Recurrence                          │
│ ( ) Days   ( ) Weeks   ( ) Months   │
│ (•) ADHOC (trigger-only)            │
└─────────────────────────────────────┘
```

**If Days/Weeks/Months selected:**
```
┌─────────────────────────────────────┐
│ Every [3] days                      │
│       ↑                             │
│    Number picker                    │
└─────────────────────────────────────┘
```

**If Weeks selected (additional):**
```
┌─────────────────────────────────────┐
│ On these days:                      │
│ [ ] Mon  [ ] Tue  [✓] Wed           │
│ [ ] Thu  [✓] Fri  [ ] Sat  [ ] Sun │
└─────────────────────────────────────┘
```
- Checkboxes for each day
- Optional (defaults to all days if not specified)

**Schedule Exclusions (Optional Subsection)**

```
┌─────────────────────────────────────┐
│ Never Schedule On (Optional)        │
│ ▼ Expand to configure               │
└─────────────────────────────────────┘

[When Expanded]
┌─────────────────────────────────────┐
│ Excluded Days of Week:              │
│ [ ] Mon  [✓] Tue  [ ] Wed           │
│ [✓] Thu  [ ] Fri  [ ] Sat  [ ] Sun │
│                                     │
│ Excluded Date Ranges:               │
│ • Dec 24 - Dec 26  [Remove]         │
│ • Jan 1            [Remove]         │
│ [+ Add Date Range]                  │
└─────────────────────────────────────┘
```

**Overdue Behavior**
```
┌─────────────────────────────────────┐
│ When incomplete at end of day:      │
│ (•) Postpone (slide to tomorrow)    │
│ ( ) Skip to next occurrence         │
└─────────────────────────────────────┘
```
- Radio buttons
- Default: Postpone
- Only relevant if task has schedule (hidden for ADHOC)

**Task Lifecycle**
```
┌─────────────────────────────────────┐
│ [✓] Delete after completion         │
│    (one-time/ephemeral task)        │
└─────────────────────────────────────┘
```
- Checkbox (default: unchecked)
- When checked: Task auto-deletes during end-of-day processing after being completed
- Deletion occurs only after the task has been marked complete (`lastCompleted IS NOT NULL`)
- Happens during daily rollover, before processing overdue tasks
- Useful for one-off tasks like "Pick up dry cleaning", "Call dentist", "Buy birthday gift"
- Available for all task types (recurring or ADHOC)
- **Warning**: Deletion is permanent and cannot be undone - use for truly one-time tasks only

##### Section 3: Relationships (Collapsible)

**Parent-Child Relationships**

```
┌─────────────────────────────────────┐
│ ▼ Parent/Child Relationships        │
└─────────────────────────────────────┘

[When Expanded]
┌─────────────────────────────────────┐
│ Parent Task                         │
│ ┌─────────────────────────────────┐ │
│ │ [Search Tasks...]       [Clear]│ │
│ └─────────────────────────────────┘ │
│ Currently: [Parent Task Name]       │
│                                     │
│ [✓] Has independent schedule        │
│    (appears on own interval)        │
└─────────────────────────────────────┘
```

**Parent Task Search:**
- Searchable dropdown
- Filter by name, category, tag
- Shows existing parent if editing child
- Clear button to remove parent
- Checkbox: Independent schedule toggle

**If This Task is a Parent:**
```
┌─────────────────────────────────────┐
│ Child Tasks                         │
│ ┌─────────────────────────────────┐ │
│ │ [Search Tasks...]        [Add] │ │
│ └─────────────────────────────────┘ │
│                                     │
│ Current children (drag to reorder): │
│ ≡ 1. [Child Task A]        [Remove]│
│ ≡ 2. [Child Task B]        [Remove]│
│ ≡ 3. [Child Task C]        [Remove]│
│                                     │
│ [✓] Require manual completion       │
│    (after all children done)        │
└─────────────────────────────────────┘
```

**Child Task Management:**
- Search to add children
- Drag handles (≡) for reordering
- Remove button for each child
- Manual completion toggle (checkbox)

**Trigger Relationships**

```
┌─────────────────────────────────────┐
│ Triggered By                        │
│ ┌─────────────────────────────────┐ │
│ │ [Search Tasks...]        [Add] │ │
│ └─────────────────────────────────┘ │
│                                     │
│ This task appears when completing:  │
│ • [Task Name A]            [Remove]│
│ • [Task Name B]            [Remove]│
└─────────────────────────────────────┘
```

```
┌─────────────────────────────────────┐
│ Triggers                            │
│ ┌─────────────────────────────────┐ │
│ │ [Search Tasks...]        [Add] │ │
│ └─────────────────────────────────┘ │
│                                     │
│ Completing this task triggers:      │
│ • [Task Name X]            [Remove]│
│ • [Task Name Y]            [Remove]│
└─────────────────────────────────────┘
```

**Trigger Management:**
- Search to add triggered/triggering tasks
- Multi-select allowed (task can have multiple triggers)
- Remove button for each relationship
- Warning icon if circular triggers detected (allowed but noted)

##### Section 4: Inventory Configuration (Collapsible)

```
┌─────────────────────────────────────┐
│ ▼ Inventory Consumption             │
└─────────────────────────────────────┘

[When Expanded]
┌─────────────────────────────────────────────────┐
│ ┌─────────────────────────────────────────────┐ │
│ │ [Search Inventory Items...]          [Add]│ │
│ └─────────────────────────────────────────────┘ │
│                                                 │
│ Consumes:                                       │
│                                                 │
│ 📦 Coffee Filters                               │
│    ( ) Fixed  (•) Prompted  ( ) Recount         │
│    Quantity: [2] filters                        │
│    [Remove Item]                                │
│                                                 │
│ 📦 Ground Coffee                                │
│    ( ) Fixed  (•) Prompted  ( ) Recount         │
│    Default: [30] grams                          │
│    [Remove Item]                                │
│                                                 │
│ 📦 Milk                                         │
│    ( ) Fixed  ( ) Prompted  (•) Recount         │
│    [Remove Item]                                │
└─────────────────────────────────────────────────┘
```

**For Each Inventory Item:**

**Item Header:**
- Item name with icon
- Current stock level (read-only info)

**Consumption Mode (Radio Buttons):**
- Fixed: Shows quantity input
- Prompted: Shows default value input
- Recount: No additional input

**Quantity/Default Input:**
- Number picker or text input
- Unit label from inventory item
- Only shown for Fixed/Prompted modes

**Actions:**
- Remove item button

**Inventory Item Search:**
- Search by name, category, tag
- Shows current stock levels in results
- Filter by low stock (optional)

##### Section 5: Time Estimation (Collapsible)

```
┌─────────────────────────────────────┐
│ ▼ Time Estimation                   │
└─────────────────────────────────────┘

[When Expanded]
┌─────────────────────────────────────┐
│ Estimation Type:                    │
│ ( ) Time Duration                   │
│ (•) Difficulty Level                │
│ ( ) None                            │
│                                     │
│ [If Time Duration selected]         │
│ Hours: [1]  Minutes: [30]           │
│                                     │
│ [If Difficulty selected]            │
│ ( ) Low  (•) Medium  ( ) High      │
└─────────────────────────────────────┘
```

**Options:**
- None (default)
- Time duration (hour/minute pickers)
- Difficulty level (Low/Medium/High)
- Mutually exclusive

#### Bottom Actions

```
┌─────────────────────────────────────┐
│                                     │
│  [Delete Task]      [Cancel] [Save]│
└─────────────────────────────────────┘
```

**Delete Task** (only when editing):
- Confirmation dialog
- Warns if task has relationships
- Offers to reassign/remove relationships

**Cancel:**
- Confirmation if changes made
- Returns to previous screen

**Save:**
- Validation (required fields)
- Returns to Task Detail screen
- Shows success feedback

---

## Search Functionality

### Task Search (Used in Relationship Config)

**Features:**
- Search by name (fuzzy matching)
- Filter by category
- Filter by tags
- Show task preview (category, tags, schedule summary)

**Results Display:**
```
┌─────────────────────────────────────┐
│ [Search: "clean"]                   │
│                                     │
│ Clean Bathroom                      │
│ Household · Every 3 days            │
│                                     │
│ Clean Kitchen                       │
│ Household · Mon, Wed, Fri           │
│                                     │
│ Clean Up After Dinner               │
│ Household · ADHOC (trigger-only)    │
└─────────────────────────────────────┘
```

**Exclusions:**
- Don't show task being edited (can't be own parent/child)
- Don't show tasks that would create invalid relationships

### Inventory Search (Used in Inventory Config)

**Features:**
- Search by name
- Filter by category
- Filter by low stock
- Show current stock levels

**Results Display:**
```
┌─────────────────────────────────────┐
│ [Search: "coffee"]                  │
│                                     │
│ Coffee Filters                      │
│ Kitchen · 8 filters in stock        │
│                                     │
│ Ground Coffee                       │
│ Kitchen · 450g in stock ⚠️ Low      │
└─────────────────────────────────────┘
```

---

## Validation Rules

### Required Fields
- Task name (non-empty, trimmed)

### Schedule Validation
- If interval-based: intervalQty > 0
- If weekly with specific days: at least one day selected
- ADHOC tasks can have triggers but don't require them

### Relationship Validation
- No circular parent-child (Task A parent of Task B, Task B parent of Task A)
- Child order must be unique within parent
- Trigger validation: Circular triggers allowed but warned

### Inventory Validation
- Fixed mode: quantity > 0
- Prompted mode: default value >= 0 (can be 0)
- Recount mode: no quantity validation needed

### Cross-Field Validation
- Can't have both time estimate AND difficulty (mutually exclusive)
- If parent task: can't also be child of own children
- If ADHOC: overdue behavior irrelevant (hidden)

---

## State Management

### ViewModel Structure

```kotlin
data class TaskEditUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isNewTask: Boolean = true,
    val hasUnsavedChanges: Boolean = false,
    
    // Basic Info
    val taskName: String = "",
    val category: String = "",
    val tags: List<String> = emptyList(),
    val priority: Int = 2,
    val description: String = "",
    
    // Schedule
    val intervalUnit: String = "DAY",
    val intervalQty: Int = 1,
    val specificDaysOfWeek: List<String> = emptyList(),
    val excludedDaysOfWeek: List<String> = emptyList(),
    val excludedDateRanges: List<DateRange> = emptyList(),
    val overdueBehavior: String = "POSTPONE",
    val deleteAfterCompletion: Boolean = false,
    
    // Relationships
    val parentTaskId: String? = null,
    val parentTaskName: String? = null,
    val hasIndependentSchedule: Boolean = false,
    val childTasks: List<ChildTaskItem> = emptyList(),
    val requiresManualCompletion: Boolean = false,
    val triggeredByTasks: List<TaskSummary> = emptyList(),
    val triggersTasks: List<TaskSummary> = emptyList(),
    
    // Inventory
    val inventoryItems: List<TaskSupplyConfig> = emptyList(),
    
    // Time Estimation
    val estimationType: EstimationType = EstimationType.NONE,
    val timeEstimateMinutes: Int? = null,
    val difficulty: String? = null,
    
    // UI State
    val expandedSections: Set<String> = emptySet(),
    val validationErrors: Map<String, String> = emptyMap(),
    val availableCategories: List<String> = emptyList(),
    val availableTags: List<String> = emptyList(),
    
    // Error handling
    val errorMessage: String? = null
)

data class ChildTaskItem(
    val taskId: String,
    val taskName: String,
    val order: Int,
    val category: String,
    val scheduleSummary: String
)

data class TaskSummary(
    val taskId: String,
    val taskName: String,
    val category: String,
    val scheduleSummary: String
)

data class TaskSupplyConfig(
    val supplyId: String,
    val supplyName: String,
    val currentStock: Int,
    val unit: String,
    val consumptionMode: String, // FIXED, PROMPTED, RECOUNT
    val quantity: Int? = null, // For FIXED
    val defaultValue: Int? = null // For PROMPTED
)

enum class EstimationType {
    NONE, TIME_DURATION, DIFFICULTY
}
```

### ViewModel Events

```kotlin
sealed class TaskEditEvent {
    // Basic Info
    data class UpdateTaskName(val name: String) : TaskEditEvent()
    data class UpdateCategory(val category: String) : TaskEditEvent()
    data class AddTag(val tag: String) : TaskEditEvent()
    data class RemoveTag(val tag: String) : TaskEditEvent()
    data class UpdatePriority(val priority: Int) : TaskEditEvent()
    data class UpdateDescription(val description: String) : TaskEditEvent()
    
    // Schedule
    data class UpdateIntervalUnit(val unit: String) : TaskEditEvent()
    data class UpdateIntervalQty(val qty: Int) : TaskEditEvent()
    data class ToggleWeekday(val day: String) : TaskEditEvent()
    data class ToggleExcludedDay(val day: String) : TaskEditEvent()
    data class AddExcludedDateRange(val range: DateRange) : TaskEditEvent()
    data class RemoveExcludedDateRange(val range: DateRange) : TaskEditEvent()
    data class UpdateOverdueBehavior(val behavior: String) : TaskEditEvent()
    data class ToggleDeleteAfterCompletion(val enabled: Boolean) : TaskEditEvent()
    
    // Relationships
    data class SetParentTask(val taskId: String?, val taskName: String?) : TaskEditEvent()
    data class ToggleIndependentSchedule(val enabled: Boolean) : TaskEditEvent()
    data class AddChildTask(val task: TaskSummary) : TaskEditEvent()
    data class RemoveChildTask(val taskId: String) : TaskEditEvent()
    data class ReorderChildren(val newOrder: List<ChildTaskItem>) : TaskEditEvent()
    data class ToggleManualCompletion(val enabled: Boolean) : TaskEditEvent()
    data class AddTriggeredBy(val task: TaskSummary) : TaskEditEvent()
    data class RemoveTriggeredBy(val taskId: String) : TaskEditEvent()
    data class AddTriggers(val task: TaskSummary) : TaskEditEvent()
    data class RemoveTriggers(val taskId: String) : TaskEditEvent()
    
    // Inventory
    data class AddInventoryItem(val item: TaskSupplyConfig) : TaskEditEvent()
    data class RemoveInventoryItem(val supplyId: String) : TaskEditEvent()
    data class UpdateInventoryMode(val supplyId: String, val mode: String) : TaskEditEvent()
    data class UpdateInventoryQuantity(val supplyId: String, val quantity: Int) : TaskEditEvent()
    
    // Time Estimation
    data class UpdateEstimationType(val type: EstimationType) : TaskEditEvent()
    data class UpdateTimeEstimate(val hours: Int, val minutes: Int) : TaskEditEvent()
    data class UpdateDifficulty(val difficulty: String) : TaskEditEvent()
    
    // UI
    data class ToggleSection(val section: String) : TaskEditEvent()
    
    // Actions
    object Save : TaskEditEvent()
    object Cancel : TaskEditEvent()
    object Delete : TaskEditEvent()
}
```

---

## Navigation & Data Flow

### Creating New Task

```
Today Screen (FAB) → Task Create Screen
                     ↓ (Save)
                     Task Detail Screen (new task)
                     ↓ (Back)
                     Today Screen (task now visible if due)
```

**Data Flow:**
1. User taps FAB on Today screen
2. Navigate to Task Edit screen with `taskId = null`
3. ViewModel initializes empty state
4. User fills in fields
5. User taps Save
6. ViewModel validates input
7. If valid: Save to database, navigate to Task Detail with new taskId
8. If invalid: Show errors, stay on edit screen

### Editing Existing Task

```
Today/All Tasks → Task Detail Screen → Task Edit Screen
                                        ↓ (Save)
                                        Task Detail Screen (updated)
```

**Data Flow:**
1. User taps Edit on Task Detail screen
2. Navigate to Task Edit screen with `taskId = [existing]`
3. ViewModel loads task data from database
4. Populate all fields with current values
5. User modifies fields
6. User taps Save
7. ViewModel validates changes
8. If valid: Update database, navigate back to Task Detail
9. If invalid: Show errors, stay on edit screen

### Unsaved Changes Handling

**When user taps Cancel or Back:**
```kotlin
if (uiState.hasUnsavedChanges) {
    showDialog {
        "Discard unsaved changes?"
        [Cancel] [Discard]
    }
} else {
    navigateBack()
}
```

**Change Detection:**
- Compare current state to original loaded state
- Mark dirty on any field modification
- Clear dirty flag on successful save

---

## UI/UX Considerations

### Progressive Disclosure

**Collapsed by Default:**
- Schedule Exclusions
- Relationship sections (if empty)
- Inventory section (if empty)
- Time Estimation (if not set)

**Always Visible:**
- Basic Information section
- Save/Cancel actions

**Expand on Content:**
- If section has data, expand it on load
- If user adds data, auto-expand section

### Input Validation Feedback

**Real-time Validation:**
- Task name: Show character count, error if empty on blur
- Numbers: Prevent negative values
- Required fields: Mark with asterisk

**Save-time Validation:**
- Check all required fields
- Validate relationships (no circular parents)
- Show errors inline near relevant fields
- Scroll to first error
- Disable Save button until valid

### Loading States

**Initial Load (Edit Mode):**
```
┌─────────────────────────────────────┐
│        Loading task details...      │
│          [Spinner]                  │
└─────────────────────────────────────┘
```

**Saving State:**
```
┌─────────────────────────────────────┐
│ ✕  Edit: Task Name         Saving...│
└─────────────────────────────────────┘
```
- Disable all inputs during save
- Show spinner on Save button
- Prevent navigation during save

### Search Performance

**Debounced Search:**
- Wait 300ms after last keystroke
- Show loading indicator in dropdown
- Limit results to 20 items
- Show "X more results" if limited

**Empty States:**
- "No tasks found" with suggestion to check filters
- "No inventory items found" with link to add new

### Relationship Warnings

**Circular Trigger Warning:**
```
┌─────────────────────────────────────┐
│ ⚠️ Circular trigger detected         │
│ This task and [Other Task] trigger  │
│ each other. This is allowed.        │
└─────────────────────────────────────┘
```

**Parent-Child Conflict:**
```
┌─────────────────────────────────────┐
│ ❌ Invalid parent-child relationship │
│ Task cannot be parent of itself or  │
│ ancestor tasks.                     │
└─────────────────────────────────────┘
```

### Deletion Confirmation

**If task has relationships:**
```
┌─────────────────────────────────────┐
│ Delete Task?                        │
│                                     │
│ This task is:                       │
│ • Parent of 3 child tasks           │
│ • Triggered by "Cook Dinner"        │
│ • Triggers "Clean Up"               │
│                                     │
│ Deleting will:                      │
│ • Orphan child tasks                │
│ • Remove trigger relationships      │
│                                     │
│ This cannot be undone.              │
│                                     │
│         [Cancel] [Delete]           │
└─────────────────────────────────────┘
```

**Note on Ephemeral Tasks:**
- Tasks marked as "Delete after completion" (ephemeral) will automatically delete themselves during end-of-day processing after being completed
- No user confirmation is required for automatic deletion
- Manual deletion via this dialog follows the same confirmation flow as non-ephemeral tasks

---

## Accessibility

### Screen Reader Support
- All form fields have labels
- Validation errors announced
- Section expand/collapse announced
- Relationship lists with item counts

### Keyboard Navigation
- Tab order follows visual layout
- Enter to save
- Escape to cancel
- Arrow keys for number pickers

### Touch Targets
- Minimum 48dp tap targets
- Adequate spacing between controls
- Drag handles for child reordering (64dp)

---

## Performance Considerations

### Database Queries

**On Load (Edit Mode):**
- Single query to load task with all relationships
- Separate query for available categories/tags
- Lazy load search results (not on open)

**On Save:**
- Transaction for all changes
- Batch insert/update for relationships
- Single commit

### Memory Management

**Search Results:**
- Limit to 20 items
- Release when section collapsed
- Debounce to prevent excessive queries

**Large Lists:**
- Use LazyColumn for child tasks if > 10
- Virtualize inventory item list if > 20

---

## Future Enhancements (Post-V1)

### Task Templates
- Save common configurations as templates
- Quick-create from template
- Template management screen

### Bulk Edit
- Multi-select tasks in All Tasks view
- Edit common properties (category, tags, priority)
- Batch relationship management

### Schedule Preview
- Calendar view showing next 10 occurrences
- Visual representation of exclusions
- Conflict detection (too many tasks on one day)

### Advanced Scheduling
- Specific dates (not just intervals)
- Monthly on specific week/day (e.g., "2nd Tuesday")
- Seasonal scheduling

### Inventory Insights
- Show which tasks consume each item
- Predicted inventory depletion date
- Suggested reorder points based on usage

---

## Open Questions / Decisions Needed

1. **Multiple Parents:**
   - Data model supports multiple parents (JSON array)
   - UI complexity: Should V1 support this?
   - Proposal: V1 single parent, V2 multiple

2. **Independent Schedule UI:**
   - Child has own schedule: should we show schedule config inline or require separate editing?
   - Proposal: Toggle only in V1, full schedule in Task Detail

3. **Task Duplication:**
   - Should there be "Duplicate Task" button?
   - Useful for creating similar tasks quickly
   - Proposal: V2 feature

4. **Undo Delete:**
   - Should deleted tasks be soft-deleted with recovery option?
   - Proposal: V1 hard delete, V2 trash/archive
   - Note: Ephemeral tasks (deleteAfterCompletion) are permanently deleted during end-of-day processing with no recovery option

5. **Relationship Visualization:**
   - Should we show relationship graph/tree?
   - Helpful for complex hierarchies
   - Proposal: V2 visualization screen

---

## Implementation Phases

### Phase 1: Task Detail Screen (Read-Only)
- Basic information display
- Schedule information formatting
- Completion data display
- Static relationship lists (not interactive)
- Static inventory display
- Navigation structure

### Phase 2: Task Edit - Basic Fields
- Name, category, tags, priority, description
- Schedule configuration (interval, exclusions, overdue behavior)
- Time estimation
- Save/cancel/delete actions
- Validation

### Phase 3: Task Edit - Relationships
- Parent task selection
- Child task management (add, remove, reorder)
- Trigger configuration (triggered by, triggers)
- Relationship validation
- Search functionality

### Phase 4: Task Edit - Inventory
- Inventory item search
- Add/remove items
- Consumption mode configuration
- Quantity/default value inputs
- Current stock display

### Phase 5: Integration & Polish
- Navigation wiring
- State persistence
- Loading states
- Error handling
- Accessibility
- Performance optimization

---

## Summary

The Task Detail and Task Edit screens are the heart of task configuration in Life-Ops. They must:

1. **Expose everything**: All task configuration visible and editable
2. **Stay organized**: Collapsible sections prevent overwhelming users
3. **Validate thoroughly**: Catch errors early with helpful messages
4. **Support relationships**: Full parent-child and trigger management
5. **Handle complexity**: Gracefully manage intricate task configurations
6. **Perform well**: Fast load and save even with many relationships

These screens transform the data model's flexibility into a usable interface, enabling users to create precisely the task behaviors they need without requiring programming knowledge.
