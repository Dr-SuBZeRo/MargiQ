package com.margiq.ui.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.margiq.data.local.CalculationEntity
import com.margiq.data.preferences.IPreferencesRepository
import com.margiq.data.preferences.UserPreferences
import com.margiq.data.repository.CalculationRepository
import com.margiq.domain.calculation.ProfitCalculationEngine
import com.margiq.domain.model.CalculationInput
import com.margiq.domain.model.PurchaseTaxClassification
import com.margiq.domain.model.TaxPriceMode
import com.margiq.domain.util.IndianCurrencyFormatter
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

    fun onSupplierNameChanged(name: String) {
        _uiState.update { it.copy(supplierName = name) }
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

    // --- Supplier Offers & Discounts (v1.2.0 & v2.0.1) ---
    fun onSupplierDiscountToggled(enabled: Boolean) {
        _uiState.update { it.copy(isSupplierDiscountEnabled = enabled) }
        recalculate()
    }

    fun onSupplierOfferToggled(enabled: Boolean) {
        _uiState.update { it.copy(isSupplierOfferEnabled = enabled) }
        recalculate()
    }

    fun onOfferBuyQuantityChanged(value: String) {
        _uiState.update { it.copy(offerBuyQuantityText = value) }
        recalculate()
    }

    fun onOfferFreeQuantityChanged(value: String) {
        _uiState.update { it.copy(offerFreeQuantityText = value) }
        recalculate()
    }

    fun onSupplierDiscountChanged(value: String) {
        _uiState.update { it.copy(supplierDiscountText = value) }
        recalculate()
    }

    fun setQuickSupplierDiscount(discountPercent: String) {
        _uiState.update {
            it.copy(
                isSupplierDiscountEnabled = true,
                supplierDiscountText = discountPercent
            )
        }
        recalculate()
    }

    fun setQuickOffer(buyQty: String, freeQty: String) {
        _uiState.update {
            it.copy(
                isSupplierOfferEnabled = true,
                offerBuyQuantityText = buyQty,
                offerFreeQuantityText = freeQty
            )
        }
        recalculate()
    }

    // --- Selling Side ---
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
        recalculate()
    }

    // Deprecated / Backwards-compatibility stubs
    fun onPackNameChanged(name: String) {
        recalculate()
    }

    fun onUnitNameChanged(name: String) {
        recalculate()
    }

    fun onSellingQuantityChanged(value: String) {
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
                supplierName = "",
                ptrText = default.ptr.toPlainString(),
                purchaseTaxRateText = default.purchaseTaxRate.toPlainString(),
                purchasePriceMode = default.purchasePriceMode,
                purchaseTaxClassification = default.purchaseTaxClassification,
                isInputTaxCreditEnabled = default.isInputTaxCreditEnabled,
                sellingPriceText = default.sellingPrice.toPlainString(),
                sellingTaxRateText = default.sellingTaxRate.toPlainString(),
                sellingPriceMode = default.sellingPriceMode,
                quantityText = default.quantity.toString(),
                unitsPerPackText = default.unitsPerPack.toString(),
                isSupplierDiscountEnabled = false,
                supplierDiscountText = "0.00",
                isSupplierOfferEnabled = false,
                offerBuyQuantityText = "1",
                offerFreeQuantityText = "1",
                currentSavedId = null,
                userMessage = "Restored reference calculation (5 packs, 10 units/pack)"
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
                    supplierName = "",
                    ptrText = "0.00",
                    purchaseTaxRateText = prefs.defaultPurchaseTaxRate.toPlainString(),
                    purchasePriceMode = prefs.defaultPurchasePriceMode,
                    purchaseTaxClassification = prefs.defaultPurchaseTaxClassification,
                    isInputTaxCreditEnabled = prefs.defaultInputTaxCreditEnabled,
                    sellingPriceText = "0.00",
                    sellingTaxRateText = prefs.defaultSellingTaxRate.toPlainString(),
                    sellingPriceMode = prefs.defaultSellingPriceMode,
                    quantityText = prefs.defaultQuantity.toString(),
                    unitsPerPackText = "10",
                    isSupplierDiscountEnabled = false,
                    supplierDiscountText = "0.00",
                    isSupplierOfferEnabled = false,
                    offerBuyQuantityText = "1",
                    offerFreeQuantityText = "1",
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
                supplierName = "",
                ptrText = "",
                sellingPriceText = "",
                isSupplierDiscountEnabled = false,
                supplierDiscountText = "0.00",
                isSupplierOfferEnabled = false,
                currentSavedId = null,
                userMessage = "Form cleared"
            )
        }
        recalculate()
    }

    fun loadCalculation(entity: CalculationEntity) {
        val input = entity.toCalculationInput()
        val isDiscountOn = entity.isSupplierDiscountEnabled || (try { BigDecimal(entity.supplierDiscountPercent) > BigDecimal.ZERO } catch (_: Exception) { false })
        _uiState.update {
            it.copy(
                productName = input.productName,
                supplierName = entity.supplierName,
                ptrText = input.ptr.toPlainString(),
                purchaseTaxRateText = input.purchaseTaxRate.toPlainString(),
                purchasePriceMode = input.purchasePriceMode,
                purchaseTaxClassification = input.purchaseTaxClassification,
                isInputTaxCreditEnabled = input.isInputTaxCreditEnabled,
                sellingPriceText = input.sellingPrice.toPlainString(),
                sellingTaxRateText = input.sellingTaxRate.toPlainString(),
                sellingPriceMode = input.sellingPriceMode,
                quantityText = input.quantity.toString(),
                unitsPerPackText = input.unitsPerPack.toString(),
                isSupplierDiscountEnabled = isDiscountOn,
                supplierDiscountText = entity.supplierDiscountPercent,
                isSupplierOfferEnabled = entity.isSupplierOfferEnabled,
                offerBuyQuantityText = entity.offerBuyQuantity.toString(),
                offerFreeQuantityText = entity.offerFreeQuantity.toString(),
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
        val supplierDiscount = IndianCurrencyFormatter.parseInput(state.supplierDiscountText) ?: BigDecimal.ZERO

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
        val offerBuyQty = when {
            state.offerBuyQuantityText.isBlank() -> 0
            state.offerBuyQuantityText.toLongOrNull() != null && state.offerBuyQuantityText.toLong() > Int.MAX_VALUE -> Int.MAX_VALUE
            else -> state.offerBuyQuantityText.toIntOrNull() ?: -1
        }
        val offerFreeQty = when {
            state.offerFreeQuantityText.isBlank() -> 0
            state.offerFreeQuantityText.toLongOrNull() != null && state.offerFreeQuantityText.toLong() > Int.MAX_VALUE -> Int.MAX_VALUE
            else -> state.offerFreeQuantityText.toIntOrNull() ?: -1
        }

        val input = CalculationInput(
            productName = state.productName,
            supplierName = state.supplierName,
            ptr = ptr,
            purchaseTaxRate = purchaseTaxRate,
            purchasePriceMode = state.purchasePriceMode,
            purchaseTaxClassification = state.purchaseTaxClassification,
            isInputTaxCreditEnabled = state.isInputTaxCreditEnabled,
            sellingPrice = sellingPrice,
            sellingTaxRate = sellingTaxRate,
            sellingPriceMode = state.sellingPriceMode,
            quantity = quantity,
            unitsPerPack = unitsPerPack,
            isSupplierDiscountEnabled = state.isSupplierDiscountEnabled,
            supplierDiscountPercent = supplierDiscount,
            isSupplierOfferEnabled = state.isSupplierOfferEnabled,
            offerBuyQuantity = offerBuyQty,
            offerFreeQuantity = offerFreeQty
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
