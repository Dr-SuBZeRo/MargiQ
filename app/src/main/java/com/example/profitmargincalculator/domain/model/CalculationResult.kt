package com.example.profitmargincalculator.domain.model

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

    val profitPerUnit: BigDecimal,
    val profitMargin: BigDecimal?,
    val markup: BigDecimal?,
    val costPercentage: BigDecimal?,

    val quantity: Int,
    val totalPurchaseInvoiceTotal: BigDecimal,
    val totalSellingCustomerTotal: BigDecimal,
    val totalOutputTax: BigDecimal,
    val totalEligibleInputTaxCredit: BigDecimal,
    val totalProfit: BigDecimal,

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

    val roundedTotalProfit: BigDecimal
        get() = totalProfit.setScale(2, RoundingMode.HALF_UP)

    val roundedTotalPurchaseInvoice: BigDecimal
        get() = totalPurchaseInvoiceTotal.setScale(2, RoundingMode.HALF_UP)

    val roundedTotalCustomerCollection: BigDecimal
        get() = totalSellingCustomerTotal.setScale(2, RoundingMode.HALF_UP)

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
            quantity = 1,
            totalPurchaseInvoiceTotal = BigDecimal.ZERO,
            totalSellingCustomerTotal = BigDecimal.ZERO,
            totalOutputTax = BigDecimal.ZERO,
            totalEligibleInputTaxCredit = BigDecimal.ZERO,
            totalProfit = BigDecimal.ZERO,
            simpleCashProfitPerUnit = BigDecimal.ZERO,
            simpleCashProfitMargin = null,
            isInputTaxCreditApplied = false,
            calculationBasis = "Invalid Input",
            isValid = false,
            validationMessage = message
        )
    }
}
