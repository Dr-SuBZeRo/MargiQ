package com.margiq.ui.compare

import androidx.lifecycle.ViewModel
import com.margiq.data.local.CalculationEntity
import com.margiq.domain.comparison.ComparisonEngine
import com.margiq.domain.model.PurchaseTaxClassification
import com.margiq.domain.model.TaxPriceMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CompareViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CompareUiState())
    val uiState: StateFlow<CompareUiState> = _uiState.asStateFlow()

    init {
        recalculateProductsComparison()
        recalculateSuppliersComparison()
    }

    fun selectTab(tab: CompareTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    // --- Product Comparison Controls ---

    fun loadCalculationIntoSideA(entity: CalculationEntity) {
        _uiState.update {
            it.copy(
                productSideA = it.productSideA.copy(
                    productName = entity.productName,
                    supplierName = entity.supplierName,
                    ptrText = entity.ptr,
                    purchaseTaxRateText = entity.purchaseTaxRate,
                    purchasePriceMode = try { TaxPriceMode.valueOf(entity.purchasePriceMode) } catch (_: Exception) { TaxPriceMode.TAX_EXCLUSIVE },
                    purchaseTaxClassification = try { PurchaseTaxClassification.valueOf(entity.purchaseTaxClassification) } catch (_: Exception) { PurchaseTaxClassification.RECOVERABLE_GST },
                    isInputTaxCreditEnabled = entity.isInputTaxCreditEnabled,
                    sellingPriceText = entity.sellingPrice,
                    sellingTaxRateText = entity.sellingTaxRate,
                    sellingPriceMode = try { TaxPriceMode.valueOf(entity.sellingPriceMode) } catch (_: Exception) { TaxPriceMode.TAX_INCLUSIVE },
                    unitsPerPackText = entity.unitsPerPack.toString(),
                    packName = entity.packName,
                    unitName = entity.unitName,
                    isSupplierDiscountEnabled = entity.isSupplierDiscountEnabled,
                    supplierDiscountText = entity.supplierDiscountPercent,
                    isSupplierOfferEnabled = entity.isSupplierOfferEnabled,
                    offerBuyQuantityText = entity.offerBuyQuantity.toString(),
                    offerFreeQuantityText = entity.offerFreeQuantity.toString()
                ),
                userMessage = "Loaded '${entity.productName}' into Side A"
            )
        }
        recalculateProductsComparison()
    }

    fun loadCalculationIntoSideB(entity: CalculationEntity) {
        _uiState.update {
            it.copy(
                productSideB = it.productSideB.copy(
                    productName = entity.productName,
                    supplierName = entity.supplierName,
                    ptrText = entity.ptr,
                    purchaseTaxRateText = entity.purchaseTaxRate,
                    purchasePriceMode = try { TaxPriceMode.valueOf(entity.purchasePriceMode) } catch (_: Exception) { TaxPriceMode.TAX_EXCLUSIVE },
                    purchaseTaxClassification = try { PurchaseTaxClassification.valueOf(entity.purchaseTaxClassification) } catch (_: Exception) { PurchaseTaxClassification.RECOVERABLE_GST },
                    isInputTaxCreditEnabled = entity.isInputTaxCreditEnabled,
                    sellingPriceText = entity.sellingPrice,
                    sellingTaxRateText = entity.sellingTaxRate,
                    sellingPriceMode = try { TaxPriceMode.valueOf(entity.sellingPriceMode) } catch (_: Exception) { TaxPriceMode.TAX_INCLUSIVE },
                    unitsPerPackText = entity.unitsPerPack.toString(),
                    packName = entity.packName,
                    unitName = entity.unitName,
                    isSupplierDiscountEnabled = entity.isSupplierDiscountEnabled,
                    supplierDiscountText = entity.supplierDiscountPercent,
                    isSupplierOfferEnabled = entity.isSupplierOfferEnabled,
                    offerBuyQuantityText = entity.offerBuyQuantity.toString(),
                    offerFreeQuantityText = entity.offerFreeQuantity.toString()
                ),
                userMessage = "Loaded '${entity.productName}' into Side B"
            )
        }
        recalculateProductsComparison()
    }

    fun updateSideA(modifier: (CompareSideState) -> CompareSideState) {
        _uiState.update { it.copy(productSideA = modifier(it.productSideA)) }
        recalculateProductsComparison()
    }

    fun updateSideB(modifier: (CompareSideState) -> CompareSideState) {
        _uiState.update { it.copy(productSideB = modifier(it.productSideB)) }
        recalculateProductsComparison()
    }

    // --- Supplier Comparison Controls ---

    fun loadCalculationIntoSupplierBase(entity: CalculationEntity) {
        _uiState.update {
            it.copy(
                supplierBaseProduct = it.supplierBaseProduct.copy(
                    productName = entity.productName,
                    sellingPriceText = entity.sellingPrice,
                    sellingTaxRateText = entity.sellingTaxRate,
                    sellingPriceMode = try { TaxPriceMode.valueOf(entity.sellingPriceMode) } catch (_: Exception) { TaxPriceMode.TAX_INCLUSIVE },
                    unitsPerPackText = entity.unitsPerPack.toString(),
                    packName = entity.packName,
                    unitName = entity.unitName
                ),
                userMessage = "Loaded '${entity.productName}' as base product"
            )
        }
        recalculateSuppliersComparison()
    }

    fun loadCalculationIntoSupplierOfferA(entity: CalculationEntity) {
        _uiState.update {
            it.copy(
                supplierSideA = it.supplierSideA.copy(
                    supplierName = entity.supplierName.ifBlank { entity.productName },
                    ptrText = entity.ptr,
                    purchaseTaxRateText = entity.purchaseTaxRate,
                    purchasePriceMode = try { TaxPriceMode.valueOf(entity.purchasePriceMode) } catch (_: Exception) { TaxPriceMode.TAX_EXCLUSIVE },
                    purchaseTaxClassification = try { PurchaseTaxClassification.valueOf(entity.purchaseTaxClassification) } catch (_: Exception) { PurchaseTaxClassification.RECOVERABLE_GST },
                    isInputTaxCreditEnabled = entity.isInputTaxCreditEnabled,
                    overrideSellingTax = true,
                    sellingTaxRateText = entity.sellingTaxRate,
                    sellingPriceMode = try { TaxPriceMode.valueOf(entity.sellingPriceMode) } catch (_: Exception) { TaxPriceMode.TAX_INCLUSIVE },
                    isSupplierDiscountEnabled = entity.isSupplierDiscountEnabled,
                    supplierDiscountText = entity.supplierDiscountPercent,
                    isSupplierOfferEnabled = entity.isSupplierOfferEnabled,
                    offerBuyQuantityText = entity.offerBuyQuantity.toString(),
                    offerFreeQuantityText = entity.offerFreeQuantity.toString()
                ),
                userMessage = "Loaded '${entity.productName}' into Offer A"
            )
        }
        recalculateSuppliersComparison()
    }

    fun loadCalculationIntoSupplierOfferB(entity: CalculationEntity) {
        _uiState.update {
            it.copy(
                supplierSideB = it.supplierSideB.copy(
                    supplierName = entity.supplierName.ifBlank { entity.productName },
                    ptrText = entity.ptr,
                    purchaseTaxRateText = entity.purchaseTaxRate,
                    purchasePriceMode = try { TaxPriceMode.valueOf(entity.purchasePriceMode) } catch (_: Exception) { TaxPriceMode.TAX_EXCLUSIVE },
                    purchaseTaxClassification = try { PurchaseTaxClassification.valueOf(entity.purchaseTaxClassification) } catch (_: Exception) { PurchaseTaxClassification.RECOVERABLE_GST },
                    isInputTaxCreditEnabled = entity.isInputTaxCreditEnabled,
                    overrideSellingTax = true,
                    sellingTaxRateText = entity.sellingTaxRate,
                    sellingPriceMode = try { TaxPriceMode.valueOf(entity.sellingPriceMode) } catch (_: Exception) { TaxPriceMode.TAX_INCLUSIVE },
                    isSupplierDiscountEnabled = entity.isSupplierDiscountEnabled,
                    supplierDiscountText = entity.supplierDiscountPercent,
                    isSupplierOfferEnabled = entity.isSupplierOfferEnabled,
                    offerBuyQuantityText = entity.offerBuyQuantity.toString(),
                    offerFreeQuantityText = entity.offerFreeQuantity.toString()
                ),
                userMessage = "Loaded '${entity.productName}' into Offer B"
            )
        }
        recalculateSuppliersComparison()
    }

    fun updateSupplierBase(modifier: (SupplierComparisonBaseProduct) -> SupplierComparisonBaseProduct) {
        _uiState.update { it.copy(supplierBaseProduct = modifier(it.supplierBaseProduct)) }
        recalculateSuppliersComparison()
    }

    fun updateSupplierA(modifier: (SupplierOfferSide) -> SupplierOfferSide) {
        _uiState.update { it.copy(supplierSideA = modifier(it.supplierSideA)) }
        recalculateSuppliersComparison()
    }

    fun updateSupplierB(modifier: (SupplierOfferSide) -> SupplierOfferSide) {
        _uiState.update { it.copy(supplierSideB = modifier(it.supplierSideB)) }
        recalculateSuppliersComparison()
    }

    fun dismissUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    private fun recalculateProductsComparison() {
        val state = _uiState.value
        val inputA = state.productSideA.toCalculationInput()
        val inputB = state.productSideB.toCalculationInput()
        val outcome = ComparisonEngine.compare(inputA, inputB)
        _uiState.update { it.copy(productsComparison = outcome) }
    }

    private fun recalculateSuppliersComparison() {
        val state = _uiState.value
        val base = state.supplierBaseProduct
        val inputA = state.supplierSideA.toCalculationInput(base)
        val inputB = state.supplierSideB.toCalculationInput(base)
        val outcome = ComparisonEngine.compare(inputA, inputB)
        _uiState.update { it.copy(suppliersComparison = outcome) }
    }
}
