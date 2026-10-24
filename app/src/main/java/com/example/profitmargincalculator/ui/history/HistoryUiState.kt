package com.example.profitmargincalculator.ui.history

import com.example.profitmargincalculator.data.local.CalculationEntity

data class HistoryUiState(
    val calculations: List<CalculationEntity> = emptyList(),
    val searchQuery: String = "",
    val isSortAscending: Boolean = false, // false = newest first, true = oldest first
    val selectedForDelete: CalculationEntity? = null,
    val showClearAllConfirmDialog: Boolean = false,
    val userMessage: String? = null
)
