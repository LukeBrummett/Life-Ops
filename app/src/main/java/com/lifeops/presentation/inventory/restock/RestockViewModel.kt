package com.lifeops.presentation.inventory.restock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeops.app.data.repository.SupplyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RestockViewModel @Inject constructor(
    private val supplyRepository: SupplyRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RestockUiState())
    val uiState: StateFlow<RestockUiState> = _uiState.asStateFlow()
    
    /**
     * Initialize restock workflow with list of supply IDs
     * Called when entering restock screen from shopping workflow
     */
    fun initializeRestock(supplyIds: List<String>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Fetch supplies and their current quantities
                val restockItems = mutableListOf<RestockItem>()
                
                supplyIds.forEach { supplyId ->
                    val supply = supplyRepository.getSupplyById(supplyId)
                    val inventory = supplyRepository.getInventory(supplyId)
                    
                    if (supply != null && inventory != null) {
                        restockItems.add(
                            RestockItem(
                                supply = supply,
                                currentQuantity = inventory.currentQuantity,
                                targetQuantity = supply.reorderTargetQuantity,
                                adjustedQuantity = supply.reorderTargetQuantity, // Default to target
                                isDone = false
                            )
                        )
                    }
                }
                
                // Auto-expand all categories initially
                val allCategories = restockItems.map { it.supply.category }.toSet()
                
                _uiState.update { 
                    it.copy(
                        items = restockItems,
                        expandedCategories = allCategories,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load restock items: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun onEvent(event: RestockUiEvent) {
        when (event) {
            is RestockUiEvent.IncrementQuantity -> incrementQuantity(event.supplyId)
            is RestockUiEvent.DecrementQuantity -> decrementQuantity(event.supplyId)
            is RestockUiEvent.SetQuantity -> setQuantity(event.supplyId, event.quantity)
            is RestockUiEvent.ToggleDone -> toggleDone(event.supplyId)
            is RestockUiEvent.CategoryExpandToggle -> toggleCategoryExpand(event.category)
            is RestockUiEvent.CompleteRestock -> completeRestock()
            is RestockUiEvent.CancelRestock -> cancelRestock()
            is RestockUiEvent.ClearError -> _uiState.update { it.copy(error = null) }
            is RestockUiEvent.ClearSuccess -> _uiState.update { it.copy(successMessage = null) }
        }
    }
    
    private fun incrementQuantity(supplyId: String) {
        _uiState.update { state ->
            state.copy(
                items = state.items.map { item ->
                    if (item.supply.id == supplyId && !item.isDone) {
                        item.copy(adjustedQuantity = item.adjustedQuantity + 1)
                    } else {
                        item
                    }
                }
            )
        }
    }
    
    private fun decrementQuantity(supplyId: String) {
        _uiState.update { state ->
            state.copy(
                items = state.items.map { item ->
                    if (item.supply.id == supplyId && !item.isDone && item.adjustedQuantity > 0) {
                        item.copy(adjustedQuantity = item.adjustedQuantity - 1)
                    } else {
                        item
                    }
                }
            )
        }
    }
    
    private fun setQuantity(supplyId: String, quantity: Int) {
        if (quantity < 0) return
        
        _uiState.update { state ->
            state.copy(
                items = state.items.map { item ->
                    if (item.supply.id == supplyId && !item.isDone) {
                        item.copy(adjustedQuantity = quantity)
                    } else {
                        item
                    }
                }
            )
        }
    }
    
    private fun toggleDone(supplyId: String) {
        _uiState.update { state ->
            val updatedItems = state.items.map { item ->
                if (item.supply.id == supplyId) {
                    item.copy(isDone = !item.isDone)
                } else {
                    item
                }
            }
            
            // Auto-collapse categories where all items are done
            val collapsedCategories = state.expandedCategories.filter { category ->
                val categoryItems = updatedItems.filter { it.supply.category == category }
                !categoryItems.all { it.isDone }
            }.toSet()
            
            state.copy(
                items = updatedItems,
                expandedCategories = collapsedCategories
            )
        }
    }
    
    private fun toggleCategoryExpand(category: String) {
        _uiState.update { state ->
            val expandedCategories = if (category in state.expandedCategories) {
                state.expandedCategories - category
            } else {
                state.expandedCategories + category
            }
            state.copy(expandedCategories = expandedCategories)
        }
    }
    
    private fun completeRestock() {
        val state = _uiState.value
        
        // Validation: All items must be marked done
        if (!state.allItemsDone) {
            _uiState.update { 
                it.copy(error = "Please mark all items as Done before completing") 
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            try {
                // Update all inventory quantities
                state.items.forEach { item ->
                    supplyRepository.updateInventoryQuantity(
                        supplyId = item.supply.id,
                        quantity = item.adjustedQuantity
                    )
                }
                
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        successMessage = "Inventory updated for ${state.items.size} items"
                    )
                }
                
                // Navigation handled by screen observing successMessage
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        error = "Failed to update inventory: ${e.message}"
                    )
                }
            }
        }
    }
    
    private fun cancelRestock() {
        // Just set a flag for the screen to handle navigation
        // The screen will show confirmation dialog and handle the actual cancellation
        _uiState.update { it.copy(successMessage = "cancelled") }
    }
}
