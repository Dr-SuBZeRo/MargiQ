package com.example.profitmargincalculator.ui.calculator

import com.example.profitmargincalculator.data.local.CalculationEntity
import com.example.profitmargincalculator.domain.calculation.ProfitCalculationEngine
import com.example.profitmargincalculator.domain.model.CalculationInput
import com.example.profitmargincalculator.domain.model.CalculationResult
import com.example.profitmargincalculator.domain.model.PurchaseTaxClassification
import com.example.profitmargincalculator.domain.model.TaxPriceMode
import com.example.profitmargincalculator.domain.util.IndianCurrencyFormatter
import java.math.BigDecimal

data class CalculatorUiState(
    val productName: String = "Sample Product",
    val ptrText: String = "48.00",
    val purchaseTaxRateText: String = "5.00",
    val purchasePriceMode: TaxPriceMode = TaxPriceMode.TAX_EXCLUSIVE,
    val purchaseTaxClassification: PurchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
    val isInputTaxCreditEnabled: Boolean = true,
    val sellingPriceText: String = "135.00",
    val sellingTaxRateText: String = "5.00",
    val sellingPriceMode: TaxPriceMode = TaxPriceMode.TAX_INCLUSIVE,
    val quantityText: String = "1",

    val calculationInput: CalculationInput = CalculationInput.defaultExample(),
    val calculationResult: CalculationResult = ProfitCalculationEngine.calculate(CalculationInput.defaultExample()),

    val currentSavedId: Long? = null,
    val userMessage: String? = null,
    val showDetailedBreakdown: Boolean = true,
    val showCashComparison: Boolean = false
) {
    val canEnableItcToggle: Boolean
        get() = purchaseTaxClassification == PurchaseTaxClassification.RECOVERABLE_GST

    fun toFormattedShareText(): String {
        val res = calculationResult
        return buildString {
            appendLine("=== Profit & Margin Calculator ===")
            if (productName.isNotBlank()) {
                appendLine("Product: $productName")
            }
            appendLine("Quantity: ${res.quantity}")
            appendLine("----------------------------------")
            appendLine("Purchase Price (PTR): ${IndianCurrencyFormatter.formatCurrency(res.enteredPtr)} (${calculationInput.purchasePriceMode.label})")
            appendLine("Purchase Tax Rate: ${IndianCurrencyFormatter.formatPercentage(res.purchaseTaxRate)}")
            appendLine("Purchase Invoice Total: ${IndianCurrencyFormatter.formatCurrency(res.roundedPurchaseInvoice)}")
            appendLine("Eligible Input GST Credit: ${IndianCurrencyFormatter.formatCurrency(res.roundedEligibleInputTaxCredit)}")
            appendLine("Economic Purchase Cost: ${IndianCurrencyFormatter.formatCurrency(res.roundedEconomicCost)}")
            appendLine("----------------------------------")
            appendLine("Selling Price (MRP): ${IndianCurrencyFormatter.formatCurrency(res.enteredSellingPrice)} (${calculationInput.sellingPriceMode.label})")
            appendLine("Selling Tax Rate: ${IndianCurrencyFormatter.formatPercentage(res.sellingTaxRate)}")
            appendLine("Customer Selling Price: ${IndianCurrencyFormatter.formatCurrency(res.roundedCustomerSellingPrice)}")
            appendLine("Net Sales Revenue: ${IndianCurrencyFormatter.formatCurrency(res.roundedNetSalesRevenue)}")
            appendLine("Output GST: ${IndianCurrencyFormatter.formatCurrency(res.roundedSellingTaxAmount)}")
            appendLine("----------------------------------")
            appendLine("PROFIT PER UNIT: ${IndianCurrencyFormatter.formatCurrency(res.roundedProfitPerUnit)}")
            appendLine("PROFIT MARGIN: ${IndianCurrencyFormatter.formatPercentage(res.roundedProfitMargin)}")
            appendLine("MARKUP: ${IndianCurrencyFormatter.formatPercentage(res.roundedMarkup)}")
            appendLine("COST % OF REVENUE: ${IndianCurrencyFormatter.formatPercentage(res.roundedCostPercentage)}")
            if (res.quantity > 1) {
                appendLine("TOTAL PROFIT (${res.quantity} units): ${IndianCurrencyFormatter.formatCurrency(res.roundedTotalProfit)}")
            }
            appendLine("Basis: ${res.calculationBasis}")
            appendLine("==================================")
        }
    }
}
