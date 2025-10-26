package com.lifeops.app.presentation.alltasks.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeops.app.presentation.alltasks.GroupByOption

/**
 * Button that opens a bottom sheet to select grouping option
 * Shows current grouping state with icon
 */
@Composable
fun GroupButton(
    currentGroupBy: GroupByOption,
    onGroupByChange: (GroupByOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    
    OutlinedButton(
        onClick = { showBottomSheet = true },
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = if (currentGroupBy == GroupByOption.CATEGORY) 
                Icons.Default.ViewModule 
            else 
                Icons.Default.ViewList,
            contentDescription = "Group",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Group")
    }
    
    if (showBottomSheet) {
        GroupBottomSheet(
            currentGroupBy = currentGroupBy,
            onGroupByChange = { option ->
                onGroupByChange(option)
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
    currentGroupBy: GroupByOption,
    onGroupByChange: (GroupByOption) -> Unit,
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
                    label = "Relative Date",
                    description = "Group by Today, Tomorrow, This Week, etc.",
                    isSelected = currentGroupBy == GroupByOption.RELATIVE_DATE,
                    onClick = { onGroupByChange(GroupByOption.RELATIVE_DATE) }
                )
                
                GroupToggleItem(
                    label = "Category",
                    description = "Group by task category",
                    isSelected = currentGroupBy == GroupByOption.CATEGORY,
                    onClick = { onGroupByChange(GroupByOption.CATEGORY) }
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
 * Single group option item with label and description
 */
@Composable
private fun GroupToggleItem(
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) 
            MaterialTheme.colorScheme.primaryContainer 
        else 
            MaterialTheme.colorScheme.surface,
        tonalElevation = if (isSelected) 0.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isSelected) {
                RadioButton(
                    selected = true,
                    onClick = null
                )
            }
        }
    }
}
