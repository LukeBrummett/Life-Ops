package com.lifeops.presentation.inventory.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Button that toggles grouping by category
 * Shows current grouping state with icon
 */
@Composable
fun GroupButton(
    groupByCategory: Boolean,
    onGroupByCategoryToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    
    OutlinedButton(
        onClick = { showBottomSheet = true },
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = if (groupByCategory) Icons.Default.ViewModule else Icons.Default.ViewList,
            contentDescription = "Group",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Group")
    }
    
    if (showBottomSheet) {
        GroupBottomSheet(
            groupByCategory = groupByCategory,
            onGroupByCategoryToggled = { enabled ->
                onGroupByCategoryToggled(enabled)
                showBottomSheet = false
            },
            onDismiss = { showBottomSheet = false }
        )
    }
}

/**
 * Bottom sheet for selecting grouping option
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupBottomSheet(
    groupByCategory: Boolean,
    onGroupByCategoryToggled: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header
            Text(
                text = "Group By",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
            
            Divider()
            
            // Group options
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GroupToggleItem(
                    label = "Group by Category",
                    description = "Organize supplies into category sections",
                    checked = groupByCategory,
                    onCheckedChange = { onGroupByCategoryToggled(it) }
                )
                
                // Future: Could add more grouping options here
                // - Group by Reorder Status
                // - Group by Location
                // - etc.
            }
        }
    }
}

/**
 * Single group toggle item with label and description
 */
@Composable
private fun GroupToggleItem(
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
