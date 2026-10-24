package com.example.profitmargincalculator.ui.calculator

import com.example.profitmargincalculator.domain.calculation.ProfitCalculationEngine
import com.example.profitmargincalculator.domain.model.CalculationInput
import com.example.profitmargincalculator.domain.model.CalculationResult
import com.example.profitmargincalculator.domain.model.PurchaseTaxClassification
import com.example.profitmargincalculator.domain.model.TaxPriceMode
import com.example.profitmargincalculator.domain.util.IndianCurrencyFormatter

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
    val quantityText: String = "5",

    // Pack and unit configuration
    val packName: String = "Strip",
    val unitName: String = "Tablet",
    val unitsPerPackText: String = "10",
    val sellingQuantityText: String = "5",

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
            appendLine("=== MargiQ ===")
            appendLine("Know your cost. Own your margin.")
            if (productName.isNotBlank()) {
                appendLine("Product: $productName")
            }
            appendLine("Pack: ${res.packName} (${res.unitsPerPack} ${res.unitName}s per pack)")
            appendLine("Purchase Quantity: ${res.quantity} ${res.packName}s (${res.totalIndividualUnits} ${res.unitName}s)")
            if (res.sellingQuantity != res.quantity) {
                appendLine("Selling Quantity: ${res.sellingQuantity} ${res.packName}s (${res.totalIndividualUnitsSold} ${res.unitName}s)")
            }
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
            appendLine("PER-PACK METRICS (${res.packName}):")
            appendLine("Profit Per Pack: ${IndianCurrencyFormatter.formatCurrency(res.roundedProfitPerUnit)}")
            appendLine("Profit Margin: ${IndianCurrencyFormatter.formatPercentage(res.roundedProfitMargin)}")
            appendLine("Markup: ${IndianCurrencyFormatter.formatPercentage(res.roundedMarkup)}")
            appendLine("----------------------------------")
            appendLine("PER-UNIT METRICS (${res.unitName}):")
            appendLine("Cost Per ${res.unitName}: ${IndianCurrencyFormatter.formatCurrency(res.roundedCostPerIndividualUnit)}")
            appendLine("Customer Price Per ${res.unitName}: ${IndianCurrencyFormatter.formatCurrency(res.roundedCustomerPricePerIndividualUnit)}")
            appendLine("Net Revenue Per ${res.unitName}: ${IndianCurrencyFormatter.formatCurrency(res.roundedNetRevenuePerIndividualUnit)}")
            appendLine("Profit Per ${res.unitName}: ${IndianCurrencyFormatter.formatCurrency(res.roundedProfitPerIndividualUnit)}")
            appendLine("----------------------------------")
            appendLine("TOTAL PROFIT (${res.sellingQuantity} ${res.packName}s): ${IndianCurrencyFormatter.formatCurrency(res.roundedTotalProfit)}")
            appendLine("Basis: ${res.calculationBasis}")
            appendLine("==================================")
        }
    }
}
