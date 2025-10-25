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
 * Search and filter bar combining search, sort, and filter controls
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Search bar
        SearchBar(
            query = searchQuery,
            onQueryChanged = onSearchQueryChanged
        )
        
        // Sort and Filter buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SortButton(
                currentSortOption = sortOption,
                onSortOptionSelected = onSortOptionSelected,
                modifier = Modifier.weight(1f)
            )
            
            FilterButton(
                filterOptions = filterOptions,
                onFilterOptionsChanged = onFilterOptionsChanged,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * List of supplies grouped by category
 * Features:
 * - Lazy scrolling for performance
 * - Category grouping with collapsible sections
 * - Supply cards with quick adjust buttons
 * - Shopping mode with checkboxes
 */
@Composable
fun SupplyList(
    supplies: List<SupplyWithInventory>,
    expandedCategories: Set<String>,
    onCategoryExpandToggle: (String) -> Unit,
    onIncrementQuantity: (String) -> Unit,
    onDecrementQuantity: (String) -> Unit,
    onSupplyClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isShoppingMode: Boolean = false,
    checkedItems: Set<String> = emptySet(),
    onToggleShoppingItem: (String) -> Unit = {}
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
                    onSupplyClick = onSupplyClick,
                    isShoppingMode = isShoppingMode,
                    checkedItems = checkedItems,
                    onToggleShoppingItem = onToggleShoppingItem
                )
            }
        }
    }
}
