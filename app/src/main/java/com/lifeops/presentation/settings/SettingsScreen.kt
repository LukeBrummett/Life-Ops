package com.lifeops.presentation.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // File picker for export
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            viewModel.onEvent(SettingsUiEvent.ExportToUri(it))
        }
    }
    
    // File picker for import
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.onEvent(SettingsUiEvent.ImportFromUri(it))
        }
    }
    
    // Trigger export file picker when state changes
    LaunchedEffect(uiState.showExportFilePicker) {
        if (uiState.showExportFilePicker) {
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"))
            exportLauncher.launch("lifeops_export_$timestamp.json")
        }
    }
    
    // Trigger import file picker when state changes
    LaunchedEffect(uiState.showImportFilePicker) {
        if (uiState.showImportFilePicker) {
            importLauncher.launch(arrayOf("application/json"))
        }
    }
    
    // Show snackbar for success/error messages
    val snackbarHostState = androidx.compose.material3.SnackbarHostState()
    
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(SettingsUiEvent.ClearSuccess)
        }
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(SettingsUiEvent.ClearError)
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        // Show conflict resolution dialog if needed
        uiState.importConflicts?.let { conflicts ->
            ImportConflictDialog(
                conflicts = conflicts,
                onResolve = { resolutions ->
                    val tasks = conflicts.map { it.importedTask }
                    viewModel.onEvent(SettingsUiEvent.ResolveConflictsAndImport(tasks, resolutions))
                },
                onDismiss = {
                    viewModel.onEvent(SettingsUiEvent.DismissConflictDialog)
                }
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Data Management Section
            SectionHeader(title = "DATA MANAGEMENT")
            
            SettingsCard(
                icon = "📤",
                title = "Export All Data",
                description = "Export entire database to JSON file",
                buttonText = "Export",
                onButtonClick = { viewModel.onEvent(SettingsUiEvent.ExportData) }
            )
            
            SettingsCard(
                icon = "📥",
                title = "Import Data",
                description = "Import tasks and inventory from JSON file",
                buttonText = "Import",
                onButtonClick = { viewModel.onEvent(SettingsUiEvent.ImportData) }
            )
            
            SettingsCard(
                icon = "💾",
                title = "Create Backup",
                description = "Save current state as backup file",
                buttonText = "Backup",
                onButtonClick = { viewModel.onEvent(SettingsUiEvent.CreateBackup) }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Developer Section
            SectionHeader(title = "DEVELOPER")
            
            DebugModeToggle(
                enabled = uiState.debugMode,
                onToggle = { viewModel.onEvent(SettingsUiEvent.ToggleDebugMode(it)) }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LoadSampleDataCard(
                onLoadSampleData = { viewModel.onEvent(SettingsUiEvent.LoadSampleData) }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // About Section
            SectionHeader(title = "ABOUT")
            
            AboutCard(uiState = uiState)
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun SettingsCard(
    icon: String,
    title: String,
    description: String,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = onButtonClick) {
                    Text(buttonText)
                }
            }
        }
    }
}

@Composable
fun AboutCard(uiState: SettingsUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AboutInfoRow(label = "Version", value = uiState.appVersion)
            AboutInfoRow(label = "Database Version", value = uiState.databaseVersion.toString())
            AboutInfoRow(label = "Total Tasks", value = uiState.totalTasks.toString())
            AboutInfoRow(label = "Total Inventory Items", value = uiState.totalSupplies.toString())
            
            if (uiState.lastManualBackup != null) {
                AboutInfoRow(label = "Last Manual Backup", value = uiState.lastManualBackup)
            }
            
            if (uiState.lastAutoBackup != null) {
                AboutInfoRow(label = "Last Auto Backup", value = uiState.lastAutoBackup)
            }
        }
    }
}

@Composable
fun AboutInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ImportConflictDialog(
    conflicts: List<com.lifeops.presentation.settings.import_data.ImportConflict>,
    onResolve: (Map<String, com.lifeops.presentation.settings.import_data.ConflictResolution>) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Conflicts Detected") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Found ${conflicts.size} task(s) with duplicate IDs.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Do you want to override the existing tasks or import them separately?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Override existing tasks
                    val resolutions = conflicts.associate { 
                        it.taskId to com.lifeops.presentation.settings.import_data.ConflictResolution.REPLACE
                    }
                    onResolve(resolutions)
                }
            ) {
                Text("Override")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                TextButton(
                    onClick = {
                        // Keep both - import with new IDs
                        val resolutions = conflicts.associate { 
                            it.taskId to com.lifeops.presentation.settings.import_data.ConflictResolution.KEEP_BOTH
                        }
                        onResolve(resolutions)
                    }
                ) {
                    Text("Import Separately")
                }
            }
        }
    )
}

@Composable
fun DebugModeToggle(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🐛",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Debug Mode",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    text = "Show debug controls for time travel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Switch(
                checked = enabled,
                onCheckedChange = onToggle
            )
        }
    }
}

@Composable
fun LoadSampleDataCard(
    onLoadSampleData: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📦",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Sample Data",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    text = "Load example tasks and supplies to explore features",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Button(
                onClick = onLoadSampleData,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Load")
            }
        }
    }
}
