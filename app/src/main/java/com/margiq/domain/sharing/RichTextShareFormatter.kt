package com.margiq.domain.sharing

import com.margiq.domain.comparison.WinnerSide
import com.margiq.domain.util.IndianCurrencyFormatter
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RichTextShareFormatter {

    private const val DIVIDER = "━━━━━━━━━━━━━━━━━━━━"

    fun format(report: ShareReport): String {
        return when (report) {
            is ShareReport.SingleCalculation -> formatSingleCalculation(report)
            is ShareReport.ProductComparison -> formatProductComparison(report)
            is ShareReport.SupplierComparison -> formatSupplierComparison(report)
        }
    }

    private fun formatSingleCalculation(report: ShareReport.SingleCalculation): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date(report.timestamp))
        val totalUnits = report.unitsPerPack * report.quantity

        val profitEmoji = when {
            report.profitPerPack > BigDecimal.ZERO -> "💚"
            report.profitPerPack < BigDecimal.ZERO -> "🔻"
            else -> "⚪"
        }

        return buildString {
            appendLine("📊 *MARGIQ — PROFIT & MARGIN REPORT*")
            appendLine("_Date: ${dateStr}_")
            appendLine(DIVIDER)

            // Product Details
            appendLine("📦 *PRODUCT DETAILS*")
            appendLine("🧾 Product: ${report.productName}")
            if (report.supplierName.isNotBlank()) {
                appendLine("🏢 Supplier: ${report.supplierName}")
            }
            appendLine("📦 Pack Size: ${report.unitsPerPack} unit${if (report.unitsPerPack > 1) "s" else ""}/pack")
            appendLine("🔢 Purchase Quantity: ${report.quantity} pack${if (report.quantity > 1) "s" else ""}")
            appendLine("📊 Total Units: $totalUnits")
            if (report.isSupplierOfferEnabled && report.freePacksReceived > 0) {
                appendLine("🎁 Scheme: Buy ${report.offerBuyQuantity}, Get ${report.offerFreeQuantity} Free (+${report.freePacksReceived} free packs)")
                appendLine("📦 Total Received: ${report.totalPacksReceived} packs")
            }
            if (report.isSupplierDiscountEnabled && report.supplierDiscountPercent > BigDecimal.ZERO) {
                appendLine("🏷️ Supplier Discount: ${IndianCurrencyFormatter.formatPercentage(report.supplierDiscountPercent)}")
            }
            appendLine(DIVIDER)

            // Purchase Economics
            appendLine("💰 *PURCHASE ECONOMICS*")
            appendLine("• PTR: ${IndianCurrencyFormatter.formatCurrency(report.enteredPtr)} (${report.purchasePriceMode.label})")
            appendLine("• Purchase GST: ${IndianCurrencyFormatter.formatPercentage(report.purchaseTaxRate)}")
            appendLine("• Invoice Total: ${IndianCurrencyFormatter.formatCurrency(report.invoiceTotal)}")
            appendLine("• Eligible Input GST Credit: ${IndianCurrencyFormatter.formatCurrency(report.eligibleInputTaxCredit)}")
            appendLine("• Effective Cost/Pack: ${IndianCurrencyFormatter.formatCurrency(report.effectiveCostPerPack)}")
            if (report.unitsPerPack > 1 && report.costPerIndividualUnit != null) {
                appendLine("• Cost per Individual Unit: ${IndianCurrencyFormatter.formatCurrency(report.costPerIndividualUnit)}")
            }
            appendLine(DIVIDER)

            // Selling & Revenue
            appendLine("🛒 *SELLING & REVENUE*")
            appendLine("• MRP: ${IndianCurrencyFormatter.formatCurrency(report.enteredSellingPrice)} (${report.sellingPriceMode.label})")
            appendLine("• Selling GST: ${IndianCurrencyFormatter.formatPercentage(report.sellingTaxRate)}")
            appendLine("• Customer Selling Price: ${IndianCurrencyFormatter.formatCurrency(report.customerSellingPrice)}")
            appendLine("• Net Sales Revenue: ${IndianCurrencyFormatter.formatCurrency(report.netSalesRevenue)}")
            appendLine("• Output GST: ${IndianCurrencyFormatter.formatCurrency(report.outputTax)}")
            appendLine(DIVIDER)

            // Profitability
            appendLine("📈 *PROFITABILITY*")
            appendLine("$profitEmoji Profit per Pack: ${formatSignedCurrency(report.profitPerPack)}")
            if (report.unitsPerPack > 1 && report.profitPerIndividualUnit != null) {
                appendLine("$profitEmoji Profit per Unit: ${formatSignedCurrency(report.profitPerIndividualUnit)}")
            }
            appendLine("📊 Profit Margin: ${IndianCurrencyFormatter.formatPercentage(report.profitMarginPercent)}")
            appendLine("📈 Markup: ${IndianCurrencyFormatter.formatPercentage(report.markupPercent)}")
            appendLine("💰 Total Batch Profit: ${formatSignedCurrency(report.totalBatchProfit)}")
            if (report.calculationBasis.isNotBlank()) {
                appendLine("ℹ️ Basis: ${report.calculationBasis}")
            }
            appendLine(DIVIDER)

            // Footer
            appendLine("✨ _Generated with MargiQ_")
            appendLine("_Know your cost. Own your margin._")
            append("https://github.com/Dr-SuBZeRo/MargiQ")
        }
    }

    private fun formatProductComparison(report: ShareReport.ProductComparison): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date(report.timestamp))

        return buildString {
            appendLine("⚖️ *MARGIQ — PRODUCT COMPARISON REPORT*")
            appendLine("_Date: ${dateStr}_")
            appendLine(DIVIDER)

            // Highlights
            appendLine("🏆 *COMMERCIAL HIGHLIGHT*")
            appendLine(report.primaryHighlightSummary)
            report.secondaryHighlightSummary?.let {
                appendLine("ℹ️ $it")
            }
            appendLine(DIVIDER)

            // Winners
            appendLine("🏅 *CATEGORY WINNERS*")
            appendLine("• Lowest Cost: *${winnerLabel(report.lowestCostWinner, report.titleA, report.titleB)}*")
            appendLine("• Highest Profit: *${winnerLabel(report.highestProfitWinner, report.titleA, report.titleB)}*")
            appendLine("• Highest Margin: *${winnerLabel(report.highestMarginWinner, report.titleA, report.titleB)}*")
            appendLine(DIVIDER)

            // Side-by-side
            appendLine("📊 *SIDE-BY-SIDE METRICS*")
            appendLine()
            appendLine("*Cost per Unit:*")
            appendLine("  ${report.titleA}: ${IndianCurrencyFormatter.formatCurrency(report.costPerUnitA)}")
            appendLine("  ${report.titleB}: ${IndianCurrencyFormatter.formatCurrency(report.costPerUnitB)}")
            appendLine()
            appendLine("*Cost per Pack:*")
            appendLine("  ${report.titleA}: ${IndianCurrencyFormatter.formatCurrency(report.costPerPackA)}")
            appendLine("  ${report.titleB}: ${IndianCurrencyFormatter.formatCurrency(report.costPerPackB)}")
            appendLine()
            appendLine("*Selling Price (MRP):*")
            appendLine("  ${report.titleA}: ${IndianCurrencyFormatter.formatCurrency(report.sellingPriceA)}")
            appendLine("  ${report.titleB}: ${IndianCurrencyFormatter.formatCurrency(report.sellingPriceB)}")
            appendLine()
            appendLine("*Profit per Unit:*")
            appendLine("  ${report.titleA}: ${formatSignedCurrency(report.profitPerUnitA)}")
            appendLine("  ${report.titleB}: ${formatSignedCurrency(report.profitPerUnitB)}")
            appendLine()
            appendLine("*Profit Margin:*")
            appendLine("  ${report.titleA}: ${IndianCurrencyFormatter.formatPercentage(report.profitMarginA)}")
            appendLine("  ${report.titleB}: ${IndianCurrencyFormatter.formatPercentage(report.profitMarginB)}")
            appendLine()
            appendLine("*Markup:*")
            appendLine("  ${report.titleA}: ${IndianCurrencyFormatter.formatPercentage(report.markupA)}")
            appendLine("  ${report.titleB}: ${IndianCurrencyFormatter.formatPercentage(report.markupB)}")
            appendLine()
            appendLine("*Purchase GST:*")
            appendLine("  ${report.titleA}: ${IndianCurrencyFormatter.formatCurrency(report.purchaseTaxA)}")
            appendLine("  ${report.titleB}: ${IndianCurrencyFormatter.formatCurrency(report.purchaseTaxB)}")
            appendLine()
            appendLine("*Selling GST:*")
            appendLine("  ${report.titleA}: ${IndianCurrencyFormatter.formatCurrency(report.sellingTaxA)}")
            appendLine("  ${report.titleB}: ${IndianCurrencyFormatter.formatCurrency(report.sellingTaxB)}")
            appendLine()
            appendLine("*ITC Treatment:*")
            appendLine("  ${report.titleA}: ${report.itcTreatmentA}")
            appendLine("  ${report.titleB}: ${report.itcTreatmentB}")
            appendLine(DIVIDER)

            // Footer
            appendLine("✨ _Generated with MargiQ_")
            appendLine("_Know your cost. Own your margin._")
            append("https://github.com/Dr-SuBZeRo/MargiQ")
        }
    }

    private fun formatSupplierComparison(report: ShareReport.SupplierComparison): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date(report.timestamp))

        return buildString {
            appendLine("⚖️ *MARGIQ — SUPPLIER COMPARISON REPORT*")
            appendLine("_Date: ${dateStr}_")
            appendLine(DIVIDER)

            // Base product
            appendLine("📦 *BASE PRODUCT DETAILS*")
            appendLine("🧾 Product: ${report.productName}")
            appendLine("📦 Pack Size: ${report.unitsPerPack} unit${if (report.unitsPerPack > 1) "s" else ""}/pack")
            appendLine("🛒 Selling Price (MRP): ${IndianCurrencyFormatter.formatCurrency(report.baseSellingPrice)} (${report.baseSellingPriceMode.label})")
            appendLine("📊 Selling GST: ${IndianCurrencyFormatter.formatPercentage(report.baseSellingTaxRate)}")
            appendLine(DIVIDER)

            // Commercial highlight
            appendLine("🏆 *COMMERCIAL HIGHLIGHT*")
            appendLine(report.primaryHighlightSummary)
            report.secondaryHighlightSummary?.let {
                appendLine("ℹ️ $it")
            }
            appendLine(DIVIDER)

            // Category winners
            appendLine("🏅 *CATEGORY WINNERS*")
            appendLine("• Lowest Cost: *${winnerLabel(report.lowestCostWinner, report.supplierNameA, report.supplierNameB)}*")
            appendLine("• Highest Profit: *${winnerLabel(report.highestProfitWinner, report.supplierNameA, report.supplierNameB)}*")
            appendLine("• Highest Margin: *${winnerLabel(report.highestMarginWinner, report.supplierNameA, report.supplierNameB)}*")
            appendLine(DIVIDER)

            // Supplier offer metrics
            appendLine("📊 *SUPPLIER OFFER METRICS*")
            appendLine()
            appendLine("*PTR (Purchase Price):*")
            appendLine("  ${report.supplierNameA}: ${IndianCurrencyFormatter.formatCurrency(report.ptrA)} (${report.purchasePriceModeA.label})")
            appendLine("  ${report.supplierNameB}: ${IndianCurrencyFormatter.formatCurrency(report.ptrB)} (${report.purchasePriceModeB.label})")
            appendLine()
            appendLine("*Purchase GST Rate:*")
            appendLine("  ${report.supplierNameA}: ${IndianCurrencyFormatter.formatPercentage(report.purchaseTaxRateA)}")
            appendLine("  ${report.supplierNameB}: ${IndianCurrencyFormatter.formatPercentage(report.purchaseTaxRateB)}")
            appendLine()
            appendLine("*Cost per Unit:*")
            appendLine("  ${report.supplierNameA}: ${IndianCurrencyFormatter.formatCurrency(report.costPerUnitA)}")
            appendLine("  ${report.supplierNameB}: ${IndianCurrencyFormatter.formatCurrency(report.costPerUnitB)}")
            appendLine()
            appendLine("*Cost per Pack:*")
            appendLine("  ${report.supplierNameA}: ${IndianCurrencyFormatter.formatCurrency(report.costPerPackA)}")
            appendLine("  ${report.supplierNameB}: ${IndianCurrencyFormatter.formatCurrency(report.costPerPackB)}")
            appendLine()
            appendLine("*Profit per Unit:*")
            appendLine("  ${report.supplierNameA}: ${formatSignedCurrency(report.profitPerUnitA)}")
            appendLine("  ${report.supplierNameB}: ${formatSignedCurrency(report.profitPerUnitB)}")
            appendLine()
            appendLine("*Profit Margin:*")
            appendLine("  ${report.supplierNameA}: ${IndianCurrencyFormatter.formatPercentage(report.profitMarginA)}")
            appendLine("  ${report.supplierNameB}: ${IndianCurrencyFormatter.formatPercentage(report.profitMarginB)}")
            appendLine()
            appendLine("*Markup:*")
            appendLine("  ${report.supplierNameA}: ${IndianCurrencyFormatter.formatPercentage(report.markupA)}")
            appendLine("  ${report.supplierNameB}: ${IndianCurrencyFormatter.formatPercentage(report.markupB)}")
            appendLine(DIVIDER)

            // Footer
            appendLine("✨ _Generated with MargiQ_")
            appendLine("_Know your cost. Own your margin._")
            append("https://github.com/Dr-SuBZeRo/MargiQ")
        }
    }

    private fun winnerLabel(winner: WinnerSide, nameA: String, nameB: String): String {
        return when (winner) {
            WinnerSide.SIDE_A -> nameA
            WinnerSide.SIDE_B -> nameB
            WinnerSide.TIE -> "Tie (Both Equal)"
            WinnerSide.NONE -> "None"
        }
    }

    private fun formatSignedCurrency(amount: BigDecimal): String {
        val formatted = IndianCurrencyFormatter.formatCurrency(amount)
        return if (amount > BigDecimal.ZERO) "+$formatted" else formatted
    }
}
