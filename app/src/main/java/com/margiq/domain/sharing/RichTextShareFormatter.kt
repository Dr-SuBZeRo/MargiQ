package com.margiq.domain.sharing

import com.margiq.domain.comparison.WinnerSide
import com.margiq.domain.util.IndianCurrencyFormatter
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RichTextShareFormatter {

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

        return buildString {
            appendLine("📊 MargiQ — Profit & Margin Report")
            appendLine("Date: $dateStr")
            appendLine("==================================")
            appendLine("Product: ${report.productName}")
            if (report.supplierName.isNotBlank()) {
                appendLine("Supplier: ${report.supplierName}")
            }
            appendLine("Pack Size: ${report.unitsPerPack} unit${if (report.unitsPerPack > 1) "s" else ""} per pack")
            appendLine("Purchase Qty: ${report.quantity} pack${if (report.quantity > 1) "s" else ""}")
            if (report.isSupplierOfferEnabled && report.freePacksReceived > 0) {
                appendLine("Scheme Offer: Buy ${report.offerBuyQuantity}, Get ${report.offerFreeQuantity} Free (+${report.freePacksReceived} free packs)")
                appendLine("Total Received: ${report.totalPacksReceived} packs")
            }
            if (report.isSupplierDiscountEnabled && report.supplierDiscountPercent > BigDecimal.ZERO) {
                appendLine("Supplier Discount: ${IndianCurrencyFormatter.formatPercentage(report.supplierDiscountPercent)}")
            }

            appendLine("----------------------------------")
            appendLine("PURCHASE ECONOMICS:")
            appendLine("• PTR (Purchase Price): ${IndianCurrencyFormatter.formatCurrency(report.enteredPtr)} (${report.purchasePriceMode.label})")
            appendLine("• Purchase Tax Rate: ${IndianCurrencyFormatter.formatPercentage(report.purchaseTaxRate)}")
            appendLine("• Invoice Total: ${IndianCurrencyFormatter.formatCurrency(report.invoiceTotal)}")
            appendLine("• Eligible Input GST Credit: ${IndianCurrencyFormatter.formatCurrency(report.eligibleInputTaxCredit)}")
            appendLine("• Effective Cost/Pack: ${IndianCurrencyFormatter.formatCurrency(report.effectiveCostPerPack)}")

            appendLine("----------------------------------")
            appendLine("SELLING & REVENUE:")
            appendLine("• MRP (Selling Price): ${IndianCurrencyFormatter.formatCurrency(report.enteredSellingPrice)} (${report.sellingPriceMode.label})")
            appendLine("• Selling Tax Rate: ${IndianCurrencyFormatter.formatPercentage(report.sellingTaxRate)}")
            appendLine("• Customer Selling Price: ${IndianCurrencyFormatter.formatCurrency(report.customerSellingPrice)}")
            appendLine("• Net Sales Revenue: ${IndianCurrencyFormatter.formatCurrency(report.netSalesRevenue)}")
            appendLine("• Output GST: ${IndianCurrencyFormatter.formatCurrency(report.outputTax)}")

            appendLine("----------------------------------")
            appendLine("PROFITABILITY METRICS:")
            appendLine("• Profit Per Pack: ${IndianCurrencyFormatter.formatCurrency(report.profitPerPack)}")
            appendLine("• Profit Margin: ${IndianCurrencyFormatter.formatPercentage(report.profitMarginPercent)}")
            appendLine("• Markup: ${IndianCurrencyFormatter.formatPercentage(report.markupPercent)}")

            if (report.unitsPerPack > 1 && report.costPerIndividualUnit != null) {
                appendLine("----------------------------------")
                appendLine("PER-UNIT BREAKDOWN (1 pack = ${report.unitsPerPack} units):")
                appendLine("• Cost Per Unit: ${IndianCurrencyFormatter.formatCurrency(report.costPerIndividualUnit)}")
                report.customerPricePerIndividualUnit?.let {
                    appendLine("• MRP Per Unit: ${IndianCurrencyFormatter.formatCurrency(it)}")
                }
                report.profitPerIndividualUnit?.let {
                    appendLine("• Profit Per Unit: ${IndianCurrencyFormatter.formatCurrency(it)}")
                }
            }

            appendLine("----------------------------------")
            appendLine("TOTAL BATCH PROFIT (${report.totalPacksReceived} packs): ${IndianCurrencyFormatter.formatCurrency(report.totalBatchProfit)}")
            if (report.calculationBasis.isNotBlank()) {
                appendLine("Basis: ${report.calculationBasis}")
            }
            appendLine("==================================")
            appendLine("Calculated with MargiQ")
            appendLine("Know your cost. Own your margin.")
            append("https://github.com/Dr-SuBZeRo/MargiQ")
        }
    }

    private fun formatProductComparison(report: ShareReport.ProductComparison): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date(report.timestamp))

        return buildString {
            appendLine("⚖️ MargiQ — Product Comparison Report")
            appendLine("Date: $dateStr")
            appendLine("==================================")
            appendLine("Option A: ${report.titleA} (${report.unitsPerPackA} units/pack)")
            appendLine("Option B: ${report.titleB} (${report.unitsPerPackB} units/pack)")
            appendLine("----------------------------------")
            appendLine("COMMERCIAL HIGHLIGHT:")
            appendLine("🏆 ${report.primaryHighlightSummary}")
            report.secondaryHighlightSummary?.let {
                appendLine("ℹ️ $it")
            }
            appendLine("----------------------------------")
            appendLine("CATEGORY WINNERS:")
            appendLine("• Lowest Cost: ${winnerLabel(report.lowestCostWinner, report.titleA, report.titleB)}")
            appendLine("• Highest Profit: ${winnerLabel(report.highestProfitWinner, report.titleA, report.titleB)}")
            appendLine("• Highest Margin: ${winnerLabel(report.highestMarginWinner, report.titleA, report.titleB)}")
            appendLine("----------------------------------")
            appendLine("SIDE-BY-SIDE METRICS:")
            appendLine("Cost / Unit:")
            appendLine("  ${report.titleA}: ${IndianCurrencyFormatter.formatCurrency(report.costPerUnitA)}")
            appendLine("  ${report.titleB}: ${IndianCurrencyFormatter.formatCurrency(report.costPerUnitB)}")
            appendLine("Cost / Pack:")
            appendLine("  ${report.titleA}: ${IndianCurrencyFormatter.formatCurrency(report.costPerPackA)}")
            appendLine("  ${report.titleB}: ${IndianCurrencyFormatter.formatCurrency(report.costPerPackB)}")
            appendLine("Selling Price (MRP):")
            appendLine("  ${report.titleA}: ${IndianCurrencyFormatter.formatCurrency(report.sellingPriceA)}")
            appendLine("  ${report.titleB}: ${IndianCurrencyFormatter.formatCurrency(report.sellingPriceB)}")
            appendLine("Profit / Unit:")
            appendLine("  ${report.titleA}: ${IndianCurrencyFormatter.formatCurrency(report.profitPerUnitA)}")
            appendLine("  ${report.titleB}: ${IndianCurrencyFormatter.formatCurrency(report.profitPerUnitB)}")
            appendLine("Profit Margin:")
            appendLine("  ${report.titleA}: ${IndianCurrencyFormatter.formatPercentage(report.profitMarginA)}")
            appendLine("  ${report.titleB}: ${IndianCurrencyFormatter.formatPercentage(report.profitMarginB)}")
            appendLine("Markup:")
            appendLine("  ${report.titleA}: ${IndianCurrencyFormatter.formatPercentage(report.markupA)}")
            appendLine("  ${report.titleB}: ${IndianCurrencyFormatter.formatPercentage(report.markupB)}")
            appendLine("Purchase Tax:")
            appendLine("  ${report.titleA}: ${IndianCurrencyFormatter.formatCurrency(report.purchaseTaxA)}")
            appendLine("  ${report.titleB}: ${IndianCurrencyFormatter.formatCurrency(report.purchaseTaxB)}")
            appendLine("Selling Tax:")
            appendLine("  ${report.titleA}: ${IndianCurrencyFormatter.formatCurrency(report.sellingTaxA)}")
            appendLine("  ${report.titleB}: ${IndianCurrencyFormatter.formatCurrency(report.sellingTaxB)}")
            appendLine("==================================")
            appendLine("Calculated with MargiQ")
            appendLine("Know your cost. Own your margin.")
            append("https://github.com/Dr-SuBZeRo/MargiQ")
        }
    }

    private fun formatSupplierComparison(report: ShareReport.SupplierComparison): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date(report.timestamp))

        return buildString {
            appendLine("🏢 MargiQ — Supplier Comparison Report")
            appendLine("Date: $dateStr")
            appendLine("==================================")
            appendLine("Product: ${report.productName} (${report.unitsPerPack} unit${if (report.unitsPerPack > 1) "s" else ""}/pack)")
            appendLine("Base MRP: ${IndianCurrencyFormatter.formatCurrency(report.baseSellingPrice)} (${report.baseSellingPriceMode.label}) | Tax: ${IndianCurrencyFormatter.formatPercentage(report.baseSellingTaxRate)}")
            appendLine("----------------------------------")
            appendLine("Supplier Alpha: ${report.supplierNameA}")
            appendLine("Supplier Beta: ${report.supplierNameB}")
            appendLine("----------------------------------")
            appendLine("COMMERCIAL HIGHLIGHT:")
            appendLine("🏆 ${report.primaryHighlightSummary}")
            report.secondaryHighlightSummary?.let {
                appendLine("ℹ️ $it")
            }
            appendLine("----------------------------------")
            appendLine("CATEGORY WINNERS:")
            appendLine("• Lowest Cost: ${winnerLabel(report.lowestCostWinner, report.supplierNameA, report.supplierNameB)}")
            appendLine("• Highest Profit: ${winnerLabel(report.highestProfitWinner, report.supplierNameA, report.supplierNameB)}")
            appendLine("• Highest Margin: ${winnerLabel(report.highestMarginWinner, report.supplierNameA, report.supplierNameB)}")
            appendLine("----------------------------------")
            appendLine("SIDE-BY-SIDE METRICS:")
            appendLine("PTR (Purchase Price):")
            appendLine("  ${report.supplierNameA}: ${IndianCurrencyFormatter.formatCurrency(report.ptrA)} (${report.purchasePriceModeA.label})")
            appendLine("  ${report.supplierNameB}: ${IndianCurrencyFormatter.formatCurrency(report.ptrB)} (${report.purchasePriceModeB.label})")
            appendLine("Cost / Pack:")
            appendLine("  ${report.supplierNameA}: ${IndianCurrencyFormatter.formatCurrency(report.costPerPackA)}")
            appendLine("  ${report.supplierNameB}: ${IndianCurrencyFormatter.formatCurrency(report.costPerPackB)}")
            appendLine("Cost / Unit:")
            appendLine("  ${report.supplierNameA}: ${IndianCurrencyFormatter.formatCurrency(report.costPerUnitA)}")
            appendLine("  ${report.supplierNameB}: ${IndianCurrencyFormatter.formatCurrency(report.costPerUnitB)}")
            appendLine("Profit / Unit:")
            appendLine("  ${report.supplierNameA}: ${IndianCurrencyFormatter.formatCurrency(report.profitPerUnitA)}")
            appendLine("  ${report.supplierNameB}: ${IndianCurrencyFormatter.formatCurrency(report.profitPerUnitB)}")
            appendLine("Profit Margin:")
            appendLine("  ${report.supplierNameA}: ${IndianCurrencyFormatter.formatPercentage(report.profitMarginA)}")
            appendLine("  ${report.supplierNameB}: ${IndianCurrencyFormatter.formatPercentage(report.profitMarginB)}")
            appendLine("Markup:")
            appendLine("  ${report.supplierNameA}: ${IndianCurrencyFormatter.formatPercentage(report.markupA)}")
            appendLine("  ${report.supplierNameB}: ${IndianCurrencyFormatter.formatPercentage(report.markupB)}")
            appendLine("==================================")
            appendLine("Calculated with MargiQ")
            appendLine("Know your cost. Own your margin.")
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
}

