# Changelog

All notable changes to Life-Ops will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
