package com.lifeops.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeops.app.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            taskRepository.observeAllOrderedByNextDue()
                .collect { tasks ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            totalTasks = tasks.size
                        )
                    }
                }
        }
    }

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            SettingsUiEvent.ExportData -> exportData()
            SettingsUiEvent.ImportData -> importData()
            SettingsUiEvent.CreateBackup -> createBackup()
            SettingsUiEvent.ClearError -> _uiState.update { it.copy(error = null) }
            SettingsUiEvent.ClearSuccess -> _uiState.update { it.copy(successMessage = null) }
        }
    }

    private fun exportData() {
        // TODO: Implement in Phase 3
        _uiState.update { it.copy(successMessage = "Export functionality coming in Phase 3") }
    }

    private fun importData() {
        // TODO: Implement in Phase 4
        _uiState.update { it.copy(successMessage = "Import functionality coming in Phase 4") }
    }

    private fun createBackup() {
        // TODO: Implement in Phase 5
        _uiState.update { it.copy(successMessage = "Backup functionality coming in Phase 5") }
    }
}
