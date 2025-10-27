package com.lifeops.presentation.supplyedit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplyEditScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: SupplyEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var tagInputText by remember { mutableStateOf("") }
    
    // Set navigation callbacks
    LaunchedEffect(Unit) {
        viewModel.setNavigationCallbacks(
            onSaveSuccess = onNavigateBack,
            onDeleteSuccess = onNavigateBack
        )
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = if (uiState.isEditing) "Edit Supply" else "Add Supply")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (uiState.isEditing) {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            enabled = !uiState.isSaving
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete supply"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize()
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Basic Information Section
                    Text(
                        text = "Basic Information",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.onEvent(SupplyEditUiEvent.NameChanged(it)) },
                        label = { Text("Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = uiState.validationErrors.nameError != null,
                        supportingText = {
                            uiState.validationErrors.nameError?.let { Text(it) }
                        }
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.category,
                            onValueChange = { viewModel.onEvent(SupplyEditUiEvent.CategoryChanged(it)) },
                            label = { Text("Category") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            placeholder = { Text("Uncategorized") }
                        )
                        
                        OutlinedTextField(
                            value = uiState.unit,
                            onValueChange = { viewModel.onEvent(SupplyEditUiEvent.UnitChanged(it)) },
                            label = { Text("Unit") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            placeholder = { Text("units") }
                        )
                    }
                    
                    Divider()
                    
                    // Reorder Settings Section
                    Text(
                        text = "Reorder Settings",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    OutlinedTextField(
                        value = uiState.reorderThreshold,
                        onValueChange = { viewModel.onEvent(SupplyEditUiEvent.ReorderThresholdChanged(it)) },
                        label = { Text("Reorder Threshold *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = uiState.validationErrors.thresholdError != null,
                        supportingText = {
                            uiState.validationErrors.thresholdError?.let { Text(it) }
                                ?: Text("Alert when quantity falls below this level")
                        }
                    )
                    
                    OutlinedTextField(
                        value = uiState.reorderTargetQuantity,
                        onValueChange = { viewModel.onEvent(SupplyEditUiEvent.ReorderTargetQuantityChanged(it)) },
                        label = { Text("Target Quantity *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = uiState.validationErrors.targetQuantityError != null,
                        supportingText = {
                            uiState.validationErrors.targetQuantityError?.let { Text(it) }
                                ?: Text("Ideal quantity to have in stock")
                        }
                    )
                    
                    if (!uiState.isEditing) {
                        OutlinedTextField(
                            value = uiState.initialQuantity,
                            onValueChange = { viewModel.onEvent(SupplyEditUiEvent.InitialQuantityChanged(it)) },
                            label = { Text("Initial Quantity *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = uiState.validationErrors.initialQuantityError != null,
                            supportingText = {
                                uiState.validationErrors.initialQuantityError?.let { Text(it) }
                                    ?: Text("Starting quantity in inventory")
                            }
                        )
                    }
                    
                    Divider()
                    
                    // Additional Details Section
                    Text(
                        text = "Additional Details",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // Tags Section with InputChips
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = tagInputText,
                            onValueChange = { tagInputText = it },
                            label = { Text("Add Tags") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("grocery, pantry, etc.") },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (tagInputText.isNotBlank()) {
                                            viewModel.onEvent(SupplyEditUiEvent.AddTag(tagInputText.trim()))
                                            tagInputText = ""
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Add, "Add tag")
                                }
                            }
                        )
                        
                        // Tag chips display
                        if (uiState.tags.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                uiState.tags.forEach { tag ->
                                    InputChip(
                                        selected = false,
                                        onClick = { viewModel.onEvent(SupplyEditUiEvent.RemoveTag(tag)) },
                                        label = { Text(tag) },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove $tag",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = { viewModel.onEvent(SupplyEditUiEvent.NotesChanged(it)) },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        placeholder = { Text("Additional notes...") }
                    )
                    
                    // Save Button
                    Button(
                        onClick = { viewModel.onEvent(SupplyEditUiEvent.SaveClicked) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        enabled = !uiState.isSaving && uiState.isValid
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(text = if (uiState.isEditing) "Save Changes" else "Add Supply")
                    }
                    
                    // Bottom spacing
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            // Error Snackbar
            uiState.errorMessage?.let { message ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.onEvent(SupplyEditUiEvent.ErrorDismissed) }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(message)
                }
            }
        }
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Supply?") },
            text = { Text("Are you sure you want to delete this supply? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.onEvent(SupplyEditUiEvent.DeleteConfirmed)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
