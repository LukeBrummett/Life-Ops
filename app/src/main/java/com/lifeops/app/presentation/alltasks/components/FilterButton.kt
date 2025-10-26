package com.lifeops.app.presentation.alltasks.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeops.app.presentation.alltasks.FilterState
import com.lifeops.app.presentation.alltasks.StatusFilter

/**
 * Button that opens a bottom sheet to configure filters
 * Shows badge with active filter count
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterButton(
    filterState: FilterState,
    onFilterChange: (FilterState) -> Unit,
    modifier: Modifier = Modifier
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    
    val activeCount = getActiveFilterCount(filterState)
    
    BadgedBox(
        badge = {
            if (activeCount > 0) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(text = activeCount.toString())
                }
            }
        },
        modifier = modifier
    ) {
        OutlinedButton(
            onClick = { showBottomSheet = true },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Filter")
        }
    }
    
    if (showBottomSheet) {
        FilterBottomSheet(
            filterState = filterState,
            onFilterChange = onFilterChange,
            onDismiss = { showBottomSheet = false }
        )
    }
}

/**
 * Count active filters (excluding default ACTIVE status)
 */
private fun getActiveFilterCount(filter: FilterState): Int {
    var count = 0
    if (filter.statusFilter != StatusFilter.ACTIVE) count++
    if (filter.hasInventory) count++
    if (filter.isParent) count++
    if (filter.isChild) count++
    if (filter.isTriggered) count++
    if (filter.adhocOnly) count++
    return count
}

/**
 * Bottom sheet for configuring filter options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    filterState: FilterState,
    onFilterChange: (FilterState) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var currentFilters by remember { mutableStateOf(filterState) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header with Clear All button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter",
                    style = MaterialTheme.typography.titleLarge
                )
                
                if (getActiveFilterCount(currentFilters) > 0) {
                    TextButton(
                        onClick = {
                            currentFilters = FilterState()
                            onFilterChange(currentFilters)
                        }
                    ) {
                        Text("Clear All")
                    }
                }
            }
            
            Divider()
            
            // Filter options
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Status filters
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = currentFilters.statusFilter == StatusFilter.ACTIVE,
                        onClick = {
                            currentFilters = currentFilters.copy(statusFilter = StatusFilter.ACTIVE)
                            onFilterChange(currentFilters)
                        },
                        label = { Text("Active") }
                    )
                    
                    FilterChip(
                        selected = currentFilters.statusFilter == StatusFilter.ALL,
                        onClick = {
                            currentFilters = currentFilters.copy(statusFilter = StatusFilter.ALL)
                            onFilterChange(currentFilters)
                        },
                        label = { Text("All") }
                    )
                    
                    FilterChip(
                        selected = currentFilters.statusFilter == StatusFilter.ARCHIVED,
                        onClick = {
                            currentFilters = currentFilters.copy(statusFilter = StatusFilter.ARCHIVED)
                            onFilterChange(currentFilters)
                        },
                        label = { Text("Archived") }
                    )
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Attribute filters
                Text(
                    text = "Attributes",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                FilterToggleItem(
                    label = "Has Inventory",
                    description = "Show only tasks that consume inventory",
                    checked = currentFilters.hasInventory,
                    onCheckedChange = { checked ->
                        currentFilters = currentFilters.copy(hasInventory = checked)
                        onFilterChange(currentFilters)
                    }
                )
                
                FilterToggleItem(
                    label = "Is Parent",
                    description = "Show only tasks with child tasks",
                    checked = currentFilters.isParent,
                    onCheckedChange = { checked ->
                        currentFilters = currentFilters.copy(isParent = checked)
                        onFilterChange(currentFilters)
                    }
                )
                
                FilterToggleItem(
                    label = "Is Child",
                    description = "Show only tasks with a parent task",
                    checked = currentFilters.isChild,
                    onCheckedChange = { checked ->
                        currentFilters = currentFilters.copy(isChild = checked)
                        onFilterChange(currentFilters)
                    }
                )
                
                FilterToggleItem(
                    label = "Is Triggered",
                    description = "Show only tasks spawned by other tasks",
                    checked = currentFilters.isTriggered,
                    onCheckedChange = { checked ->
                        currentFilters = currentFilters.copy(isTriggered = checked)
                        onFilterChange(currentFilters)
                    }
                )
                
                FilterToggleItem(
                    label = "Adhoc Only",
                    description = "Show only tasks with no automatic schedule",
                    checked = currentFilters.adhocOnly,
                    onCheckedChange = { checked ->
                        currentFilters = currentFilters.copy(adhocOnly = checked)
                        onFilterChange(currentFilters)
                    }
                )
            }
            
            // Apply button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text("Apply")
            }
        }
    }
}

/**
 * Single filter toggle item with label and description
 */
@Composable
private fun FilterToggleItem(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
