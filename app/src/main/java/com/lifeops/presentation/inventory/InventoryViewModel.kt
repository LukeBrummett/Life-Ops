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
            InventoryUiEvent.NavigateToShopping -> {
                // Navigation will be handled by the composable
            }
            InventoryUiEvent.ClearError -> {
                _uiState.update { it.copy(error = null) }
            }
            InventoryUiEvent.ClearSuccess -> {
                _uiState.update { it.copy(successMessage = null) }
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

        // Apply search filter
        if (state.searchQuery.isNotBlank()) {
            val query = state.searchQuery.lowercase()
            filtered = filtered.filter { supplyWithInventory ->
                supplyWithInventory.supply.name.lowercase().contains(query) ||
                supplyWithInventory.supply.category.lowercase().contains(query) ||
                supplyWithInventory.supply.tags?.lowercase()?.contains(query) == true
            }
        }

        // Apply filter options
        if (state.filterOptions.needsReorder) {
            filtered = filtered.filter { it.needsReorder }
        }
        if (state.filterOptions.wellStocked) {
            filtered = filtered.filter { it.isWellStocked }
        }
        if (state.filterOptions.categories.isNotEmpty()) {
            filtered = filtered.filter { it.supply.category in state.filterOptions.categories }
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
