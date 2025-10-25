package com.lifeops.presentation.settings

sealed interface SettingsUiEvent {
    data object ExportData : SettingsUiEvent
    data object ImportData : SettingsUiEvent
    data object CreateBackup : SettingsUiEvent
    data object ClearError : SettingsUiEvent
    data object ClearSuccess : SettingsUiEvent
}
