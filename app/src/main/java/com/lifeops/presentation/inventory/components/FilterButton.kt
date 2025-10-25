package com.lifeops.presentation.inventory.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeops.presentation.inventory.FilterOptions

/**
 * Button that opens a bottom sheet to configure filters
 * Shows badge with active filter count
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterButton(
    filterOptions: FilterOptions,
    onFilterOptionsChanged: (FilterOptions) -> Unit,
    modifier: Modifier = Modifier
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    
    BadgedBox(
        badge = {
            if (filterOptions.activeCount > 0) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(text = filterOptions.activeCount.toString())
                }
            }
        }
    ) {
        OutlinedButton(
            onClick = { showBottomSheet = true },
            modifier = modifier,
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
            filterOptions = filterOptions,
            onFilterOptionsChanged = onFilterOptionsChanged,
            onDismiss = { showBottomSheet = false }
        )
    }
}

/**
 * Bottom sheet for configuring filter options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    filterOptions: FilterOptions,
    onFilterOptionsChanged: (FilterOptions) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var currentFilters by remember { mutableStateOf(filterOptions) }
    
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
                
                if (currentFilters.isActive) {
                    TextButton(
                        onClick = {
                            currentFilters = FilterOptions()
                            onFilterOptionsChanged(currentFilters)
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
                
                FilterToggleItem(
                    label = "Needs Reorder",
                    description = "Show items below reorder threshold",
                    checked = currentFilters.needsReorder,
                    onCheckedChange = { checked ->
                        currentFilters = currentFilters.copy(needsReorder = checked)
                        onFilterOptionsChanged(currentFilters)
                    }
                )
                
                FilterToggleItem(
                    label = "Well Stocked",
                    description = "Show items at or above target quantity",
                    checked = currentFilters.wellStocked,
                    onCheckedChange = { checked ->
                        currentFilters = currentFilters.copy(wellStocked = checked)
                        onFilterOptionsChanged(currentFilters)
                    }
                )
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Category filters (placeholder for Phase 4 when we have categories from DB)
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "Category filters will appear here once you add supplies",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Task association filter
                Text(
                    text = "Associations",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                FilterToggleItem(
                    label = "Has Task Associations",
                    description = "Show items linked to tasks",
                    checked = currentFilters.hasTaskAssociations,
                    onCheckedChange = { checked ->
                        currentFilters = currentFilters.copy(hasTaskAssociations = checked)
                        onFilterOptionsChanged(currentFilters)
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
