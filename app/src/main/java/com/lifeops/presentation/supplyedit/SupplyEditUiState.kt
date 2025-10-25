package com.lifeops.presentation.supplyedit

import com.lifeops.app.data.local.entity.Supply

/**
 * UI state for the Supply Edit screen
 */
data class SupplyEditUiState(
    val supplyId: String? = null, // null = creating new supply
    val name: String = "",
    val category: String = "",
    val unit: String = "",
    val reorderThreshold: String = "5",
    val reorderTargetQuantity: String = "10",
    val tags: String = "",
    val notes: String = "",
    val initialQuantity: String = "0",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val validationErrors: ValidationErrors = ValidationErrors()
) {
    val isValid: Boolean
        get() = validationErrors.isValid && name.isNotBlank()
    
    val isEditing: Boolean
        get() = supplyId != null
}

/**
 * Validation errors for form fields
 */
data class ValidationErrors(
    val nameError: String? = null,
    val categoryError: String? = null,
    val unitError: String? = null,
    val thresholdError: String? = null,
    val targetQuantityError: String? = null,
    val initialQuantityError: String? = null
) {
    val isValid: Boolean
        get() = nameError == null && 
                categoryError == null && 
                unitError == null &&
                thresholdError == null &&
                targetQuantityError == null &&
                initialQuantityError == null
}

/**
 * Events for the Supply Edit screen
 */
sealed class SupplyEditUiEvent {
    data class NameChanged(val name: String) : SupplyEditUiEvent()
    data class CategoryChanged(val category: String) : SupplyEditUiEvent()
    data class UnitChanged(val unit: String) : SupplyEditUiEvent()
    data class ReorderThresholdChanged(val threshold: String) : SupplyEditUiEvent()
    data class ReorderTargetQuantityChanged(val quantity: String) : SupplyEditUiEvent()
    data class TagsChanged(val tags: String) : SupplyEditUiEvent()
    data class NotesChanged(val notes: String) : SupplyEditUiEvent()
    data class InitialQuantityChanged(val quantity: String) : SupplyEditUiEvent()
    object SaveClicked : SupplyEditUiEvent()
    object DeleteClicked : SupplyEditUiEvent()
    object DeleteConfirmed : SupplyEditUiEvent()
    object ErrorDismissed : SupplyEditUiEvent()
}

/**
 * Extension to convert UI state to Supply entity
 */
fun SupplyEditUiState.toSupply(): Supply {
    return Supply(
        id = supplyId ?: "", // Will be replaced with UUID in ViewModel
        name = name.trim(),
        category = category.trim().ifBlank { "Uncategorized" },
        unit = unit.trim().ifBlank { "units" },
        reorderThreshold = reorderThreshold.toIntOrNull() ?: 5,
        reorderTargetQuantity = reorderTargetQuantity.toIntOrNull() ?: 10,
        tags = tags.trim(),
        notes = notes.trim()
    )
}
