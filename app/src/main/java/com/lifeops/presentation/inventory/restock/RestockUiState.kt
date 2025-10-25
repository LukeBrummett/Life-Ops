package com.lifeops.presentation.inventory.restock

import com.lifeops.app.data.local.entity.Supply

/**
 * Item in restock workflow with quantity adjustments
 */
data class RestockItem(
    val supply: Supply,
    val currentQuantity: Int,
    val targetQuantity: Int,
    val adjustedQuantity: Int, // User-adjusted quantity
    val isDone: Boolean = false
) {
    val suggestedIncrease: Int get() = targetQuantity - currentQuantity
}

/**
 * UI state for restock workflow screen
 */
data class RestockUiState(
    val items: List<RestockItem> = emptyList(),
    val expandedCategories: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
) {
    /**
     * Check if all items are marked done
     */
    val allItemsDone: Boolean get() = items.isNotEmpty() && items.all { it.isDone }
    
    /**
     * Count of done items
     */
    val doneCount: Int get() = items.count { it.isDone }
    
    /**
     * Group items by category
     */
    val itemsByCategory: Map<String, List<RestockItem>>
        get() = items.groupBy { it.supply.category }
    
    /**
     * Check if a category should auto-collapse (all items done)
     */
    fun shouldCollapseCategory(category: String): Boolean {
        val categoryItems = itemsByCategory[category] ?: return false
        return categoryItems.all { it.isDone }
    }
}

/**
 * Events for restock workflow
 */
sealed class RestockUiEvent {
    data class IncrementQuantity(val supplyId: String) : RestockUiEvent()
    data class DecrementQuantity(val supplyId: String) : RestockUiEvent()
    data class SetQuantity(val supplyId: String, val quantity: Int) : RestockUiEvent()
    data class ToggleDone(val supplyId: String) : RestockUiEvent()
    data class CategoryExpandToggle(val category: String) : RestockUiEvent()
    object CompleteRestock : RestockUiEvent()
    object CancelRestock : RestockUiEvent()
    object ClearError : RestockUiEvent()
    object ClearSuccess : RestockUiEvent()
}
