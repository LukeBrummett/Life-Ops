package com.lifeops.presentation.settings

import android.net.Uri

sealed interface SettingsUiEvent {
    data object ExportData : SettingsUiEvent
    data class ExportToUri(val uri: Uri) : SettingsUiEvent
    data object ImportData : SettingsUiEvent
    data object CreateBackup : SettingsUiEvent
    data object ClearError : SettingsUiEvent
    data object ClearSuccess : SettingsUiEvent
}
