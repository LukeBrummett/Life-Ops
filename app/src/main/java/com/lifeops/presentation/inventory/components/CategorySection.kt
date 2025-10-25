package com.lifeops.presentation.inventory.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lifeops.app.data.local.dao.SupplyWithInventory

/**
 * Collapsible section for a category of supplies
 * Features:
 * - Category name with expand/collapse arrow
 * - Item count badge
 * - Animated expand/collapse
 * - List of supplies when expanded
 * - Shopping mode support with checkboxes
 */
@Composable
fun CategorySection(
    categoryName: String,
    supplies: List<SupplyWithInventory>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onIncrementQuantity: (String) -> Unit,
    onDecrementQuantity: (String) -> Unit,
    onSupplyClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isShoppingMode: Boolean = false,
    checkedItems: Set<String> = emptySet(),
    onToggleShoppingItem: (String) -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Category Header
        CategoryHeader(
            categoryName = categoryName,
            itemCount = supplies.size,
            isExpanded = isExpanded,
            onToggleExpand = onToggleExpand
        )
        
        // Supplies List (animated visibility)
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                
                supplies.forEach { supply ->
                    if (isShoppingMode) {
                        ShoppingListItemCard(
                            supply = supply,
                            isChecked = checkedItems.contains(supply.supply.id),
                            onToggleChecked = { onToggleShoppingItem(supply.supply.id) },
                            onClick = { onSupplyClick(supply.supply.id) }
                        )
                    } else {
                        SupplyItemCard(
                            supply = supply,
                            onIncrementQuantity = { onIncrementQuantity(supply.supply.id) },
                            onDecrementQuantity = { onDecrementQuantity(supply.supply.id) },
                            onClick = { onSupplyClick(supply.supply.id) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

/**
 * Header for a category section
 */
@Composable
private fun CategoryHeader(
    categoryName: String,
    itemCount: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "arrow_rotation"
    )
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleExpand),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category name and count
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Item count badge
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = itemCount.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            
            // Expand/collapse arrow
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                modifier = Modifier.rotate(rotationAngle),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
