package com.lifeops.presentation.inventory.restock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeops.presentation.inventory.restock.components.RestockItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestockScreen(
    supplyIds: List<String>,
    onNavigateBack: () -> Unit = {},
    onNavigateToSupplyEdit: (String?) -> Unit = {},
    viewModel: RestockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Initialize restock items
    LaunchedEffect(supplyIds) {
        viewModel.initializeRestock(supplyIds)
    }
    
    // Handle success (completion or cancellation)
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            if (message == "cancelled") {
                // Just navigate back without showing message
                onNavigateBack()
            } else {
                // Show success message then navigate
                onNavigateBack()
            }
        }
    }
    
    // Snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.onEvent(RestockUiEvent.ClearError)
        }
    }
    
    // Confirmation dialog state
    var showCancelDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Restock Inventory") },
                navigationIcon = {
                    IconButton(onClick = { showCancelDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cancel"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.onEvent(RestockUiEvent.CompleteRestock) },
                        enabled = !uiState.isSaving && uiState.allItemsDone
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Complete")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToSupplyEdit(null) }
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Supply"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header info
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Update quantities for purchased items",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    if (uiState.items.isNotEmpty()) {
                        Text(
                            text = "${uiState.doneCount} of ${uiState.items.size} items marked Done",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            
            // Content
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize()
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.items.isEmpty()) {
                EmptyRestockState()
            } else {
                RestockItemList(
                    uiState = uiState,
                    onEvent = viewModel::onEvent,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
    
    // Cancel confirmation dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Discard restock progress?") },
            text = { Text("Your restock progress will not be saved.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        viewModel.onEvent(RestockUiEvent.CancelRestock)
                    }
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Continue Restocking")
                }
            }
        )
    }
}

@Composable
fun RestockItemList(
    uiState: RestockUiState,
    onEvent: (RestockUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        uiState.itemsByCategory.forEach { (category, items) ->
            item(key = "header_$category") {
                CategoryHeader(
                    category = category,
                    itemCount = items.size,
                    doneCount = items.count { it.isDone },
                    isExpanded = category in uiState.expandedCategories,
                    onToggleExpand = { onEvent(RestockUiEvent.CategoryExpandToggle(category)) }
                )
            }
            
            if (category in uiState.expandedCategories) {
                items(
                    items = items,
                    key = { it.supply.id }
                ) { item ->
                    RestockItemCard(
                        item = item,
                        onToggleDone = { onEvent(RestockUiEvent.ToggleDone(item.supply.id)) },
                        onIncrement = { onEvent(RestockUiEvent.IncrementQuantity(item.supply.id)) },
                        onDecrement = { onEvent(RestockUiEvent.DecrementQuantity(item.supply.id)) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(
    category: String,
    itemCount: Int,
    doneCount: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onToggleExpand,
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.uppercase(),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "$doneCount/$itemCount done",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (isExpanded) "▼" else "▶",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun EmptyRestockState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "✅",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Items to Restock",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add items using the ➕ button",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
