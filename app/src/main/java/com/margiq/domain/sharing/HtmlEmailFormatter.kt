package com.margiq.domain.sharing

import com.margiq.domain.comparison.WinnerSide
import com.margiq.domain.util.IndianCurrencyFormatter
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class EmailContent(
    val subject: String,
    val htmlBody: String,
    val plainTextFallback: String
)

object HtmlEmailFormatter {

    fun format(report: ShareReport): EmailContent {
        val subject = when (report) {
            is ShareReport.SingleCalculation -> "[MargiQ Report] ${report.productName} — Profit & Margin Analysis"
            is ShareReport.ProductComparison -> "[MargiQ Report] Product Comparison: ${report.titleA} vs ${report.titleB}"
            is ShareReport.SupplierComparison -> "[MargiQ Report] Supplier Comparison: ${report.productName}"
        }

        val plainText = RichTextShareFormatter.format(report)
        val html = when (report) {
            is ShareReport.SingleCalculation -> formatSingleCalculationHtml(report)
            is ShareReport.ProductComparison -> formatProductComparisonHtml(report)
            is ShareReport.SupplierComparison -> formatSupplierComparisonHtml(report)
        }

        return EmailContent(
            subject = subject,
            htmlBody = html,
            plainTextFallback = plainText
        )
    }

