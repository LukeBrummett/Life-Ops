package com.lifeops.presentation.inventory

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
    private val supplyRepository: SupplyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    init {
        loadSupplies()
    }

    private fun loadSupplies() {
        viewModelScope.launch {
            supplyRepository.observeSuppliesWithInventory()
                .catch { e ->
                    _uiState.update { it.copy(error = "Failed to load supplies: ${e.message}", isLoading = false) }
                }
                .collect { supplies ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            supplies = applyFiltersAndSort(supplies, currentState),
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
        }
    }
    
    private fun toggleShoppingMode() {
        _uiState.update { currentState ->
            if (currentState.isShoppingMode) {
                // Exiting shopping mode - clear checked items
                currentState.copy(
                    isShoppingMode = false,
                    shoppingCheckedItems = emptySet()
                )
            } else {
                // Entering shopping mode - auto-filter to items needing reorder
                currentState.copy(
                    isShoppingMode = true,
                    shoppingCheckedItems = emptySet()
                )
            }
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
            currentState.copy(shoppingCheckedItems = checkedItems)
        }
    }
    
    private fun completeShoppingSession() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val checkedSupplyIds = currentState.shoppingCheckedItems
            
            if (checkedSupplyIds.isEmpty()) {
                _uiState.update { it.copy(isShoppingMode = false) }
                return@launch
            }
            
            // For each checked item, increment quantity to target
            checkedSupplyIds.forEach { supplyId ->
                val supply = currentState.supplies.find { it.supply.id == supplyId }
                supply?.let {
                    val currentQty = it.currentQuantity ?: 0
                    val targetQty = it.supply.reorderTargetQuantity
                    val amountToAdd = (targetQty - currentQty).coerceAtLeast(0)
                    
                    // Increment by the difference to reach target
                    repeat(amountToAdd) {
                        supplyRepository.incrementInventory(supplyId)
                    }
                }
            }
            
            _uiState.update {
                it.copy(
                    isShoppingMode = false,
                    shoppingCheckedItems = emptySet(),
                    successMessage = "Shopping completed! ${checkedSupplyIds.size} items restocked."
                )
            }
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
