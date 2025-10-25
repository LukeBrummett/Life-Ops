package com.lifeops.app.presentation.today.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.style.TextAlign
import com.lifeops.app.ui.theme.LifeOpsTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Header component for Today Screen
 * 
 * Displays (in order):
 * 1. All Tasks button (left)
 * 2. Show Completed toggle (left, in actions)
 * 3. Date display (center title)
 * 4. Inventory button (right)
 * 5. Settings button (right)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreenHeader(
    selectedDate: LocalDate,
    showCompleted: Boolean,
    onNavigateToAllTasks: () -> Unit,
    onToggleCompleted: () -> Unit,
    onDateClick: () -> Unit, // Kept for future functionality
    onNavigateToInventory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            // Center-aligned date
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formatDate(selectedDate),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
            }
        },
        navigationIcon = {
            // Left side: All Tasks button (position 1) and Show Completed toggle (position 2)
            Row {
                // All Tasks button (position 1)
                IconButton(onClick = onNavigateToAllTasks) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "All Tasks"
                    )
                }
                
                // Show Completed toggle (position 2)
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
            }
        },
        actions = {
            // Inventory button (position 4)
            IconButton(onClick = onNavigateToInventory) {
                Icon(
                    imageVector = Icons.Default.Inventory,
                    contentDescription = "Inventory"
                )
            }
            
            // Settings button (position 5)
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
