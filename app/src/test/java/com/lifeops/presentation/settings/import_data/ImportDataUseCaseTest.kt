package com.lifeops.presentation.settings.import_data

import android.content.Context
import android.net.Uri
import com.google.common.truth.Truth.assertThat
import com.lifeops.app.data.local.entity.Task
import com.lifeops.app.data.repository.TaskRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Unit tests for ImportDataUseCase
 * Tests the import functionality, especially handling conflicts with all tasks
 */
class ImportDataUseCaseTest {

    private lateinit var mockContext: Context
    private lateinit var mockTaskRepository: TaskRepository
    private lateinit var importDataUseCase: ImportDataUseCase

    @Before
    fun setup() {
        mockContext = mock()
        mockTaskRepository = mock()
        importDataUseCase = ImportDataUseCase(mockContext, mockTaskRepository)
    }

    @Test
    fun `executeImport with conflicts should import all tasks not just conflicting ones`() = runTest {
        // Given - 3 tasks to import, 1 has a conflict
        val existingTask = Task(
            id = "task-1",
            name = "Existing Task",
            category = "Work"
        )

        val task1 = Task(
            id = "task-1",
            name = "Imported Task 1 (conflicts)",
            category = "Personal"
        )

        val task2 = Task(
            id = "task-2",
            name = "Imported Task 2 (new)",
            category = "Health"
        )

        val task3 = Task(
            id = "task-3",
            name = "Imported Task 3 (new)",
            category = "Household"
        )

        val allTasksToImport = listOf(task1, task2, task3)

        // Mock repository responses
        whenever(mockTaskRepository.getTaskById("task-1")).thenReturn(Result.success(existingTask))
        whenever(mockTaskRepository.getTaskById("task-2")).thenReturn(Result.failure(Exception("Not found")))
        whenever(mockTaskRepository.getTaskById("task-3")).thenReturn(Result.failure(Exception("Not found")))
        whenever(mockTaskRepository.createTask(any())).thenReturn(Result.success("generated-id"))
        whenever(mockTaskRepository.updateTask(any())).thenReturn(Result.success(Unit))

        // When - user chooses to SKIP the conflicting task
        val resolutions = mapOf("task-1" to ConflictResolution.SKIP)
        val result = importDataUseCase.executeImport(allTasksToImport, resolutions)

        // Then - should import 2 new tasks and skip the conflicting one
        assertThat(result).isInstanceOf(ImportResult.Success::class.java)
        val successResult = result as ImportResult.Success
        assertThat(successResult.tasksImported).isEqualTo(2) // task-2 and task-3
        assertThat(successResult.tasksSkipped).isEqualTo(1) // task-1
        assertThat(successResult.tasksReplaced).isEqualTo(0)
    }

    @Test
    fun `executeImport with REPLACE resolution should import all tasks`() = runTest {
        // Given - 3 tasks to import, 1 has a conflict
        val existingTask = Task(
            id = "task-1",
            name = "Existing Task",
            category = "Work"
        )

        val task1 = Task(
            id = "task-1",
            name = "Imported Task 1 (conflicts)",
            category = "Personal"
        )

        val task2 = Task(
            id = "task-2",
            name = "Imported Task 2 (new)",
            category = "Health"
        )

        val task3 = Task(
            id = "task-3",
            name = "Imported Task 3 (new)",
            category = "Household"
        )

        val allTasksToImport = listOf(task1, task2, task3)

        // Mock repository responses
        whenever(mockTaskRepository.getTaskById("task-1")).thenReturn(Result.success(existingTask))
        whenever(mockTaskRepository.getTaskById("task-2")).thenReturn(Result.failure(Exception("Not found")))
        whenever(mockTaskRepository.getTaskById("task-3")).thenReturn(Result.failure(Exception("Not found")))
        whenever(mockTaskRepository.createTask(any())).thenReturn(Result.success("generated-id"))
        whenever(mockTaskRepository.updateTask(any())).thenReturn(Result.success(Unit))

        // When - user chooses to REPLACE the conflicting task
        val resolutions = mapOf("task-1" to ConflictResolution.REPLACE)
        val result = importDataUseCase.executeImport(allTasksToImport, resolutions)

        // Then - should import 2 new tasks and replace the conflicting one
        assertThat(result).isInstanceOf(ImportResult.Success::class.java)
        val successResult = result as ImportResult.Success
        assertThat(successResult.tasksImported).isEqualTo(2) // task-2 and task-3
        assertThat(successResult.tasksSkipped).isEqualTo(0)
        assertThat(successResult.tasksReplaced).isEqualTo(1) // task-1
    }

    @Test
    fun `executeImport with KEEP_BOTH resolution should import all tasks`() = runTest {
        // Given - 3 tasks to import, 1 has a conflict
        val existingTask = Task(
            id = "task-1",
            name = "Existing Task",
            category = "Work"
        )

        val task1 = Task(
            id = "task-1",
            name = "Imported Task 1 (conflicts)",
            category = "Personal"
        )

        val task2 = Task(
            id = "task-2",
            name = "Imported Task 2 (new)",
            category = "Health"
        )

        val task3 = Task(
            id = "task-3",
            name = "Imported Task 3 (new)",
            category = "Household"
        )

        val allTasksToImport = listOf(task1, task2, task3)

        // Mock repository responses
        whenever(mockTaskRepository.getTaskById("task-1")).thenReturn(Result.success(existingTask))
        whenever(mockTaskRepository.getTaskById("task-2")).thenReturn(Result.failure(Exception("Not found")))
        whenever(mockTaskRepository.getTaskById("task-3")).thenReturn(Result.failure(Exception("Not found")))
        whenever(mockTaskRepository.createTask(any())).thenReturn(Result.success("generated-id"))

        // When - user chooses to KEEP_BOTH (import with new ID)
        val resolutions = mapOf("task-1" to ConflictResolution.KEEP_BOTH)
        val result = importDataUseCase.executeImport(allTasksToImport, resolutions)

        // Then - should import all 3 tasks (task-1 gets a new ID)
        assertThat(result).isInstanceOf(ImportResult.Success::class.java)
        val successResult = result as ImportResult.Success
        assertThat(successResult.tasksImported).isEqualTo(3) // all tasks imported
        assertThat(successResult.tasksSkipped).isEqualTo(0)
        assertThat(successResult.tasksReplaced).isEqualTo(0)
    }

    @Test
    fun `executeImport with no conflicts should import all tasks`() = runTest {
        // Given - 3 tasks to import, no conflicts
        val task1 = Task(
            id = "task-1",
            name = "Imported Task 1",
            category = "Personal"
        )

        val task2 = Task(
            id = "task-2",
            name = "Imported Task 2",
            category = "Health"
        )

        val task3 = Task(
            id = "task-3",
            name = "Imported Task 3",
            category = "Household"
        )

        val allTasksToImport = listOf(task1, task2, task3)

        // Mock repository responses - no existing tasks
        whenever(mockTaskRepository.getTaskById(any())).thenReturn(Result.failure(Exception("Not found")))
        whenever(mockTaskRepository.createTask(any())).thenReturn(Result.success("generated-id"))

        // When - importing with no resolutions needed (no conflicts)
        val result = importDataUseCase.executeImport(allTasksToImport, emptyMap())

        // Then - should import all 3 tasks
        assertThat(result).isInstanceOf(ImportResult.Success::class.java)
        val successResult = result as ImportResult.Success
        assertThat(successResult.tasksImported).isEqualTo(3) // all tasks imported
        assertThat(successResult.tasksSkipped).isEqualTo(0)
        assertThat(successResult.tasksReplaced).isEqualTo(0)
    }
}
