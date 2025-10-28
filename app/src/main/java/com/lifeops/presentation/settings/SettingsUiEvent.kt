package com.lifeops.presentation.settings

import android.net.Uri
import com.lifeops.app.data.local.entity.Task
import com.lifeops.presentation.settings.import_data.ConflictResolution

sealed interface SettingsUiEvent {
    data object ExportData : SettingsUiEvent
    data class ExportToUri(val uri: Uri) : SettingsUiEvent
    data object ImportData : SettingsUiEvent
    data class ImportFromUri(val uri: Uri) : SettingsUiEvent
    data class ResolveConflictsAndImport(
        val tasks: List<Task>,
        val resolutions: Map<String, ConflictResolution>
    ) : SettingsUiEvent
    data object DismissConflictDialog : SettingsUiEvent
    data object CreateBackup : SettingsUiEvent
    data class ToggleDebugMode(val enabled: Boolean) : SettingsUiEvent
    data object LoadSampleData : SettingsUiEvent
    data object AboutSectionTapped : SettingsUiEvent
    data object ClearError : SettingsUiEvent
    data object ClearSuccess : SettingsUiEvent
}
