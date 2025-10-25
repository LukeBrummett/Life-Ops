package com.lifeops.presentation.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeops.app.data.local.entity.Task
import com.lifeops.app.data.repository.TaskRepository
import com.lifeops.app.domain.usecase.CreateBackupUseCase
import com.lifeops.presentation.settings.export.ExportDataUseCase
import com.lifeops.presentation.settings.export.ExportResult
import com.lifeops.presentation.settings.import_data.ConflictResolution
import com.lifeops.presentation.settings.import_data.ImportDataUseCase
import com.lifeops.presentation.settings.import_data.ImportResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val exportDataUseCase: ExportDataUseCase,
    private val importDataUseCase: ImportDataUseCase,
    private val createBackupUseCase: CreateBackupUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
        loadBackupTimestamps()
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

    private fun loadBackupTimestamps() {
        viewModelScope.launch {
            val lastManual = createBackupUseCase.getLastManualBackupTime()
            val lastAuto = createBackupUseCase.getLastAutoBackupTime()
            
            _uiState.update { currentState ->
                currentState.copy(
                    lastManualBackup = lastManual?.let { formatBackupTime(it) },
                    lastAutoBackup = lastAuto?.let { formatBackupTime(it) }
                )
            }
        }
    }

    private fun formatBackupTime(dateTime: java.time.LocalDateTime): String {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' h:mm a")
        return dateTime.format(formatter)
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
            SettingsUiEvent.ImportData -> {
                _uiState.update { it.copy(showImportFilePicker = true) }
            }
            is SettingsUiEvent.ImportFromUri -> {
                _uiState.update { it.copy(showImportFilePicker = false) }
                importFromUri(event.uri)
            }
            is SettingsUiEvent.ResolveConflictsAndImport -> {
                resolveConflictsAndImport(event.tasks, event.resolutions)
            }
            SettingsUiEvent.DismissConflictDialog -> {
                _uiState.update { it.copy(importConflicts = null) }
            }
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

    private fun importFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = importDataUseCase.parseAndValidate(uri)) {
                is ImportResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Imported ${result.tasksImported} tasks" +
                                (if (result.tasksSkipped > 0) ", skipped ${result.tasksSkipped}" else "") +
                                (if (result.tasksReplaced > 0) ", replaced ${result.tasksReplaced}" else "")
                        )
                    }
                }
                is ImportResult.NeedsResolution -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            importConflicts = result.conflicts
                        )
                    }
                }
                is ImportResult.Error -> {
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

    private fun resolveConflictsAndImport(
        tasks: List<Task>,
        resolutions: Map<String, ConflictResolution>
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, importConflicts = null) }
            
            when (val result = importDataUseCase.executeImport(tasks, resolutions)) {
                is ImportResult.Success -> {
                    // Create automatic backup after successful import
                    createBackupUseCase.execute(isAutomatic = true)
                    loadBackupTimestamps()
                    
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Imported ${result.tasksImported} tasks" +
                                (if (result.tasksSkipped > 0) ", skipped ${result.tasksSkipped}" else "") +
                                (if (result.tasksReplaced > 0) ", replaced ${result.tasksReplaced}" else "")
                        )
                    }
                }
                is ImportResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
                is ImportResult.NeedsResolution -> {
                    // This shouldn't happen after resolution
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Unexpected conflict resolution error"
                        )
                    }
                }
            }
        }
    }

    private fun importData() {
        // Trigger file picker in UI
        _uiState.update { it.copy(showImportFilePicker = true) }
    }

    private fun createBackup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val result = createBackupUseCase.execute(isAutomatic = false)
            
            if (result.isSuccess) {
                // Reload backup timestamps to show the new backup
                loadBackupTimestamps()
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        successMessage = "Backup created: ${result.getOrNull()}"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to create backup: ${result.exceptionOrNull()?.message}"
                    )
                }
            }
        }
    }
}
