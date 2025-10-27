# Fix: Can't Assign Multiple Triggered Tasks

## Issue Description

Users were unable to properly assign multiple triggered tasks to a single task. When attempting to add a second triggered task, it appeared to not save correctly, though the data was actually being persisted to the database.

## Root Cause

The issue was caused by **missing bidirectional relationship updates** in the `SaveTaskUseCase`.

When a task was saved with trigger relationships:
- Task A's `triggersTaskIds` field was correctly saved with `[T1, T2]`
- However, T1's `triggeredByTaskIds` and T2's `triggeredByTaskIds` were NOT updated to include Task A

This meant that when the UI queried for tasks triggered by Task A using `getTasksTriggeredBy()`, it relied on finding tasks where `triggeredByTaskIds` contained Task A's ID. Since this field wasn't being updated, the query returned incomplete results.

## The Fix

Added a new `updateTriggerRelationships()` function in `SaveTaskUseCase` that ensures bidirectional synchronization of trigger relationships:

### What the function does:

1. **Updates "triggered by" relationships:**
   - Removes this task from tasks that no longer trigger it
   - Adds this task to new triggering tasks

2. **Updates "triggers" relationships:**
   - Removes this task from tasks it no longer triggers
   - Adds this task to newly triggered tasks

### Example Flow:

**Before fix:**
```
Task A is saved with triggersTaskIds = [T1, T2]
Result:
- Task A.triggersTaskIds = [T1, T2] ✓
- Task T1.triggeredByTaskIds = [] ✗
- Task T2.triggeredByTaskIds = [] ✗
```

**After fix:**
```
Task A is saved with triggersTaskIds = [T1, T2]
updateTriggerRelationships() is called
Result:
- Task A.triggersTaskIds = [T1, T2] ✓
- Task T1.triggeredByTaskIds = [A] ✓
- Task T2.triggeredByTaskIds = [A] ✓
```

## Files Changed

- `app/src/main/java/com/lifeops/app/domain/usecase/task/SaveTaskUseCase.kt`
  - Added `updateTriggerRelationships()` method
  - Called `updateTriggerRelationships()` in the save flow after updating child relationships

## Testing

The fix should be tested with the following scenarios:

1. **Add single triggered task:**
   - Edit Task A
   - Add Task T1 to triggers list
   - Save
   - Verify Task A shows T1 in triggered tasks
   - Verify Task T1 shows Task A in "triggered by" tasks

2. **Add multiple triggered tasks:**
   - Edit Task A (already has T1)
   - Add Task T2 to triggers list
   - Save
   - Verify Task A shows both T1 and T2 in triggered tasks
   - Verify Task T2 shows Task A in "triggered by" tasks

3. **Remove triggered task:**
   - Edit Task A (has T1 and T2)
   - Remove T1 from triggers list
   - Save
   - Verify Task A shows only T2 in triggered tasks
   - Verify Task T1 no longer shows Task A in "triggered by" tasks

4. **Bidirectional navigation:**
   - Navigate from Task A to triggered task T1
   - Verify T1's detail screen shows Task A in "triggered by" section
   - Navigate from T1 back to Task A
   - Verify data remains consistent

## Additional Notes

The fix mirrors the existing `updateChildRelationships()` function, which correctly handles bidirectional parent-child relationships. This ensures consistency across the codebase.
