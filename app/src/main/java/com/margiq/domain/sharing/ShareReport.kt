package com.margiq.domain.sharing

import com.margiq.domain.comparison.WinnerSide
import com.margiq.domain.model.PurchaseTaxClassification
import com.margiq.domain.model.TaxPriceMode
import java.math.BigDecimal

/**
 * Immutable canonical share report data model.
 * All formatters (Rich Text, HTML Email, Image Renderer) consume this single model
 * to ensure 100% calculation consistency across every export format without independent recomputation.
 */
sealed interface ShareReport {
    val title: String
    val timestamp: Long

    /**
     * Complete financial representation of a single product pricing calculation.
     */
    data class SingleCalculation(
        val productName: String,
        val supplierName: String = "",
        val unitsPerPack: Int = 1,
        val quantity: Int = 1,
        val enteredPtr: BigDecimal,
        val purchasePriceMode: TaxPriceMode,
        val purchaseTaxRate: BigDecimal,
        val isSupplierDiscountEnabled: Boolean = false,
        val supplierDiscountPercent: BigDecimal = BigDecimal.ZERO,
        val isSupplierOfferEnabled: Boolean = false,
        val offerBuyQuantity: Int = 0,
        val offerFreeQuantity: Int = 0,
        val freePacksReceived: Int = 0,
        val totalPacksReceived: Int = 1,
        val inputTaxCreditEnabled: Boolean = true,
        val purchaseTaxClassification: PurchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
        val enteredSellingPrice: BigDecimal,
        val sellingPriceMode: TaxPriceMode,
        val sellingTaxRate: BigDecimal,
        // Authoritative results computed by ProfitCalculationEngine
        val invoiceTotal: BigDecimal,
        val eligibleInputTaxCredit: BigDecimal,
        val effectiveCostPerPack: BigDecimal,
        val customerSellingPrice: BigDecimal,
        val netSalesRevenue: BigDecimal,
        val outputTax: BigDecimal,
        val profitPerPack: BigDecimal,
        val profitMarginPercent: BigDecimal? = null,
        val markupPercent: BigDecimal? = null,
        val totalBatchProfit: BigDecimal,
        // Individual unit metrics when unitsPerPack > 1
        val costPerIndividualUnit: BigDecimal? = null,
        val customerPricePerIndividualUnit: BigDecimal? = null,
        val profitPerIndividualUnit: BigDecimal? = null,
        val calculationBasis: String = "",
        override val title: String = productName.ifBlank { "Calculation Report" },
        override val timestamp: Long = System.currentTimeMillis()
    ) : ShareReport

    /**
     * Side-by-side comparison between two alternative products.
     */
    data class ProductComparison(
        val titleA: String,
        val titleB: String,
        val unitsPerPackA: Int,
        val unitsPerPackB: Int,
        val costPerPackA: BigDecimal,
        val costPerPackB: BigDecimal,
        val costPerUnitA: BigDecimal,
        val costPerUnitB: BigDecimal,
        val sellingPriceA: BigDecimal,
        val sellingPriceB: BigDecimal,
        val profitPerUnitA: BigDecimal,
        val profitPerUnitB: BigDecimal,
        val profitMarginA: BigDecimal? = null,
        val profitMarginB: BigDecimal? = null,
        val markupA: BigDecimal? = null,
        val markupB: BigDecimal? = null,
        val purchaseTaxA: BigDecimal,
        val purchaseTaxB: BigDecimal,
        val sellingTaxA: BigDecimal,
        val sellingTaxB: BigDecimal,
        val itcTreatmentA: String,
        val itcTreatmentB: String,
        val lowestCostWinner: WinnerSide,
        val highestProfitWinner: WinnerSide,
        val highestMarginWinner: WinnerSide,
        val primaryHighlightSummary: String,
        val secondaryHighlightSummary: String? = null,
        override val title: String = "Product Comparison Report",
        override val timestamp: Long = System.currentTimeMillis()
    ) : ShareReport

    /**
     * Side-by-side comparison between two supplier quotes for the same base product.
     */
    data class SupplierComparison(
        val productName: String,
        val supplierNameA: String,
        val supplierNameB: String,
        val unitsPerPack: Int,
        val baseSellingPrice: BigDecimal,
        val baseSellingTaxRate: BigDecimal,
        val baseSellingPriceMode: TaxPriceMode,
        val ptrA: BigDecimal,
        val ptrB: BigDecimal,
        val purchaseTaxRateA: BigDecimal,
        val purchaseTaxRateB: BigDecimal,
        val purchasePriceModeA: TaxPriceMode,
        val purchasePriceModeB: TaxPriceMode,
        val costPerPackA: BigDecimal,
        val costPerPackB: BigDecimal,
        val costPerUnitA: BigDecimal,
        val costPerUnitB: BigDecimal,
        val profitPerUnitA: BigDecimal,
        val profitPerUnitB: BigDecimal,
        val profitMarginA: BigDecimal? = null,
        val profitMarginB: BigDecimal? = null,
        val markupA: BigDecimal? = null,
        val markupB: BigDecimal? = null,
        val purchaseTaxA: BigDecimal,
        val purchaseTaxB: BigDecimal,
        val sellingTaxA: BigDecimal,
        val sellingTaxB: BigDecimal,
        val itcTreatmentA: String,
        val itcTreatmentB: String,
        val lowestCostWinner: WinnerSide,
        val highestProfitWinner: WinnerSide,
        val highestMarginWinner: WinnerSide,
        val primaryHighlightSummary: String,
        val secondaryHighlightSummary: String? = null,
        override val title: String = if (productName.isNotBlank()) "Supplier Comparison — $productName" else "Supplier Comparison Report",
        override val timestamp: Long = System.currentTimeMillis()
    ) : ShareReport
}

