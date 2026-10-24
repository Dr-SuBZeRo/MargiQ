package com.example.profitmargincalculator.ui.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.profitmargincalculator.data.local.CalculationEntity
import com.example.profitmargincalculator.data.preferences.IPreferencesRepository
import com.example.profitmargincalculator.data.preferences.UserPreferences
import com.example.profitmargincalculator.data.repository.CalculationRepository
import com.example.profitmargincalculator.domain.calculation.ProfitCalculationEngine
import com.example.profitmargincalculator.domain.model.CalculationInput
import com.example.profitmargincalculator.domain.model.PurchaseTaxClassification
import com.example.profitmargincalculator.domain.model.TaxPriceMode
import com.example.profitmargincalculator.domain.util.IndianCurrencyFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

class CalculatorViewModel(
    private val calculationRepository: CalculationRepository,
    private val preferencesRepository: IPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    init {
        // Initial recalculation with default reference scenario
        recalculate()
    }

    fun onProductNameChanged(name: String) {
        _uiState.update { it.copy(productName = name) }
        recalculate()
    }

    fun onPackNameChanged(name: String) {
        _uiState.update { it.copy(packName = name) }
        recalculate()
    }

    fun onUnitNameChanged(name: String) {
        _uiState.update { it.copy(unitName = name) }
        recalculate()
    }

    fun onUnitsPerPackChanged(value: String) {
        _uiState.update { it.copy(unitsPerPackText = value) }
        recalculate()
    }

    fun onPtrChanged(value: String) {
        _uiState.update { it.copy(ptrText = value) }
        recalculate()
    }

    fun onPurchaseTaxRateChanged(value: String) {
        _uiState.update { it.copy(purchaseTaxRateText = value) }
        recalculate()
    }

    fun onPurchasePriceModeChanged(mode: TaxPriceMode) {
        _uiState.update { it.copy(purchasePriceMode = mode) }
        recalculate()
    }

    fun onPurchaseTaxClassificationChanged(classification: PurchaseTaxClassification) {
        val itcEnabled = if (classification == PurchaseTaxClassification.NON_RECOVERABLE) {
            false
        } else {
            _uiState.value.isInputTaxCreditEnabled
        }
        _uiState.update {
            it.copy(
                purchaseTaxClassification = classification,
                isInputTaxCreditEnabled = itcEnabled
            )
        }
        recalculate()
    }

    fun onInputTaxCreditToggled(enabled: Boolean) {
        if (_uiState.value.purchaseTaxClassification == PurchaseTaxClassification.RECOVERABLE_GST) {
            _uiState.update { it.copy(isInputTaxCreditEnabled = enabled) }
            recalculate()
        }
    }

    fun onSellingPriceChanged(value: String) {
        _uiState.update { it.copy(sellingPriceText = value) }
        recalculate()
    }

    fun onSellingTaxRateChanged(value: String) {
        _uiState.update { it.copy(sellingTaxRateText = value) }
        recalculate()
    }

    fun onSellingPriceModeChanged(mode: TaxPriceMode) {
        _uiState.update { it.copy(sellingPriceMode = mode) }
        recalculate()
    }

    fun onQuantityChanged(value: String) {
        _uiState.update { it.copy(quantityText = value) }
        // If selling quantity matches previous purchase quantity or hasn't been decoupled, keep sync
        recalculate()
    }

    fun onSellingQuantityChanged(value: String) {
        _uiState.update { it.copy(sellingQuantityText = value) }
        recalculate()
    }

    fun setQuickPurchaseTaxRate(rate: String) {
        _uiState.update { it.copy(purchaseTaxRateText = rate) }
        recalculate()
    }

    fun setQuickSellingTaxRate(rate: String) {
        _uiState.update { it.copy(sellingTaxRateText = rate) }
        recalculate()
    }

    fun toggleCashComparison() {
        _uiState.update { it.copy(showCashComparison = !it.showCashComparison) }
    }

    fun toggleDetailedBreakdown() {
        _uiState.update { it.copy(showDetailedBreakdown = !it.showDetailedBreakdown) }
    }

    fun dismissUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun saveCalculation() {
        val currentState = _uiState.value
        if (!currentState.calculationResult.isValid) {
            _uiState.update { it.copy(userMessage = "Cannot save invalid calculation: ${currentState.calculationResult.validationMessage}") }
            return
        }

        viewModelScope.launch {
            try {
                val id = calculationRepository.saveCalculation(
                    input = currentState.calculationInput,
                    result = currentState.calculationResult,
                    existingId = currentState.currentSavedId ?: 0
                )
                _uiState.update {
                    it.copy(
                        currentSavedId = id,
                        userMessage = "Calculation saved to history"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = "Error saving calculation: ${e.message}") }
            }
        }
    }

    fun restoreExampleScenario() {
        val default = CalculationInput.defaultExample()
        _uiState.update {
            it.copy(
                productName = default.productName,
                ptrText = default.ptr.toPlainString(),
                purchaseTaxRateText = default.purchaseTaxRate.toPlainString(),
                purchasePriceMode = default.purchasePriceMode,
                purchaseTaxClassification = default.purchaseTaxClassification,
                isInputTaxCreditEnabled = default.isInputTaxCreditEnabled,
                sellingPriceText = default.sellingPrice.toPlainString(),
                sellingTaxRateText = default.sellingTaxRate.toPlainString(),
                sellingPriceMode = default.sellingPriceMode,
                quantityText = default.quantity.toString(),
                packName = default.packName,
                unitName = default.unitName,
                unitsPerPackText = default.unitsPerPack.toString(),
                sellingQuantityText = default.sellingQuantity.toString(),
                currentSavedId = null,
                userMessage = "Restored reference calculation (5 strips, 10 tablets/strip)"
            )
        }
        recalculate()
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            val prefs = preferencesRepository.userPreferencesFlow.firstOrNull() ?: UserPreferences()
            _uiState.update {
                it.copy(
                    productName = "",
                    ptrText = "0.00",
                    purchaseTaxRateText = prefs.defaultPurchaseTaxRate.toPlainString(),
                    purchasePriceMode = prefs.defaultPurchasePriceMode,
                    purchaseTaxClassification = prefs.defaultPurchaseTaxClassification,
                    isInputTaxCreditEnabled = prefs.defaultInputTaxCreditEnabled,
                    sellingPriceText = "0.00",
                    sellingTaxRateText = prefs.defaultSellingTaxRate.toPlainString(),
                    sellingPriceMode = prefs.defaultSellingPriceMode,
                    quantityText = prefs.defaultQuantity.toString(),
                    packName = "Pack",
                    unitName = "Unit",
                    unitsPerPackText = "10",
                    sellingQuantityText = prefs.defaultQuantity.toString(),
                    currentSavedId = null,
                    userMessage = "Reset to saved calculator defaults"
                )
            }
            recalculate()
        }
    }

    fun clearForm() {
        _uiState.update {
            it.copy(
                productName = "",
                ptrText = "",
                sellingPriceText = "",
                currentSavedId = null,
                userMessage = "Form cleared"
            )
        }
        recalculate()
    }

    fun loadCalculation(entity: CalculationEntity) {
        val input = entity.toCalculationInput()
        _uiState.update {
            it.copy(
                productName = input.productName,
                ptrText = input.ptr.toPlainString(),
                purchaseTaxRateText = input.purchaseTaxRate.toPlainString(),
                purchasePriceMode = input.purchasePriceMode,
                purchaseTaxClassification = input.purchaseTaxClassification,
                isInputTaxCreditEnabled = input.isInputTaxCreditEnabled,
                sellingPriceText = input.sellingPrice.toPlainString(),
                sellingTaxRateText = input.sellingTaxRate.toPlainString(),
                sellingPriceMode = input.sellingPriceMode,
                quantityText = input.quantity.toString(),
                packName = input.packName,
                unitName = input.unitName,
                unitsPerPackText = input.unitsPerPack.toString(),
                sellingQuantityText = input.sellingQuantity.toString(),
                currentSavedId = entity.id,
                userMessage = "Loaded '${entity.productName}' from history"
            )
        }
        recalculate()
    }

    private fun recalculate() {
        val state = _uiState.value
        val ptr = IndianCurrencyFormatter.parseInput(state.ptrText) ?: BigDecimal.ZERO
        val purchaseTaxRate = IndianCurrencyFormatter.parseInput(state.purchaseTaxRateText) ?: BigDecimal.ZERO
        val sellingPrice = IndianCurrencyFormatter.parseInput(state.sellingPriceText) ?: BigDecimal.ZERO
        val sellingTaxRate = IndianCurrencyFormatter.parseInput(state.sellingTaxRateText) ?: BigDecimal.ZERO
        val quantity = when {
            state.quantityText.isBlank() -> 0
            state.quantityText.toLongOrNull() != null && state.quantityText.toLong() > Int.MAX_VALUE -> Int.MAX_VALUE
            else -> state.quantityText.toIntOrNull() ?: -1
        }
        val unitsPerPack = when {
            state.unitsPerPackText.isBlank() -> 0
            state.unitsPerPackText.toLongOrNull() != null && state.unitsPerPackText.toLong() > Int.MAX_VALUE -> Int.MAX_VALUE
            else -> state.unitsPerPackText.toIntOrNull() ?: -1
        }
        val sellingQuantity = when {
            state.sellingQuantityText.isBlank() -> 0
            state.sellingQuantityText.toLongOrNull() != null && state.sellingQuantityText.toLong() > Int.MAX_VALUE -> Int.MAX_VALUE
            else -> state.sellingQuantityText.toIntOrNull() ?: -1
        }

        val input = CalculationInput(
            productName = state.productName,
            ptr = ptr,
            purchaseTaxRate = purchaseTaxRate,
            purchasePriceMode = state.purchasePriceMode,
            purchaseTaxClassification = state.purchaseTaxClassification,
            isInputTaxCreditEnabled = state.isInputTaxCreditEnabled,
            sellingPrice = sellingPrice,
            sellingTaxRate = sellingTaxRate,
            sellingPriceMode = state.sellingPriceMode,
            quantity = quantity,
            packName = state.packName.ifBlank { "Pack" },
            unitName = state.unitName.ifBlank { "Unit" },
            unitsPerPack = unitsPerPack,
            sellingQuantity = sellingQuantity
        )

        val result = ProfitCalculationEngine.calculate(input)

        _uiState.update {
            it.copy(
                calculationInput = input,
                calculationResult = result
            )
        }
    }
}
