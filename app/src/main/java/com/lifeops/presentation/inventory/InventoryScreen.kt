package com.lifeops.presentation.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeops.presentation.inventory.components.SearchAndFilterBar
import com.lifeops.presentation.inventory.components.SupplyList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToSupplyEdit: (String?) -> Unit = {}, // null = create new
    onNavigateToShopping: () -> Unit = {},
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.onEvent(InventoryUiEvent.ClearError)
        }
    }
    
    // Show success messages
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onEvent(InventoryUiEvent.ClearSuccess)
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Inventory") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Shopping button
                    IconButton(onClick = { viewModel.onEvent(InventoryUiEvent.NavigateToShopping) }) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = "Go Shopping"
                        )
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
            // Search and filter bar
            SearchAndFilterBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChanged = { viewModel.onEvent(InventoryUiEvent.SearchQueryChanged(it)) },
                sortOption = uiState.sortOption,
                onSortOptionSelected = { viewModel.onEvent(InventoryUiEvent.SortOptionSelected(it)) },
                filterOptions = uiState.filterOptions,
                onFilterOptionsChanged = { viewModel.onEvent(InventoryUiEvent.FilterOptionsChanged(it)) }
            )
            
            // Supply list
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize()
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.supplies.isEmpty()) {
                EmptyState()
            } else {
                SupplyList(
                    supplies = uiState.supplies,
                    expandedCategories = uiState.expandedCategories,
                    onCategoryExpandToggle = { viewModel.onEvent(InventoryUiEvent.CategoryExpandToggle(it)) },
                    onIncrementQuantity = { viewModel.onEvent(InventoryUiEvent.IncrementQuantity(it)) },
                    onDecrementQuantity = { viewModel.onEvent(InventoryUiEvent.DecrementQuantity(it)) },
                    onSupplyClick = { onNavigateToSupplyEdit(it) }
                )
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(
            text = "📦",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Inventory Items",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap ➕ to start tracking supplies",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
