package com.lifeops.presentation.inventory.restock.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lifeops.presentation.inventory.restock.RestockItem

@Composable
fun RestockItemCard(
    item: RestockItem,
    onToggleDone: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isDone)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Done checkbox (left side)
            Column(
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Checkbox(
                    checked = item.isDone,
                    onCheckedChange = { onToggleDone() }
                )
                Text(
                    text = "Done",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Item details (middle - takes remaining space)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Item name
                Text(
                    text = item.supply.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Info line: Current • Target • Suggested
                Text(
                    text = "Current: ${item.currentQuantity} • Target: ${item.targetQuantity} • Suggested: +${item.suggestedIncrease}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Quantity controls (right side, stacked)
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 16.dp)
            ) {
                // Quantity display (large)
                Text(
                    text = "${item.adjustedQuantity}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (item.isDone)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Plus button
                IconButton(
                    onClick = onIncrement,
                    enabled = !item.isDone,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Increase quantity",
                        tint = if (item.isDone)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        else
                            MaterialTheme.colorScheme.primary
                    )
                }
                
                // Minus button
                IconButton(
                    onClick = onDecrement,
                    enabled = !item.isDone && item.adjustedQuantity > 0,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Remove,
                        contentDescription = "Decrease quantity",
                        tint = if (item.isDone || item.adjustedQuantity == 0)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        else
                            MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
