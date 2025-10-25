package com.lifeops.presentation.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeops.app.data.repository.TaskRepository
import com.lifeops.presentation.settings.export.ExportDataUseCase
import com.lifeops.presentation.settings.export.ExportResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val exportDataUseCase: ExportDataUseCase
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
            SettingsUiEvent.ExportData -> {
                _uiState.update { it.copy(showExportFilePicker = true) }
            }
            is SettingsUiEvent.ExportToUri -> {
                _uiState.update { it.copy(showExportFilePicker = false) }
                exportToUri(event.uri)
            }
            SettingsUiEvent.ImportData -> importData()
            SettingsUiEvent.CreateBackup -> createBackup()
            SettingsUiEvent.ClearError -> _uiState.update { it.copy(error = null) }
            SettingsUiEvent.ClearSuccess -> _uiState.update { it.copy(successMessage = null) }
        }
    }

    private fun exportToUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = exportDataUseCase.execute(uri)) {
                is ExportResult.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            successMessage = "Exported to ${result.fileName}"
                        )
                    }
                }
                is ExportResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    private fun exportData() {
        // Trigger file picker in UI
        _uiState.update { it.copy(showExportFilePicker = true) }
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
