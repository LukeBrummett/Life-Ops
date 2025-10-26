package com.lifeops.presentation.inventory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeops.app.data.local.dao.SupplyWithInventory
import com.lifeops.app.data.repository.SupplyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val supplyRepository: SupplyRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    companion object {
        private const val KEY_SHOPPING_MODE = "shopping_mode"
        private const val KEY_SHOPPING_CHECKED_ITEMS = "shopping_checked_items"
        private const val KEY_PENDING_RESTOCK_ITEMS = "pending_restock_items"
    }

    init {
        // Restore shopping mode state
        val savedShoppingMode = savedStateHandle.get<Boolean>(KEY_SHOPPING_MODE) ?: false
        val savedCheckedItems = savedStateHandle.get<ArrayList<String>>(KEY_SHOPPING_CHECKED_ITEMS)?.toSet() ?: emptySet()
        val savedPendingRestockItems = savedStateHandle.get<ArrayList<String>>(KEY_PENDING_RESTOCK_ITEMS)?.toSet() ?: emptySet()
        
        android.util.Log.d("InventoryViewModel", "Restoring state - Shopping mode: $savedShoppingMode, Checked items: ${savedCheckedItems.size}, Pending restock: ${savedPendingRestockItems.size}")
        
        _uiState.update { 
            it.copy(
                isShoppingMode = savedShoppingMode,
                shoppingCheckedItems = savedCheckedItems,
                pendingRestockItems = savedPendingRestockItems
            )
        }
        
        loadSupplies()
    }

    private fun loadSupplies() {
        viewModelScope.launch {
            // Combine supplies with UI state to react to filter/sort changes
            combine(
                supplyRepository.observeSuppliesWithInventory(),
                _uiState
            ) { supplies, state ->
                applyFiltersAndSort(supplies, state)
            }
                .catch { e ->
                    _uiState.update { it.copy(error = "Failed to load supplies: ${e.message}", isLoading = false) }
                }
                .collect { filteredSupplies ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            supplies = filteredSupplies,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun onEvent(event: InventoryUiEvent) {
        when (event) {
            is InventoryUiEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
            }
            is InventoryUiEvent.SortOptionSelected -> {
                _uiState.update { it.copy(sortOption = event.option) }
            }
            is InventoryUiEvent.FilterOptionsChanged -> {
                _uiState.update { it.copy(filterOptions = event.options) }
            }
            is InventoryUiEvent.GroupByCategoryToggled -> {
                _uiState.update { it.copy(groupByCategory = event.enabled) }
            }
            is InventoryUiEvent.CategoryExpandToggle -> {
                _uiState.update { currentState ->
                    val expanded = currentState.expandedCategories.toMutableSet()
                    if (expanded.contains(event.category)) {
                        expanded.remove(event.category)
                    } else {
                        expanded.add(event.category)
                    }
                    currentState.copy(expandedCategories = expanded)
                }
            }
            is InventoryUiEvent.IncrementQuantity -> incrementQuantity(event.supplyId)
            is InventoryUiEvent.DecrementQuantity -> decrementQuantity(event.supplyId)
            is InventoryUiEvent.NavigateToSupplyEdit -> {
                // Navigation will be handled by the composable
            }
            InventoryUiEvent.NavigateToSupplyCreate -> {
                // Navigation will be handled by the composable
            }
            InventoryUiEvent.ToggleShoppingMode -> toggleShoppingMode()
            is InventoryUiEvent.ToggleShoppingItem -> toggleShoppingItem(event.supplyId)
            InventoryUiEvent.CompleteShoppingSession -> completeShoppingSession()
            InventoryUiEvent.ClearError -> {
                _uiState.update { it.copy(error = null) }
            }
            InventoryUiEvent.ClearSuccess -> {
                _uiState.update { it.copy(successMessage = null) }
            }
            InventoryUiEvent.ClearNavigation -> {
                _uiState.update { it.copy(navigateToRestock = null) }
            }
        }
    }
    
    private fun toggleShoppingMode() {
        _uiState.update { currentState ->
            val newShoppingMode = !currentState.isShoppingMode
            // Keep the checked items when toggling - don't clear them
            val newCheckedItems = currentState.shoppingCheckedItems
            
            // Save to SavedStateHandle
            savedStateHandle[KEY_SHOPPING_MODE] = newShoppingMode
            savedStateHandle[KEY_SHOPPING_CHECKED_ITEMS] = ArrayList(newCheckedItems)
            
            android.util.Log.d("InventoryViewModel", "Toggle shopping mode to $newShoppingMode - Checked items: ${newCheckedItems.size}")
            
            currentState.copy(
                isShoppingMode = newShoppingMode,
                shoppingCheckedItems = newCheckedItems
            )
        }
    }
    
    private fun toggleShoppingItem(supplyId: String) {
        _uiState.update { currentState ->
            val checkedItems = currentState.shoppingCheckedItems.toMutableSet()
            if (checkedItems.contains(supplyId)) {
                checkedItems.remove(supplyId)
            } else {
                checkedItems.add(supplyId)
            }
            
            // Save to SavedStateHandle
            savedStateHandle[KEY_SHOPPING_CHECKED_ITEMS] = ArrayList(checkedItems)
            
            android.util.Log.d("InventoryViewModel", "Toggled item $supplyId - Total checked: ${checkedItems.size}")
            
            currentState.copy(shoppingCheckedItems = checkedItems)
        }
    }
    
    private fun completeShoppingSession() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val checkedSupplyIds = currentState.shoppingCheckedItems
            
            if (checkedSupplyIds.isEmpty()) {
                savedStateHandle[KEY_SHOPPING_MODE] = false
                savedStateHandle[KEY_SHOPPING_CHECKED_ITEMS] = ArrayList<String>()
                _uiState.update { it.copy(isShoppingMode = false) }
                return@launch
            }
            
            // Save the checked items as pending restock
            savedStateHandle[KEY_PENDING_RESTOCK_ITEMS] = ArrayList(checkedSupplyIds)
            
            // Exit shopping mode and show success message
            savedStateHandle[KEY_SHOPPING_MODE] = false
            savedStateHandle[KEY_SHOPPING_CHECKED_ITEMS] = ArrayList<String>()
            
            _uiState.update {
                it.copy(
                    isShoppingMode = false,
                    shoppingCheckedItems = emptySet(),
                    pendingRestockItems = checkedSupplyIds,
                    successMessage = "Shopping complete! ${checkedSupplyIds.size} items ready to restock."
                )
            }
        }
    }
    
    private fun completeRestockSession() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val restockSupplyIds = currentState.pendingRestockItems
            
            if (restockSupplyIds.isEmpty()) {
                return@launch
            }
            
            // Increment inventory for all restocked items
            var successCount = 0
            var failureCount = 0
            
            restockSupplyIds.forEach { supplyId ->
                val result = supplyRepository.incrementInventory(supplyId)
                if (result.isSuccess) {
                    successCount++
                } else {
                    failureCount++
                }
            }
            
            // Clear pending restock items
            savedStateHandle[KEY_PENDING_RESTOCK_ITEMS] = ArrayList<String>()
            
            _uiState.update {
                it.copy(
                    pendingRestockItems = emptySet(),
                    successMessage = if (failureCount == 0) {
                        "Restock complete! Updated $successCount items."
                    } else {
                        "Restock complete with errors: $successCount succeeded, $failureCount failed."
                    }
                )
            }
        }
    }
    
    fun clearPendingRestock() {
        // Called when restock screen completes successfully
        savedStateHandle[KEY_PENDING_RESTOCK_ITEMS] = ArrayList<String>()
        _uiState.update {
            it.copy(pendingRestockItems = emptySet())
        }
    }

    private fun incrementQuantity(supplyId: String) {
        viewModelScope.launch {
            val result = supplyRepository.incrementInventory(supplyId)
            if (result.isFailure) {
                _uiState.update { 
                    it.copy(error = "Failed to update quantity: ${result.exceptionOrNull()?.message}") 
                }
            }
        }
    }

    private fun decrementQuantity(supplyId: String) {
        viewModelScope.launch {
            val result = supplyRepository.decrementInventory(supplyId)
            if (result.isFailure) {
                _uiState.update { 
                    it.copy(error = "Failed to update quantity: ${result.exceptionOrNull()?.message}") 
                }
            }
        }
    }

    private fun applyFiltersAndSort(
        supplies: List<SupplyWithInventory>,
        state: InventoryUiState
    ): List<SupplyWithInventory> {
        var filtered = supplies
        
        // In shopping mode, automatically filter to items needing reorder
        if (state.isShoppingMode) {
            filtered = filtered.filter { it.needsReorder }
        }

        // Apply search filter
        if (state.searchQuery.isNotBlank()) {
            val query = state.searchQuery.lowercase()
            filtered = filtered.filter { supplyWithInventory ->
                supplyWithInventory.supply.name.lowercase().contains(query) ||
                supplyWithInventory.supply.category.lowercase().contains(query) ||
                supplyWithInventory.supply.tags?.lowercase()?.contains(query) == true
            }
        }

        // Apply filter options (only if not in shopping mode - shopping mode overrides)
        if (!state.isShoppingMode) {
            if (state.filterOptions.needsReorder) {
                filtered = filtered.filter { it.needsReorder }
            }
            if (state.filterOptions.wellStocked) {
                filtered = filtered.filter { it.isWellStocked }
            }
            // TODO: Implement task associations filter when the relationship is added to the database
            // if (state.filterOptions.hasTaskAssociations) {
            //     filtered = filtered.filter { 
            //         it.supply.taskAssociations?.isNotEmpty() == true 
            //     }
            // }
            if (state.filterOptions.categories.isNotEmpty()) {
                filtered = filtered.filter { it.supply.category in state.filterOptions.categories }
            }
        }

        // Apply sort
        filtered = when (state.sortOption) {
            SortOption.BY_CATEGORY -> filtered.sortedWith(
                compareBy({ it.supply.category }, { it.supply.name })
            )
            SortOption.BY_NAME_ASC -> filtered.sortedBy { it.supply.name }
            SortOption.BY_NAME_DESC -> filtered.sortedByDescending { it.supply.name }
            SortOption.BY_QUANTITY_ASC -> filtered.sortedBy { it.currentQuantity ?: 0 }
            SortOption.BY_QUANTITY_DESC -> filtered.sortedByDescending { it.currentQuantity ?: 0 }
            SortOption.BY_REORDER_URGENCY -> filtered.sortedWith(
                compareBy(
                    { !it.needsReorder }, // Needs reorder first
                    { it.supply.category },
                    { it.supply.name }
                )
            )
        }

        return filtered
    }
}
