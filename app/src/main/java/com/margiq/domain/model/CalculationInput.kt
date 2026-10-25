package com.margiq.domain.model

import java.math.BigDecimal

data class CalculationInput(
    val productName: String = "Sample Product",
    val supplierName: String = "",
    val ptr: BigDecimal = BigDecimal("48.00"),
    val purchaseTaxRate: BigDecimal = BigDecimal("5.00"),
    val purchasePriceMode: TaxPriceMode = TaxPriceMode.TAX_EXCLUSIVE,
    val purchaseTaxClassification: PurchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
    val isInputTaxCreditEnabled: Boolean = true,
    val sellingPrice: BigDecimal = BigDecimal("135.00"),
    val sellingTaxRate: BigDecimal = BigDecimal("5.00"),
    val sellingPriceMode: TaxPriceMode = TaxPriceMode.TAX_INCLUSIVE,
    val quantity: Int = 5,
    val packName: String = "Strip",
    val unitName: String = "Tablet",
    val unitsPerPack: Int = 10,
    val sellingQuantity: Int = 5,
    val isSupplierDiscountEnabled: Boolean = false,
    val supplierDiscountPercent: BigDecimal = BigDecimal.ZERO,
    val isSupplierOfferEnabled: Boolean = false,
    val offerBuyQuantity: Int = 1,
    val offerFreeQuantity: Int = 0
) {
    companion object {
        fun defaultExample(): CalculationInput = CalculationInput(
            productName = "Sample Product",
            supplierName = "",
            ptr = BigDecimal("48.00"),
            purchaseTaxRate = BigDecimal("5.00"),
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            purchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
            isInputTaxCreditEnabled = true,
            sellingPrice = BigDecimal("135.00"),
            sellingTaxRate = BigDecimal("5.00"),
            sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE,
            quantity = 5,
            packName = "Strip",
            unitName = "Tablet",
            unitsPerPack = 10,
            sellingQuantity = 5,
            isSupplierDiscountEnabled = false,
            supplierDiscountPercent = BigDecimal.ZERO,
            isSupplierOfferEnabled = false,
            offerBuyQuantity = 1,
            offerFreeQuantity = 0
        )

        fun empty(): CalculationInput = CalculationInput(
            productName = "",
            supplierName = "",
            ptr = BigDecimal.ZERO,
            purchaseTaxRate = BigDecimal("5.00"),
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            purchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
            isInputTaxCreditEnabled = true,
            sellingPrice = BigDecimal.ZERO,
            sellingTaxRate = BigDecimal("5.00"),
            sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE,
            quantity = 1,
            packName = "Pack",
            unitName = "Unit",
            unitsPerPack = 10,
            sellingQuantity = 1,
            isSupplierDiscountEnabled = false,
            supplierDiscountPercent = BigDecimal.ZERO,
            isSupplierOfferEnabled = false,
            offerBuyQuantity = 1,
            offerFreeQuantity = 0
        )
    }
}
