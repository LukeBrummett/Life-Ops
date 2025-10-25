# All Tasks Screen Design

**Screen Type**: Secondary Navigation / Full Task List  
**Purpose**: View, search, and manage all tasks across the entire timeline  
**Status**: Not Started  
**Last Updated**: October 25, 2025

---

## Table of Contents
1. [Overview](#overview)
2. [Screen Layout](#screen-layout)
3. [Component Specifications](#component-specifications)
4. [User Interactions](#user-interactions)
5. [Filtering & Sorting](#filtering--sorting)
6. [Search Functionality](#search-functionality)
7. [Task Creation](#task-creation)
8. [Navigation Flow](#navigation-flow)
9. [Implementation Checklist](#implementation-checklist)

---

## Overview

The All Tasks screen provides a comprehensive view of all tasks in the system, organized chronologically by their next scheduled date. Unlike the Today screen which shows only tasks due today, this screen allows users to view and edit tasks scheduled for any date in the future.

### Design Principles

1. **Temporal Organization**: Tasks ordered by `nextDue` date for future planning
2. **Comprehensive Search**: Find tasks by name, category, tags, or attributes
3. **Quick Access**: Jump directly to task details for editing
4. **Status Visibility**: Clear indicators for task state and configuration
5. **Efficient Browsing**: Scan large task lists with smart grouping

### Key Features

- **Chronological Timeline**: Tasks ordered by next occurrence date
- **Advanced Search**: Multi-field search with filters
- **Quick Indicators**: Visual badges for task properties
- **Date Grouping**: Tasks grouped by relative date (Today, Tomorrow, This Week, etc.)
- **Floating Action Button**: Quick task creation
- **Direct Navigation**: Tap to view/edit task details

---

## Screen Layout

### Visual Hierarchy

```
┌─────────────────────────────────────────────────────────┐
│                      HEADER                             │
│  ← Back    All Tasks                        [Filter] 🔍 │
├─────────────────────────────────────────────────────────┤
│  ┌───────────────────────────────────────────────────┐ │
│  │ 🔍 Search tasks...                               │ │
│  └───────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────┤
│  Filters: [Active] [All] [Archived]                    │
│  Sort: [By Date ▼] [By Category] [By Priority]         │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  TODAY                                                  │
│  ┌─────────────────────────────────────────────────┐   │
│  │ Morning Workout                    🏋️ Fitness    │   │
│  │ Every day • 30 min • 🔥 3 streak               │   │
│  │ 📦 ⚡                                            │   │
│  └─────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────┐   │
│  │ Daily Standup                      💼 Work       │   │
│  │ Weekdays only • 15 min                         │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  TOMORROW                                               │
│  ┌─────────────────────────────────────────────────┐   │
│  │ Water Plants                       🏠 Home       │   │
│  │ Every 3 days • 5 min                           │   │
│  │ 📦                                              │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  THIS WEEK (Oct 26-Nov 1)                              │
│  ┌─────────────────────────────────────────────────┐   │
│  │ [Thu] Grocery Shopping             🛒 Errands    │   │
│  │ Every week • 60 min                            │   │
│  │ 📦                                              │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  NEXT WEEK (Nov 2-8)                                   │
│  ┌─────────────────────────────────────────────────┐   │
│  │ [Mon] Change Air Filter            🏠 Home       │   │
│  │ Every 30 days • 10 min                         │   │
│  │ 📦                                              │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  (Scroll for more...)                                  │
│                                                         │
│                                           ┌──────┐     │
│                                           │  +   │ FAB │
│                                           └──────┘     │
└─────────────────────────────────────────────────────────┘
```

---

## Component Specifications

### TopAppBar (Header)

**Purpose**: Navigation and search access

**Elements**:
- **Back Button** (Left): Icon button returns to Today screen
- **Title**: "All Tasks" centered text
- **Filter Button** (Right): Opens filter/sort options
- **Search Button** (Right): Toggles search bar visibility

**Behavior**:
- Search button expands search bar when tapped
- Filter button shows bottom sheet with options
- Back button navigates to previous screen (Today or other)

**Material3 Styling**:
- Uses `CenterAlignedTopAppBar`
- Primary container color
- Elevation: 0dp (flat)

---

### Search Bar

**Purpose**: Quick task lookup by multiple criteria

**Layout**:
- Full-width text field below header
- Search icon prefix
- Clear button suffix (when text present)
- Placeholder: "Search tasks..."

**Search Criteria**:
- Task name (primary)
- Category
- Tags
- Description/notes

**Behavior**:
- Real-time filtering as user types
- Debounced search (300ms delay)
- Highlights matching text in results
- Empty state message if no matches

**Implementation**:
```kotlin
OutlinedTextField(
    value = searchQuery,
    onValueChange = { query -> 
        // Debounced search logic
    },
    placeholder = { Text("Search tasks...") },
    leadingIcon = { Icon(Icons.Default.Search) },
    trailingIcon = { 
        if (searchQuery.isNotEmpty()) 
            IconButton(onClick = { searchQuery = "" }) {
                Icon(Icons.Default.Clear)
            }
    }
)
```

---

### Filter & Sort Bar

**Purpose**: Refine task list display

**Filter Options**:
- **Status**:
  - Active (default)
  - All
  - Archived only
- **Has Inventory**: Tasks with inventory associations
- **Has Children**: Parent/group tasks
- **Is Triggered**: Tasks spawned by other tasks
- **ADHOC Only**: Tasks with no schedule

**Sort Options**:
- **By Date** (default): `nextDue` chronological
- **By Category**: Alphabetical category grouping
- **By Priority**: High → Medium → Low
- **By Name**: Alphabetical

**Layout**:
- Horizontal scrollable chip group
- Active filters shown as filled chips
- Inactive filters shown as outlined chips
- Sort dropdown at the end

**Behavior**:
- Multiple filters can be active simultaneously (AND logic)
- Tapping active filter removes it
- Sort is single-selection

---

### Date Group Header

**Purpose**: Temporal organization of task list

**Group Labels**:
- **TODAY** - Tasks with `nextDue` = today's date
- **TOMORROW** - Tasks with `nextDue` = tomorrow
- **THIS WEEK** - Tasks within next 7 days (with date range)
- **NEXT WEEK** - Tasks 8-14 days out (with date range)
- **THIS MONTH** - Tasks 15-30 days out (month name)
- **LATER** - Tasks more than 30 days out

**Styling**:
- Material3 `titleSmall` typography
- OnSurfaceVariant color
- 16dp horizontal padding
- 8dp vertical padding
- Sticky header behavior (stays visible while scrolling group)

**Behavior**:
- Headers appear only if group has tasks
- Empty groups are hidden
- Header scrolls with content (LazyColumn with `stickyHeader()`)

---

### Task List Item

**Purpose**: Compact task preview with essential info

**Layout** (per task card):
```
┌─────────────────────────────────────────────────┐
│ Task Name                          📁 Category  │
│ Schedule description • Time/Diff               │
│ 📦 ⚡ 👥 🔄                                      │
└─────────────────────────────────────────────────┘
```

**Primary Line**:
- **Task Name** (left, bold, `bodyLarge`)
- **Category Icon + Name** (right, `bodyMedium`, OnSurfaceVariant)

**Secondary Line**:
- **Schedule Info**: Human-readable recurrence
  - "Every 3 days"
  - "Every Monday, Wednesday, Friday"
  - "Every 2 weeks"
  - "ADHOC (trigger only)"
- **Time Estimate** or **Difficulty**: "30 min" or "⚠️ High"
- Separator: " • " between items

**Indicator Badges** (third line):
- 📦 **Inventory**: Task consumes supplies
- ⚡ **Triggered**: Task spawned by another task
- 👥 **Has Children**: Parent/group task
- 🔄 **Triggers Others**: Task triggers other tasks
- 🔥 **Streak**: Current completion streak (if > 0)

**Schedule Info Examples**:
- `intervalUnit: DAY, intervalQty: 1` → "Every day"
- `intervalUnit: DAY, intervalQty: 3` → "Every 3 days"
- `intervalUnit: WEEK, intervalQty: 1, specificDaysOfWeek: [MON,WED,FRI]` → "Every Monday, Wednesday, Friday"
- `intervalUnit: WEEK, intervalQty: 2` → "Every 2 weeks"
- `intervalUnit: MONTH, intervalQty: 1` → "Every month"
- `intervalUnit: ADHOC` → "ADHOC (trigger only)"

**Styling**:
- `ElevatedCard` with 2dp elevation
- 8dp vertical padding between cards
- 16dp horizontal margin
- Tap ripple effect

**Behavior**:
- Tap anywhere on card → Navigate to Task Detail screen
- No swipe gestures (use detail screen for actions)
- Visual feedback on press (Material ripple)

---

### Floating Action Button (FAB)

**Purpose**: Quick task creation

**Design**:
- Material3 extended FAB
- Icon: `Icons.Default.Add`
- Label: "New Task"
- Position: Bottom-right corner
- Elevation: 6dp

**Behavior**:
- Tap → Navigate to Task Create screen (empty form)
- Hides on scroll down, shows on scroll up
- Always visible when at top of list

**Implementation**:
```kotlin
ExtendedFloatingActionButton(
    onClick = { /* Navigate to create */ },
    icon = { Icon(Icons.Default.Add, "Add task") },
    text = { Text("New Task") }
)
```

---

### Empty States

**No Tasks State**:
```
        📋
    
    No Tasks Yet
    
Tap the + button to create your first task
```

**No Search Results**:
```
        🔍
    
    No Results
    
Try different search terms or filters
```

**No Tasks in Filter**:
```
        🔎
    
    No Tasks Match Filter
    
Adjust your filters to see more tasks
```

---

## User Interactions

### Primary Actions

**1. View Task Details**
- **Trigger**: Tap on any task card
- **Action**: Navigate to Task Detail screen with task ID
- **Result**: Full task information display with edit option

**2. Create New Task**
- **Trigger**: Tap FAB
- **Action**: Navigate to Task Create screen (empty form)
- **Result**: New task form ready for input

**3. Search for Task**
- **Trigger**: Tap search icon or type in search bar
- **Action**: Filter task list in real-time
- **Result**: List shows only matching tasks

**4. Apply Filters**
- **Trigger**: Tap filter chips or filter button
- **Action**: Show/hide tasks based on criteria
- **Result**: Refined task list

**5. Change Sort Order**
- **Trigger**: Select sort option
- **Action**: Re-order task list
- **Result**: Tasks displayed in selected order

**6. Navigate Back**
- **Trigger**: Tap back button
- **Action**: Return to previous screen
- **Result**: Return to Today screen or previous context

---

## Filtering & Sorting

### Filter Logic

**Status Filter** (mutually exclusive):
- **Active**: `active = true` (default)
- **All**: Show all tasks (active + archived)
- **Archived**: `active = false`

**Attribute Filters** (can combine):
- **Has Inventory**: `requiresInventory = true`
- **Has Children**: `parentTaskIds` array is not empty
- **Is Triggered**: `triggeredByTaskIds` array is not empty
- **ADHOC Only**: `intervalUnit = 'ADHOC'`

**Filter Combination**:
- Status filter is single-selection
- Attribute filters use AND logic (all must match)
- Search text applies on top of filters

**Filter State Management**:
```kotlin
data class FilterState(
    val statusFilter: StatusFilter = StatusFilter.ACTIVE, // Default to Active
    val hasInventory: Boolean = false,
    val hasChildren: Boolean = false,
    val isTriggered: Boolean = false,
    val adhocOnly: Boolean = false
)

enum class StatusFilter {
    ACTIVE,   // Default
    ALL,
    ARCHIVED
}
```

---

### Sort Logic

**Sort Options**:

**By Date** (default):
- Primary: `nextDue` ascending (earliest first)
- Secondary: `name` alphabetical
- Null `nextDue` (ADHOC with no trigger date) appears last

**By Category**:
- Primary: `category` alphabetical
- Secondary: `nextDue` ascending within category
- Groups tasks by category with category headers

**By Priority**:
- Primary: `priority` ascending (1=High, 2=Medium, 3=Low)
- Secondary: `nextDue` ascending within priority
- Groups by priority level

**By Name**:
- Primary: `name` alphabetical
- No secondary sort

**Sort State**:
```kotlin
enum class SortOption {
    BY_DATE,      // Default
    BY_CATEGORY,
    BY_PRIORITY,
    BY_NAME
}
```

---

## Search Functionality

### Search Implementation

**Search Fields**:
- `name` (primary, weighted highest)
- `category`
- `tags` (comma-separated string search)
- `description`

**Search Behavior**:
- Case-insensitive
- Partial matching (contains)
- Debounced input (300ms)
- Real-time results

**Search Algorithm**:
```kotlin
fun searchTasks(query: String, tasks: List<Task>): List<Task> {
    if (query.isBlank()) return tasks
    
    val normalizedQuery = query.lowercase().trim()
    
    return tasks.filter { task ->
        task.name.lowercase().contains(normalizedQuery) ||
        task.category.lowercase().contains(normalizedQuery) ||
        task.tags.lowercase().contains(normalizedQuery) ||
        task.description?.lowercase()?.contains(normalizedQuery) == true
    }
}
```

**Search Highlighting** (future enhancement):
- Highlight matching text in results
- Use `AnnotatedString` with background color
- Apply to name field only for clarity

---

## Task Creation

### FAB Behavior

**Interaction**:
- Extended FAB with "New Task" label
- Tapping navigates to Task Create screen
- No inline creation (dedicated screen for complexity)

**Navigation**:
```kotlin
onFabClick = {
    navController.navigate(Screen.TaskCreate.route)
}
```

**Create Screen**:
- Full form for task configuration
- Pre-filled defaults (active=true, priority=MEDIUM)
- Save returns to All Tasks screen (or Today if preferred)

---

## Navigation Flow

### Entry Points

**From Today Screen**:
- Header "All Tasks" button → All Tasks screen

**From Other Screens**:
- Task Detail "View All Tasks" option
- Settings "Manage Tasks" option

### Exit Points

**To Task Detail**:
- Tap any task card → Task Detail screen with task ID

**To Task Create**:
- Tap FAB → Task Create screen (empty form)

**Back Navigation**:
- Back button → Return to previous screen (typically Today)

### Navigation Graph

```
All Tasks Screen
├─→ Task Detail Screen [Tap task card]
│   ├─→ Task Edit Screen
│   └─→ Back → All Tasks Screen
├─→ Task Create Screen [Tap FAB]
│   └─→ Save → All Tasks Screen
└─→ Back → Today Screen (or previous)
```

---

## Data Flow

### ViewModel Structure

```kotlin
@HiltViewModel
class AllTasksViewModel @Inject constructor(
    private val getAllTasksUseCase: GetAllTasksUseCase,
    private val searchTasksUseCase: SearchTasksUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AllTasksUiState())
    val uiState: StateFlow<AllTasksUiState> = _uiState.asStateFlow()
    
    fun onEvent(event: AllTasksUiEvent) {
        when (event) {
            is AllTasksUiEvent.SearchQueryChanged -> updateSearch(event.query)
            is AllTasksUiEvent.FilterChanged -> updateFilter(event.filter)
            is AllTasksUiEvent.SortChanged -> updateSort(event.sort)
            is AllTasksUiEvent.NavigateToTaskDetail -> { /* Handle navigation */ }
            is AllTasksUiEvent.NavigateToTaskCreate -> { /* Handle navigation */ }
            AllTasksUiEvent.Refresh -> loadTasks()
        }
    }
}

data class AllTasksUiState(
    val tasks: List<Task> = emptyList(),
    val filteredTasks: List<Task> = emptyList(),
    val searchQuery: String = "",
    val filterState: FilterState = FilterState(),
    val sortOption: SortOption = SortOption.BY_DATE,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface AllTasksUiEvent {
    data class SearchQueryChanged(val query: String) : AllTasksUiEvent
    data class FilterChanged(val filter: FilterState) : AllTasksUiEvent
    data class SortChanged(val sort: SortOption) : AllTasksUiEvent
    data class NavigateToTaskDetail(val taskId: Long) : AllTasksUiEvent
    data object NavigateToTaskCreate : AllTasksUiEvent
    data object Refresh : AllTasksUiEvent
}
```

### Use Cases

**GetAllTasksUseCase**:
```kotlin
class GetAllTasksUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(): Flow<List<Task>> {
        return repository.observeAllTasks()
    }
}
```

**SearchTasksUseCase**:
```kotlin
class SearchTasksUseCase @Inject constructor() {
    operator fun invoke(
        tasks: List<Task>,
        query: String,
        filterState: FilterState,
        sortOption: SortOption
    ): List<Task> {
        return tasks
            .filter { applyFilters(it, filterState) }
            .filter { matchesSearch(it, query) }
            .sortedWith(getSortComparator(sortOption))
    }
}
```

---

## Implementation Checklist

### Phase 1: Basic Structure
- [ ] Create `AllTasksScreen.kt` composable
- [ ] Create `AllTasksViewModel` with StateFlow
- [ ] Create `AllTasksUiState` and `AllTasksUiEvent`
- [ ] Add navigation route and update NavGraph
- [ ] Create basic TopAppBar with back button
- [ ] Create task list with LazyColumn

### Phase 2: Task Display
- [ ] Create `TaskListItem` component
- [ ] Implement date group headers (Today, Tomorrow, etc.)
- [ ] Add task schedule info formatting
- [ ] Add indicator badges (inventory, triggers, children)
- [ ] Add category display
- [ ] Implement sticky headers for date groups

### Phase 3: Search & Filter
- [ ] Create search bar component
- [ ] Implement real-time search logic
- [ ] Create filter chip bar
- [ ] Implement filter logic (status, attributes)
- [ ] Create filter state management
- [ ] Add debounced search (300ms delay)

### Phase 4: Sorting
- [ ] Create sort options dropdown
- [ ] Implement sort by date logic
- [ ] Implement sort by category logic
- [ ] Implement sort by priority logic
- [ ] Implement sort by name logic
- [ ] Add sort state persistence

### Phase 5: Navigation & Actions
- [ ] Wire up task card tap → Task Detail navigation
- [ ] Create and wire up FAB → Task Create navigation
- [ ] Implement back button navigation
- [ ] Add navigation callbacks to screen

### Phase 6: Use Cases
- [ ] Create `GetAllTasksUseCase`
- [ ] Create `SearchTasksUseCase`
- [ ] Add `observeAllTasks()` to TaskRepository
- [ ] Wire up use cases to ViewModel

### Phase 7: Polish
- [ ] Add loading state
- [ ] Add empty states (no tasks, no results, no matches)
- [ ] Add error state
- [ ] Implement FAB hide/show on scroll
- [ ] Add search clear button
- [ ] Test with large task lists (200+ tasks)

---

## Technical Notes

### Performance Considerations

**Large Lists**:
- Use `LazyColumn` with `key` parameter for efficient recycling
- Debounce search input to avoid excessive filtering
- Consider pagination if task count exceeds 1000

**Search Optimization**:
- Create search index for frequently searched fields
- Cache search results for repeated queries
- Limit search to first 100 characters of description

**Filter/Sort Optimization**:
- Apply filters before sorting
- Cache sorted lists until data changes
- Use `derivedStateOf` for computed filtered/sorted lists

### Accessibility

**Screen Readers**:
- Descriptive content descriptions for all icons
- Announce filter state changes
- Group task card elements semantically

**Touch Targets**:
- Minimum 48dp for all interactive elements
- Adequate spacing between filter chips
- Large FAB for easy access

**Text Contrast**:
- Ensure all text meets WCAG AA standards
- Use sufficient color contrast for indicators
- Support dynamic font sizing

---

## Future Enhancements

### V2 Considerations

**Advanced Features**:
- Bulk task operations (multi-select)
- Quick actions (swipe to complete/skip)
- Task templates for quick creation
- Recently viewed tasks section
- Favorite tasks pinning

**Customization**:
- User-defined sort orders
- Saved filter presets
- Custom date grouping preferences
- List density options (compact/comfortable/spacious)

**Performance**:
- Virtual scrolling for 1000+ tasks
- Search result caching
- Background task indexing
- Query optimization

---

**Implementation Priority**: After Today Screen phases complete  
**Dependencies**: Task Detail screen, Task Create screen, GetAllTasksUseCase  
**Estimated Complexity**: Medium (search/filter logic is straightforward)
