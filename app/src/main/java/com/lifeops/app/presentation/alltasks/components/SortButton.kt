package com.lifeops.app.presentation.alltasks.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.lifeops.app.presentation.alltasks.SortOption

/**
 * Button that opens a bottom sheet to select sort option
 */
@Composable
fun SortButton(
    currentSortOption: SortOption,
    onSortOptionSelected: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    
    OutlinedButton(
        onClick = { showBottomSheet = true },
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Sort,
            contentDescription = "Sort",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Sort")
    }
    
    if (showBottomSheet) {
        SortBottomSheet(
            currentSortOption = currentSortOption,
            onSortOptionSelected = { option ->
                onSortOptionSelected(option)
                showBottomSheet = false
            },
            onDismiss = { showBottomSheet = false }
        )
    }
}

/**
 * Bottom sheet for selecting sort option
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortBottomSheet(
    currentSortOption: SortOption,
    onSortOptionSelected: (SortOption) -> Unit,
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
                text = "Sort By",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
            
            Divider()
            
            // Sort options
            Column(
                modifier = Modifier
                    .selectableGroup()
                    .padding(vertical = 8.dp)
            ) {
                SortOption.entries.forEach { option ->
                    SortOptionItem(
                        option = option,
                        isSelected = option == currentSortOption,
                        onClick = { onSortOptionSelected(option) }
                    )
                }
            }
        }
    }
}

/**
 * Single sort option item in the list
 */
@Composable
private fun SortOptionItem(
    option: SortOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = option.displayName,
                style = MaterialTheme.typography.bodyLarge
            )
            if (option.description.isNotEmpty()) {
                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Extension properties for SortOption display
 */
private val SortOption.displayName: String
    get() = when (this) {
        SortOption.BY_DATE -> "Date"
        SortOption.BY_NAME -> "Name (A-Z)"
        SortOption.BY_CATEGORY -> "Category"
    }

private val SortOption.description: String
    get() = when (this) {
        SortOption.BY_DATE -> "Sort by next due date"
        SortOption.BY_NAME -> "Sort alphabetically by name"
        SortOption.BY_CATEGORY -> "Sort by category"
    }
