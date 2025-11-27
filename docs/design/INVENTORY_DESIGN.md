# Inventory Screen Design

**Screen Type**: Data Management / Shopping Support  
**Purpose**: Manage consumable supplies, track quantities, and facilitate shopping/restocking workflows  
**Status**: Not Started  
**Last Updated**: October 25, 2025

---

## Table of Contents
1. [Overview](#overview)
2. [Screen Layout](#screen-layout)
3. [Component Specifications](#component-specifications)
4. [Workflow Details](#workflow-details)
5. [Shopping Workflow](#shopping-workflow)
6. [Restock Workflow](#restock-workflow)
7. [Navigation Flow](#navigation-flow)
8. [Implementation Checklist](#implementation-checklist)

---

## Overview

The Inventory Screen is the central hub for managing all consumable supplies used by tasks throughout the application. It provides visibility into current stock levels, facilitates shopping list generation, and supports restocking workflows.

### Design Principles

1. **Quick Visibility**: At-a-glance view of current inventory levels
2. **Shopping Support**: Easy identification of items needing reorder
3. **Manual Control**: User-initiated shopping workflow (not automatic)
4. **Category Organization**: Logical grouping of related items
5. **Task Integration**: Clear connection between inventory and tasks that consume them

### Key Features

- **Inventory List**: View all supplies with current quantities and reorder status
- **Quick Adjustments**: Increment/decrement quantities without full edit
- **Shopping Workflow**: Generate shopping list from items below reorder threshold
- **Restock Tasks**: Automatic task creation after shopping for updating quantities
- **Item Management**: Add, edit, delete supplies with full configuration
- **Search & Filter**: Find items quickly by name, category, or reorder status
- **Task Associations**: View which tasks consume each supply

---

## Screen Layout

### Visual Hierarchy

```
┌─────────────────────────────────────────────────────────┐
│                      HEADER                             │
│  ← Back         Inventory              🛒 Go Shopping   │
├─────────────────────────────────────────────────────────┤
│  🔍 Search...                    [🔽 Sort] [⚙️ Filter]  │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  📦 HOUSEHOLD (5 items)                        [v]      │
│  ┌─────────────────────────────────────────────────┐   │
│  │  Air Filters                      ⚠️  2  [ + ]  │   │
│  │  Reorder at 3 • Target 10               [ - ]  │   │
│  └─────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────┐   │
│  │  Trash Bags                       ✅ 25  [ + ]  │   │
│  │  Reorder at 10 • Target 30              [ - ]  │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  🍽️ KITCHEN (8 items)                          [>]     │
│  ┌─────────────────────────────────────────────────┐   │
│  │  Coffee Filters                   ⚠️  1  [ + ]  │   │
│  │  Reorder at 5 • Target 20               [ - ]  │   │
│  └─────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────┐   │
│  │  Dish Soap                        ✅  2  [ + ]  │   │
│  │  Reorder at 1 • Target 3                [ - ]  │   │
│  └─────────────────────────────────────────────────┘   │
│  [... more items ...]                                   │
│                                                         │
│  🧹 CLEANING (3 items)                         [>]     │
│                                                         │
│                                           [➕ FAB]      │
└─────────────────────────────────────────────────────────┘
```

### Empty State

```
┌─────────────────────────────────────────────────────────┐
│                      HEADER                             │
│  ← Back         Inventory              🛒 Go Shopping   │
├─────────────────────────────────────────────────────────┤
│                                                         │
│                                                         │
│                      📦                                 │
│                                                         │
│              No Inventory Items                         │
│                                                         │
│     Tap ➕ to start tracking supplies                   │
│                                                         │
│                                                         │
│                                           [➕ FAB]      │
└─────────────────────────────────────────────────────────┘
```

---

## Component Specifications

### Header Bar
**Component**: `CenterAlignedTopAppBar`

**Elements**:
- Back button (left) - Returns to Today Screen
- Title: "Inventory" (center)
- "Go Shopping" button (right) - Initiates shopping workflow

**Floating Action Button**:
- Standard Material Design FAB (bottom right)
- Add icon (➕)
- Opens Supply Edit Screen (create mode)

**Behavior**:
- Back button returns to previous screen (typically Today Screen)
- Go Shopping button opens Shopping Workflow Screen
- FAB opens Supply Edit Screen for creating new supply

**Visual Design**:
- Shopping cart icon with badge showing count of items needing reorder
- FAB positioned in bottom-right corner (standard Material Design placement)

---

### Search & Filter Bar

**Layout**: Matches All Tasks screen implementation

**Search Field**:
- Hint text: "Search by name..."
- Real-time filtering as user types
- Searches across:
  - Supply name
  - Category
  - Tags
  - Notes

**Sort Dropdown** (right side):
- Icon: 🔽 with "Sort" label
- Opens bottom sheet with sort options:
  - Category (default)
  - Name (A-Z)
  - Name (Z-A)
  - Quantity (Low to High)
  - Quantity (High to Low)
  - Reorder Urgency
- Selected option shown with checkmark
- Dismisses on selection

**Filter Button** (right side):
- Icon: ⚙️ with "Filter" label
- Opens bottom sheet with filter options:
  - ☐ Needs Reorder (below threshold)
  - ☐ Well Stocked (above threshold)
  - ☐ Has Task Associations
  - Category filters (dynamic based on existing categories)
- Multiple selections allowed
- "Clear All" and "Apply" buttons at bottom
- Active filter count badge on button when filters applied

**Visual Design**:
- Consistent with All Tasks screen styling
- Sort and Filter buttons side-by-side on right
- Search bar takes remaining space on left

---

### Category Section

**Display**:
- Category icon and name
- Item count in category
- Expand/collapse indicator

**Behavior**:
- Tap to expand/collapse category
- Collapsible state persists during session
- Default: All categories expanded

**Visual Design**:
- Category header with distinct background color
- Chevron icon indicates expanded/collapsed state
- Badge shows item count

---

### Supply Item Card

**Primary Display**:
- Supply name (bold, left-aligned)
- Status indicator and quantity (right side, same line):
  - ⚠️ Red warning if below reorder threshold
  - ✅ Green checkmark if above threshold
  - Quantity number (no unit displayed)
- Plus button [ + ] (top right)
- Minus button [ - ] (below plus button, right-aligned)
- Reorder information line (second line, left-aligned):
  - "Reorder at [threshold] • Target [target]"

**Layout**:
```
┌─────────────────────────────────────────────────┐
│  Air Filters                      ⚠️  2  [ + ]  │
│  Reorder at 3 • Target 10               [ - ]  │
└─────────────────────────────────────────────────┘
```

**Behavior**:
- Tap anywhere on card (except +/- buttons) → Opens Supply Edit Screen
- Plus button: Increment quantity by 1
- Minus button: Decrement quantity by 1 (disabled at 0)
- Quantity updates immediately with database write
- Visual feedback on quantity change (brief highlight animation)

**Visual Design**:
- Card with subtle elevation
- Warning color (orange/red) tint when below threshold
- Success color (green) tint when well-stocked
- +/- buttons stacked vertically on right side
- Consistent spacing matching All Tasks item cards

---

### Supply Detail/Edit Screen

**Purpose**: View and edit complete supply configuration

**Header**:
- Back button (cancel changes)
- Title: "Edit Supply" or "Add Supply"
- Delete button (edit mode only)
- Save button

**Fields**:

**Basic Information**:
- Supply name (required)
- Category (dropdown/autocomplete)
- Tags (multi-select or comma-separated)
- Unit of measurement (dropdown: count, oz, liters, bottles, rolls, etc.)
- Notes/description

**Reorder Configuration**:
- Reorder threshold (number input)
  - Hint: "Show in shopping list when quantity falls below..."
- Reorder target quantity (number input)
  - Hint: "Ideal quantity after restocking"
- Validation: Target must be ≥ threshold

**Current Quantity**:
- Current on-hand quantity (number input)
- Last updated timestamp (read-only)

**Task Associations** (Read-Only):
- List of tasks that consume this supply
- Each task shows:
  - Task name
  - Consumption mode (Fixed/Prompted/Recount)
  - Quantity per execution (if fixed)
- Tap task to navigate to Task Detail Screen

**Purchase Information** (Future):
- Purchase URL (optional)
- Preferred store/location
- Average cost

**Interactions**:
- Save button validates and saves changes
- Delete button shows confirmation dialog
- Cannot delete if tasks depend on supply (show warning with task list)

---

## Workflow Details

### Quick Quantity Adjustment

**User Goal**: Quickly update inventory without full edit

**Flow**:
1. User finds item in inventory list
2. User taps + or - button on the item card
3. Quantity updates immediately
4. Brief visual feedback (highlight/animation)
5. Database updated in background

**Edge Cases**:
- Cannot decrement below 0 (minus button disabled)
- Large quantities can be adjusted (no limit)
- Network-independent (offline operation)

---

### Navigate to Supply Edit

**User Goal**: View or edit supply configuration

**Flow**:
1. User taps anywhere on supply card (except +/- buttons)
2. Supply Edit Screen opens with all current values
3. User can view or modify any field
4. User taps Save or Back
5. Changes saved, returns to Inventory Screen

---

### Add New Supply

**User Goal**: Create new inventory item for tracking

**Flow**:
1. User taps FAB (➕) in bottom right
2. Supply Edit Screen opens (create mode)
3. User enters:
   - Name (required)
   - Category
   - Unit of measurement
   - Reorder threshold
   - Target quantity
   - Initial quantity
4. User taps Save
5. Validation checks:
   - Name not empty
   - Target ≥ threshold
6. Success → returns to Inventory Screen with new item visible
7. Failure → shows validation errors

**Validation Rules**:
- Name required (not empty)
- Reorder threshold ≥ 0
- Reorder target ≥ reorder threshold
- Initial quantity ≥ 0

---

### Edit Existing Supply

**User Goal**: Update supply configuration or current quantity

**Flow**:
1. User taps anywhere on supply card (except +/- buttons)
2. Supply Edit Screen opens (edit mode)
3. All current values pre-populated
4. User modifies desired fields
5. User taps Save
6. Validation runs
7. Success → returns to Inventory Screen
8. Failure → shows validation errors

**Special Considerations**:
- Changing category re-groups item in list
- Changing threshold may affect shopping list
- Quantity changes update "last updated" timestamp

---

### Delete Supply

**User Goal**: Remove unused supply from tracking

**Flow**:
1. User opens Supply Edit Screen
2. User taps Delete button
3. System checks for task dependencies
4. **If no dependencies**:
   - Confirmation dialog: "Delete [name]? This cannot be undone."
   - Confirm → item deleted, return to Inventory Screen
   - Cancel → stay on edit screen
5. **If task dependencies exist**:
   - Warning dialog: "Cannot delete [name]. The following tasks use this supply:"
   - List of dependent tasks shown
   - Only option: "OK" (returns to edit screen)
   - User must remove supply from all tasks before deletion

---

### View Task Associations

**User Goal**: Understand which tasks use a supply

**Flow**:
1. User opens Supply Edit Screen
2. Task Associations section shows list of tasks
3. Each task card shows:
   - Task name
   - Consumption mode
   - Quantity consumed
4. User taps task card
5. Navigates to Task Detail Screen
6. User can edit task to remove supply association if desired

---

## Shopping Workflow

### Initiating Shopping

**Trigger**: User taps "Go Shopping" button in Inventory Screen header

**Purpose**: Create a shopping list for items needing reorder

**Flow**:
1. User taps "Go Shopping" button
2. System queries inventory for items where `currentQuantity < reorderThreshold`
3. If no items need reorder:
   - Show toast: "All supplies well-stocked! 🎉"
   - Stay on Inventory Screen
4. If items need reorder:
   - Navigate to Shopping Workflow Screen
   - Show checklist of items

---

### Shopping Workflow Screen

**Purpose**: Grocery list interface for shopping session

**Layout**:
```
┌─────────────────────────────────────────────────────────┐
│                 Shopping List                           │
│  X Cancel                      💾 Pause  ✅ Done        │
├─────────────────────────────────────────────────────────┤
│  12 items need restocking                               │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  📦 HOUSEHOLD (3 items)                        [v]      │
│  ┌─────────────────────────────────────────────────┐   │
│  │  ☐ Air Filters                                  │   │
│  │     Current: 2 • Need: 8 more                   │   │
│  └─────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────┐   │
│  │  ☐ Trash Bags                                   │   │
│  │     Current: 6 • Need: 24 more                  │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  🍽️ KITCHEN (5 items)                          [>]     │
│  ┌─────────────────────────────────────────────────┐   │
│  │  ☑ Coffee Filters                               │   │
│  │     Current: 1 • Need: 19 more                  │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

**Header Buttons**:
- **Cancel** (X) - Discard shopping session, return to Inventory Screen
- **Pause** (💾) - Save current selections, return to Inventory Screen
- **Done** (✅) - Complete shopping, create Restock Task

**Item Display**:
- Checkbox for each item
- Item name
- Current quantity (updates in real-time if user adjusts inventory)
- Suggested purchase quantity: `reorderTargetQuantity - currentQuantity`
- Organized by category (collapsible)

**Interactions**:
- Tap item or checkbox to toggle selection
- Check items as you add them to physical shopping cart
- Uncheck if you decide not to purchase
- Categories collapse/expand to manage long lists
- **Cancel**: Returns to Inventory Screen, shopping session discarded
- **Pause**: Saves current checkbox states, returns to Inventory Screen
- **Done Shopping**: Proceeds to Restock Task creation with checked items

**Pause Functionality**:
- Saves shopping session state (which items are checked)
- Returns to Inventory Screen
- Shopping badge/indicator shows "In Progress"
- Tapping "Go Shopping" again resumes session:
  - Restores previously checked items
  - Updates current quantities (reflects any inventory changes)
  - Updates suggested quantities (recalculates based on new current values)
- User can continue shopping where they left off

**Real-Time Updates**:
- If user navigates back to Inventory Screen (via Pause or Back button)
- And adjusts quantities using +/- buttons
- When resuming shopping:
  - Current quantities refresh
  - Suggested purchase quantities recalculate
  - Previously checked items remain checked
  - May show/hide items if they move above/below threshold

**Completion Flow**:
1. User checks off items acquired during shopping
2. User taps "Done Shopping" button
3. System saves checked items as pending restock
4. Shopping session cleared
5. Returns to Inventory Screen
6. Restock button appears in header with badge showing item count
7. Toast: "Shopping complete! [N] items ready to restock."

---

## Restock Workflow

### Restock Button in Header

**Purpose**: Provide quick access to restock pending items from last shopping session

**Trigger**: 
- Automatically appears after completing Shopping Workflow
- Only visible when there are pending restock items
- Positioned to the left of the shopping cart button

**Visual Design**:
- Inventory icon (📦) with badge showing count of pending items
- Badge displays number of items waiting to be restocked
- Colored with secondary theme color to draw attention
- Only visible when `pendingRestockItems` is not empty
- Hidden during shopping mode

**Behavior**:
- Tap button to navigate to Restock Inventory Screen
- Badge count updates in real-time as items are restocked
- Button disappears when all items are restocked
- State persists across app restarts via SavedStateHandle

### Restock Inventory Screen

**Purpose**: Update inventory quantities after shopping

**Trigger**: 
- User taps Restock button in Inventory Screen header
- OR automatically created when a task with RECOUNT mode inventory completes

**Navigation**: Opens as dedicated screen (not a task)

**Layout**:
```
┌─────────────────────────────────────────────────────────┐
│              Restock Inventory                          │
│  X Cancel                                    ✅ Complete │
├─────────────────────────────────────────────────────────┤
│  Update quantities for purchased items                  │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  📦 HOUSEHOLD (2 items)                        [v]      │
│  ┌─────────────────────────────────────────────────┐   │
│  │  Air Filters                                    │   │
│  │  Current: 2 • Target: 10 • Suggested: +8       │   │
│  │  ☐ Done                                 10      │   │
│  │                                        [ + ]    │   │
│  │                                        [ - ]    │   │
│  └─────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────┐   │
│  │  Coffee Filters                                 │   │
│  │  Current: 1 • Target: 20 • Suggested: +19      │   │
│  │  ☑ Done                                 20      │   │
│  │                                   (grayed out)  │   │
│  │                                   (grayed out)  │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  🍽️ KITCHEN (3 items)                          [>]     │
│                                                         │
│                                           [➕ FAB]      │
└─────────────────────────────────────────────────────────┘
```

**Item Card Layout**:
- Item name (top line)
- Info line: "Current: X • Target: Y • Suggested: +Z"
- Checkbox labeled "Done" (left side)
- Quantity display (right side, large)
- Plus button [ + ] (below quantity, right-aligned)
- Minus button [ - ] (below plus button, right-aligned)

**Checkbox Behavior** ("Done"):
- **Unchecked** (default):
  - Plus/minus buttons active and clickable
  - Quantity can be adjusted
  - Normal button colors
- **Checked**:
  - Plus/minus buttons grayed out (disabled appearance)
  - Buttons not clickable
  - Quantity locked at current value
  - Indicates "I'm done restocking this item"
- **Toggle**: Tap checkbox again to unlock and continue adjusting

**Quantity Controls**:
- Quantity defaults to target quantity (suggested restock amount)
- User can adjust to actual purchased amount using +/-
- Direct number entry for large quantities (tap on number)
- Lock by checking "Done" checkbox
- Unlock by unchecking "Done" checkbox

**Floating Action Button**:
- Standard FAB in bottom-right corner
- Add icon (➕)
- Opens Supply Edit Screen (create mode)
- Allows adding new items discovered during shopping
- New items added to current restock session
- Returns to restock screen after saving

**Category Behavior**:
- Categories auto-collapse when all items in category are marked "Done"
- User can manually expand to review or unlock items
- Visual indicator shows category progress (X of Y items done)

**Completion Flow**:
1. User adjusts quantities for each item
2. User checks "Done" for each item as they're finalized
3. User taps "Complete" button in header
4. Validation: All items must be marked "Done"
5. If incomplete items exist:
   - Toast/dialog: "Please mark all items as Done or remove them"
   - Stay on restock screen
6. If all items done:
   - All inventory quantities updated in database
   - Pending restock items cleared from state
   - Restock button disappears from Inventory Screen header
   - Returns to Inventory Screen
   - Toast: "Inventory updated for [N] items"

**Cancel Behavior**:
- Confirmation dialog: "Discard restock progress?"
- Confirm: Returns to Inventory Screen, pending items remain (button still visible)
- Cancel: Stay in restock workflow

---

### Recount Mode Workflow

**Trigger**: Task with RECOUNT mode inventory completes

**Purpose**: Bundle difficult-to-track consumption into manual recount

**Flow**:
1. User completes task (e.g., "Monthly Cleaning")
2. Task has supplies in RECOUNT mode
3. System saves items as pending restock
4. Restock button appears in Inventory Screen header with badge
5. User taps restock button when convenient
6. User opens restock screen
7. User recounts and updates quantities
8. User completes restock workflow
9. Inventory updated, button disappears

**Multiple Recount Items**:
- If multiple supplies have RECOUNT mode on same task
- All bundle into pending restock items
- User updates all in one workflow via restock screen

**Difference from Shopping**:
- No shopping list phase
- Goes directly to quantity update
- Defaults to current quantity (not target)
- User recounts and enters actual amounts

---

## Navigation Flow

```
Today Screen
    │
    ├─→ Inventory Screen [Header button]
    │   │
    │   ├─→ Shopping Workflow Screen [Go Shopping button]
    │   │   ├─→ Pause → Inventory Screen (session saved, can resume)
    │   │   ├─→ Cancel → Inventory Screen (session discarded)
    │   │   └─→ Done → Saves pending restock items → Inventory Screen (restock button appears)
    │   │
    │   ├─→ Restock Inventory Screen [Restock button - only visible with pending items]
    │   │   ├─→ FAB → Supply Edit Screen → back to Restock
    │   │   ├─→ Complete → Updates inventory → Inventory Screen (restock button disappears)
    │   │   └─→ Cancel → Inventory Screen (pending items remain, button still visible)
    │   │
    │   ├─→ Supply Edit Screen [FAB or tap on item card]
    │   │   ├─→ Task Detail Screen [from task association]
    │   │   │   └─→ Back to Supply Edit
    │   │   └─→ Save → Inventory Screen
    │   │   └─→ Cancel → Inventory Screen
    │   │
    │   └─→ Resume Shopping [Go Shopping with saved session]
    │       └─→ Shopping Workflow Screen (restored state)
    │
    └─→ All Tasks Screen
```

**Navigation Patterns**:
- Inventory Screen accessible from Today Screen header
- Shopping workflow supports pause/resume with state preservation
- Paused shopping updates quantities in real-time when resumed
- Restock Screen accessible via header button (only visible with pending items)
- Restock button appears after shopping completion with badge count
- Restock button persists until all pending items are processed
- Supply Edit accessible via FAB or tapping item cards
- FAB available on both Inventory and Restock screens
- Back navigation preserves state where appropriate

---

## Implementation Checklist

### Phase 1: Basic Inventory List
- [ ] Create `InventoryScreen.kt` composable
- [ ] Create `InventoryViewModel.kt` with state management
- [ ] Add navigation route in `Screen.kt`
- [ ] Wire up navigation from Today Screen header
- [ ] Implement header bar with back and shopping buttons
- [ ] Implement FAB for adding items
- [ ] Create `Supply` entity in database
- [ ] Create `Inventory` entity for quantity tracking
- [ ] Create `SupplyDao` with basic CRUD operations
- [ ] Create `SupplyRepository` for data access

### Phase 2: Supply Display
- [ ] Create `SupplyCategorySection` composable (collapsible)
- [ ] Create `SupplyItemCard` composable with compact layout
- [ ] Implement category grouping logic
- [ ] Add quick adjustment buttons (+/- stacked on right)
- [ ] Show reorder status indicators (⚠️/✅)
- [ ] Display current quantity (no unit on card)
- [ ] Show reorder threshold and target info (second line)
- [ ] Handle empty state display
- [ ] Implement tap-to-edit (opens Supply Edit Screen)

### Phase 3: Search & Filter (All Tasks Style)
- [ ] Implement search bar with real-time filtering
- [ ] Create sort bottom sheet (matches All Tasks)
- [ ] Create filter bottom sheet (matches All Tasks)
- [ ] Apply filters to supply list
- [ ] Apply search across name/category/tags
- [ ] Show active filter count badge
- [ ] Persist filter/sort state during session

### Phase 4: Supply Management
- [ ] Create `SupplyEditScreen.kt` composable
- [ ] Implement all input fields
- [ ] Add validation logic
- [ ] Create `CreateSupplyUseCase`
- [ ] Create `UpdateSupplyUseCase`
- [ ] Create `DeleteSupplyUseCase`
- [ ] Handle task dependency checking
- [ ] Show task associations in edit screen
- [ ] Navigate to Task Detail from associations

### Phase 5: Shopping Workflow
- [ ] Create `ShoppingWorkflowScreen.kt` composable
- [ ] Create `ShoppingViewModel.kt`
- [ ] Query items below reorder threshold
- [ ] Display shopping list with categories
- [ ] Implement item checkboxes
- [ ] Calculate suggested purchase quantities
- [ ] Handle category collapse/expand
- [ ] Implement "Pause" functionality
- [ ] Save shopping session state (checked items)
- [ ] Resume shopping with restored state
- [ ] Update quantities in real-time when resuming
- [ ] Recalculate suggested quantities on resume
- [ ] Show shopping badge/indicator for in-progress session
- [ ] Create "Done Shopping" completion flow
- [ ] Create Restock Task on completion
- [ ] Clear shopping session after completion

### Phase 6: Restock Workflow
- [ ] Create `RestockTask` entity
- [ ] Create `RestockTaskScreen.kt` composable
- [ ] Create `RestockViewModel.kt`
- [ ] Display items to restock with compact card layout
- [ ] Implement quantity adjustment (+/- buttons on right, stacked)
- [ ] Support direct number entry (tap on quantity)
- [ ] Implement "Done" checkbox mechanism
- [ ] Gray out +/- buttons when "Done" is checked
- [ ] Enable/disable buttons based on checkbox state
- [ ] Show current, target, and suggested quantities
- [ ] Implement FAB for adding new items
- [ ] Auto-collapse categories when all items marked "Done"
- [ ] Validate all items marked "Done" before completion
- [ ] Update inventory on completion
- [ ] Handle task completion and cleanup

### Phase 7: Integration
- [ ] Link inventory to tasks (TaskSupply junction table)
- [ ] Implement RECOUNT mode restock creation
- [ ] Test shopping → restock workflow end-to-end
- [ ] Test recount mode workflow
- [ ] Add inventory badge to shopping button
- [ ] Integrate with Export/Import (include supplies)

### Phase 8: Polish & Testing
- [ ] Add loading states
- [ ] Implement error handling
- [ ] Add success/failure messages
- [ ] Test with large inventory (100+ items)
- [ ] Test edge cases (empty, zero quantities)
- [ ] Add haptic feedback
- [ ] Performance optimization for large lists
- [ ] Accessibility support

---

## Technical Notes

### Data Model

**Supply Entity**:
```kotlin
@Entity(tableName = "supplies")
data class Supply(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val tags: String? = null, // Comma-separated
    val unit: String, // "count", "oz", "liters", etc.
    val reorderThreshold: Int,
    val reorderTargetQuantity: Int,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
```

**Inventory Entity**:
```kotlin
@Entity(tableName = "inventory")
data class Inventory(
    @PrimaryKey val supplyId: String,
    val currentQuantity: Int,
    val lastUpdated: Long = System.currentTimeMillis()
)
```

**TaskSupply Junction Table**:
```kotlin
@Entity(
    tableName = "task_supplies",
    primaryKeys = ["taskId", "supplyId"],
    foreignKeys = [
        ForeignKey(entity = Task::class, ...),
        ForeignKey(entity = Supply::class, ...)
    ]
)
data class TaskSupply(
    val taskId: String,
    val supplyId: String,
    val consumptionMode: ConsumptionMode, // FIXED, PROMPTED, RECOUNT
    val fixedQuantity: Int? = null, // Used if mode is FIXED
    val promptedDefaultValue: Int? = null // Used if mode is PROMPTED
)

enum class ConsumptionMode {
    FIXED, PROMPTED, RECOUNT
}
```

**RestockTask Entity**:
```kotlin
@Entity(tableName = "restock_tasks")
data class RestockTask(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val createdAt: Long = System.currentTimeMillis(),
    val status: RestockStatus, // PENDING, COMPLETE
    val itemsJson: String, // JSON array of {supplyId, suggestedQuantity}
    val completedAt: Long? = null,
    val source: RestockSource // SHOPPING, RECOUNT
)

enum class RestockStatus { PENDING, COMPLETE }
enum class RestockSource { SHOPPING, RECOUNT }
```

### State Management

```kotlin
data class InventoryUiState(
    val supplies: List<Supply> = emptyList(),
    val inventoryLevels: Map<String, Int> = emptyMap(),
    val searchQuery: String = "",
    val filterOptions: FilterOptions = FilterOptions(),
    val sortOption: SortOption = SortOption.BY_CATEGORY,
    val expandedCategories: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class FilterOptions(
    val needsReorder: Boolean = false,
    val wellStocked: Boolean = false,
    val categories: Set<String> = emptySet(),
    val hasTaskAssociations: Boolean = false
)

enum class SortOption {
    BY_CATEGORY, 
    BY_NAME_ASC, 
    BY_NAME_DESC, 
    BY_QUANTITY_ASC, 
    BY_QUANTITY_DESC, 
    BY_REORDER_URGENCY
}

data class ShoppingUiState(
    val items: List<ShoppingItem> = emptyList(),
    val checkedItems: Set<String> = emptySet(), // Supply IDs
    val isPaused: Boolean = false,
    val expandedCategories: Set<String> = emptySet(),
    val isLoading: Boolean = false
)

data class ShoppingItem(
    val supplyId: String,
    val name: String,
    val category: String,
    val currentQuantity: Int,
    val suggestedQuantity: Int, // Calculated: target - current
    val isChecked: Boolean
)

data class RestockUiState(
    val items: List<RestockItem> = emptyList(),
    val completedItems: Set<String> = emptySet(), // Supply IDs marked "Done"
    val quantities: Map<String, Int> = emptyMap(), // Current adjustment values
    val expandedCategories: Set<String> = emptySet(),
    val isLoading: Boolean = false
)

data class RestockItem(
    val supplyId: String,
    val name: String,
    val category: String,
    val currentQuantity: Int,
    val targetQuantity: Int,
    val suggestedIncrease: Int, // target - current
    val newQuantity: Int, // User-adjusted value
    val isDone: Boolean // Checkbox state
)
```

### Performance Considerations

**Large Lists**:
- Use LazyColumn for efficient rendering
- Implement pagination if > 100 items
- Cache category groupings
- Debounce search input

**Database Queries**:
- Index on `category` for grouping
- Index on `currentQuantity` for reorder filtering
- JOIN optimization for task associations
- Batch updates for restock operations

**State Updates**:
- Flow-based reactive updates
- Minimize recomposition scope
- Cache computed values (category groups)

---

## User Experience Considerations

### Visual Feedback
- Immediate quantity updates with animations
- Color coding for reorder status (red/green)
- Loading states during database operations
- Success toasts for completed actions

### Error Handling
- Validation errors inline with fields
- Clear messages for constraint violations
- Graceful handling of database errors
- Confirmation dialogs for destructive actions

### Accessibility
- Screen reader support for all elements
- High contrast mode support
- Touch targets ≥ 48dp
- Keyboard navigation support

### Offline Support
- All operations fully offline
- No network dependencies
- Local database as source of truth
- Export/import for data portability

---

**Implementation Priority**: After Settings screen  
**Dependencies**: Task system must support TaskSupply associations  
**Estimated Complexity**: High (complex workflows, multiple screens)
