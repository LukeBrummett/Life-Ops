package com.lifeops.presentation.supplyedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeops.app.data.local.entity.Inventory
import com.lifeops.app.data.repository.SupplyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for Supply Edit screen
 * Handles form state, validation, and save/delete operations
 */
@HiltViewModel
class SupplyEditViewModel @Inject constructor(
    private val supplyRepository: SupplyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val supplyId: String? = savedStateHandle["supplyId"]
    
    private val _uiState = MutableStateFlow(
        SupplyEditUiState(supplyId = supplyId)
    )
    val uiState: StateFlow<SupplyEditUiState> = _uiState.asStateFlow()
    
    private var _onSaveSuccess: (() -> Unit)? = null
    private var _onDeleteSuccess: (() -> Unit)? = null
    
    init {
        if (supplyId != null) {
            loadSupply(supplyId)
        }
    }
    
    fun setNavigationCallbacks(
        onSaveSuccess: () -> Unit,
        onDeleteSuccess: () -> Unit
    ) {
        _onSaveSuccess = onSaveSuccess
        _onDeleteSuccess = onDeleteSuccess
    }
    
    private fun loadSupply(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val supply = supplyRepository.getSupplyById(id)
                
                supply?.let {
                    _uiState.update { state ->
                        state.copy(
                            supplyId = it.id,
                            name = it.name,
                            category = it.category,
                            unit = it.unit,
                            reorderThreshold = it.reorderThreshold.toString(),
                            reorderTargetQuantity = it.reorderTargetQuantity.toString(),
                            tags = it.tags?.split(",")?.map { tag -> tag.trim() }?.filter { tag -> tag.isNotBlank() } ?: emptyList(),
                            notes = it.notes ?: "",
                            isLoading = false
                        )
                    }
                } ?: run {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Supply not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load supply: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun onEvent(event: SupplyEditUiEvent) {
        when (event) {
            is SupplyEditUiEvent.NameChanged -> updateName(event.name)
            is SupplyEditUiEvent.CategoryChanged -> updateCategory(event.category)
            is SupplyEditUiEvent.UnitChanged -> updateUnit(event.unit)
            is SupplyEditUiEvent.ReorderThresholdChanged -> updateReorderThreshold(event.threshold)
            is SupplyEditUiEvent.ReorderTargetQuantityChanged -> updateReorderTargetQuantity(event.quantity)
            is SupplyEditUiEvent.AddTag -> addTag(event.tag)
            is SupplyEditUiEvent.RemoveTag -> removeTag(event.tag)
            is SupplyEditUiEvent.NotesChanged -> updateNotes(event.notes)
            is SupplyEditUiEvent.InitialQuantityChanged -> updateInitialQuantity(event.quantity)
            SupplyEditUiEvent.SaveClicked -> saveSupply()
            SupplyEditUiEvent.DeleteClicked -> {} // Will show confirmation dialog in UI
            SupplyEditUiEvent.DeleteConfirmed -> deleteSupply()
            SupplyEditUiEvent.ErrorDismissed -> dismissError()
        }
    }
    
    private fun updateName(name: String) {
        _uiState.update { state ->
            state.copy(
                name = name,
                validationErrors = state.validationErrors.copy(
                    nameError = when {
                        name.isBlank() -> "Name is required"
                        name.length < 2 -> "Name must be at least 2 characters"
                        else -> null
                    }
                )
            )
        }
    }
    
    private fun updateCategory(category: String) {
        _uiState.update { it.copy(category = category) }
    }
    
    private fun updateUnit(unit: String) {
        _uiState.update { it.copy(unit = unit) }
    }
    
    private fun updateReorderThreshold(threshold: String) {
        _uiState.update { state ->
            val thresholdInt = threshold.toIntOrNull()
            val targetInt = state.reorderTargetQuantity.toIntOrNull()
            
            state.copy(
                reorderThreshold = threshold,
                validationErrors = state.validationErrors.copy(
                    thresholdError = when {
                        threshold.isBlank() -> "Threshold is required"
                        thresholdInt == null -> "Must be a valid number"
                        thresholdInt < 0 -> "Must be 0 or greater"
                        targetInt != null && thresholdInt > targetInt -> 
                            "Must be less than or equal to target quantity"
                        else -> null
                    }
                )
            )
        }
    }
    
    private fun updateReorderTargetQuantity(quantity: String) {
        _uiState.update { state ->
            val targetInt = quantity.toIntOrNull()
            val thresholdInt = state.reorderThreshold.toIntOrNull()
            
            state.copy(
                reorderTargetQuantity = quantity,
                validationErrors = state.validationErrors.copy(
                    targetQuantityError = when {
                        quantity.isBlank() -> "Target quantity is required"
                        targetInt == null -> "Must be a valid number"
                        targetInt < 0 -> "Must be 0 or greater"
                        thresholdInt != null && targetInt < thresholdInt -> 
                            "Must be greater than or equal to threshold"
                        else -> null
                    }
                )
            )
        }
    }
    
    private fun addTag(tag: String) {
        if (tag.isBlank()) return
        val currentTags = _uiState.value.tags
        if (!currentTags.contains(tag)) {
            _uiState.update { it.copy(tags = currentTags + tag) }
        }
    }
    
    private fun removeTag(tag: String) {
        _uiState.update {
            it.copy(tags = it.tags.filter { t -> t != tag })
        }
    }
    
    private fun updateNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }
    
    private fun updateInitialQuantity(quantity: String) {
        _uiState.update { state ->
            val quantityInt = quantity.toIntOrNull()
            
            state.copy(
                initialQuantity = quantity,
                validationErrors = state.validationErrors.copy(
                    initialQuantityError = when {
                        quantity.isBlank() -> "Initial quantity is required"
                        quantityInt == null -> "Must be a valid number"
                        quantityInt < 0 -> "Must be 0 or greater"
                        else -> null
                    }
                )
            )
        }
    }
    
    private fun saveSupply() {
        val currentState = _uiState.value
        
        // Validate all fields
        if (!currentState.isValid) {
            _uiState.update {
                it.copy(errorMessage = "Please fix validation errors before saving")
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            try {
                val supply = currentState.toSupply().copy(
                    id = currentState.supplyId ?: UUID.randomUUID().toString()
                )
                
                val result = if (currentState.isEditing) {
                    // Update existing supply
                    supplyRepository.updateSupply(supply)
                } else {
                    // Create new supply with initial inventory
                    val initialQuantity = currentState.initialQuantity.toIntOrNull() ?: 0
                    supplyRepository.createSupply(supply, initialQuantity)
                }
                
                result.fold(
                    onSuccess = {
                        _uiState.update { it.copy(isSaving = false) }
                        _onSaveSuccess?.invoke()
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                errorMessage = "Failed to save supply: ${error.message}"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Failed to save supply: ${e.message}"
                    )
                }
            }
        }
    }
    
    private fun deleteSupply() {
        val currentState = _uiState.value
        if (currentState.supplyId == null) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            val supply = currentState.toSupply()
            supplyRepository.deleteSupply(supply).fold(
                onSuccess = {
                    _uiState.update { it.copy(isSaving = false) }
                    _onDeleteSuccess?.invoke()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = "Failed to delete supply: ${error.message}"
                        )
                    }
                }
            )
        }
    }
    
    private fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
