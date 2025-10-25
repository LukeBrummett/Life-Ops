package com.lifeops.presentation.inventory

import com.lifeops.app.data.local.dao.SupplyWithInventory

/**
 * UI State for Inventory Screen
 */
data class InventoryUiState(
    val supplies: List<SupplyWithInventory> = emptyList(),
    val searchQuery: String = "",
    val filterOptions: FilterOptions = FilterOptions(),
    val sortOption: SortOption = SortOption.BY_CATEGORY,
    val expandedCategories: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val isShoppingMode: Boolean = false,
    val shoppingCheckedItems: Set<String> = emptySet() // Supply IDs that are checked in shopping list
)

/**
 * Filter options for inventory list
 */
data class FilterOptions(
    val needsReorder: Boolean = false,
    val wellStocked: Boolean = false,
    val categories: Set<String> = emptySet(),
    val hasTaskAssociations: Boolean = false
) {
    val isActive: Boolean
        get() = needsReorder || wellStocked || categories.isNotEmpty() || hasTaskAssociations
        
    val activeCount: Int
        get() = listOfNotNull(
            if (needsReorder) 1 else null,
            if (wellStocked) 1 else null,
            if (hasTaskAssociations) 1 else null
        ).sum() + categories.size
}

/**
 * Sort options for inventory list
 */
enum class SortOption(val displayName: String) {
    BY_CATEGORY("Category"),
    BY_NAME_ASC("Name (A-Z)"),
    BY_NAME_DESC("Name (Z-A)"),
    BY_QUANTITY_ASC("Quantity (Low to High)"),
    BY_QUANTITY_DESC("Quantity (High to Low)"),
    BY_REORDER_URGENCY("Reorder Urgency")
}

/**
 * UI Events for Inventory Screen
 */
sealed class InventoryUiEvent {
    data class SearchQueryChanged(val query: String) : InventoryUiEvent()
    data class SortOptionSelected(val option: SortOption) : InventoryUiEvent()
    data class FilterOptionsChanged(val options: FilterOptions) : InventoryUiEvent()
    data class CategoryExpandToggle(val category: String) : InventoryUiEvent()
    data class IncrementQuantity(val supplyId: String) : InventoryUiEvent()
    data class DecrementQuantity(val supplyId: String) : InventoryUiEvent()
    data class NavigateToSupplyEdit(val supplyId: String) : InventoryUiEvent()
    data object NavigateToSupplyCreate : InventoryUiEvent()
    data object ToggleShoppingMode : InventoryUiEvent()
    data class ToggleShoppingItem(val supplyId: String) : InventoryUiEvent()
    data object CompleteShoppingSession : InventoryUiEvent()
    data object ClearError : InventoryUiEvent()
    data object ClearSuccess : InventoryUiEvent()
}

