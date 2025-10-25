# Life-Ops Architecture

**Last Updated**: October 24, 2025  
**Version**: 1.0  
**Status**: Data Layer Complete

---

## Table of Contents
1. [Overview](#overview)
2. [Architecture Pattern](#architecture-pattern)
3. [Technology Stack](#technology-stack)
4. [Project Structure](#project-structure)
5. [Data Layer](#data-layer)
6. [Domain Layer](#domain-layer)
7. [Presentation Layer](#presentation-layer)
8. [Dependency Injection](#dependency-injection)
9. [Testing Strategy](#testing-strategy)
10. [Build Configuration](#build-configuration)

---

## Overview

Life-Ops is an Android application built with **Clean Architecture** principles, designed to manage tasks, inventory, and daily operations with a focus on flexibility, scalability, and testability.

### Design Principles

1. **Separation of Concerns**: Each layer has a single, well-defined responsibility
2. **Dependency Rule**: Dependencies point inward (Presentation → Domain → Data)
3. **Testability**: Each layer can be tested independently
4. **SOLID Principles**: Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion
5. **Reactive Programming**: Using Kotlin Flow for data streams

### Key Features

- **Flexible Task Scheduling**: Daily, weekly, monthly, or ad-hoc intervals
- **Parent-Child Task Hierarchies**: Support for complex task structures
- **Trigger System**: Tasks can automatically trigger other tasks
- **Inventory Integration**: Track supplies consumed by tasks
- **Completion Tracking**: History and streak tracking for gamification

---

## Architecture Pattern

Life-Ops follows **Clean Architecture** as popularized by Robert C. Martin (Uncle Bob), with three distinct layers:

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                       │
│                     (Planned - Future)                      │
├─────────────────────────────────────────────────────────────┤
│  • Jetpack Compose UI Components                           │
│  • ViewModels (State Management)                           │
│  • UI State & Events                                       │
│  • Navigation                                              │
│                                                            │
│  Dependencies: Domain Layer (Use Cases)                    │
└─────────────────────────────────────────────────────────────┘
                            ↓ (calls)
┌─────────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                          │
│                     (Planned - Future)                     │
├─────────────────────────────────────────────────────────────┤
│  • Use Cases (Business Logic)                              │
│  • Domain Models                                           │
│  • Repository Interfaces                                   │
│  • Business Rules                                          │
│                                                            │
│  Examples:                                                 │
│  - CompleteTaskUseCase                                     │
│  - CalculateNextDueDateUseCase                            │
│  - TriggerRelatedTasksUseCase                             │
│  - ConsumeInventoryUseCase                                │
│                                                            │
│  Dependencies: None (Pure Kotlin)                          │
└─────────────────────────────────────────────────────────────┘
                            ↓ (implements)
┌─────────────────────────────────────────────────────────────┐
│                       DATA LAYER                           │
│                    (✅ IMPLEMENTED)                        │
├─────────────────────────────────────────────────────────────┤
│  • Repositories (Implementation)                           │
│  • Data Sources (Room Database)                            │
│  • DAOs (Data Access Objects)                              │
│  • Entities (Database Models)                              │
│  • Type Converters                                         │
│                                                            │
│  Implemented:                                              │
│  - Task Entity (21 fields)                                 │
│  - TaskDao (16 queries)                                    │
│  - TaskRepository (18 methods)                             │
│  - Type Converters (LocalDate, Lists, Enums)              │
│                                                            │
│  Dependencies: Room, Kotlin Coroutines                     │
└─────────────────────────────────────────────────────────────┘
```

### Layer Responsibilities

#### Presentation Layer (Future)
- **Responsibility**: Display data and handle user interactions
- **Components**:
  - **Composables**: UI screens and components
  - **ViewModels**: Manage UI state, handle user events, coordinate use cases
  - **UI State Classes**: Immutable data classes representing screen states
  - **Navigation**: Screen routing and deep linking
- **Rules**:
  - Only depends on Domain layer
  - No direct database or data source access
  - UI logic only (no business logic)

#### Domain Layer (Future)
- **Responsibility**: Core business logic and rules
- **Components**:
  - **Use Cases**: Single-responsibility business operations
  - **Domain Models**: Business entities (if different from data models)
  - **Repository Interfaces**: Contracts for data access
- **Rules**:
  - Pure Kotlin (no Android framework dependencies)
  - No dependencies on outer layers
  - Reusable across platforms (Android, Desktop, etc.)

#### Data Layer (Implemented ✅)
- **Responsibility**: Data persistence and retrieval
- **Components**:
  - **Repositories**: Implement domain repository interfaces
  - **DAOs**: SQL queries and database operations
  - **Entities**: Database table definitions with Room annotations
  - **Type Converters**: Convert complex types for database storage
- **Rules**:
  - Abstracts data sources from domain layer
  - Handles data caching, synchronization
  - Returns domain models (or data models if same)

---

## Technology Stack

### Core Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| **Kotlin** | 1.9.22 | Primary programming language |
| **Java** | 21.0.8 (LTS) | Runtime environment and bytecode target |
| **Gradle** | 8.5 | Build automation |

### Android Platform

| Component | Version | Purpose |
|-----------|---------|---------|
| **Min SDK** | 26 | Android 8.0 (Oreo) |
| **Target SDK** | 34 | Android 14 |
| **Compile SDK** | 34 | Latest Android features |

### UI Framework

| Library | Version | Purpose |
|---------|---------|---------|
| **Jetpack Compose** | BOM 2023.10.01 | Declarative UI framework |
| **Compose Compiler** | 1.5.10 | Kotlin compiler plugin for Compose |
| **Material Design 3** | Via Compose BOM | UI components and theming |
| **Compose UI Tooling** | Via Compose BOM | Preview and debugging |

### Database & Persistence

| Library | Version | Purpose |
|---------|---------|---------|
| **Room** | 2.6.1 | SQLite ORM and abstraction layer |
| **Room KTX** | 2.6.1 | Kotlin extensions and coroutines support |
| **Room Compiler** | 2.6.1 | Annotation processor for Room |

### Dependency Injection

| Library | Version | Purpose |
|---------|---------|---------|
| **Hilt** | 2.50 | Dependency injection framework |
| **Hilt Compiler** | 2.50 | Annotation processor for Hilt |
| **KSP** | 1.9.22-1.0.17 | Kotlin Symbol Processing for annotations |

### Data Serialization

| Library | Version | Purpose |
|---------|---------|---------|
| **Gson** | 2.10.1 | JSON serialization for type converters |

### Concurrency

| Library | Version | Purpose |
|---------|---------|---------|
| **Kotlin Coroutines** | 1.7.3 | Asynchronous programming |
| **Coroutines Android** | 1.7.3 | Android-specific coroutine support |
| **Kotlin Flow** | Via Coroutines | Reactive data streams |

### Testing

| Library | Version | Purpose |
|---------|---------|---------|
| **JUnit** | 4.13.2 | Unit testing framework |
| **Google Truth** | 1.1.5 | Fluent assertion library |
| **Coroutines Test** | 1.7.3 | Testing coroutines and flows |
| **Room Testing** | 2.6.1 | In-memory database for tests |
| **Compose UI Test** | Via Compose BOM | UI testing for Compose |

---

## Project Structure

### Directory Layout

```
Life-Ops/
├── app/
│   ├── build.gradle.kts              # App-level build configuration
│   ├── proguard-rules.pro            # ProGuard configuration
│   │
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/lifeops/app/
│   │   │   │   │
│   │   │   │   ├── MainActivity.kt                    # Application entry point
│   │   │   │   ├── LifeOpsApplication.kt              # Hilt application class
│   │   │   │   │
│   │   │   │   ├── data/                              # DATA LAYER ✅
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── entity/
│   │   │   │   │   │   │   └── Task.kt                # Task entity (21 fields)
│   │   │   │   │   │   ├── converter/
│   │   │   │   │   │   │   └── Converters.kt          # Type converters for Room
│   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   │   └── TaskDao.kt             # Data access object (16 queries)
│   │   │   │   │   │   └── LifeOpsDatabase.kt         # Room database configuration
│   │   │   │   │   │
│   │   │   │   │   └── repository/
│   │   │   │   │       └── TaskRepository.kt          # Repository implementation
│   │   │   │   │
│   │   │   │   ├── domain/                            # DOMAIN LAYER (Future)
│   │   │   │   │   ├── usecase/
│   │   │   │   │   │   ├── CompleteTaskUseCase.kt
│   │   │   │   │   │   ├── CalculateNextDueDateUseCase.kt
│   │   │   │   │   │   └── TriggerRelatedTasksUseCase.kt
│   │   │   │   │   └── repository/
│   │   │   │   │       └── ITaskRepository.kt         # Repository interface
│   │   │   │   │
│   │   │   │   ├── presentation/                      # PRESENTATION LAYER (Future)
│   │   │   │   │   ├── today/
│   │   │   │   │   │   ├── TodayScreen.kt
│   │   │   │   │   │   └── TodayViewModel.kt
│   │   │   │   │   ├── calendar/
│   │   │   │   │   ├── tasklist/
│   │   │   │   │   └── taskdetails/
│   │   │   │   │
│   │   │   │   └── di/                                # DEPENDENCY INJECTION ✅
│   │   │   │       ├── DatabaseModule.kt              # Database DI module
│   │   │   │       ├── DomainModule.kt                # (Future) Domain DI module
│   │   │   │       └── AppModule.kt                   # (Future) App DI module
│   │   │   │
│   │   │   ├── res/
│   │   │   │   ├── values/
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   └── themes.xml
│   │   │   │   └── drawable/
│   │   │   │
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   ├── test/                                      # UNIT TESTS ✅
│   │   │   └── java/com/lifeops/app/
│   │   │       └── data/local/entity/
│   │   │           └── TaskTest.kt                    # Task entity tests
│   │   │
│   │   └── androidTest/                               # INTEGRATION TESTS ✅
│   │       └── java/com/lifeops/app/
│   │           ├── data/local/dao/
│   │           │   └── TaskDaoTest.kt                 # DAO integration tests
│   │           └── workflow/
│   │               └── Workflow1ExerciseRoutineTest.kt
│   │
│   └── build/                                         # Generated build files
│
├── docs/
│   ├── Architecture.md                                # This document
│   └── Project Overview.md                            # Product requirements
│
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties
│
├── build.gradle.kts                                   # Project-level build config
├── settings.gradle.kts                                # Project settings
├── gradle.properties                                  # Gradle configuration
├── local.properties                                   # Local SDK paths
└── README.md
```

### Package Organization

The codebase follows a **layered package structure** to enforce architectural boundaries:

```
com.lifeops.app/
├── data/           # Data layer (database, repositories)
├── domain/         # Domain layer (use cases, business logic)
├── presentation/   # Presentation layer (UI, ViewModels)
└── di/             # Dependency injection modules
```

**Benefits**:
- Clear separation of concerns
- Easy to navigate and find components
- Enforces dependency rules (outer layers can't access inner layers without DI)
- Scalable as the project grows

---

## Data Layer

The Data Layer is fully implemented and handles all data persistence and retrieval operations.

### Entity: Task

The `Task` entity represents the core data model for tasks in the application.

#### Schema
**File**: `com.lifeops.app.data.local.entity.Task`

```kotlin
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Core Properties
    val name: String,
    val category: String,
    val tags: String? = null,
    val description: String? = null,
    val active: Boolean = true,
    
    // Scheduling
    val intervalUnit: IntervalUnit = IntervalUnit.ADHOC,
    val intervalQty: Int = 1,
    val specificDaysOfWeek: List<DayOfWeek>? = null,
    val excludedDates: List<LocalDate>? = null,
    val excludedDaysOfWeek: List<DayOfWeek>? = null,
    
    // State
    val nextDue: LocalDate? = null,
    val lastCompleted: LocalDate? = null,
    val completionStreak: Int = 0,
    
    // Metadata
    val timeEstimate: Int? = null,        // in minutes
    val difficulty: Difficulty = Difficulty.MEDIUM,
    
    // Relationships
    val parentTaskIds: List<Long>? = null,
    val childOrder: Int? = null,
    val triggeredByTaskIds: List<Long>? = null,
    val triggersTaskIds: List<Long>? = null,
    
    // Inventory
    val requiresInventory: Boolean = false,
    val requiresManualCompletion: Boolean = false
)
```

#### Enums
```kotlin
enum class IntervalUnit { DAY, WEEK, MONTH, ADHOC }
enum class DayOfWeek { MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY }
enum class Difficulty { LOW, MEDIUM, HIGH }
```

#### Field Descriptions

| Field | Type | Purpose |
|-------|------|---------|
| `id` | Long | Auto-generated primary key |
| `name` | String | Task name/title |
| `category` | String | Task category for organization |
| `tags` | String? | Comma-separated tags for filtering |
| `description` | String? | Detailed task description |
| `active` | Boolean | Whether task is active or archived |
| `intervalUnit` | IntervalUnit | Schedule frequency (DAY/WEEK/MONTH/ADHOC) |
| `intervalQty` | Int | Number of interval units between occurrences |
| `specificDaysOfWeek` | List<DayOfWeek>? | Specific days for weekly recurrence |
| `excludedDates` | List<LocalDate>? | Dates to skip task |
| `excludedDaysOfWeek` | List<DayOfWeek>? | Days of week to skip |
| `nextDue` | LocalDate? | Next scheduled date |
| `lastCompleted` | LocalDate? | Last completion date |
| `completionStreak` | Int | Consecutive completions for gamification |
| `timeEstimate` | Int? | Estimated duration in minutes |
| `difficulty` | Difficulty | Task difficulty (LOW/MEDIUM/HIGH) |
| `parentTaskIds` | List<Long>? | Parent task IDs for child tasks |
| `childOrder` | Int? | Order when displayed under parent |
| `triggeredByTaskIds` | List<Long>? | Tasks that trigger this task |
| `triggersTaskIds` | List<Long>? | Tasks triggered by completing this task |
| `requiresInventory` | Boolean | Whether task consumes supplies |
| `requiresManualCompletion` | Boolean | Override automatic completion |

### Type Converters

**File**: `com.lifeops.app.data.local.converter.Converters`

Room requires type converters to store complex Kotlin types in SQLite:

```kotlin
@TypeConverter
fun fromLocalDate(value: LocalDate?): String? = value?.toString()

@TypeConverter
fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

@TypeConverter
fun fromLongList(value: List<Long>?): String? = gson.toJson(value)

@TypeConverter
fun toLongList(value: String?): List<Long>? = ...

@TypeConverter
fun fromDayOfWeekList(value: List<DayOfWeek>?): String? = gson.toJson(value)

@TypeConverter
fun toDayOfWeekList(value: String?): List<DayOfWeek>? = ...

@TypeConverter
fun fromLocalDateList(value: List<LocalDate>?): String? = ...

@TypeConverter
fun toLocalDateList(value: String?): List<LocalDate>? = ...
```

**Supported Conversions**:
- `LocalDate` ↔ ISO-8601 String (e.g., "2025-10-24")
- `List<Long>` ↔ JSON Array
- `List<DayOfWeek>` ↔ JSON Array
- `List<LocalDate>` ↔ JSON Array
- Enums ↔ String (automatic by Room)

### Data Access Object (DAO)

**File**: `com.lifeops.app.data.local.dao.TaskDao`

The DAO provides 16 database operations:

#### CRUD Operations

```kotlin
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insert(task: Task): Long

@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insertAll(tasks: List<Task>): List<Long>

@Update
suspend fun update(task: Task)

@Delete
suspend fun delete(task: Task)
```

#### Query Operations

```kotlin
// Get single task
@Query("SELECT * FROM tasks WHERE id = :taskId")
suspend fun getById(taskId: Long): Task?

// Get all active tasks
@Query("SELECT * FROM tasks WHERE active = 1")
fun observeAllActive(): Flow<List<Task>>

// Get tasks due by specific date
@Query("SELECT * FROM tasks WHERE active = 1 AND nextDue <= :date ORDER BY nextDue ASC")
suspend fun getTasksDueByDate(date: LocalDate): List<Task>

// Get all tasks ordered by due date
@Query("SELECT * FROM tasks WHERE active = 1 ORDER BY nextDue ASC")
fun observeAllTasksOrderedByDueDate(): Flow<List<Task>>

// Get tasks by category
@Query("SELECT * FROM tasks WHERE active = 1 AND category = :category")
suspend fun getTasksByCategory(category: String): List<Task>

// Get distinct categories
@Query("SELECT DISTINCT category FROM tasks WHERE active = 1")
suspend fun getAllCategories(): List<String>

// Get child tasks of a parent
@Query("SELECT * FROM tasks WHERE parentTaskIds LIKE '%' || :parentId || '%' ORDER BY childOrder ASC")
suspend fun getChildrenOfParent(parentId: Long): List<Task>

// Get tasks triggered by specific task
@Query("SELECT * FROM tasks WHERE triggeredByTaskIds LIKE '%' || :taskId || '%'")
suspend fun getTasksTriggeredBy(taskId: Long): List<Task>

// Search tasks
@Query("SELECT * FROM tasks WHERE active = 1 AND (name LIKE :query OR category LIKE :query OR tags LIKE :query)")
suspend fun search(query: String): List<Task>

// Check if task exists
@Query("SELECT EXISTS(SELECT 1 FROM tasks WHERE id = :taskId)")
suspend fun exists(taskId: Long): Boolean
```

**Key Features**:
- **Reactive Queries**: `Flow<List<Task>>` for automatic UI updates
- **Suspend Functions**: Coroutine support for async operations
- **LIKE Queries**: Search in JSON arrays (e.g., `parentTaskIds LIKE '%42%'`)
- **Ordering**: `childOrder` for parent-child relationships

### Repository

**File**: `com.lifeops.app.data.repository.TaskRepository`

The repository abstracts the data source and provides a clean API with error handling:

```kotlin
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {
    // Wraps all operations in Result<T> for error handling
    suspend fun createTask(task: Task): Result<Long> = try {
        Result.success(taskDao.insert(task))
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    suspend fun getTaskById(id: Long): Result<Task?> = ...
    suspend fun updateTask(task: Task): Result<Unit> = ...
    suspend fun deleteTask(task: Task): Result<Unit> = ...
    // ... 14 more methods
}
```

**All Methods** (18 total):
1. `createTask` - Insert single task
2. `createTasks` - Insert multiple tasks
3. `updateTask` - Update existing task
4. `updateTaskSchedule` - Update only scheduling fields
5. `deleteTask` - Delete task
6. `archiveTask` - Set `active = false`
7. `restoreTask` - Set `active = true`
8. `getTaskById` - Retrieve by ID
9. `getAllActiveTasks` - Get all active tasks as Flow
10. `getTasksDueByDate` - Get tasks due by date
11. `getAllTasksOrderedByDueDate` - Get all tasks sorted by due date
12. `getTasksByCategory` - Filter by category
13. `getAllCategories` - Get unique categories
14. `getChildTasks` - Get children of parent task
15. `getTriggeredTasks` - Get tasks triggered by a task
16. `searchTasks` - Full-text search
17. `taskExists` - Check existence
18. `observeAllActiveTasks` - Reactive task list

**Design Patterns**:
- **Result<T> Wrapper**: Type-safe error handling without exceptions
- **Repository Pattern**: Abstracts data source implementation
- **Dependency Injection**: Constructor injection via Hilt

### Room Database

**File**: `com.lifeops.app.data.local.LifeOpsDatabase`

```kotlin
@Database(
    entities = [Task::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LifeOpsDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    
    companion object {
        const val DATABASE_NAME = "lifeops_database"
    }
}
```

**Configuration**:
- **Entities**: Task (more entities to be added)
- **Version**: 1 (will increment with schema changes)
- **Type Converters**: Registered globally
- **Export Schema**: Disabled for development

---

## Domain Layer

**Status**: Planned (Not Yet Implemented)

The Domain Layer will contain pure business logic independent of Android framework.

### Use Cases (Examples)

#### CompleteTaskUseCase
```kotlin
class CompleteTaskUseCase @Inject constructor(
    private val taskRepository: ITaskRepository,
    private val calculateNextDueDateUseCase: CalculateNextDueDateUseCase,
    private val triggerRelatedTasksUseCase: TriggerRelatedTasksUseCase,
    private val consumeInventoryUseCase: ConsumeInventoryUseCase
) {
    suspend operator fun invoke(taskId: Long, completionDate: LocalDate): Result<Unit> {
        // 1. Get task
        // 2. Calculate next due date
        // 3. Update completion streak
        // 4. Trigger related tasks
        // 5. Consume inventory if required
        // 6. Create TaskLog entry
    }
}
```

#### CalculateNextDueDateUseCase
```kotlin
class CalculateNextDueDateUseCase @Inject constructor() {
    operator fun invoke(task: Task, completionDate: LocalDate): LocalDate? {
        // Calculate next due date based on:
        // - intervalUnit & intervalQty
        // - specificDaysOfWeek
        // - excludedDates & excludedDaysOfWeek
        // - ADHOC tasks return null
    }
}
```

#### TriggerRelatedTasksUseCase
```kotlin
class TriggerRelatedTasksUseCase @Inject constructor(
    private val taskRepository: ITaskRepository
) {
    suspend operator fun invoke(completedTaskId: Long): Result<Unit> {
        // 1. Get tasks with triggeredByTaskIds containing completedTaskId
        // 2. Update their nextDue to today or calculate
        // 3. Optionally auto-complete if conditions met
    }
}
```

### Repository Interface

```kotlin
interface ITaskRepository {
    suspend fun createTask(task: Task): Result<Long>
    suspend fun getTaskById(id: Long): Result<Task?>
    // ... all methods from TaskRepository
}
```

**Benefits**:
- Domain layer depends on interface, not implementation
- Easy to swap implementations (Room → Remote API)
- Better testability with mock repositories

---

## Presentation Layer

**Status**: Planned (Not Yet Implemented)

The Presentation Layer will use Jetpack Compose for UI with ViewModels managing state.

### Screen Architecture

Each screen follows this structure:

```
TodayScreen/
├── TodayScreen.kt          # Composable UI
├── TodayViewModel.kt       # State management
├── TodayUiState.kt         # UI state data class
└── TodayUiEvent.kt         # User interaction events
```

### ViewModel Pattern (Example)

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
            is TodayUiEvent.Refresh -> loadTasksDueToday()
        }
    }
    
    private fun loadTasksDueToday() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getTasksDueUseCase(LocalDate.now()).collect { result ->
                _uiState.update {
                    it.copy(
                        tasks = result.getOrNull() ?: emptyList(),
                        isLoading = false,
                        error = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }
    
    private fun completeTask(taskId: Long) {
        viewModelScope.launch {
            completeTaskUseCase(taskId, LocalDate.now())
            // UI updates automatically via Flow
        }
    }
}
```

### UI State

```kotlin
data class TodayUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface TodayUiEvent {
    data class CompleteTask(val taskId: Long) : TodayUiEvent
    object Refresh : TodayUiEvent
}
```

### Composable Screen (Example)

```kotlin
@Composable
fun TodayScreen(
    viewModel: TodayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    TodayScreenContent(
        tasks = uiState.tasks,
        isLoading = uiState.isLoading,
        error = uiState.error,
        onCompleteTask = { taskId ->
            viewModel.onEvent(TodayUiEvent.CompleteTask(taskId))
        },
        onRefresh = {
            viewModel.onEvent(TodayUiEvent.Refresh)
        }
    )
}

@Composable
private fun TodayScreenContent(
    tasks: List<Task>,
    isLoading: Boolean,
    error: String?,
    onCompleteTask: (Long) -> Unit,
    onRefresh: () -> Unit
) {
    // Compose UI implementation
}
```

### Planned Screens

Per Project Overview document:

1. **Today Screen**: Checklist of tasks due today
2. **Calendar Screen**: Month view with tasks
3. **Task List Screen**: All tasks organized by category
4. **Task Details Screen**: Create/edit task
5. **Inventory Screen**: Supply management
6. **Settings Screen**: App preferences

---

## Dependency Injection

**Framework**: Hilt (built on Dagger)

### Application Class

**File**: `com.lifeops.app.LifeOpsApplication`

```kotlin
@HiltAndroidApp
class LifeOpsApplication : Application()
```

Registered in `AndroidManifest.xml`:
```xml
<application
    android:name=".LifeOpsApplication"
    ...>
```

### Modules

#### DatabaseModule

**File**: `com.lifeops.app.di.DatabaseModule`

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideLifeOpsDatabase(
        @ApplicationContext context: Context
    ): LifeOpsDatabase {
        return Room.databaseBuilder(
            context,
            LifeOpsDatabase::class.java,
            LifeOpsDatabase.DATABASE_NAME
        ).build()
    }
    
    @Provides
    @Singleton
    fun provideTaskDao(database: LifeOpsDatabase): TaskDao {
        return database.taskDao()
    }
}
```

**Provided Dependencies**:
- `LifeOpsDatabase` - Singleton Room database
- `TaskDao` - Singleton DAO instance

#### Future Modules

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class DomainModule {
    @Binds
    abstract fun bindTaskRepository(
        impl: TaskRepository
    ): ITaskRepository
}

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {
    // Provide use cases scoped to ViewModel lifecycle
}
```

### Entry Points

Activities and Fragments use `@AndroidEntryPoint`:

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Can now inject dependencies
}
```

ViewModels use `@HiltViewModel`:

```kotlin
@HiltViewModel
class TodayViewModel @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase
) : ViewModel()
```

---

## Testing Strategy

### Test Pyramid

```
       ┌─────────────┐
       │  E2E Tests  │  (Future - UI Tests with Compose)
       │   (Small)   │
       └─────────────┘
      ┌───────────────┐
      │ Integration   │  (✅ DAO Tests, Workflow Tests)
      │     Tests     │
      │   (Medium)    │
      └───────────────┘
    ┌─────────────────────┐
    │    Unit Tests       │  (✅ Entity Tests, Future: Use Case Tests)
    │     (Large)         │
    └─────────────────────┘
```

### Unit Tests

**Location**: `app/src/test/java/`  
**Framework**: JUnit 4, Google Truth

#### TaskTest.kt

Tests the `Task` entity in isolation without database.

**Coverage** (20+ tests):
- Task creation with different configurations
- ADHOC vs scheduled tasks
- Parent-child relationships (single and multiple parents)
- Trigger relationships
- Excluded dates and days of week
- Time estimates and difficulty
- Completion streaks
- Tag parsing

**Example Test**:
```kotlin
@Test
fun `task with multiple parents has correct parent IDs`() {
    val task = Task(
        name = "Subtask",
        category = "Fitness",
        parentTaskIds = listOf(1L, 2L, 3L)
    )
    
    assertThat(task.parentTaskIds).containsExactly(1L, 2L, 3L)
    assertThat(task.parentTaskIds).hasSize(3)
}
```

### Integration Tests

**Location**: `app/src/androidTest/java/`  
**Framework**: AndroidX Test, Room Testing

#### TaskDaoTest.kt

Tests database operations with in-memory Room database.

**Setup**:
```kotlin
@get:Rule
val instantTaskExecutorRule = InstantTaskExecutorRule()

private lateinit var database: LifeOpsDatabase
private lateinit var taskDao: TaskDao

@Before
fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, LifeOpsDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    taskDao = database.taskDao()
}
```

**Coverage** (25+ tests):
- CRUD operations (insert, update, delete)
- Query correctness (getById, getByCategory, search)
- Flow observations (reactive updates)
- Parent-child queries
- Trigger relationships
- Ordering and filtering
- Edge cases (null handling, empty results)

**Example Test**:
```kotlin
@Test
fun insertTaskAndGetById() = runTest {
    val task = Task(name = "Test Task", category = "Test")
    val id = taskDao.insert(task)
    
    val retrieved = taskDao.getById(id)
    
    assertThat(retrieved).isNotNull()
    assertThat(retrieved?.name).isEqualTo("Test Task")
}
```

#### Workflow1ExerciseRoutineTest.kt

Tests real-world user scenarios from Project Overview document.

**Scenario**: Exercise Routine with Parent-Child Tasks
- Parent: "Morning Workout"
  - Child 1: "Warm-up" (childOrder = 1)
  - Child 2: "Cardio" (childOrder = 2)
  - Child 3: "Cool-down" (childOrder = 3)

**Coverage** (6 tests):
- Parent-child grouping
- Custom ordering of children
- Recurring schedule setup
- Automatic completion logic
- Difficulty indicators
- Time estimation

**Example Test**:
```kotlin
@Test
fun childTasksAppearInCorrectOrderUnderParent() = runTest {
    // Create parent
    val parentId = taskDao.insert(parentTask)
    
    // Create children with specific order
    taskDao.insertAll(listOf(warmup, cardio, cooldown))
    
    // Retrieve children
    val children = taskDao.getChildrenOfParent(parentId)
    
    // Verify order
    assertThat(children).hasSize(3)
    assertThat(children[0].name).isEqualTo("Warm-up")
    assertThat(children[1].name).isEqualTo("Cardio")
    assertThat(children[2].name).isEqualTo("Cool-down")
}
```

### Future Testing

#### Use Case Tests
```kotlin
@Test
fun `CompleteTaskUseCase updates streak and triggers related tasks`() = runTest {
    // Mock repository and dependencies
    // Execute use case
    // Verify business logic
}
```

#### UI Tests (Compose)
```kotlin
@Test
fun todayScreenDisplaysTasksDueToday() {
    composeTestRule.setContent {
        TodayScreen()
    }
    
    composeTestRule.onNodeWithText("Task 1").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Complete").performClick()
}
```

---

## Build Configuration

### Gradle Optimization

**File**: `gradle.properties`

```properties
# Java 21 Configuration
org.gradle.java.home=C\:\\Program Files\\Java\\jdk-21.0.8

# Performance Optimizations
org.gradle.daemon=true              # Keep Gradle daemon running
org.gradle.parallel=true            # Parallel module builds
org.gradle.caching=true             # Enable build cache
kotlin.incremental=true             # Incremental Kotlin compilation

# JVM Options
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m

# Android Options
android.useAndroidX=true
android.enableJetifier=true
```

**Impact**:
- Build time: 1m18s → 3s (incremental builds)
- Gradle daemon eliminates startup overhead
- Parallel builds utilize multi-core CPUs
- Caching reuses previous build outputs

### App-Level Build Script

**File**: `app/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.lifeops.app"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.lifeops.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    
    kotlinOptions {
        jvmTarget = "21"
    }
    
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // Hilt Dependency Injection
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    
    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.google.truth:truth:1.1.5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.room:room-testing:2.6.1")
    androidTestImplementation("com.google.truth:truth:1.1.5")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```

### Kotlin Symbol Processing (KSP)

Replaced KAPT with KSP for faster annotation processing:

- **Room Compiler**: Generates DAO implementations
- **Hilt Compiler**: Generates dependency injection code

**Benefits over KAPT**:
- 2x faster compilation
- Better Kotlin support
- Lower memory usage

---

## Future Considerations

### Remaining Entities

Per Data Model specification:

1. **Supply**: Inventory items
2. **TaskSupply**: Many-to-many task-supply relationship
3. **Inventory**: Supply transaction history
4. **ChecklistItem**: Sub-tasks within tasks
5. **TaskLog**: Task completion history
6. **RestockTask**: Auto-generated inventory replenishment

### Advanced Features

- **Notifications**: AlarmManager + WorkManager for task reminders
- **Widgets**: Home screen widget for today's tasks
- **Backup/Restore**: Export/import data as JSON
- **Cloud Sync**: Firebase or custom backend (optional)
- **Analytics**: Track completion rates, streaks
- **Dark Mode**: Material 3 dynamic theming
- **Localization**: Multi-language support

### Performance Optimization

- **Database Indexing**: Add indices for frequently queried columns
- **Pagination**: Use Paging 3 library for large task lists
- **WorkManager**: Background task processing
- **Lazy Loading**: Load parent tasks first, children on demand

### Security

- **Data Encryption**: Encrypt sensitive task descriptions
- **App Lock**: PIN/Biometric authentication
- **Secure Storage**: Use EncryptedSharedPreferences for settings

---

## Development Guidelines

### Code Style

- **Kotlin Coding Conventions**: Follow official Kotlin style guide
- **Compose Guidelines**: Use Material 3 guidelines
- **Naming**:
  - Use Cases: `VerbNounUseCase` (e.g., `CompleteTaskUseCase`)
  - ViewModels: `ScreenNameViewModel` (e.g., `TodayViewModel`)
  - Composables: PascalCase function names

### Git Workflow

- **Branch Strategy**: Feature branches from `main`
- **Commit Messages**: Conventional Commits format
  - `feat: Add task completion use case`
  - `fix: Correct next due date calculation`
  - `test: Add workflow 2 integration tests`
  - `docs: Update architecture documentation`

### Testing Requirements

- **Unit Tests**: Required for all use cases and domain logic
- **Integration Tests**: Required for all DAOs and repositories
- **Workflow Tests**: Required for each workflow in Project Overview
- **Coverage Goal**: 80%+ for domain layer, 60%+ for data layer

### Documentation

- **Code Comments**: Document complex business logic
- **KDoc**: Public APIs and interfaces
- **README Updates**: Keep development setup current
- **Architecture Updates**: Maintain this document as architecture evolves

---

## References

### Internal Documents
- **Project Overview**: `docs/Project Overview.md` - Product requirements and specifications
- **README**: `README.md` - Getting started guide

### External Resources
- [Clean Architecture (Robert C. Martin)](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

---

## Appendix

### Build Commands

```powershell
# Full build
.\gradlew build

# Clean build
.\gradlew clean build

# Run unit tests
.\gradlew test

# Run integration tests (requires emulator/device)
.\gradlew connectedAndroidTest

# Run specific test class
.\gradlew test --tests TaskTest

# Generate test coverage report
.\gradlew jacocoTestReport

# Install debug APK
.\gradlew installDebug

# List all tasks
.\gradlew tasks
```

### Project Metrics

| Metric | Value |
|--------|-------|
| **Total Classes** | 15+ |
| **Total Lines of Code** | ~2000+ |
| **Test Classes** | 3 |
| **Total Tests** | 51+ |
| **Test Coverage** | ~85% (Data Layer) |
| **Build Time (Incremental)** | 3s |
| **Build Time (Clean)** | 12s |
| **Min Android Version** | 8.0 (API 26) |
| **Target Android Version** | 14 (API 34) |

### Technology Versions

| Technology | Version | Release Date |
|------------|---------|--------------|
| Kotlin | 1.9.22 | Jan 2024 |
| Java | 21.0.8 (LTS) | Sep 2023 |
| Gradle | 8.5 | Nov 2023 |
| Android Gradle Plugin | 8.2.0 | Nov 2023 |
| Compose BOM | 2023.10.01 | Oct 2023 |
| Room | 2.6.1 | Nov 2023 |
| Hilt | 2.50 | Dec 2023 |
| KSP | 1.9.22-1.0.17 | Jan 2024 |

---

**Document Version**: 1.0  
**Last Updated**: October 24, 2025  
**Maintained By**: Development Team  
**Repository**: https://github.com/LukeBrummett/Life-Ops