    private fun formatSingleCalculationHtml(report: ShareReport.SingleCalculation): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date(report.timestamp))
        val escapedProduct = escapeHtml(report.productName)
        val escapedSupplier = escapeHtml(report.supplierName)
        val totalUnits = report.unitsPerPack * report.quantity

        return buildString {
            append("""<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>$escapedProduct — Profit &amp; Margin Report</title>
</head>
<body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #F8FAFC; color: #0F172A; margin: 0; padding: 20px; -webkit-text-size-adjust: 100%;">
  <div style="max-width: 640px; margin: 0 auto; background: #FFFFFF; border: 1px solid #E2E8F0; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);">
    
    <!-- Brand Header -->
    <div style="background-color: #1A365D; color: #FFFFFF; padding: 20px 24px;">
      <h1 style="margin: 0; font-size: 22px; font-weight: bold; letter-spacing: 0.5px; color: #FFFFFF;">Margi<span style="color: #4ADE80;">Q</span></h1>
      <p style="margin: 4px 0 0 0; font-size: 12px; color: #94A3B8;">Know your cost. Own your margin. • $dateStr</p>
    </div>

    <!-- Product Summary Card -->
    <div style="padding: 18px 24px; border-bottom: 1px solid #E2E8F0; background-color: #FAFAFA;">
      <h2 style="margin: 0 0 6px 0; font-size: 18px; color: #1A365D;">$escapedProduct</h2>
""")
            if (escapedSupplier.isNotBlank()) {
                append("""      <p style="margin: 3px 0; font-size: 13px; color: #475569;">Supplier: <strong>$escapedSupplier</strong></p>
""")
            }
            append("""      <p style="margin: 3px 0; font-size: 13px; color: #475569;">Pack Size: <strong>${report.unitsPerPack} unit${if (report.unitsPerPack > 1) "s" else ""}/pack</strong> | Paid Qty: <strong>${report.quantity} pack${if (report.quantity > 1) "s" else ""}</strong> | Total Units: <strong>$totalUnits</strong></p>
""")
            if (report.isSupplierOfferEnabled && report.freePacksReceived > 0) {
                append("""      <p style="margin: 6px 0 0 0; font-size: 12px; color: #0F766E; background: #DCFCE7; padding: 6px 10px; border-radius: 6px; display: inline-block;">🎁 Scheme Offer: Buy ${report.offerBuyQuantity}, Get ${report.offerFreeQuantity} Free (+${report.freePacksReceived} free packs, Total: ${report.totalPacksReceived})</p>
""")
            }
            if (report.isSupplierDiscountEnabled && report.supplierDiscountPercent > BigDecimal.ZERO) {
                append("""      <p style="margin: 4px 0 0 0; font-size: 12px; color: #1E40AF; background: #DBEAFE; padding: 4px 8px; border-radius: 6px; display: inline-block;">🏷️ Supplier Discount: ${IndianCurrencyFormatter.formatPercentage(report.supplierDiscountPercent)}</p>
""")
            }
            append("""    </div>

    <!-- Profit Hero Card -->
    <div style="background-color: #F0FDF4; border: 1px solid #BBF7D0; margin: 18px 24px; padding: 16px; border-radius: 8px;">
      <table style="width: 100%; border-collapse: collapse;">
        <tr>
          <td style="text-align: center; padding: 8px; width: 33%;">
            <div style="font-size: 11px; color: #166534; font-weight: 600; text-transform: uppercase;">Profit / Pack</div>
            <div style="font-size: 20px; color: #15803D; font-weight: bold; margin-top: 4px;">${formatSignedCurrency(report.profitPerPack)}</div>
          </td>
          <td style="text-align: center; padding: 8px; width: 34%; border-left: 1px solid #DCFCE7; border-right: 1px solid #DCFCE7;">
            <div style="font-size: 11px; color: #166534; font-weight: 600; text-transform: uppercase;">Profit Margin</div>
            <div style="font-size: 20px; color: #15803D; font-weight: bold; margin-top: 4px;">${IndianCurrencyFormatter.formatPercentage(report.profitMarginPercent)}</div>
          </td>
          <td style="text-align: center; padding: 8px; width: 33%;">
            <div style="font-size: 11px; color: #166534; font-weight: 600; text-transform: uppercase;">Markup</div>
            <div style="font-size: 20px; color: #15803D; font-weight: bold; margin-top: 4px;">${IndianCurrencyFormatter.formatPercentage(report.markupPercent)}</div>
          </td>
        </tr>
      </table>
    </div>

    <!-- Purchase Economics Table -->
    <div style="padding: 0 24px 16px 24px;">
      <h3 style="margin: 0 0 8px 0; font-size: 14px; color: #1A365D; text-transform: uppercase; letter-spacing: 0.5px;">💰 Purchase Economics</h3>
      <table style="width: 100%; border-collapse: collapse; font-size: 13px; border: 1px solid #E2E8F0; border-radius: 6px;">
        <thead>
          <tr style="background-color: #F8FAFC; border-bottom: 2px solid #E2E8F0;">
            <th style="padding: 10px 14px; text-align: left; color: #475569; font-size: 11px; text-transform: uppercase; width: 60%;">Financial Metric</th>
            <th style="padding: 10px 14px; text-align: right; color: #475569; font-size: 11px; text-transform: uppercase; width: 40%;">Amount</th>
          </tr>
        </thead>
        <tbody>
        <tr style="border-bottom: 1px solid #E2E8F0; background-color: #FFFFFF;">
          <td style="padding: 10px 14px; color: #475569; width: 60%;">PTR (Purchase Price)</td>
          <td style="padding: 10px 14px; text-align: right; font-weight: 600; color: #0F172A; width: 40%;">${IndianCurrencyFormatter.formatCurrency(report.enteredPtr)} (${report.purchasePriceMode.label})</td>
        </tr>
        <tr style="border-bottom: 1px solid #E2E8F0; background-color: #F8FAFC;">
          <td style="padding: 10px 14px; color: #475569;">Purchase GST Rate</td>
          <td style="padding: 10px 14px; text-align: right; font-weight: 600; color: #0F172A;">${IndianCurrencyFormatter.formatPercentage(report.purchaseTaxRate)}</td>
        </tr>
        <tr style="border-bottom: 1px solid #E2E8F0; background-color: #FFFFFF;">
          <td style="padding: 10px 14px; color: #475569;">Invoice Total</td>
          <td style="padding: 10px 14px; text-align: right; font-weight: 600; color: #0F172A;">${IndianCurrencyFormatter.formatCurrency(report.invoiceTotal)}</td>
        </tr>
        <tr style="border-bottom: 1px solid #E2E8F0; background-color: #F8FAFC;">
          <td style="padding: 10px 14px; color: #475569;">Eligible GST Input Tax Credit (ITC)</td>
          <td style="padding: 10px 14px; text-align: right; font-weight: 600; color: #047857;">${IndianCurrencyFormatter.formatCurrency(report.eligibleInputTaxCredit)}</td>
        </tr>
        <tr style="background-color: #FFFFFF;">
          <td style="padding: 10px 14px; color: #1A365D; font-weight: bold;">Effective Cost per Pack</td>
          <td style="padding: 10px 14px; text-align: right; font-weight: bold; color: #1A365D;">${IndianCurrencyFormatter.formatCurrency(report.effectiveCostPerPack)}</td>
        </tr>
        </tbody>
      </table>
    </div>

    <!-- Selling & Revenue Table -->
    <div style="padding: 0 24px 16px 24px;">
      <h3 style="margin: 0 0 8px 0; font-size: 14px; color: #1A365D; text-transform: uppercase; letter-spacing: 0.5px;">🛒 Selling &amp; Revenue</h3>
      <table style="width: 100%; border-collapse: collapse; font-size: 13px; border: 1px solid #E2E8F0; border-radius: 6px;">
        <thead>
          <tr style="background-color: #F8FAFC; border-bottom: 2px solid #E2E8F0;">
            <th style="padding: 10px 14px; text-align: left; color: #475569; font-size: 11px; text-transform: uppercase; width: 60%;">Financial Metric</th>
            <th style="padding: 10px 14px; text-align: right; color: #475569; font-size: 11px; text-transform: uppercase; width: 40%;">Amount</th>
          </tr>
        </thead>
        <tbody>
        <tr style="border-bottom: 1px solid #E2E8F0; background-color: #FFFFFF;">
          <td style="padding: 10px 14px; color: #475569; width: 60%;">Selling Price (MRP)</td>
          <td style="padding: 10px 14px; text-align: right; font-weight: 600; color: #0F172A; width: 40%;">${IndianCurrencyFormatter.formatCurrency(report.enteredSellingPrice)} (${report.sellingPriceMode.label})</td>
        </tr>
        <tr style="border-bottom: 1px solid #E2E8F0; background-color: #F8FAFC;">
          <td style="padding: 10px 14px; color: #475569;">Selling GST Rate</td>
          <td style="padding: 10px 14px; text-align: right; font-weight: 600; color: #0F172A;">${IndianCurrencyFormatter.formatPercentage(report.sellingTaxRate)}</td>
        </tr>
        <tr style="border-bottom: 1px solid #E2E8F0; background-color: #FFFFFF;">
          <td style="padding: 10px 14px; color: #475569;">Customer Selling Price</td>
          <td style="padding: 10px 14px; text-align: right; font-weight: 600; color: #0F172A;">${IndianCurrencyFormatter.formatCurrency(report.customerSellingPrice)}</td>
        </tr>
        <tr style="border-bottom: 1px solid #E2E8F0; background-color: #F8FAFC;">
          <td style="padding: 10px 14px; color: #475569;">Net Sales Revenue</td>
          <td style="padding: 10px 14px; text-align: right; font-weight: 600; color: #0F172A;">${IndianCurrencyFormatter.formatCurrency(report.netSalesRevenue)}</td>
        </tr>
        <tr style="background-color: #FFFFFF;">
          <td style="padding: 10px 14px; color: #475569;">Output GST</td>
          <td style="padding: 10px 14px; text-align: right; font-weight: 600; color: #0F172A;">${IndianCurrencyFormatter.formatCurrency(report.outputTax)}</td>
        </tr>
        </tbody>
      </table>
    </div>
""")
            if (report.unitsPerPack > 1 && report.costPerIndividualUnit != null) {
                append("""    <!-- Individual Unit Breakdown -->
    <div style="padding: 0 24px 16px 24px;">
      <h3 style="margin: 0 0 8px 0; font-size: 14px; color: #1A365D; text-transform: uppercase; letter-spacing: 0.5px;">🔢 Per-Unit Breakdown (1 Pack = ${report.unitsPerPack} Units)</h3>
      <table style="width: 100%; border-collapse: collapse; font-size: 13px; border: 1px solid #E2E8F0; border-radius: 6px;">
        <tr style="border-bottom: 1px solid #E2E8F0; background-color: #FFFFFF;">
          <td style="padding: 10px 14px; color: #475569; width: 60%;">Cost per Individual Unit</td>
          <td style="padding: 10px 14px; text-align: right; font-weight: 600; color: #0F172A; width: 40%;">${IndianCurrencyFormatter.formatCurrency(report.costPerIndividualUnit)}</td>
        </tr>
""")
                report.customerPricePerIndividualUnit?.let { mrpUnit ->
                    append("""        <tr style="border-bottom: 1px solid #E2E8F0; background-color: #F8FAFC;">
          <td style="padding: 10px 14px; color: #475569;">MRP per Individual Unit</td>
          <td style="padding: 10px 14px; text-align: right; font-weight: 600; color: #0F172A;">${IndianCurrencyFormatter.formatCurrency(mrpUnit)}</td>
        </tr>
""")
                }
                append("""        <tr style="background-color: #FFFFFF;">
          <td style="padding: 10px 14px; color: #15803D; font-weight: bold;">Profit per Individual Unit</td>
          <td style="padding: 10px 14px; text-align: right; font-weight: bold; color: #15803D;">${formatSignedCurrency(report.profitPerIndividualUnit ?: BigDecimal.ZERO)}</td>
        </tr>
      </table>
    </div>
""")
            }
            append("""    <!-- Total Batch Profit -->
    <div style="margin: 0 24px 20px 24px; padding: 14px 18px; background-color: #F1F5F9; border: 1px solid #CBD5E1; border-radius: 8px;">
      <table style="width: 100%; border-collapse: collapse;">
        <tr>
          <td style="font-size: 14px; font-weight: bold; color: #1A365D;">Total Batch Profit (${report.totalPacksReceived} pack${if (report.totalPacksReceived > 1) "s" else ""})</td>
          <td style="text-align: right; font-size: 18px; font-weight: bold; color: #15803D;">${formatSignedCurrency(report.totalBatchProfit)}</td>
        </tr>
      </table>
    </div>
""")
            if (report.calculationBasis.isNotBlank()) {
                append("""    <div style="padding: 0 24px 14px 24px; font-size: 11px; color: #64748B;">
      Basis: ${escapeHtml(report.calculationBasis)}
    </div>
""")
            }
            append("""    <!-- Footer -->
    <div style="background-color: #F1F5F9; padding: 14px 24px; text-align: center; border-top: 1px solid #E2E8F0; font-size: 11px; color: #64748B;">
      Generated with <strong>MargiQ</strong> • <i>Know your cost. Own your margin.</i> • <a href="https://github.com/Dr-SuBZeRo/MargiQ" style="color: #0369A1; text-decoration: none;">GitHub</a>
    </div>

  </div>
</body>
</html>""")
        }
    }

    private fun formatProductComparisonHtml(report: ShareReport.ProductComparison): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date(report.timestamp))
        val escapedA = escapeHtml(report.titleA)
        val escapedB = escapeHtml(report.titleB)

        return """<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Product Comparison: $escapedA vs $escapedB</title>
</head>
<body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #F8FAFC; color: #0F172A; margin: 0; padding: 20px;">
  <div style="max-width: 640px; margin: 0 auto; background: #FFFFFF; border: 1px solid #E2E8F0; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);">
    
    <!-- Brand Header -->
    <div style="background-color: #1A365D; color: #FFFFFF; padding: 20px 24px;">
      <h1 style="margin: 0; font-size: 22px; font-weight: bold; color: #FFFFFF;">Margi<span style="color: #4ADE80;">Q</span></h1>
      <p style="margin: 4px 0 0 0; font-size: 12px; color: #94A3B8;">Product Comparison Report • $dateStr</p>
    </div>

    <!-- Commercial Highlight Banner -->
    <div style="padding: 16px 24px; background-color: #EFF6FF; border-bottom: 1px solid #DBEAFE;">
      <strong style="color: #1E40AF; font-size: 14px;">🏆 ${escapeHtml(report.primaryHighlightSummary)}</strong>
    </div>

    <!-- Category Winners -->
    <div style="padding: 16px 24px; background-color: #FAFAFA; border-bottom: 1px solid #E2E8F0;">
      <table style="width: 100%; border-collapse: collapse; font-size: 12px;">
        <tr>
          <td style="padding: 6px; text-align: center; width: 33%;">
            <div style="color: #64748B;">Lowest Cost</div>
            <div style="font-weight: bold; color: #15803D; font-size: 13px; margin-top: 2px;">${winnerLabel(report.lowestCostWinner, escapedA, escapedB)}</div>
          </td>
          <td style="padding: 6px; text-align: center; width: 34%; border-left: 1px solid #E2E8F0; border-right: 1px solid #E2E8F0;">
            <div style="color: #64748B;">Highest Profit</div>
            <div style="font-weight: bold; color: #15803D; font-size: 13px; margin-top: 2px;">${winnerLabel(report.highestProfitWinner, escapedA, escapedB)}</div>
          </td>
          <td style="padding: 6px; text-align: center; width: 33%;">
            <div style="color: #64748B;">Highest Margin</div>
            <div style="font-weight: bold; color: #15803D; font-size: 13px; margin-top: 2px;">${winnerLabel(report.highestMarginWinner, escapedA, escapedB)}</div>
          </td>
        </tr>
      </table>
    </div>

    <!-- Side-by-Side Table -->
    <div style="padding: 20px 24px;">
      <table style="width: 100%; border-collapse: collapse; font-size: 13px;">
        <thead>
          <tr style="border-bottom: 2px solid #CBD5E1; background-color: #F1F5F9;">
            <th style="padding: 10px 12px; text-align: left; color: #475569;">Metric</th>
            <th style="padding: 10px 12px; text-align: right; color: #1A365D;">$escapedA</th>
            <th style="padding: 10px 12px; text-align: right; color: #1A365D;">$escapedB</th>
          </tr>
        </thead>
        <tbody>
          <tr style="border-bottom: 1px solid #E2E8F0;">
            <td style="padding: 10px 12px; color: #64748B;">Cost per Unit</td>
            <td style="padding: 10px 12px; text-align: right; font-weight: bold;">${IndianCurrencyFormatter.formatCurrency(report.costPerUnitA)}</td>
            <td style="padding: 10px 12px; text-align: right; font-weight: bold;">${IndianCurrencyFormatter.formatCurrency(report.costPerUnitB)}</td>
          </tr>
          <tr style="border-bottom: 1px solid #E2E8F0; background-color: #F8FAFC;">
            <td style="padding: 10px 12px; color: #64748B;">Cost per Pack</td>
            <td style="padding: 10px 12px; text-align: right;">${IndianCurrencyFormatter.formatCurrency(report.costPerPackA)}</td>
            <td style="padding: 10px 12px; text-align: right;">${IndianCurrencyFormatter.formatCurrency(report.costPerPackB)}</td>
          </tr>
          <tr style="border-bottom: 1px solid #E2E8F0;">
            <td style="padding: 10px 12px; color: #64748B;">Selling Price (MRP)</td>
            <td style="padding: 10px 12px; text-align: right;">${IndianCurrencyFormatter.formatCurrency(report.sellingPriceA)}</td>
            <td style="padding: 10px 12px; text-align: right;">${IndianCurrencyFormatter.formatCurrency(report.sellingPriceB)}</td>
          </tr>
          <tr style="border-bottom: 1px solid #E2E8F0; background-color: #F8FAFC;">
            <td style="padding: 10px 12px; color: #64748B;">Profit per Unit</td>
            <td style="padding: 10px 12px; text-align: right; font-weight: bold; color: #15803D;">${formatSignedCurrency(report.profitPerUnitA)}</td>
            <td style="padding: 10px 12px; text-align: right; font-weight: bold; color: #15803D;">${formatSignedCurrency(report.profitPerUnitB)}</td>
          </tr>
          <tr style="border-bottom: 1px solid #E2E8F0;">
            <td style="padding: 10px 12px; color: #64748B;">Profit Margin</td>
            <td style="padding: 10px 12px; text-align: right; font-weight: bold;">${IndianCurrencyFormatter.formatPercentage(report.profitMarginA)}</td>
            <td style="padding: 10px 12px; text-align: right; font-weight: bold;">${IndianCurrencyFormatter.formatPercentage(report.profitMarginB)}</td>
          </tr>
          <tr style="border-bottom: 1px solid #E2E8F0; background-color: #F8FAFC;">
            <td style="padding: 10px 12px; color: #64748B;">Markup</td>
            <td style="padding: 10px 12px; text-align: right;">${IndianCurrencyFormatter.formatPercentage(report.markupA)}</td>
            <td style="padding: 10px 12px; text-align: right;">${IndianCurrencyFormatter.formatPercentage(report.markupB)}</td>
          </tr>
          <tr style="border-bottom: 1px solid #E2E8F0;">
            <td style="padding: 10px 12px; color: #64748B;">Purchase GST</td>
            <td style="padding: 10px 12px; text-align: right;">${IndianCurrencyFormatter.formatCurrency(report.purchaseTaxA)}</td>
            <td style="padding: 10px 12px; text-align: right;">${IndianCurrencyFormatter.formatCurrency(report.purchaseTaxB)}</td>
          </tr>
          <tr style="border-bottom: 1px solid #E2E8F0; background-color: #F8FAFC;">
            <td style="padding: 10px 12px; color: #64748B;">Selling GST</td>
            <td style="padding: 10px 12px; text-align: right;">${IndianCurrencyFormatter.formatCurrency(report.sellingTaxA)}</td>
            <td style="padding: 10px 12px; text-align: right;">${IndianCurrencyFormatter.formatCurrency(report.sellingTaxB)}</td>
          </tr>
          <tr style="background-color: #FFFFFF;">
            <td style="padding: 10px 12px; color: #64748B;">ITC Treatment</td>
            <td style="padding: 10px 12px; text-align: right;">${report.itcTreatmentA}</td>
            <td style="padding: 10px 12px; text-align: right;">${report.itcTreatmentB}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Footer -->
    <div style="background-color: #F1F5F9; padding: 14px 24px; text-align: center; border-top: 1px solid #E2E8F0; font-size: 11px; color: #64748B;">
      Generated with <strong>MargiQ</strong> • <i>Know your cost. Own your margin.</i> • <a href="https://github.com/Dr-SuBZeRo/MargiQ" style="color: #0369A1; text-decoration: none;">GitHub</a>
    </div>
  </div>
</body>
</html>"""
    }

    private fun formatSupplierComparisonHtml(report: ShareReport.SupplierComparison): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date(report.timestamp))
        val escapedProduct = escapeHtml(report.productName)
        val escapedA = escapeHtml(report.supplierNameA)
        val escapedB = escapeHtml(report.supplierNameB)

        return """<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Supplier Comparison: $escapedProduct</title>
</head>
<body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #F8FAFC; color: #0F172A; margin: 0; padding: 20px;">
  <div style="max-width: 640px; margin: 0 auto; background: #FFFFFF; border: 1px solid #E2E8F0; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);">
    
    <!-- Brand Header -->
    <div style="background-color: #1A365D; color: #FFFFFF; padding: 20px 24px;">
      <h1 style="margin: 0; font-size: 22px; font-weight: bold; color: #FFFFFF;">Margi<span style="color: #4ADE80;">Q</span></h1>
      <p style="margin: 4px 0 0 0; font-size: 12px; color: #94A3B8;">Supplier Comparison Report • $dateStr</p>
    </div>

    <!-- Base Product Card -->
    <div style="padding: 16px 24px; border-bottom: 1px solid #E2E8F0; background-color: #FAFAFA;">
      <h2 style="margin: 0 0 4px 0; font-size: 17px; color: #1A365D;">$escapedProduct</h2>
      <p style="margin: 0; font-size: 13px; color: #64748B;">MRP: <strong>${IndianCurrencyFormatter.formatCurrency(report.baseSellingPrice)}</strong> (${report.baseSellingPriceMode.label}) | Tax: <strong>${IndianCurrencyFormatter.formatPercentage(report.baseSellingTaxRate)}</strong> | Pack Size: <strong>${report.unitsPerPack} unit${if (report.unitsPerPack > 1) "s" else ""}/pack</strong></p>
    </div>

    <!-- Commercial Highlight Banner -->
    <div style="padding: 16px 24px; background-color: #EFF6FF; border-bottom: 1px solid #DBEAFE;">
      <strong style="color: #1E40AF; font-size: 14px;">🏆 ${escapeHtml(report.primaryHighlightSummary)}</strong>
    </div>

    <!-- Category Winners -->
    <div style="padding: 16px 24px; background-color: #FAFAFA; border-bottom: 1px solid #E2E8F0;">
      <table style="width: 100%; border-collapse: collapse; font-size: 12px;">
        <tr>
          <td style="padding: 6px; text-align: center; width: 33%;">
            <div style="color: #64748B;">Lowest Cost</div>
            <div style="font-weight: bold; color: #15803D; font-size: 13px; margin-top: 2px;">${winnerLabel(report.lowestCostWinner, escapedA, escapedB)}</div>
          </td>
          <td style="padding: 6px; text-align: center; width: 34%; border-left: 1px solid #E2E8F0; border-right: 1px solid #E2E8F0;">
            <div style="color: #64748B;">Highest Profit</div>
            <div style="font-weight: bold; color: #15803D; font-size: 13px; margin-top: 2px;">${winnerLabel(report.highestProfitWinner, escapedA, escapedB)}</div>
          </td>
          <td style="padding: 6px; text-align: center; width: 33%;">
            <div style="color: #64748B;">Highest Margin</div>
            <div style="font-weight: bold; color: #15803D; font-size: 13px; margin-top: 2px;">${winnerLabel(report.highestMarginWinner, escapedA, escapedB)}</div>
          </td>
        </tr>
      </table>
    </div>

    <!-- Side-by-Side Table -->
    <div style="padding: 20px 24px;">
      <table style="width: 100%; border-collapse: collapse; font-size: 13px;">
        <thead>
          <tr style="border-bottom: 2px solid #CBD5E1; background-color: #F1F5F9;">
            <th style="padding: 10px 12px; text-align: left; color: #475569;">Metric</th>
            <th style="padding: 10px 12px; text-align: right; color: #1A365D;">$escapedA</th>
            <th style="padding: 10px 12px; text-align: right; color: #1A365D;">$escapedB</th>
          </tr>
        </thead>
        <tbody>
          <tr style="border-bottom: 1px solid #E2E8F0;">
            <td style="padding: 10px 12px; color: #64748B;">Purchase PTR</td>
            <td style="padding: 10px 12px; text-align: right;">${IndianCurrencyFormatter.formatCurrency(report.ptrA)}</td>
            <td style="padding: 10px 12px; text-align: right;">${IndianCurrencyFormatter.formatCurrency(report.ptrB)}</td>
          </tr>
          <tr style="border-bottom: 1px solid #E2E8F0; background-color: #F8FAFC;">
            <td style="padding: 10px 12px; color: #64748B;">Purchase Tax Rate</td>
            <td style="padding: 10px 12px; text-align: right;">${IndianCurrencyFormatter.formatPercentage(report.purchaseTaxRateA)}</td>
            <td style="padding: 10px 12px; text-align: right;">${IndianCurrencyFormatter.formatPercentage(report.purchaseTaxRateB)}</td>
          </tr>
          <tr style="border-bottom: 1px solid #E2E8F0;">
            <td style="padding: 10px 12px; color: #64748B;">Cost per Unit</td>
            <td style="padding: 10px 12px; text-align: right; font-weight: bold;">${IndianCurrencyFormatter.formatCurrency(report.costPerUnitA)}</td>
            <td style="padding: 10px 12px; text-align: right; font-weight: bold;">${IndianCurrencyFormatter.formatCurrency(report.costPerUnitB)}</td>
          </tr>
          <tr style="border-bottom: 1px solid #E2E8F0; background-color: #F8FAFC;">
            <td style="padding: 10px 12px; color: #64748B;">Cost per Pack</td>
            <td style="padding: 10px 12px; text-align: right;">${IndianCurrencyFormatter.formatCurrency(report.costPerPackA)}</td>
            <td style="padding: 10px 12px; text-align: right;">${IndianCurrencyFormatter.formatCurrency(report.costPerPackB)}</td>
          </tr>
          <tr style="border-bottom: 1px solid #E2E8F0;">
            <td style="padding: 10px 12px; color: #64748B;">Profit per Unit</td>
            <td style="padding: 10px 12px; text-align: right; font-weight: bold; color: #15803D;">${formatSignedCurrency(report.profitPerUnitA)}</td>
            <td style="padding: 10px 12px; text-align: right; font-weight: bold; color: #15803D;">${formatSignedCurrency(report.profitPerUnitB)}</td>
          </tr>
          <tr style="border-bottom: 1px solid #E2E8F0; background-color: #F8FAFC;">
            <td style="padding: 10px 12px; color: #64748B;">Profit Margin</td>
            <td style="padding: 10px 12px; text-align: right; font-weight: bold;">${IndianCurrencyFormatter.formatPercentage(report.profitMarginA)}</td>
            <td style="padding: 10px 12px; text-align: right; font-weight: bold;">${IndianCurrencyFormatter.formatPercentage(report.profitMarginB)}</td>
          </tr>
          <tr style="background-color: #FFFFFF;">
            <td style="padding: 10px 12px; color: #64748B;">Markup</td>
            <td style="padding: 10px 12px; text-align: right;">${IndianCurrencyFormatter.formatPercentage(report.markupA)}</td>
            <td style="padding: 10px 12px; text-align: right;">${IndianCurrencyFormatter.formatPercentage(report.markupB)}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Footer -->
    <div style="background-color: #F1F5F9; padding: 14px 24px; text-align: center; border-top: 1px solid #E2E8F0; font-size: 11px; color: #64748B;">
      Generated with <strong>MargiQ</strong> • <i>Know your cost. Own your margin.</i> • <a href="https://github.com/Dr-SuBZeRo/MargiQ" style="color: #0369A1; text-decoration: none;">GitHub</a>
    </div>
  </div>
</body>
</html>"""
    }

    private fun winnerLabel(winner: WinnerSide, nameA: String, nameB: String): String {
        return when (winner) {
            WinnerSide.SIDE_A -> nameA
            WinnerSide.SIDE_B -> nameB
            WinnerSide.TIE -> "Tie"
            WinnerSide.NONE -> "-"
        }
    }

    private fun formatSignedCurrency(amount: BigDecimal): String {
        val formatted = IndianCurrencyFormatter.formatCurrency(amount)
        return if (amount > BigDecimal.ZERO) "+$formatted" else formatted
    }

    fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}
