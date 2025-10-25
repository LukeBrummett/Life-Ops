# Settings Screen Design

**Screen Type**: Configuration / Data Management  
**Purpose**: Configure app-wide preferences and manage data import/export  
**Status**: Not Started  
**Last Updated**: October 25, 2025

---

## Table of Contents
1. [Overview](#overview)
2. [Screen Layout](#screen-layout)
3. [Component Specifications](#component-specifications)
4. [Settings Categories](#settings-categories)
5. [Import/Export Functionality](#importexport-functionality)
6. [Navigation Flow](#navigation-flow)
7. [Implementation Checklist](#implementation-checklist)

---

## Overview

The Settings screen provides users with control over app-wide configuration and data management capabilities. As an offline-first application, the primary focus is on data portability through import/export functionality, with minimal configuration options to maintain simplicity.

### Design Principles

1. **Simplicity First**: Minimal settings to reduce complexity
2. **Data Portability**: Easy export/import for backup and LLM-assisted task creation
3. **User Control**: Full transparency of data operations
4. **Offline-Only**: No cloud sync or account settings (V1)
5. **Clear Actions**: Obvious outcomes for every setting change

### Key Features

- **Data Export**: Complete database export to JSON format
- **Data Import**: Validation and conflict resolution for imported data
- **Backup Management**: User-controlled backup creation
- **Data Portability**: Support for external task creation via LLM-generated files
- **Future Extensibility**: Structure supports additional settings in future versions

---

## Screen Layout

### Visual Hierarchy

```
┌─────────────────────────────────────────────────────────┐
│                      HEADER                             │
│  ← Back              Settings                           │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  DATA MANAGEMENT                                        │
│  ┌─────────────────────────────────────────────────┐   │
│  │  📤 Export All Data                            │   │
│  │  Export entire database to JSON file          │   │
│  │                                                │   │
│  │                            [Export] ───────►   │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │  📥 Import Data                                │   │
│  │  Import tasks and inventory from JSON file    │   │
│  │                                                │   │
│  │                            [Import] ───────►   │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │  💾 Create Backup                              │   │
│  │  Save current state as backup file            │   │
│  │                                                │   │
│  │                            [Backup] ───────►   │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  ABOUT                                                  │
│  ┌─────────────────────────────────────────────────┐   │
│  │  Version: 1.0.0                                │   │
│  │  Database Version: 1                           │   │
│  │  Total Tasks: 47                               │   │
│  │  Total Inventory Items: 23                     │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  [Future settings sections will appear here]            │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## Component Specifications

### Header Bar
**Component**: `CenterAlignedTopAppBar`

**Elements**:
- Back button (left) - Returns to previous screen
- Title: "Settings" (center)
- No action buttons on right

**Behavior**:
- Back navigation returns to Today Screen (or previous screen in nav stack)
- Standard Material 3 app bar styling

---

### Settings Item Card
**Component**: Reusable `SettingCard` composable

**Structure**:
```kotlin
SettingCard(
    icon: ImageVector,
    title: String,
    description: String,
    actionButton: @Composable () -> Unit
)
```

**Visual Design**:
- Card with subtle elevation
- Icon on left (24dp, primary color)
- Title (titleMedium style, bold)
- Description (bodyMedium style, onSurfaceVariant color)
- Action button on right (aligned to end)
- 16dp padding
- 8dp spacing between cards

**States**:
- Default state
- Loading state (when operation in progress)
- Success state (brief confirmation)
- Error state (with error message)

---

### Section Headers
**Component**: Text with divider

**Visual Design**:
- Section title (labelLarge, all caps)
- Subtle divider line below
- 24dp top padding, 8dp bottom padding
- onSurfaceVariant color

**Sections**:
- DATA MANAGEMENT (export, import, backup)
- ABOUT (version info, statistics)
- Future sections as needed

---

## Settings Categories

### Data Management

#### Export All Data

**Purpose**: Export complete database to JSON file

**Button**: "Export"

**Behavior**:
1. User taps Export button
2. System generates JSON file with:
   - All tasks (with relationships)
   - All supplies
   - All inventory quantities
   - All checklist items
   - All task logs
   - Database version info
3. Android file picker opens
4. User selects save location and filename
5. File saves successfully → show success message
6. File save fails → show error with details

**JSON Structure**:
```json
{
  "version": "1.0",
  "exportDate": "2025-10-25T14:30:00Z",
  "tasks": [...],
  "supplies": [...],
  "inventory": [...],
  "checklistItems": [...],
  "taskLogs": [...]
}
```

**Error Handling**:
- Storage permission denied → request permission
- Insufficient storage → clear error message
- Write failure → retry option

---

#### Import Data

**Purpose**: Import tasks and inventory from JSON file

**Button**: "Import"

**Behavior**:
1. User taps Import button
2. Android file picker opens
3. User selects JSON file
4. System validates file:
   - Valid JSON format
   - Compatible schema version
   - Required fields present
5. Show conflict resolution dialog if needed:
   - Tasks with duplicate IDs
   - Supplies with duplicate names
   - Options: Skip, Replace, Keep Both
6. Import executes with user's conflict choices
7. Success → show summary (X tasks imported, Y skipped)
8. Failure → show error with details

**Validation Rules**:
- JSON must be valid
- Schema version must be compatible
- Required fields must exist
- Relationships must be valid (no orphaned children/triggers)
- Dates must be valid ISO-8601 format

**Conflict Resolution**:
- **Skip**: Don't import conflicting item
- **Replace**: Overwrite existing item with imported data
- **Keep Both**: Generate new ID for imported item

**Success Summary**:
```
Import Complete
✓ 15 tasks imported
✓ 8 supplies imported
⚠ 2 tasks skipped (duplicates)
```

---

#### Create Backup

**Purpose**: Quick backup creation with timestamp

**Button**: "Backup"

**Behavior**:
1. User taps Backup button
2. System generates export JSON (same format as Export)
3. Automatically saves to:
   - `Downloads/LifeOps/Backups/backup_2025-10-25_14-30.json`
4. Show success toast: "Backup created: backup_2025-10-25_14-30.json"
5. Failure → show error with details

**Auto-naming Convention**:
- Format: `backup_YYYY-MM-DD_HH-mm.json`
- Saves to Downloads folder (user-accessible)
- No backup rotation/cleanup (user manages manually)

---

### About Section

**Purpose**: Display app and database information

**Display-Only Information**:
- App version (from BuildConfig)
- Database version (schema version)
- Total active tasks count
- Total supplies count
- Last backup date (if available)

**Visual Design**:
- Card with subtle background
- Read-only text fields
- No interactive elements
- Updates dynamically when screen loads

**Example**:
```
Version: 1.0.0
Database Version: 1
Total Tasks: 47
Total Inventory Items: 23
Last Backup: Oct 24, 2025 at 3:15 PM
```

---

## Import/Export Functionality

### Export Format Specification

**File Extension**: `.json`

**MIME Type**: `application/json`

**Encoding**: UTF-8

**Root Object Structure**:
```json
{
  "version": "1.0",
  "exportDate": "ISO-8601 timestamp",
  "metadata": {
    "appVersion": "1.0.0",
    "databaseVersion": 1,
    "taskCount": 47,
    "supplyCount": 23
  },
  "tasks": [
    {
      "id": 1,
      "name": "Morning Workout",
      "category": "Health",
      "tags": "exercise,fitness",
      "description": "30 min cardio + strength",
      "active": true,
      "intervalUnit": "DAY",
      "intervalQty": 1,
      "specificDaysOfWeek": null,
      "excludedDates": [],
      "excludedDaysOfWeek": [],
      "nextDue": "2025-10-26",
      "lastCompleted": "2025-10-25T07:30:00Z",
      "timeEstimate": 30,
      "difficulty": "MEDIUM",
      "parentTaskIds": [],
      "requiresManualCompletion": false,
      "childOrder": null,
      "triggeredByTaskIds": [],
      "triggersTaskIds": [],
      "requiresInventory": false,
      "completionStreak": 14
    }
  ],
  "supplies": [
    {
      "id": 1,
      "name": "Air Filter",
      "category": "HVAC",
      "tags": "home,maintenance",
      "unit": "count",
      "reorderThreshold": 2,
      "reorderTargetQuantity": 6,
      "notes": "16x20x1 size"
    }
  ],
  "inventory": [
    {
      "supplyId": 1,
      "currentQuantity": 4,
      "lastUpdated": "2025-10-20T10:00:00Z"
    }
  ],
  "taskSupplies": [
    {
      "taskId": 5,
      "supplyId": 1,
      "consumptionMode": "FIXED",
      "fixedQuantity": 1,
      "promptedDefaultValue": null
    }
  ]
}
```

**Omitted from Export** (not needed for restore):
- Checklist items (regenerated from schedule)
- Task logs (historical data, optional in future)
- Restock tasks (regenerated as needed)

---

### Import Validation

**Schema Version Compatibility**:
- V1 imports only V1 exports
- Future versions will handle migration
- Incompatible version → clear error message

**Field Validation**:
- Required fields must be present
- Data types must match schema
- Dates must be valid ISO-8601
- Enums must match allowed values
- Foreign keys must resolve (or use conflict resolution)

**Relationship Validation**:
- Parent task IDs must exist in import or database
- Trigger task IDs must exist in import or database
- Supply IDs in TaskSupply must exist
- No circular parent-child relationships (warning, not error)

**Error Messages**:
```
✗ Invalid file format
  - Expected JSON, received corrupted data

✗ Incompatible version
  - File version: 2.0
  - Supported: 1.0
  - Update app to import this file

✗ Missing required fields
  - Task 5: missing 'intervalUnit'
  - Supply 3: missing 'reorderThreshold'

✗ Invalid relationships
  - Task 12: parent task ID 99 not found
  - Fix: Create parent task first or skip this task
```

---

## Navigation Flow

```
Today Screen
    │
    └─→ Settings Screen (Header button)
        ├─→ Export Data
        │   └─→ File picker → Export complete → Settings Screen
        │
        ├─→ Import Data
        │   └─→ File picker → Validation → Conflict Resolution Dialog → Import complete → Settings Screen
        │
        └─→ Create Backup
            └─→ Backup created → Success toast → Settings Screen

All flows return to Settings Screen after completion
Back button returns to Today Screen
```

---

## Implementation Checklist

### Phase 1: Basic Screen Structure
- [ ] Create `SettingsScreen.kt` composable
- [ ] Create `SettingsViewModel.kt` with state management
- [ ] Add navigation route in `Screen.kt`
- [ ] Wire up navigation from Today Screen header
- [ ] Implement header bar with back navigation

### Phase 2: About Section (Read-Only)
- [ ] Create `AboutCard` composable
- [ ] Get app version from BuildConfig
- [ ] Query database for task/supply counts
- [ ] Display database version
- [ ] Show last backup date (if available from preferences)

### Phase 3: Export Functionality
- [ ] Create `ExportUseCase` for data serialization
- [ ] Implement JSON serialization for all entities
- [ ] Add file picker integration (Android Storage Access Framework)
- [ ] Handle export success/failure states
- [ ] Show success confirmation
- [ ] Add error handling with user-friendly messages

### Phase 4: Backup Functionality
- [ ] Create `CreateBackupUseCase`
- [ ] Implement auto-naming with timestamp
- [ ] Save to Downloads/LifeOps/Backups folder
- [ ] Store last backup timestamp in preferences
- [ ] Show success toast with filename
- [ ] Handle permission requests

### Phase 5: Import Functionality
- [ ] Create `ImportUseCase` for data parsing
- [ ] Implement JSON deserialization
- [ ] Add schema version validation
- [ ] Implement field validation
- [ ] Create relationship validation
- [ ] Build conflict resolution dialog
- [ ] Handle import with user choices (Skip/Replace/Keep Both)
- [ ] Show import summary with statistics

### Phase 6: Polish
- [ ] Add loading states during operations
- [ ] Implement proper error messages
- [ ] Add confirmation dialogs for destructive actions
- [ ] Test with large data sets (200+ tasks)
- [ ] Test edge cases (empty database, corrupted files)
- [ ] Add haptic feedback for success/error

### Phase 7: Future Enhancements (Post-V1)
- [ ] Automatic backup scheduling
- [ ] Backup rotation/cleanup
- [ ] Selective export (tasks only, inventory only)
- [ ] Theme settings (dark mode)
- [ ] Notification preferences (when notifications added)
- [ ] Data statistics dashboard

---

## Technical Notes

### Storage Permissions

**Android 10+ (Scoped Storage)**:
- Export/Import: No permissions needed (uses SAF - Storage Access Framework)
- Backup: No permissions needed (saves to app-specific Downloads folder)

**Android 9 and below**:
- May require WRITE_EXTERNAL_STORAGE permission
- Handle permission request flow

### File Operations

**Export/Backup**:
```kotlin
// Use Storage Access Framework
val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
    addCategory(Intent.CATEGORY_OPENABLE)
    type = "application/json"
    putExtra(Intent.EXTRA_TITLE, "lifeops_export_${timestamp}.json")
}
startActivityForResult(intent, CREATE_FILE_REQUEST)
```

**Import**:
```kotlin
// Use Storage Access Framework
val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
    addCategory(Intent.CATEGORY_OPENABLE)
    type = "application/json"
}
startActivityForResult(intent, PICK_FILE_REQUEST)
```

### JSON Serialization

**Library**: Kotlinx Serialization or Gson

**Considerations**:
- LocalDate serialization (use ISO-8601 format)
- Null handling (omit null fields)
- Pretty printing for readability
- Large file handling (stream processing for 200+ tasks)

### Conflict Resolution

**ID Conflicts**:
- Task with same ID exists → let user choose Skip/Replace/Keep Both
- Keep Both → generate new ID, preserve relationships

**Name Conflicts** (Supplies):
- Supply with same name exists → let user choose
- Replace → update existing supply
- Keep Both → create new supply with "(1)" suffix

**Relationship Conflicts**:
- Missing parent → option to skip or create as standalone
- Missing trigger source → option to skip or remove trigger
- Orphaned children → option to skip or make standalone

---

## User Experience Considerations

### Success Feedback
- Toast messages for quick actions (backup)
- Dialog confirmations for long actions (export/import)
- Progress indicators for large operations
- Clear success summaries with statistics

### Error Handling
- User-friendly error messages (not technical stack traces)
- Actionable suggestions (e.g., "Free up storage space")
- Retry options where applicable
- Support for reporting issues (future)

### Accessibility
- Screen reader support for all elements
- Clear labels for buttons and actions
- High contrast mode support
- Touch target sizes ≥ 48dp

### Performance
- Export/Import should handle 200+ tasks smoothly
- Show progress for operations > 2 seconds
- Non-blocking UI during file operations
- Background processing with coroutines

---

## Future Enhancements (V2+)

### Advanced Import/Export
- Selective export (filter by category, date range)
- Merge import (combine with existing data intelligently)
- CSV export for spreadsheet analysis
- Bulk task creation from CSV

### Backup Management
- Automatic daily/weekly backups
- Backup rotation (keep last N backups)
- Cloud backup integration (optional)
- Backup restoration history

### Additional Settings
- Theme customization (dark mode, colors)
- Notification preferences
- Task completion sound/haptic settings
- Default time estimates and difficulty
- Category color customization
- First day of week preference

### Data Analysis
- Task completion statistics
- Inventory usage trends
- Category distribution charts
- Streak tracking across all tasks

---

**Implementation Priority**: After Task Detail and Task Edit screens  
**Dependencies**: All core entities must be finalized before export/import  
**Estimated Complexity**: Medium (file I/O and validation logic)
