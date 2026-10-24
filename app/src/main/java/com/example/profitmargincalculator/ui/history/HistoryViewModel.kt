package com.example.profitmargincalculator.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.profitmargincalculator.data.local.CalculationEntity
import com.example.profitmargincalculator.data.repository.CalculationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private data class HistoryFilterState(
    val searchQuery: String = "",
    val isSortAscending: Boolean = false,
    val selectedForDelete: CalculationEntity? = null,
    val showClearAllConfirmDialog: Boolean = false,
    val userMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val calculationRepository: CalculationRepository
) : ViewModel() {

    private val _filterState = MutableStateFlow(HistoryFilterState())

    val uiState: StateFlow<HistoryUiState> = combine(
        _filterState.flatMapLatest { filter ->
            if (filter.searchQuery.isBlank()) {
                calculationRepository.getAllCalculations()
            } else {
                calculationRepository.searchCalculations(filter.searchQuery.trim())
            }
        },
        _filterState
    ) { calculations: List<CalculationEntity>, filter: HistoryFilterState ->
        val sortedList = if (filter.isSortAscending) {
            calculations.sortedBy { it.timestamp }
        } else {
            calculations.sortedByDescending { it.timestamp }
        }

        HistoryUiState(
            calculations = sortedList,
            searchQuery = filter.searchQuery,
            isSortAscending = filter.isSortAscending,
            selectedForDelete = filter.selectedForDelete,
            showClearAllConfirmDialog = filter.showClearAllConfirmDialog,
            userMessage = filter.userMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _filterState.update { it.copy(searchQuery = query) }
    }

    fun toggleSortOrder() {
        _filterState.update { it.copy(isSortAscending = !it.isSortAscending) }
    }

    fun requestDelete(entity: CalculationEntity) {
        _filterState.update { it.copy(selectedForDelete = entity) }
    }

    fun cancelDelete() {
        _filterState.update { it.copy(selectedForDelete = null) }
    }

    fun confirmDelete() {
        val entity = _filterState.value.selectedForDelete ?: return
        viewModelScope.launch {
            try {
                calculationRepository.deleteCalculation(entity)
                _filterState.update {
                    it.copy(
                        selectedForDelete = null,
                        userMessage = "Deleted '${entity.productName}'"
                    )
                }
            } catch (e: Exception) {
                _filterState.update { it.copy(userMessage = "Error deleting calculation: ${e.message}") }
            }
        }
    }

    fun requestClearAll() {
        _filterState.update { it.copy(showClearAllConfirmDialog = true) }
    }

    fun cancelClearAll() {
        _filterState.update { it.copy(showClearAllConfirmDialog = false) }
    }

    fun confirmClearAll() {
        viewModelScope.launch {
            try {
                calculationRepository.clearAll()
                _filterState.update {
                    it.copy(
                        showClearAllConfirmDialog = false,
                        userMessage = "All calculation history cleared"
                    )
                }
            } catch (e: Exception) {
                _filterState.update { it.copy(userMessage = "Error clearing history: ${e.message}") }
            }
        }
    }

    fun duplicateCalculation(entity: CalculationEntity) {
        viewModelScope.launch {
            try {
                val duplicated = entity.copy(
                    id = 0,
                    productName = "${entity.productName} (Copy)",
                    timestamp = System.currentTimeMillis()
                )
                calculationRepository.saveCalculation(
                    input = duplicated.toCalculationInput(),
                    result = com.example.profitmargincalculator.domain.calculation.ProfitCalculationEngine.calculate(duplicated.toCalculationInput())
                )
                _filterState.update { it.copy(userMessage = "Duplicated '${entity.productName}'") }
            } catch (e: Exception) {
                _filterState.update { it.copy(userMessage = "Error duplicating: ${e.message}") }
            }
        }
    }

    fun dismissUserMessage() {
        _filterState.update { it.copy(userMessage = null) }
    }
}
