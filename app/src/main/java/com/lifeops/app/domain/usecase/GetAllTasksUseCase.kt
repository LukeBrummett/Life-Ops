package com.lifeops.app.domain.usecase

import com.lifeops.app.data.local.entity.Task
import com.lifeops.app.data.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting all tasks ordered by next due date
 * 
 * Returns a Flow that emits the list of all tasks whenever the underlying data changes,
 * enabling reactive UI updates. Tasks are ordered by their next due date, with tasks
 * that have no due date appearing last.
 * 
 * @param repository The task repository for database access
 */
class GetAllTasksUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    /**
     * Get all tasks ordered by next due date
     * 
     * @return Flow of all tasks ordered by nextDue date (nulls last)
     */
    operator fun invoke(): Flow<List<Task>> {
        return repository.observeAllOrderedByNextDue()
    }
}
