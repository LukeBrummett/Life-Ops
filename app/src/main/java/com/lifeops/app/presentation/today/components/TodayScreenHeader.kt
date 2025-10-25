package com.lifeops.app.presentation.today.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.lifeops.app.ui.theme.LifeOpsTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Header component for Today Screen
 * 
 * Displays:
 * - All Tasks button (left)
 * - Show Completed toggle (left)
 * - Date selector (center)
 * - Inventory button (right)
 * - Settings button (right)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreenHeader(
    selectedDate: LocalDate,
    showCompleted: Boolean,
    onNavigateToAllTasks: () -> Unit,
    onToggleCompleted: () -> Unit,
    onDateClick: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            // Center-aligned date
            Text(
                text = formatDate(selectedDate),
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            // All Tasks button
            IconButton(onClick = onNavigateToAllTasks) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "All Tasks"
                )
            }
        },
        actions = {
            // Show Completed toggle
            IconButton(onClick = onToggleCompleted) {
                Icon(
                    imageVector = if (showCompleted) {
                        Icons.Default.CheckBox
                    } else {
                        Icons.Default.CheckBoxOutlineBlank
                    },
                    contentDescription = if (showCompleted) {
                        "Hide completed tasks"
                    } else {
                        "Show completed tasks"
                    },
                    tint = if (showCompleted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
            
            // Date selector button
            IconButton(onClick = onDateClick) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Select date"
                )
            }
            
            // Inventory button
            IconButton(onClick = onNavigateToInventory) {
                Icon(
                    imageVector = Icons.Default.Inventory,
                    contentDescription = "Inventory"
                )
            }
            
            // Settings button
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            }
        },
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

/**
 * Format LocalDate for display in header
 * Shows "Today" for current date, otherwise shows formatted date
 */
private fun formatDate(date: LocalDate): String {
    val today = LocalDate.now()
    return when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        today.plusDays(1) -> "Tomorrow"
        else -> date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    }
}

// ================================
// Preview Functions
// ================================

@Preview(name = "Header - Default", showBackground = true)
@Composable
private fun PreviewTodayScreenHeaderDefault() {
    LifeOpsTheme {
        Surface {
            TodayScreenHeader(
                selectedDate = LocalDate.now(),
                showCompleted = false,
                onNavigateToAllTasks = {},
                onToggleCompleted = {},
                onDateClick = {},
                onNavigateToInventory = {},
                onNavigateToSettings = {}
            )
        }
    }
}

@Preview(name = "Header - Show Completed", showBackground = true)
@Composable
private fun PreviewTodayScreenHeaderWithCompleted() {
    LifeOpsTheme {
        Surface {
            TodayScreenHeader(
                selectedDate = LocalDate.now(),
                showCompleted = true,
                onNavigateToAllTasks = {},
                onToggleCompleted = {},
                onDateClick = {},
                onNavigateToInventory = {},
                onNavigateToSettings = {}
            )
        }
    }
}

@Preview(name = "Header - Yesterday", showBackground = true)
@Composable
private fun PreviewTodayScreenHeaderYesterday() {
    LifeOpsTheme {
        Surface {
            TodayScreenHeader(
                selectedDate = LocalDate.now().minusDays(1),
                showCompleted = false,
                onNavigateToAllTasks = {},
                onToggleCompleted = {},
                onDateClick = {},
                onNavigateToInventory = {},
                onNavigateToSettings = {}
            )
        }
    }
}

@Preview(name = "Header - Custom Date", showBackground = true)
@Composable
private fun PreviewTodayScreenHeaderCustomDate() {
    LifeOpsTheme {
        Surface {
            TodayScreenHeader(
                selectedDate = LocalDate.of(2025, 12, 25),
                showCompleted = false,
                onNavigateToAllTasks = {},
                onToggleCompleted = {},
                onDateClick = {},
                onNavigateToInventory = {},
                onNavigateToSettings = {}
            )
        }
    }
}
