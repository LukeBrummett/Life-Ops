package com.lifeops.presentation.settings

data class SettingsUiState(
    val appVersion: String = "1.0.0",
    val databaseVersion: Int = 1,
    val totalTasks: Int = 0,
    val totalSupplies: Int = 0,
    val lastManualBackup: String? = null,
    val lastAutoBackup: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
