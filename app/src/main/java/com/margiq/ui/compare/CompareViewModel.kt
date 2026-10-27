package com.margiq.ui.compare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.margiq.data.local.ProductEntity
import com.margiq.data.repository.ProductRepository
import com.margiq.domain.comparison.ComparisonEngine
import com.margiq.domain.model.PurchaseTaxClassification
import com.margiq.domain.model.TaxPriceMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CompareViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

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

    fun loadProductIntoSideA(product: ProductEntity) {
        _uiState.update {
            it.copy(
                productSideA = it.productSideA.copy(
                    productId = product.id,
                    productName = product.productName,
                    supplierName = product.supplierName,
                    ptrText = product.ptr,
                    purchaseTaxRateText = product.purchaseTaxRate,
                    purchasePriceMode = try { TaxPriceMode.valueOf(product.purchasePriceMode) } catch (_: Exception) { TaxPriceMode.TAX_EXCLUSIVE },
                    purchaseTaxClassification = try { PurchaseTaxClassification.valueOf(product.purchaseTaxClassification) } catch (_: Exception) { PurchaseTaxClassification.RECOVERABLE_GST },
                    isInputTaxCreditEnabled = product.isInputTaxCreditEnabled,
                    sellingPriceText = product.sellingPrice,
                    sellingTaxRateText = product.sellingTaxRate,
                    sellingPriceMode = try { TaxPriceMode.valueOf(product.sellingPriceMode) } catch (_: Exception) { TaxPriceMode.TAX_INCLUSIVE },
                    unitsPerPackText = product.unitsPerPack.toString(),
                    packName = product.packName,
                    unitName = product.unitName,
                    isSupplierDiscountEnabled = product.isSupplierDiscountEnabled,
                    supplierDiscountText = product.supplierDiscountPercent,
                    isSupplierOfferEnabled = product.isSupplierOfferEnabled,
                    offerBuyQuantityText = product.offerBuyQuantity.toString(),
                    offerFreeQuantityText = product.offerFreeQuantity.toString()
                ),
                userMessage = "Loaded '${product.productName}' into Side A"
            )
        }
        recalculateProductsComparison()
    }

    fun loadProductIntoSideB(product: ProductEntity) {
        _uiState.update {
            it.copy(
                productSideB = it.productSideB.copy(
                    productId = product.id,
                    productName = product.productName,
                    supplierName = product.supplierName,
                    ptrText = product.ptr,
                    purchaseTaxRateText = product.purchaseTaxRate,
                    purchasePriceMode = try { TaxPriceMode.valueOf(product.purchasePriceMode) } catch (_: Exception) { TaxPriceMode.TAX_EXCLUSIVE },
                    purchaseTaxClassification = try { PurchaseTaxClassification.valueOf(product.purchaseTaxClassification) } catch (_: Exception) { PurchaseTaxClassification.RECOVERABLE_GST },
                    isInputTaxCreditEnabled = product.isInputTaxCreditEnabled,
                    sellingPriceText = product.sellingPrice,
                    sellingTaxRateText = product.sellingTaxRate,
                    sellingPriceMode = try { TaxPriceMode.valueOf(product.sellingPriceMode) } catch (_: Exception) { TaxPriceMode.TAX_INCLUSIVE },
                    unitsPerPackText = product.unitsPerPack.toString(),
                    packName = product.packName,
                    unitName = product.unitName,
                    isSupplierDiscountEnabled = product.isSupplierDiscountEnabled,
                    supplierDiscountText = product.supplierDiscountPercent,
                    isSupplierOfferEnabled = product.isSupplierOfferEnabled,
                    offerBuyQuantityText = product.offerBuyQuantity.toString(),
                    offerFreeQuantityText = product.offerFreeQuantity.toString()
                ),
                userMessage = "Loaded '${product.productName}' into Side B"
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

    fun saveProductFromSide(isSideA: Boolean) {
        val side = if (isSideA) _uiState.value.productSideA else _uiState.value.productSideB
        if (side.productName.isBlank()) {
            _uiState.update { it.copy(userMessage = "Please enter a product name before saving") }
            return
        }

        val entity = ProductEntity.fromCalculationInput(
            input = side.toCalculationInput(),
            id = side.productId ?: 0
        )

        viewModelScope.launch {
            try {
                val savedId = productRepository.saveOrUpdateProduct(entity)
                _uiState.update {
                    if (isSideA) {
                        it.copy(
                            productSideA = it.productSideA.copy(productId = savedId),
                            userMessage = "Saved '${side.productName}' to Product Library"
                        )
                    } else {
                        it.copy(
                            productSideB = it.productSideB.copy(productId = savedId),
                            userMessage = "Saved '${side.productName}' to Product Library"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = "Error saving product: ${e.message}") }
            }
        }
    }

    // --- Supplier Comparison Controls ---

    fun loadProductIntoSupplierBase(product: ProductEntity) {
        _uiState.update {
            it.copy(
                supplierBaseProduct = it.supplierBaseProduct.copy(
                    productId = product.id,
                    productName = product.productName,
                    sellingPriceText = product.sellingPrice,
                    sellingTaxRateText = product.sellingTaxRate,
                    sellingPriceMode = try { TaxPriceMode.valueOf(product.sellingPriceMode) } catch (_: Exception) { TaxPriceMode.TAX_INCLUSIVE },
                    unitsPerPackText = product.unitsPerPack.toString(),
                    packName = product.packName,
                    unitName = product.unitName
                ),
                userMessage = "Loaded '${product.productName}' as base product"
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

    fun saveSupplierOfferAsProduct(isSupplierA: Boolean) {
        val base = _uiState.value.supplierBaseProduct
        val offer = if (isSupplierA) _uiState.value.supplierSideA else _uiState.value.supplierSideB

        if (base.productName.isBlank()) {
            _uiState.update { it.copy(userMessage = "Please specify a product name") }
            return
        }

        val input = offer.toCalculationInput(base)
        val entity = ProductEntity.fromCalculationInput(input = input)

        viewModelScope.launch {
            try {
                productRepository.saveOrUpdateProduct(entity)
                _uiState.update {
                    it.copy(userMessage = "Saved '${base.productName}' (${offer.supplierName}) to Product Library")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = "Error saving offer: ${e.message}") }
            }
        }
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
