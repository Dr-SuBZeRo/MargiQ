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

    // Units per pack
    val unitsPerPackText: String = "10",

    // Supplier Offers & Discounts (v1.2.0)
    val isSupplierOfferEnabled: Boolean = false,
    val offerBuyQuantityText: String = "1",
    val offerFreeQuantityText: String = "1",
    val supplierDiscountText: String = "0.00",

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
            appendLine("Pack Size: ${res.unitsPerPack} units per pack")
            appendLine("Paid Purchase Quantity: ${res.quantity} packs")
            if (res.isSupplierOfferEnabled && res.freePacksReceived > 0) {
                appendLine("Supplier Offer: Buy ${res.offerBuyQuantity}, Get ${res.offerFreeQuantity} Free (+${res.freePacksReceived} free packs)")
                appendLine("Total Packs Received: ${res.totalPacksReceived} packs")
            }
            if (res.supplierDiscountPercent > java.math.BigDecimal.ZERO) {
                appendLine("Supplier Discount: ${IndianCurrencyFormatter.formatPercentage(res.supplierDiscountPercent)}")
            }
            appendLine("----------------------------------")
            appendLine("Purchase Price (PTR): ${IndianCurrencyFormatter.formatCurrency(res.enteredPtr)} (${calculationInput.purchasePriceMode.label})")
            appendLine("Purchase Tax Rate: ${IndianCurrencyFormatter.formatPercentage(res.purchaseTaxRate)}")
            appendLine("Purchase Invoice Total: ${IndianCurrencyFormatter.formatCurrency(res.roundedPurchaseInvoice)}")
            appendLine("Eligible Input GST Credit: ${IndianCurrencyFormatter.formatCurrency(res.roundedEligibleInputTaxCredit)}")
            appendLine("Effective Economic Cost/Pack: ${IndianCurrencyFormatter.formatCurrency(res.roundedEffectiveCostPerReceivedPack)}")
            appendLine("----------------------------------")
            appendLine("Selling Price (MRP): ${IndianCurrencyFormatter.formatCurrency(res.enteredSellingPrice)} (${calculationInput.sellingPriceMode.label})")
            appendLine("Selling Tax Rate: ${IndianCurrencyFormatter.formatPercentage(res.sellingTaxRate)}")
            appendLine("Customer Selling Price: ${IndianCurrencyFormatter.formatCurrency(res.roundedCustomerSellingPrice)}")
            appendLine("Net Sales Revenue: ${IndianCurrencyFormatter.formatCurrency(res.roundedNetSalesRevenue)}")
            appendLine("Output GST: ${IndianCurrencyFormatter.formatCurrency(res.roundedSellingTaxAmount)}")
            appendLine("----------------------------------")
            appendLine("PER-PACK METRICS:")
            appendLine("Profit Per Received Pack: ${IndianCurrencyFormatter.formatCurrency(res.roundedProfitPerUnit)}")
            appendLine("Profit Margin: ${IndianCurrencyFormatter.formatPercentage(res.roundedProfitMargin)}")
            appendLine("Markup: ${IndianCurrencyFormatter.formatPercentage(res.roundedMarkup)}")
            if (res.unitsPerPack > 1) {
                appendLine("----------------------------------")
                appendLine("PER-UNIT METRICS:")
                appendLine("Cost Per Unit: ${IndianCurrencyFormatter.formatCurrency(res.roundedCostPerIndividualUnit)}")
                appendLine("MRP Per Unit: ${IndianCurrencyFormatter.formatCurrency(res.roundedCustomerPricePerIndividualUnit)}")
                appendLine("Profit Per Unit: ${IndianCurrencyFormatter.formatCurrency(res.roundedProfitPerIndividualUnit)}")
            }
            appendLine("----------------------------------")
            appendLine("TOTAL BATCH PROFIT (${res.totalPacksReceived} packs): ${IndianCurrencyFormatter.formatCurrency(res.roundedTotalProfit)}")
            appendLine("Basis: ${res.calculationBasis}")
            appendLine("==================================")
        }
    }
}
