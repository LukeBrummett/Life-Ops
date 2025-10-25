package com.lifeops.presentation.inventory.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeops.app.data.local.dao.SupplyWithInventory
import com.lifeops.presentation.inventory.FilterOptions
import com.lifeops.presentation.inventory.SortOption

/**
 * Placeholder for Search and Filter Bar
 * Will be implemented in Phase 3
 */
@Composable
fun SearchAndFilterBar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    sortOption: SortOption,
    onSortOptionSelected: (SortOption) -> Unit,
    filterOptions: FilterOptions,
    onFilterOptionsChanged: (FilterOptions) -> Unit
) {
    // Placeholder - will be implemented in Phase 3
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = "Search & Filter (Coming in Phase 3)",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/**
 * List of supplies grouped by category
 * Features:
 * - Lazy scrolling for performance
 * - Category grouping with collapsible sections
 * - Supply cards with quick adjust buttons
 */
@Composable
fun SupplyList(
    supplies: List<SupplyWithInventory>,
    expandedCategories: Set<String>,
    onCategoryExpandToggle: (String) -> Unit,
    onIncrementQuantity: (String) -> Unit,
    onDecrementQuantity: (String) -> Unit,
    onSupplyClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Group supplies by category
    val suppliesByCategory = supplies.groupBy { it.supply.category }
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        suppliesByCategory.forEach { (category, categorySupplies) ->
            item(key = "category_$category") {
                CategorySection(
                    categoryName = category,
                    supplies = categorySupplies,
                    isExpanded = expandedCategories.contains(category),
                    onToggleExpand = { onCategoryExpandToggle(category) },
                    onIncrementQuantity = onIncrementQuantity,
                    onDecrementQuantity = onDecrementQuantity,
                    onSupplyClick = onSupplyClick
                )
            }
        }
    }
}
