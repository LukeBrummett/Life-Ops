# Changelog

All notable changes to Life-Ops will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.3] - 2025-11-26

### ✨ Features

**Parent-Child Schedule Control**
- Added `inheritParentSchedule` toggle to control whether child tasks inherit their parent's schedule
- Child tasks can now have independent schedules or follow parent schedule based on user preference
- Improves flexibility in managing task hierarchies

### 🐛 Bug Fixes

**Parent Task Completion Behavior**
- Fixed parent tasks automatically completing when all children are marked complete
- Parents now require manual completion regardless of `requiresManualCompletion` flag
- Parent tasks remain visible until both parent AND all due children are marked complete
- Child tasks can disappear freely once completed while parent stays visible

**Child Task Visibility**
- Fixed scheduled children not appearing standalone when parent is not due
- Children with independent schedules now correctly appear on their own due dates
- Improved task grouping logic to show children under parents only when both are due

**Task Editing - ADHOC Mode**
- Fixed intervalUnit remaining ADHOC when switching to Interval mode in task edit
- Tasks now correctly save with interval schedule instead of ADHOC when mode is changed
- Resolved issue where only `specificDaysOfWeek` was cleared but `intervalUnit` was not updated

**Orphaned Child Tasks**
- Fixed child tasks becoming permanently hidden when their parent is deleted
- Implemented automatic cleanup of parent references when deleting tasks
- Added defensive display logic to handle edge cases with orphaned children

**Task Import**
- Fixed non-conflicting tasks being silently dropped during import when conflicts exist
- Import now correctly imports all non-conflicting tasks along with resolved conflicts
- Conflict resolution dialog no longer loses tasks that don't have ID conflicts

**Category Dropdown Crash**
- Fixed app crash with `IllegalStateException: FocusRequester is not initialized` in Task Edit screen
- Replaced `ExposedDropdownMenuBox` with regular `DropdownMenu` for category field
- Category dropdown now only opens via arrow button click, not on text field focus

**Task Ordering**
- Fixed tasks reordering/shuffling when marked complete in Today view
- Changed sort order from nextDue/name to category/id for stable positioning
- Tasks now maintain consistent position within categories

### 📚 Documentation

**Architecture & Design**
- Overhauled documentation to reflect current codebase state
- Updated architecture documents with accurate implementation details
- Improved design documentation for screens and features

---

## [1.0.2] - 2025-11-11

### 🐛 Bug Fixes

**Today View**
- Fixed ADHOC child tasks incorrectly appearing in Today view when not triggered
- ADHOC tasks now only appear when explicitly triggered (nextDue is set)
- Added comprehensive test coverage for ADHOC task query behavior

**Test Infrastructure**
- Fixed androidTest compilation errors (method names with spaces)
- Added missing androidTest dependencies (junit, test-core, runner, rules)

### ✨ Features

**Task Creation**
- Added "Next Due Date" field to task creation/editing screen
- Users can now set custom due dates when creating or editing tasks
- Provides better control over task scheduling

### 🔧 Maintenance

**Build System**
- Upgraded Gradle wrapper to 8.13
- Updated Gradle wrapper scripts with latest improvements
- All 57 tests passing (23 unit tests + 34 instrumented tests)

---

## [1.0.1] - 2025-10-26

### 🐛 Bug Fixes

**Task Editing**
- Fixed TaskEditScreen save button not triggering navigation after successful save
- Fixed ADHOC tasks unable to be saved (validation incorrectly required intervalQty >= 1)
- Fixed event consumption happening before navigation callbacks in TaskEditScreen

**Sample Data**
- Fixed "cannot access database on main thread" error when loading sample data
- Wrapped database.clearAllTables() in withContext(Dispatchers.IO) for proper thread handling

### ✨ Features

**Task Deletion**
- Added delete functionality to TaskDetailScreen
- Delete button in TopAppBar next to Edit button
- Confirmation dialog with task name and "cannot be undone" warning
- Proper error handling and navigation after deletion
- Styled with error color scheme for destructive actions

**Developer Experience**
- Sample data loading now optional via Settings instead of auto-loading on every launch
- Improved project structure (removed unused folders, organized documentation)

---

## [1.0.0] - 2025-10-26

### 🎉 Initial Release

The first production release of Life-Ops - a complete offline task management system with intelligent scheduling and inventory tracking.

#### ✨ Features

**Core Task Management**
- Interval-based recurring tasks (daily, weekly, monthly, adhoc)
- Specific days of week scheduling
- Schedule exclusions (never schedule on specific days/dates)
- Configurable overdue behavior (postpone vs skip to next occurrence)
- Ephemeral tasks (delete after completion)
- Task categories and tags

**Task Relationships**
- Parent-child task hierarchies
- Automatic parent completion when all children complete
- Manual completion override for parent tasks
- Task triggering (completion of one task spawns another)
- Dependency visualization in task details

**Inventory Management**
- Supply tracking with quantity monitoring
- Task-supply associations with consumption modes:
  - Fixed quantity consumption
  - Prompted quantity (with defaults)
  - Full inventory recount
- Supply availability gating for task execution
- Low supply warnings

**User Interface**
- Today Screen: Daily checklist with all scheduled tasks
- All Tasks Screen: Complete task catalog with search and filters
- Task Detail Screen: Completion history, streaks, relationships
- Task Edit Screen: Comprehensive task configuration
- Inventory Screen: Supply management and tracking
- Settings Screen: Data import/export, preferences, statistics

**Data Management**
- Complete offline operation (zero network dependencies)
- Import/Export functionality with conflict resolution
- Sample data for testing and demonstration
- Debug mode with time travel for testing

**Polish & Quality**
- Material3 design with full dark mode support
- Smooth animations and transitions
- Form validation with helpful error messages
- Confirmation dialogs for destructive actions
- Empty states with contextual guidance

#### 🏗️ Technical

**Architecture**
- Clean Architecture with MVVM pattern
- Jetpack Compose UI with Material3
- Room database with migrations
- Hilt dependency injection
- Kotlin Coroutines for async operations
- StateFlow for reactive state management

**Build & Deploy**
- Gradle Kotlin DSL build configuration
- KSP for code generation
- ProGuard rules for release builds
- Signed APK generation

#### 📚 Documentation
- Comprehensive README with installation instructions
- Detailed project overview and feature specifications
- Architecture documentation
- Screen-specific design documents
- Contributing guidelines

#### 🐛 Known Issues
- None critical for v1.0.0 release

#### 🔄 Migration Notes
- First release - no migration needed

---

## [Unreleased]

### Planned Features
- Time-based scheduling (specific times of day)
- Calendar integration (view-only)
- Advanced statistics and insights
- Customizable notifications
- Custom themes and color schemes
- Tablet/landscape layout optimization

---

[1.0.0]: https://github.com/LukeBrummett/Life-Ops/releases/tag/v1.0.0
