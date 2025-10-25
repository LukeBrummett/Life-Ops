package com.lifeops.presentation.settings

import com.lifeops.presentation.settings.import_data.ImportConflict

data class SettingsUiState(
    val appVersion: String = "1.0.0",
    val databaseVersion: Int = 1,
    val totalTasks: Int = 0,
    val totalSupplies: Int = 0,
    val lastManualBackup: String? = null,
    val lastAutoBackup: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showExportFilePicker: Boolean = false,
    val showImportFilePicker: Boolean = false,
    val importConflicts: List<ImportConflict>? = null
)
