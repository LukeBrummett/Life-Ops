package com.lifeops.app.domain.usecase

import com.lifeops.app.data.local.entity.Task
import com.lifeops.app.data.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

/**
 * Use case for getting tasks due on a specific date
 * 
 * Returns a Flow that emits the list of tasks whenever the underlying data changes,
 * enabling reactive UI updates.
 * 
 * @param repository The task repository for database access
 */
class GetTasksDueUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    /**
     * Get all active tasks due on the specified date
     * 
     * @param date The date to check for due tasks
     * @return Flow of tasks that are due on the given date
     */
    operator fun invoke(date: LocalDate): Flow<List<Task>> {
        return repository.observeTasksDueByDate(date)
    }
}
