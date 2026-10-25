package com.margiq.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

data class CalculationResult(
    val enteredPtr: BigDecimal,
    val purchaseTaxRate: BigDecimal,
    val taxablePurchaseBase: BigDecimal,
    val purchaseTaxAmount: BigDecimal,
    val purchaseInvoiceTotal: BigDecimal,
    val eligibleInputTaxCredit: BigDecimal,
    val economicPurchaseCost: BigDecimal,

    val enteredSellingPrice: BigDecimal,
    val sellingTaxRate: BigDecimal,
    val sellingTaxAmount: BigDecimal,
    val customerSellingPrice: BigDecimal,
    val netSalesRevenue: BigDecimal,

    // Per-pack profit & ratios
    val profitPerUnit: BigDecimal,
    val profitMargin: BigDecimal?,
    val markup: BigDecimal?,
    val costPercentage: BigDecimal?,

    // Pack & Individual Unit Details
    val packName: String = "Pack",
    val unitName: String = "Unit",
    val unitsPerPack: Int = 1,
    val totalIndividualUnits: Int = 1,
    val sellingQuantity: Int = 1,
    val totalIndividualUnitsSold: Int = 1,
    val remainingIndividualUnits: Int = 0,
    val remainingPacks: Int = 0,

    val costPerIndividualUnit: BigDecimal = BigDecimal.ZERO,
    val purchaseInvoicePerIndividualUnit: BigDecimal = BigDecimal.ZERO,
    val customerPricePerIndividualUnit: BigDecimal = BigDecimal.ZERO,
    val netRevenuePerIndividualUnit: BigDecimal = BigDecimal.ZERO,
    val profitPerIndividualUnit: BigDecimal = BigDecimal.ZERO,

    // Multi-pack Totals
    val quantity: Int,
    val totalPurchaseInvoiceTotal: BigDecimal,
    val totalEconomicPurchaseCost: BigDecimal = BigDecimal.ZERO,
    val totalSellingCustomerTotal: BigDecimal,
    val totalNetSalesRevenue: BigDecimal = BigDecimal.ZERO,
    val totalOutputTax: BigDecimal,
    val totalEligibleInputTaxCredit: BigDecimal,
    val totalProfit: BigDecimal,

    // Supplier Name (Metadata)
    val supplierName: String = "",

    // Supplier Offers & Discounts
    val isSupplierDiscountEnabled: Boolean = false,
    val isSupplierOfferEnabled: Boolean = false,
    val offerBuyQuantity: Int = 1,
    val offerFreeQuantity: Int = 0,
    val freePacksReceived: Int = 0,
    val totalPacksReceived: Int = 1,
    val supplierDiscountPercent: BigDecimal = BigDecimal.ZERO,
    val supplierDiscountAmount: BigDecimal = BigDecimal.ZERO,
    val grossPaidPurchaseValue: BigDecimal = BigDecimal.ZERO,

    val effectiveCostPerReceivedPack: BigDecimal = BigDecimal.ZERO,
    val effectiveInvoicePerReceivedPack: BigDecimal = BigDecimal.ZERO,

    val simpleCashProfitPerUnit: BigDecimal,
    val simpleCashProfitMargin: BigDecimal?,

    val isInputTaxCreditApplied: Boolean,
    val calculationBasis: String,
    val isValid: Boolean = true,
    val validationMessage: String? = null
) {
    val isLoss: Boolean
        get() = profitPerUnit < BigDecimal.ZERO

    val isBreakEven: Boolean
        get() = profitPerUnit.compareTo(BigDecimal.ZERO) == 0

    val isProfitable: Boolean
        get() = profitPerUnit > BigDecimal.ZERO

    // Per-pack rounded accessors
    val roundedProfitPerUnit: BigDecimal
        get() = profitPerUnit.setScale(2, RoundingMode.HALF_UP)

    val roundedProfitMargin: BigDecimal?
        get() = profitMargin?.setScale(2, RoundingMode.HALF_UP)

    val roundedMarkup: BigDecimal?
        get() = markup?.setScale(2, RoundingMode.HALF_UP)

    val roundedCostPercentage: BigDecimal?
        get() = costPercentage?.setScale(2, RoundingMode.HALF_UP)

    val roundedNetSalesRevenue: BigDecimal
        get() = netSalesRevenue.setScale(2, RoundingMode.HALF_UP)

    val roundedEconomicCost: BigDecimal
        get() = economicPurchaseCost.setScale(2, RoundingMode.HALF_UP)

    val roundedPurchaseInvoice: BigDecimal
        get() = purchaseInvoiceTotal.setScale(2, RoundingMode.HALF_UP)

    val roundedCustomerSellingPrice: BigDecimal
        get() = customerSellingPrice.setScale(2, RoundingMode.HALF_UP)

    val roundedSellingTaxAmount: BigDecimal
        get() = sellingTaxAmount.setScale(2, RoundingMode.HALF_UP)

    val roundedEligibleInputTaxCredit: BigDecimal
        get() = eligibleInputTaxCredit.setScale(2, RoundingMode.HALF_UP)

    // Supplier Offer & Discount rounded accessors
    val roundedSupplierDiscountAmount: BigDecimal
        get() = supplierDiscountAmount.setScale(2, RoundingMode.HALF_UP)

    val roundedGrossPaidPurchaseValue: BigDecimal
        get() = grossPaidPurchaseValue.setScale(2, RoundingMode.HALF_UP)

    val roundedEffectiveCostPerReceivedPack: BigDecimal
        get() = effectiveCostPerReceivedPack.setScale(2, RoundingMode.HALF_UP)

    val roundedEffectiveInvoicePerReceivedPack: BigDecimal
        get() = effectiveInvoicePerReceivedPack.setScale(2, RoundingMode.HALF_UP)

    // Per-individual-unit rounded accessors
    val roundedCostPerIndividualUnit: BigDecimal
        get() = costPerIndividualUnit.setScale(2, RoundingMode.HALF_UP)

    val roundedPurchaseInvoicePerIndividualUnit: BigDecimal
        get() = purchaseInvoicePerIndividualUnit.setScale(2, RoundingMode.HALF_UP)

    val roundedCustomerPricePerIndividualUnit: BigDecimal
        get() = customerPricePerIndividualUnit.setScale(2, RoundingMode.HALF_UP)

    val roundedNetRevenuePerIndividualUnit: BigDecimal
        get() = netRevenuePerIndividualUnit.setScale(2, RoundingMode.HALF_UP)

    val roundedProfitPerIndividualUnit: BigDecimal
        get() = profitPerIndividualUnit.setScale(2, RoundingMode.HALF_UP)

    // Multi-pack rounded totals
    val roundedTotalProfit: BigDecimal
        get() = totalProfit.setScale(2, RoundingMode.HALF_UP)

    val roundedTotalPurchaseInvoice: BigDecimal
        get() = totalPurchaseInvoiceTotal.setScale(2, RoundingMode.HALF_UP)

    val roundedTotalEconomicCost: BigDecimal
        get() = totalEconomicPurchaseCost.setScale(2, RoundingMode.HALF_UP)

    val roundedTotalCustomerCollection: BigDecimal
        get() = totalSellingCustomerTotal.setScale(2, RoundingMode.HALF_UP)

    val roundedTotalNetSalesRevenue: BigDecimal
        get() = totalNetSalesRevenue.setScale(2, RoundingMode.HALF_UP)

    val roundedTotalOutputTax: BigDecimal
        get() = totalOutputTax.setScale(2, RoundingMode.HALF_UP)

    val roundedTotalInputCredit: BigDecimal
        get() = totalEligibleInputTaxCredit.setScale(2, RoundingMode.HALF_UP)

    companion object {
        fun invalid(message: String): CalculationResult = CalculationResult(
            enteredPtr = BigDecimal.ZERO,
            purchaseTaxRate = BigDecimal.ZERO,
            taxablePurchaseBase = BigDecimal.ZERO,
            purchaseTaxAmount = BigDecimal.ZERO,
            purchaseInvoiceTotal = BigDecimal.ZERO,
            eligibleInputTaxCredit = BigDecimal.ZERO,
            economicPurchaseCost = BigDecimal.ZERO,
            enteredSellingPrice = BigDecimal.ZERO,
            sellingTaxRate = BigDecimal.ZERO,
            sellingTaxAmount = BigDecimal.ZERO,
            customerSellingPrice = BigDecimal.ZERO,
            netSalesRevenue = BigDecimal.ZERO,
            profitPerUnit = BigDecimal.ZERO,
            profitMargin = null,
            markup = null,
            costPercentage = null,
            packName = "Pack",
            unitName = "Unit",
            unitsPerPack = 1,
            totalIndividualUnits = 0,
            sellingQuantity = 0,
            totalIndividualUnitsSold = 0,
            remainingIndividualUnits = 0,
            remainingPacks = 0,
            costPerIndividualUnit = BigDecimal.ZERO,
            purchaseInvoicePerIndividualUnit = BigDecimal.ZERO,
            customerPricePerIndividualUnit = BigDecimal.ZERO,
            netRevenuePerIndividualUnit = BigDecimal.ZERO,
            profitPerIndividualUnit = BigDecimal.ZERO,
            quantity = 1,
            totalPurchaseInvoiceTotal = BigDecimal.ZERO,
            totalEconomicPurchaseCost = BigDecimal.ZERO,
            totalSellingCustomerTotal = BigDecimal.ZERO,
            totalNetSalesRevenue = BigDecimal.ZERO,
            totalOutputTax = BigDecimal.ZERO,
            totalEligibleInputTaxCredit = BigDecimal.ZERO,
            totalProfit = BigDecimal.ZERO,
            supplierName = "",
            isSupplierDiscountEnabled = false,
            isSupplierOfferEnabled = false,
            offerBuyQuantity = 1,
            offerFreeQuantity = 0,
            freePacksReceived = 0,
            totalPacksReceived = 1,
            supplierDiscountPercent = BigDecimal.ZERO,
            supplierDiscountAmount = BigDecimal.ZERO,
            grossPaidPurchaseValue = BigDecimal.ZERO,
            effectiveCostPerReceivedPack = BigDecimal.ZERO,
            effectiveInvoicePerReceivedPack = BigDecimal.ZERO,
            simpleCashProfitPerUnit = BigDecimal.ZERO,
            simpleCashProfitMargin = null,
            isInputTaxCreditApplied = false,
            calculationBasis = "Invalid Input",
            isValid = false,
            validationMessage = message
        )
    }
}
