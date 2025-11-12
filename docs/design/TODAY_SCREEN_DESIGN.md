# Today Screen Design

**Screen Type**: Home Screen / Primary Interface  
**Purpose**: Display tasks due today and provide quick access to app features  
**Status**: Phase 4 Complete ✅ - Navigation & Interactions Implemented  
**Last Updated**: October 25, 2025

---

## Table of Contents
1. [Overview](#overview)
2. [Screen Layout](#screen-layout)
3. [Component Specifications](#component-specifications)
4. [UI States](#ui-states)
5. [User Interactions](#user-interactions)
6. [Compose Implementation](#compose-implementation)
7. [ViewModel Structure](#viewmodel-structure)
8. [Data Flow](#data-flow)

---

## Overview

The Today Screen serves as the application's home screen and primary interface. It displays all tasks scheduled for the current day, organized by category, with a clean and intuitive interface that allows users to quickly see what needs attention.

### Design Principles

1. **Clarity**: Users should instantly see what tasks are due today
2. **Efficiency**: Complete tasks with minimal taps (≤3 taps per spec)
3. **Organization**: Tasks grouped by category for easy scanning
4. **Feedback**: Clear visual indicators for completion status
5. **Delight**: Celebrate completion with encouraging messages

### Key Features

- **Category-Based Organization**: Tasks grouped by category with progress indicators
- **Completion Tracking**: Visual progress for each category
- **Quick Actions**: Tap to complete, long-press for details
- **Filter Toggle**: Show/hide completed tasks
- **Navigation**: Access all major features from header
- **Empty State**: Encouraging message when all tasks complete

---

## Screen Layout

### Visual Hierarchy

```
┌─────────────────────────────────────────────────────────┐
│                      HEADER                             │
│  [All Tasks] [Completed] [Oct 24, 2025] [📦] [⚙️]      │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │ 🏋️ FITNESS                          ✓ 2/3        │   │
│  ├─────────────────────────────────────────────────┤   │
│  │  ☐ Morning Workout - 30 min                     │   │
│  │  ☑ Stretch - 10 min                             │   │
│  │  ☑ Walk - 20 min                                │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │ 🏠 HOME                              ✓ 0/2        │   │
│  ├─────────────────────────────────────────────────┤   │
│  │  ☐ Water Plants                                 │   │
│  │  ☐ Check Mail                                   │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │ 💼 WORK                              ✓ 1/1        │   │
│  ├─────────────────────────────────────────────────┤   │
│  │  ☑ Daily Standup - 15 min                       │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  (Scroll for more categories...)                       │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### Empty State (All Tasks Completed)

```
┌─────────────────────────────────────────────────────────┐
│                      HEADER                             │
│  [All Tasks] [Completed] [Oct 24, 2025] [📦] [⚙️]      │
├─────────────────────────────────────────────────────────┤
│                                                         │
│                                                         │
│                        🎉                               │
│                                                         │
│              All tasks completed!                       │
│                                                         │
│           You've crushed today's list.                  │
│              Time to relax! 😊                          │
│                                                         │
│                                                         │
│                                                         │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## Component Specifications

### Header Section

#### 1. All Tasks Button
- **Type**: Icon Button (leftmost, position 1)
- **Icon**: List icon or "All" text
- **Purpose**: Navigate to All Tasks View screen
- **State**: Normal (enabled)
- **Click Action**: Navigate to All Tasks screen (future)
- **Visual**: Material3 FilledTonalIconButton

#### 2. Show Completed Toggle
- **Type**: Toggle Icon Button (position 2)
- **Icons**: 
  - Unchecked: Eye icon / "Show" 
  - Checked: Eye-off icon / "Hide"
- **Purpose**: Toggle visibility of completed tasks
- **States**: 
  - OFF (default): Hide completed tasks and categories
  - ON: Show completed tasks within categories
- **Click Action**: Toggle completed filter state
- **Visual**: Material3 IconToggleButton

#### 3. Date Display
- **Type**: Text (center, position 3)
- **Format**: "MMM DD, YYYY" (e.g., "Oct 24, 2025")
- **Purpose**: Show current date
- **State**: Static (non-interactive)
- **Style**: 
  - Typography: Material3 titleLarge
  - Color: onSurface
  - Alignment: Center

#### 4. Inventory Button
- **Type**: Icon Button (position 4)
- **Icon**: Package/Box icon or "📦"
- **Purpose**: Navigate to Inventory Management screen
- **State**: Normal (enabled)
- **Click Action**: Navigate to Inventory screen (future)
- **Visual**: Material3 IconButton

#### 5. Settings Button
- **Type**: Icon Button (rightmost, position 5)
- **Icon**: Gear/Settings icon
- **Purpose**: Navigate to Settings screen
- **State**: Normal (enabled)
- **Click Action**: Navigate to Settings (future)
- **Visual**: Material3 IconButton

### Category Card

#### Structure
```
┌─────────────────────────────────────────────────────┐
│ [Icon] CATEGORY NAME              ✓ Completed/Total │
├─────────────────────────────────────────────────────┤
│ [TaskItem]                                          │
│ [TaskItem]                                          │
│ [TaskItem]                                          │
└─────────────────────────────────────────────────────┘
```

#### Properties
- **Container**: Material3 Card (Elevated)
- **Padding**: 16dp all sides
- **Margin**: 8dp vertical between cards
- **Corner Radius**: 12dp
- **Elevation**: 2dp

#### Header
- **Category Icon**: Emoji or material icon (left)
- **Category Name**: 
  - Typography: Material3 titleMedium
  - Color: onSurface
  - Weight: Bold
- **Progress Indicator**: 
  - Format: "✓ X/Y" where X = completed, Y = total
  - Typography: Material3 bodyMedium
  - Color: primary (if complete), onSurfaceVariant (if incomplete)
  - Alignment: Right

#### Behavior
- **Collapsible**: Future feature (tap header to expand/collapse)
- **Auto-hide**: Hide entire card when all tasks complete (if show completed = OFF)
- **Ordering**: Alphabetical by category name

### Task Item

#### Structure
```
☐ Task Name - Time Estimate/Difficulty
```

#### Properties
- **Container**: Row with padding 12dp vertical, 8dp horizontal
- **Checkbox**: 
  - Unchecked: Empty circle or square
  - Checked: Filled with checkmark
  - Size: 24dp
  - Color: primary
- **Task Name**:
  - Typography: Material3 bodyLarge
  - Color: onSurface (unchecked), onSurfaceVariant (checked)
  - Strike-through: Applied when checked
  - Max lines: 2
  - Overflow: Ellipsis
- **Metadata** (optional):
  - Time Estimate: "X min" format
  - OR Difficulty: "Low/Medium/High" with colored indicator
  - Typography: Material3 bodySmall
  - Color: onSurfaceVariant
  - Prefix: " - "

#### Interactions
- **Tap Checkbox**: Toggle task completion
- **Tap Task Name**: Navigate to Task Detail screen (future)
- **Long Press**: Show context menu (future - postpone, skip)

#### States
1. **Incomplete** (default):
   - Checkbox: empty
   - Text: normal
   - Background: transparent

2. **Completed**:
   - Checkbox: filled with checkmark
   - Text: strike-through, lighter color
   - Optional: fade-out animation
   - Optional: confetti animation on completion

3. **Overdue** (future):
   - Red accent color
   - Warning icon
   - Bold text

### Empty State Component

#### When Shown
- All categories have zero incomplete tasks
- Show completed filter is OFF

#### Properties
- **Container**: Column, centered
- **Icon**: 
  - Type: Emoji or large icon
  - Options: 🎉, ✨, 🏆, 😊, 👏
  - Size: 72dp
  - Random selection from list
- **Primary Message**:
  - Text: "All tasks completed!"
  - Typography: Material3 headlineMedium
  - Color: primary
  - Alignment: Center
- **Secondary Message**:
  - Text: Random encouraging message
  - Typography: Material3 bodyLarge
  - Color: onSurfaceVariant
  - Alignment: Center
  - Examples:
    - "You've crushed today's list. Time to relax! 😊"
    - "Nothing left to do! Enjoy your free time! 🌟"
    - "All done! You deserve a break! 🎊"
    - "Task master! Everything's complete! 👑"
    - "Great job! All tasks finished! 🚀"

---

## UI States

### 1. Loading State
**When**: App launching, refreshing data from database

**Visual**:
```
┌─────────────────────────────────────────────────────────┐
│                      HEADER                             │
│  [All Tasks] [Date: Oct 24, 2025] [Completed] [⚙️]     │
├─────────────────────────────────────────────────────────┤
│                                                         │
│                                                         │
│                  [Loading Spinner]                      │
│                                                         │
│                  Loading tasks...                       │
│                                                         │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

**Components**:
- Circular progress indicator (center)
- "Loading tasks..." text
- Header visible but buttons disabled

### 2. Has Tasks State
**When**: One or more tasks due today

**Visual**: See [Screen Layout](#screen-layout) above

**Components**:
- Header (fully interactive)
- One or more category cards
- Task items within cards
- Scroll container for overflow

### 3. Empty State (No Tasks)
**When**: No tasks scheduled for today

**Visual**:
```
┌─────────────────────────────────────────────────────────┐
│                      HEADER                             │
│  [All Tasks] [Date: Oct 24, 2025] [Completed] [⚙️]     │
├─────────────────────────────────────────────────────────┤
│                                                         │
│                        📅                               │
│                                                         │
│              No tasks scheduled today                   │
│                                                         │
│            Enjoy your free day! 🌴                      │
│                                                         │
│         [+ Add New Task] (optional button)              │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

**Components**:
- Icon: Calendar or relaxing emoji
- Message: "No tasks scheduled today"
- Subtitle: Encouraging message
- Optional: Add task button

### 4. All Complete State
**When**: All tasks completed, show completed filter OFF

**Visual**: See [Empty State](#empty-state-all-tasks-completed) above

**Components**:
- Celebration icon (random)
- Congratulations message
- Encouraging subtitle

### 5. Error State
**When**: Database error, data loading failure

**Visual**:
```
┌─────────────────────────────────────────────────────────┐
│                      HEADER                             │
│  [All Tasks] [Date: Oct 24, 2025] [Completed] [⚙️]     │
├─────────────────────────────────────────────────────────┤
│                                                         │
│                        ⚠️                               │
│                                                         │
│              Unable to load tasks                       │
│                                                         │
│        Please try restarting the app                    │
│                                                         │
│              [Retry Button]                             │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

**Components**:
- Warning icon
- Error message
- Retry button
- Optional: Error details (debug mode)

---

## User Interactions

### Primary Actions

#### 1. Complete Task
**Trigger**: Tap checkbox next to task  
**Behavior**:
1. Checkbox animates to checked state
2. Task text applies strike-through
3. Optional: Brief confetti/celebration animation
4. Category progress updates (e.g., 1/3 → 2/3)
5. If last task in category and show completed = OFF, category card fades out
6. If all tasks complete, show empty state

**Future**: Call `CompleteTaskUseCase` to:
- Update `lastCompleted` date
- Increment `completionStreak`
- Calculate and update `nextDue` date
- Trigger related tasks if configured
- Create `TaskLog` entry

#### 2. Toggle Show Completed
**Trigger**: Tap show/hide completed button  
**Behavior**:
1. Button icon toggles (eye ↔ eye-off)
2. If turning ON:
   - Show all completed tasks within categories
   - Expand categories that were hidden
   - Change progress to show "(All complete)" if all done
3. If turning OFF:
   - Hide completed tasks
   - Hide categories with zero incomplete tasks
   - Show empty state if all complete

#### 3. Navigate to All Tasks
**Trigger**: Tap "All Tasks" button  
**Behavior**:
1. Navigate to All Tasks View screen (future implementation)
2. Show chronological list of all tasks

#### 4. Navigate to Task Details
**Trigger**: Tap task name (not checkbox)  
**Behavior**:
1. Navigate to Task Detail Screen (future implementation)
2. Pass task ID for detailed view
3. Allow editing from detail screen

#### 5. Open Settings
**Trigger**: Tap settings gear icon  
**Behavior**:
1. Navigate to Settings screen (future implementation)
2. Show app preferences

### Secondary Actions (Future)

#### Long Press Task
**Trigger**: Long press on task item  
**Behavior**:
1. Show context menu with options:
   - Complete
   - Postpone to Tomorrow
   - Skip to Next Occurrence
   - View Details
   - Edit Task

#### Pull to Refresh
**Trigger**: Pull down gesture on task list  
**Behavior**:
1. Show refresh indicator
2. Reload tasks from database
3. Update UI with latest data

#### Swipe Actions
**Trigger**: Swipe left/right on task item  
**Behavior**:
- Swipe right: Complete task
- Swipe left: Postpone task

---

## Compose Implementation

### Screen Composable

```kotlin
@Composable
fun TodayScreen(
    modifier: Modifier = Modifier,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    TodayScreenContent(
        uiState = uiState,
        onTaskComplete = { taskId -> 
            viewModel.onEvent(TodayUiEvent.CompleteTask(taskId))
        },
        onToggleCompleted = {
            viewModel.onEvent(TodayUiEvent.ToggleShowCompleted)
        },
        onNavigateToAllTasks = {
            viewModel.onEvent(TodayUiEvent.NavigateToAllTasks)
        },
        onNavigateToInventory = {
            viewModel.onEvent(TodayUiEvent.NavigateToInventory)
        },
        onNavigateToSettings = {
            viewModel.onEvent(TodayUiEvent.NavigateToSettings)
        },
        onTaskClick = { taskId ->
            viewModel.onEvent(TodayUiEvent.NavigateToTaskDetail(taskId))
        },
        modifier = modifier
    )
}

@Composable
private fun TodayScreenContent(
    uiState: TodayUiState,
    onTaskComplete: (Long) -> Unit,
    onToggleCompleted: () -> Unit,
    onNavigateToAllTasks: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onTaskClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TodayScreenHeader(
                currentDate = uiState.currentDate,
                showCompleted = uiState.showCompleted,
                onToggleCompleted = onToggleCompleted,
                onNavigateToAllTasks = onNavigateToAllTasks,
                onNavigateToInventory = onNavigateToInventory,
                onNavigateToSettings = onNavigateToSettings
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LoadingState(modifier = Modifier.padding(paddingValues))
            }
            uiState.error != null -> {
                ErrorState(
                    message = uiState.error,
                    onRetry = { /* Future: retry action */ },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            uiState.tasksByCategory.isEmpty() && !uiState.showCompleted -> {
                EmptyState(
                    isAllComplete = uiState.allTasksComplete,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {
                TasksList(
                    tasksByCategory = uiState.tasksByCategory,
                    showCompleted = uiState.showCompleted,
                    onTaskComplete = onTaskComplete,
                    onTaskClick = onTaskClick,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}
```

### Header Component

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodayScreenHeader(
    currentDate: String,
    showCompleted: Boolean,
    onToggleCompleted: () -> Unit,
    onNavigateToAllTasks: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = currentDate,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateToAllTasks) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "All Tasks"
                )
            }
        },
        actions = {
            IconToggleButton(
                checked = showCompleted,
                onCheckedChange = { onToggleCompleted() }
            ) {
                Icon(
                    imageVector = if (showCompleted) {
                        Icons.Default.VisibilityOff
                    } else {
                        Icons.Default.Visibility
                    },
                    contentDescription = if (showCompleted) {
                        "Hide completed tasks"
                    } else {
                        "Show completed tasks"
                    }
                )
            }
            
            IconButton(onClick = onNavigateToInventory) {
                Icon(
                    imageVector = Icons.Default.Inventory,
                    contentDescription = "Inventory Management"
                )
            }
            
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            }
        },
        modifier = modifier
    )
}
```

### Category Card Component

```kotlin
@Composable
private fun CategoryCard(
    category: String,
    tasks: List<Task>,
    completedCount: Int,
    showCompleted: Boolean,
    onTaskComplete: (Long) -> Unit,
    onTaskClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Category Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "✓ $completedCount/${tasks.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (completedCount == tasks.size) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Task Items
            tasks.forEach { task ->
                val isCompleted = task.lastCompleted == LocalDate.now()
                if (showCompleted || !isCompleted) {
                    TaskItem(
                        task = task,
                        isCompleted = isCompleted,
                        onComplete = { onTaskComplete(task.id) },
                        onClick = { onTaskClick(task.id) }
                    )
                }
            }
        }
    }
}
```

### Task Item Component

```kotlin
@Composable
private fun TaskItem(
    task: Task,
    isCompleted: Boolean,
    onComplete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isCompleted,
            onCheckedChange = { onComplete() }
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isCompleted) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textDecoration = if (isCompleted) {
                    TextDecoration.LineThrough
                } else {
                    TextDecoration.None
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            // Metadata: time estimate or difficulty
            task.timeEstimate?.let { minutes ->
                Text(
                    text = "$minutes min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } ?: run {
                if (task.difficulty != Difficulty.MEDIUM) {
                    Text(
                        text = task.difficulty.name.lowercase().capitalize(),
                        style = MaterialTheme.typography.bodySmall,
                        color = when (task.difficulty) {
                            Difficulty.LOW -> Color.Green
                            Difficulty.MEDIUM -> Color.Yellow
                            Difficulty.HIGH -> Color.Red
                        }
                    )
                }
            }
        }
    }
}
```

### Empty State Component

```kotlin
@Composable
private fun EmptyState(
    isAllComplete: Boolean,
    modifier: Modifier = Modifier
) {
    val celebrationEmojis = listOf("🎉", "✨", "🏆", "😊", "👏", "🌟", "🎊", "👑", "🚀")
    val relaxEmojis = listOf("📅", "🌴", "☕", "😌", "🌸")
    
    val completionMessages = listOf(
        "You've crushed today's list. Time to relax! 😊",
        "Nothing left to do! Enjoy your free time! 🌟",
        "All done! You deserve a break! 🎊",
        "Task master! Everything's complete! 👑",
        "Great job! All tasks finished! 🚀",
        "Boom! All tasks conquered! 💪",
        "Perfection! All done for today! ✨"
    )
    
    val noTaskMessages = listOf(
        "Enjoy your free day! 🌴",
        "No tasks today. Time to relax! ☕",
        "A peaceful day ahead! 🌸",
        "Free time is the best time! 😌"
    )
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isAllComplete) {
                celebrationEmojis.random()
            } else {
                relaxEmojis.random()
            },
            fontSize = 72.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = if (isAllComplete) {
                "All tasks completed!"
            } else {
                "No tasks scheduled today"
            },
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (isAllComplete) {
                completionMessages.random()
            } else {
                noTaskMessages.random()
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
```

### Loading State Component

```kotlin
@Composable
private fun LoadingState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Loading tasks...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
```

### Error State Component

```kotlin
@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚠️",
            fontSize = 72.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Unable to load tasks",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
```

### Tasks List Component

```kotlin
@Composable
private fun TasksList(
    tasksByCategory: Map<String, List<Task>>,
    showCompleted: Boolean,
    onTaskComplete: (Long) -> Unit,
    onTaskClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        tasksByCategory.forEach { (category, tasks) ->
            val completedCount = tasks.count { task ->
                task.lastCompleted == LocalDate.now()
            }
            
            // Only show category if it has incomplete tasks or show completed is ON
            if (showCompleted || completedCount < tasks.size) {
                item(key = category) {
                    CategoryCard(
                        category = category,
                        tasks = tasks,
                        completedCount = completedCount,
                        showCompleted = showCompleted,
                        onTaskComplete = onTaskComplete,
                        onTaskClick = onTaskClick
                    )
                }
            }
        }
    }
}
```

---

## ViewModel Structure

### UI State

```kotlin
data class TodayUiState(
    val currentDate: String = "",
    val tasksByCategory: Map<String, List<Task>> = emptyMap(),
    val showCompleted: Boolean = false,
    val allTasksComplete: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
```

### UI Events

```kotlin
sealed interface TodayUiEvent {
    data class CompleteTask(val taskId: Long) : TodayUiEvent
    object ToggleShowCompleted : TodayUiEvent
    object NavigateToAllTasks : TodayUiEvent
    object NavigateToInventory : TodayUiEvent
    object NavigateToSettings : TodayUiEvent
    data class NavigateToTaskDetail(val taskId: Long) : TodayUiEvent
    object Refresh : TodayUiEvent
}
```

### ViewModel (Future Implementation)

```kotlin
@HiltViewModel
class TodayViewModel @Inject constructor(
    private val getTasksDueUseCase: GetTasksDueUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()
    
    init {
        loadTasksDueToday()
    }
    
    fun onEvent(event: TodayUiEvent) {
        when (event) {
            is TodayUiEvent.CompleteTask -> completeTask(event.taskId)
            is TodayUiEvent.ToggleShowCompleted -> toggleShowCompleted()
            is TodayUiEvent.NavigateToAllTasks -> {
                // Navigation handled by MainActivity/NavHost
            }
            is TodayUiEvent.NavigateToInventory -> {
                // Navigation handled by MainActivity/NavHost
            }
            is TodayUiEvent.NavigateToSettings -> {
                // Navigation handled by MainActivity/NavHost
            }
            is TodayUiEvent.NavigateToTaskDetail -> {
                // Navigation handled by MainActivity/NavHost
            }
            is TodayUiEvent.Refresh -> loadTasksDueToday()
        }
    }
    
    private fun loadTasksDueToday() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val today = LocalDate.now()
            val formattedDate = today.format(
                DateTimeFormatter.ofPattern("MMM dd, yyyy")
            )
            
            getTasksDueUseCase(today).collect { result ->
                result.fold(
                    onSuccess = { tasks ->
                        val grouped = tasks.groupBy { it.category }
                            .toSortedMap()
                        
                        val allComplete = tasks.isNotEmpty() && 
                            tasks.all { it.lastCompleted == today }
                        
                        _uiState.update {
                            it.copy(
                                currentDate = formattedDate,
                                tasksByCategory = grouped,
                                allTasksComplete = allComplete,
                                isLoading = false,
                                error = null
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Unknown error"
                            )
                        }
                    }
                )
            }
        }
    }
    
    private fun completeTask(taskId: Long) {
        viewModelScope.launch {
            completeTaskUseCase(taskId, LocalDate.now())
            // Tasks list updates automatically via Flow
        }
    }
    
    private fun toggleShowCompleted() {
        _uiState.update { 
            it.copy(showCompleted = !it.showCompleted) 
        }
    }
}
```

---

## Data Flow

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────┐
│                    TodayScreen.kt                       │
│                   (Composable UI)                       │
└────────────────────┬────────────────────────────────────┘
                     │
                     │ collectAsState()
                     ▼
┌─────────────────────────────────────────────────────────┐
│                  TodayViewModel.kt                      │
│                 (State Management)                      │
│                                                         │
│  • currentDate: String                                  │
│  • tasksByCategory: Map<String, List<Task>>            │
│  • showCompleted: Boolean                              │
│  • allTasksComplete: Boolean                           │
└────────────┬───────────────────────┬────────────────────┘
             │                       │
             │ calls                 │ observes
             ▼                       ▼
┌──────────────────────┐   ┌──────────────────────┐
│ CompleteTaskUseCase  │   │ GetTasksDueUseCase   │
│  (Future - Domain)   │   │  (Future - Domain)   │
└──────────┬───────────┘   └──────────┬───────────┘
           │                          │
           │ calls                    │ calls
           ▼                          ▼
┌─────────────────────────────────────────────────────────┐
│                  TaskRepository.kt                      │
│                (Data Layer - Implemented)               │
│                                                         │
│  • getTasksDueByDate(date)                             │
│  • updateTask(task)                                    │
│  • observeAllActiveTasks()                             │
└────────────────────┬────────────────────────────────────┘
                     │
                     │ queries
                     ▼
┌─────────────────────────────────────────────────────────┐
│                     TaskDao.kt                          │
│               (Room Database - Implemented)             │
│                                                         │
│  • getTasksDueByDate(date): List<Task>                 │
│  • update(task)                                        │
│  • observeAllActive(): Flow<List<Task>>                │
└────────────────────┬────────────────────────────────────┘
                     │
                     │ SQLite
                     ▼
┌─────────────────────────────────────────────────────────┐
│                  LifeOpsDatabase                        │
│                   (SQLite Database)                     │
│                                                         │
│  Table: tasks                                          │
│  - id, name, category, nextDue, lastCompleted, etc.    │
└─────────────────────────────────────────────────────────┘
```

### User Action Flow: Complete Task

```
User taps checkbox
       │
       ▼
TodayScreen.onTaskComplete(taskId)
       │
       ▼
TodayViewModel.onEvent(CompleteTask(taskId))
       │
       ▼
TodayViewModel.completeTask(taskId)
       │
       ▼
CompleteTaskUseCase.invoke(taskId, today)  [Future]
       │
       ├─→ Update task.lastCompleted = today
       ├─→ Increment task.completionStreak
       ├─→ Calculate task.nextDue (based on interval)
       ├─→ Trigger related tasks (if configured)
       ├─→ Create TaskLog entry
       │
       ▼
TaskRepository.updateTask(task)
       │
       ▼
TaskDao.update(task)
       │
       ▼
SQLite UPDATE statement
       │
       ▼
Flow<List<Task>> emits new data
       │
       ▼
TodayViewModel receives updated tasks
       │
       ▼
UI State updates
       │
       ▼
TodayScreen recomposes with new state
       │
       ▼
User sees checked checkbox, updated progress
```

---

## Implementation Checklist

### Phase 1: Static UI ✅ COMPLETE
- [x] Create `TodayScreen.kt` composable
- [x] Create `TodayScreenHeader` component
- [x] Create `CategoryCard` component
- [x] Create `TaskItem` component
- [x] Create `EmptyState` component
- [x] Create `LoadingState` component
- [x] Create `ErrorState` component
- [x] Create `TasksList` component
- [x] Add Material3 theme integration (Color.kt, Type.kt, Theme.kt)
- [x] Add placeholder/mock data for preview (MockData.kt)
- [x] Test UI with different states (loading, empty, has tasks, error)
- [x] Fix header button order to match design spec
- [x] Remove extra divider from CategoryCard
- [x] Center date text in TopAppBar

**Implementation Details:**
- Created 12 Compose UI components with 25+ preview functions
- Complete Material3 theme system with difficulty-based colors
- All components follow Material Design 3 guidelines
- Proper state management hooks for future ViewModel integration
- User-approved visual design matching specifications

### Phase 2: State Management ✅ COMPLETE
- [x] Create `TodayUiState` data class
- [x] Create `TodayUiEvent` sealed interface
- [x] Create `TodayViewModel` class
- [x] Implement state flow management
- [x] Add event handling logic
- [x] Connect to ViewModel with hiltViewModel()

**Implementation Details:**
- Reactive StateFlow architecture with Flow observation
- Event-based architecture for user interactions
- Hilt dependency injection for ViewModel
- Clean separation of UI state and events

### Phase 3: Database Integration ✅ COMPLETE
- [x] Create `GetTasksDueUseCase`
- [x] Create `CompleteTaskUseCase`
- [x] Implement date calculation logic
- [x] Implement task filtering logic
- [x] Implement category grouping logic
- [x] Add task completion logic with streak tracking
- [x] Add observeTasksDueByDate to DAO and Repository
- [x] Create DatabaseInitializer with sample data
- [x] Fix completed task visibility bug
- [x] Fix uncomplete task behavior

**Implementation Details:**
- Full Clean Architecture with Domain layer use cases
- Reactive Flow-based database queries
- Smart scheduling with interval calculations
- Completion streak tracking (increments/decrements/resets)
- Complex business logic for specific days and exclusions
- 14 sample tasks across 5 categories for testing

### Phase 4: Navigation & Interactions ✅ COMPLETE
- [x] Add Navigation Compose dependency
- [x] Create Screen routes with type safety
- [x] Create LifeOpsNavGraph with NavHost
- [x] Update MainActivity to use navigation
- [x] Connect header buttons to navigation
- [x] Create placeholder screens (AllTasks, Inventory, Settings, TaskDetail)
- [x] Add task click navigation to detail screen
- [x] Add long press context menu for tasks
- [x] Wire all navigation callbacks

**Implementation Details:**
- Navigation Compose 2.7.5 with type-safe routes
- Placeholder screens with back navigation
- Task tap → navigate to detail
- Task long press → context menu (Edit, Postpone, Skip, Delete)
- All touch interactions initialized and functional

### Phase 5: Polish (Future)
- [ ] Add haptic feedback on task completion
- [ ] Add celebration animations
- [ ] Implement pull-to-refresh
- [ ] Add swipe gestures
- [ ] Accessibility improvements
- [ ] Dark mode testing
- [ ] Tablet/landscape layout
- [ ] Implement context menu actions (Edit, Postpone, Skip, Delete)
- [ ] Task detail screen implementation
- [ ] All Tasks screen implementation
- [ ] Inventory screen implementation
- [ ] Settings screen implementation

---

## Design Notes

### Color Scheme
- **Primary**: Task completion indicators, headers
- **OnSurface**: Main text color
- **OnSurfaceVariant**: Secondary text, metadata
- **Surface**: Card backgrounds
- **Error**: Error messages, overdue tasks (future)

### Typography
- **headlineMedium**: Empty state titles
- **titleLarge**: Date display
- **titleMedium**: Category names
- **bodyLarge**: Task names
- **bodyMedium**: Progress indicators
- **bodySmall**: Task metadata (time, difficulty)

### Spacing
- **Category card padding**: 16dp
- **Task item padding**: 12dp vertical, 8dp horizontal
- **Card margin**: 8dp vertical
- **Content padding**: 16dp horizontal

### Animations (Future)
- **Task completion**: Fade + strike-through (300ms)
- **Category hide**: Fade out (200ms)
- **Confetti**: Particle animation on final task complete
- **Loading**: Circular progress with fade-in

---

## Accessibility

### Screen Reader Support
- All interactive elements have `contentDescription`
- Checkbox state announced ("checked" / "unchecked")
- Task completion state changes announced
- Category progress announced

### Touch Targets
- Minimum 48dp touch target for all interactive elements
- Adequate spacing between tasks for easy tapping

### Color Contrast
- Text meets WCAG AA standards
- Color not sole indicator (use icons + text)
- Support for system dark/light mode

### Focus Management
- Logical tab order through tasks
- Visible focus indicators
- Grouped related elements (category cards)

---

**Implementation Priority**: Phase 5 (Polish & Feature Completion)  
**Phase 1-4 Status**: ✅ COMPLETE - All core functionality implemented  
**Next Steps**: Implement placeholder screen content and context menu actions  
**Files Created**: 
- **Phase 1 - UI Components:**
  - `presentation/today/TodayScreen.kt`
  - `presentation/today/components/TodayScreenHeader.kt`
  - `presentation/today/components/CategoryCard.kt`
  - `presentation/today/components/TaskItem.kt`
  - `presentation/today/components/TasksList.kt`
  - `presentation/today/components/EmptyState.kt`
  - `presentation/today/components/LoadingState.kt`
  - `presentation/today/components/ErrorState.kt`
  - `presentation/today/MockData.kt`
  - `ui/theme/Color.kt`
  - `ui/theme/Type.kt`
  - `ui/theme/Theme.kt`
  - Updated: `MainActivity.kt`

- **Phase 2 - State Management:**
  - `presentation/today/TodayUiState.kt`
  - `presentation/today/TodayUiEvent.kt`
  - `presentation/today/TodayViewModel.kt`

- **Phase 3 - Database Integration:**
  - `domain/usecase/GetTasksDueUseCase.kt`
  - `domain/usecase/CompleteTaskUseCase.kt`
  - `data/local/DatabaseInitializer.kt`
  - Updated: `data/local/dao/TaskDao.kt`
  - Updated: `data/repository/TaskRepository.kt`
  - Updated: `LifeOpsApplication.kt`

- **Phase 4 - Navigation:**
  - `navigation/Screen.kt`
  - `navigation/LifeOpsNavGraph.kt`
  - `presentation/alltasks/AllTasksScreen.kt`
  - `presentation/inventory/InventoryScreen.kt`
  - `presentation/settings/SettingsScreen.kt`
  - `presentation/taskdetail/TaskDetailScreen.kt`
  - Updated: `app/build.gradle.kts` (Navigation Compose dependency)
  - Updated: `MainActivity.kt` (NavHost integration)
  - Updated: `presentation/today/TodayScreen.kt` (navigation callbacks)

