package com.margiq.ui.compare

import com.margiq.domain.comparison.ProductComparisonOutcome
import com.margiq.domain.model.CalculationInput
import com.margiq.domain.model.PurchaseTaxClassification
import com.margiq.domain.model.TaxPriceMode
import com.margiq.domain.util.IndianCurrencyFormatter
import java.math.BigDecimal

enum class CompareTab { PRODUCTS, SUPPLIERS }

data class CompareSideState(
    val productId: Long? = null,
    val productName: String = "",
    val supplierName: String = "",
    val ptrText: String = "48.00",
    val purchaseTaxRateText: String = "5.00",
    val purchasePriceMode: TaxPriceMode = TaxPriceMode.TAX_EXCLUSIVE,
    val purchaseTaxClassification: PurchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
    val isInputTaxCreditEnabled: Boolean = true,
    val sellingPriceText: String = "135.00",
    val sellingTaxRateText: String = "5.00",
    val sellingPriceMode: TaxPriceMode = TaxPriceMode.TAX_INCLUSIVE,
    val unitsPerPackText: String = "10",
    val packName: String = "Strip",
    val unitName: String = "Tablet",
    val isSupplierDiscountEnabled: Boolean = false,
    val supplierDiscountText: String = "0.00",
    val isSupplierOfferEnabled: Boolean = false,
    val offerBuyQuantityText: String = "1",
    val offerFreeQuantityText: String = "0"
) {
    fun toCalculationInput(): CalculationInput {
        val ptr = IndianCurrencyFormatter.parseInput(ptrText) ?: BigDecimal.ZERO
        val pTax = IndianCurrencyFormatter.parseInput(purchaseTaxRateText) ?: BigDecimal.ZERO
        val sPrice = IndianCurrencyFormatter.parseInput(sellingPriceText) ?: BigDecimal.ZERO
        val sTax = IndianCurrencyFormatter.parseInput(sellingTaxRateText) ?: BigDecimal.ZERO
        val discount = IndianCurrencyFormatter.parseInput(supplierDiscountText) ?: BigDecimal.ZERO
        val units = unitsPerPackText.toIntOrNull() ?: 1
        val buy = offerBuyQuantityText.toIntOrNull() ?: 1
        val free = offerFreeQuantityText.toIntOrNull() ?: 0

        return CalculationInput(
            productName = productName.ifBlank { "Side Product" },
            supplierName = supplierName,
            ptr = ptr,
            purchaseTaxRate = pTax,
            purchasePriceMode = purchasePriceMode,
            purchaseTaxClassification = purchaseTaxClassification,
            isInputTaxCreditEnabled = isInputTaxCreditEnabled,
            sellingPrice = sPrice,
            sellingTaxRate = sTax,
            sellingPriceMode = sellingPriceMode,
            quantity = 1,
            packName = packName.ifBlank { "Pack" },
            unitName = unitName.ifBlank { "Unit" },
            unitsPerPack = if (units > 0) units else 1,
            isSupplierDiscountEnabled = isSupplierDiscountEnabled,
            supplierDiscountPercent = discount,
            isSupplierOfferEnabled = isSupplierOfferEnabled,
            offerBuyQuantity = if (buy > 0) buy else 1,
            offerFreeQuantity = if (free >= 0) free else 0
        )
    }
}

data class SupplierComparisonBaseProduct(
    val productId: Long? = null,
    val productName: String = "Medicine X",
    val sellingPriceText: String = "150.00",
    val sellingTaxRateText: String = "5.00",
    val sellingPriceMode: TaxPriceMode = TaxPriceMode.TAX_INCLUSIVE,
    val unitsPerPackText: String = "10",
    val packName: String = "Pack",
    val unitName: String = "Unit"
)

data class SupplierOfferSide(
    val supplierName: String = "",
    val ptrText: String = "100.00",
    val purchaseTaxRateText: String = "5.00",
    val purchasePriceMode: TaxPriceMode = TaxPriceMode.TAX_EXCLUSIVE,
    val purchaseTaxClassification: PurchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
    val isInputTaxCreditEnabled: Boolean = true,
    val unitsPerPackText: String = "10",
    val isSupplierDiscountEnabled: Boolean = false,
    val supplierDiscountText: String = "0.00",
    val isSupplierOfferEnabled: Boolean = false,
    val offerBuyQuantityText: String = "10",
    val offerFreeQuantityText: String = "1"
) {
    fun toCalculationInput(base: SupplierComparisonBaseProduct): CalculationInput {
        val ptr = IndianCurrencyFormatter.parseInput(ptrText) ?: BigDecimal.ZERO
        val pTax = IndianCurrencyFormatter.parseInput(purchaseTaxRateText) ?: BigDecimal.ZERO
        val sPrice = IndianCurrencyFormatter.parseInput(base.sellingPriceText) ?: BigDecimal.ZERO
        val sTax = IndianCurrencyFormatter.parseInput(base.sellingTaxRateText) ?: BigDecimal.ZERO
        val discount = IndianCurrencyFormatter.parseInput(supplierDiscountText) ?: BigDecimal.ZERO
        val units = unitsPerPackText.toIntOrNull() ?: (base.unitsPerPackText.toIntOrNull() ?: 1)
        val buy = offerBuyQuantityText.toIntOrNull() ?: 1
        val free = offerFreeQuantityText.toIntOrNull() ?: 0

        return CalculationInput(
            productName = base.productName.ifBlank { "Base Product" },
            supplierName = supplierName,
            ptr = ptr,
            purchaseTaxRate = pTax,
            purchasePriceMode = purchasePriceMode,
            purchaseTaxClassification = purchaseTaxClassification,
            isInputTaxCreditEnabled = isInputTaxCreditEnabled,
            sellingPrice = sPrice,
            sellingTaxRate = sTax,
            sellingPriceMode = base.sellingPriceMode,
            quantity = if (isSupplierOfferEnabled && buy > 0) buy else 1,
            packName = base.packName.ifBlank { "Pack" },
            unitName = base.unitName.ifBlank { "Unit" },
            unitsPerPack = if (units > 0) units else 1,
            isSupplierDiscountEnabled = isSupplierDiscountEnabled,
            supplierDiscountPercent = discount,
            isSupplierOfferEnabled = isSupplierOfferEnabled,
            offerBuyQuantity = if (buy > 0) buy else 1,
            offerFreeQuantity = if (free >= 0) free else 0
        )
    }
}

data class CompareUiState(
    val activeTab: CompareTab = CompareTab.PRODUCTS,
    val productSideA: CompareSideState = CompareSideState(productName = "Product A", supplierName = "Supplier 1", ptrText = "48.00", sellingPriceText = "135.00", unitsPerPackText = "10"),
    val productSideB: CompareSideState = CompareSideState(productName = "Product B", supplierName = "Supplier 2", ptrText = "42.00", sellingPriceText = "135.00", unitsPerPackText = "10"),
    val productsComparison: ProductComparisonOutcome? = null,

    val supplierBaseProduct: SupplierComparisonBaseProduct = SupplierComparisonBaseProduct(),
    val supplierSideA: SupplierOfferSide = SupplierOfferSide(supplierName = "Supplier Alpha", ptrText = "95.00", isSupplierDiscountEnabled = true, supplierDiscountText = "5.00"),
    val supplierSideB: SupplierOfferSide = SupplierOfferSide(supplierName = "Supplier Beta", ptrText = "100.00", isSupplierOfferEnabled = true, offerBuyQuantityText = "10", offerFreeQuantityText = "2"),
    val suppliersComparison: ProductComparisonOutcome? = null,

    val userMessage: String? = null
)
