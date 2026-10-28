package com.margiq.ui.compare

import com.margiq.data.local.CalculationEntity
import com.margiq.domain.comparison.ProductComparisonOutcome
import com.margiq.domain.model.CalculationInput
import com.margiq.domain.model.PurchaseTaxClassification
import com.margiq.domain.model.TaxPriceMode
import com.margiq.domain.util.IndianCurrencyFormatter
import java.math.BigDecimal

enum class CompareTab { PRODUCTS, SUPPLIERS }

data class CompareSideState(
    val calculationId: Long? = null,
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

    companion object {
        fun fromCalculationEntity(entity: CalculationEntity): CompareSideState {
            val input = entity.toCalculationInput()
            return CompareSideState(
                calculationId = entity.id,
                productName = entity.productName,
                supplierName = entity.supplierName,
                ptrText = input.ptr.toPlainString(),
                purchaseTaxRateText = input.purchaseTaxRate.toPlainString(),
                purchasePriceMode = input.purchasePriceMode,
                purchaseTaxClassification = input.purchaseTaxClassification,
                isInputTaxCreditEnabled = input.isInputTaxCreditEnabled,
                sellingPriceText = input.sellingPrice.toPlainString(),
                sellingTaxRateText = input.sellingTaxRate.toPlainString(),
                sellingPriceMode = input.sellingPriceMode,
                unitsPerPackText = input.unitsPerPack.toString(),
                packName = entity.packName,
                unitName = entity.unitName,
                isSupplierDiscountEnabled = input.isSupplierDiscountEnabled,
                supplierDiscountText = input.supplierDiscountPercent.toPlainString(),
                isSupplierOfferEnabled = input.isSupplierOfferEnabled,
                offerBuyQuantityText = input.offerBuyQuantity.toString(),
                offerFreeQuantityText = input.offerFreeQuantity.toString()
            )
        }
    }
}

data class SupplierComparisonBaseProduct(
    val calculationId: Long? = null,
    val productName: String = "Medicine X",
    val sellingPriceText: String = "150.00",
    val sellingTaxRateText: String = "5.00",
    val sellingPriceMode: TaxPriceMode = TaxPriceMode.TAX_INCLUSIVE,
    val unitsPerPackText: String = "10",
    val packName: String = "Pack",
    val unitName: String = "Unit"
) {
    companion object {
        fun fromCalculationEntity(entity: CalculationEntity): SupplierComparisonBaseProduct {
            val input = entity.toCalculationInput()
            return SupplierComparisonBaseProduct(
                calculationId = entity.id,
                productName = entity.productName,
                sellingPriceText = input.sellingPrice.toPlainString(),
                sellingTaxRateText = input.sellingTaxRate.toPlainString(),
                sellingPriceMode = input.sellingPriceMode,
                unitsPerPackText = input.unitsPerPack.toString(),
                packName = entity.packName,
                unitName = entity.unitName
            )
        }
    }
}

data class SupplierOfferSide(
    val supplierName: String = "",
    val ptrText: String = "100.00",
    val purchaseTaxRateText: String = "5.00",
    val purchasePriceMode: TaxPriceMode = TaxPriceMode.TAX_EXCLUSIVE,
    val purchaseTaxClassification: PurchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
    val isInputTaxCreditEnabled: Boolean = true,
    // Per-supplier selling tax configuration overrides (or defaults from base)
    val overrideSellingTax: Boolean = false,
    val sellingTaxRateText: String = "5.00",
    val sellingPriceMode: TaxPriceMode = TaxPriceMode.TAX_INCLUSIVE,
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
        val sTax = IndianCurrencyFormatter.parseInput(
            if (overrideSellingTax) sellingTaxRateText else base.sellingTaxRateText
        ) ?: BigDecimal.ZERO
        val sMode = if (overrideSellingTax) sellingPriceMode else base.sellingPriceMode
        val discount = IndianCurrencyFormatter.parseInput(supplierDiscountText) ?: BigDecimal.ZERO
        val baseUnits = base.unitsPerPackText.toIntOrNull() ?: 1
        val units = if (baseUnits > 0) baseUnits else 1
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
            sellingPriceMode = sMode,
            quantity = if (isSupplierOfferEnabled && buy > 0) buy else 1,
            packName = base.packName.ifBlank { "Pack" },
            unitName = base.unitName.ifBlank { "Unit" },
            unitsPerPack = units,
            isSupplierDiscountEnabled = isSupplierDiscountEnabled,
            supplierDiscountPercent = discount,
            isSupplierOfferEnabled = isSupplierOfferEnabled,
            offerBuyQuantity = if (buy > 0) buy else 1,
            offerFreeQuantity = if (free >= 0) free else 0
        )
    }

    companion object {
        fun fromCalculationEntity(entity: CalculationEntity): SupplierOfferSide {
            val input = entity.toCalculationInput()
            return SupplierOfferSide(
                supplierName = entity.supplierName,
                ptrText = input.ptr.toPlainString(),
                purchaseTaxRateText = input.purchaseTaxRate.toPlainString(),
                purchasePriceMode = input.purchasePriceMode,
                purchaseTaxClassification = input.purchaseTaxClassification,
                isInputTaxCreditEnabled = input.isInputTaxCreditEnabled,
                overrideSellingTax = true,
                sellingTaxRateText = input.sellingTaxRate.toPlainString(),
                sellingPriceMode = input.sellingPriceMode,
                isSupplierDiscountEnabled = input.isSupplierDiscountEnabled,
                supplierDiscountText = input.supplierDiscountPercent.toPlainString(),
                isSupplierOfferEnabled = input.isSupplierOfferEnabled,
                offerBuyQuantityText = input.offerBuyQuantity.toString(),
                offerFreeQuantityText = input.offerFreeQuantity.toString()
            )
        }
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
