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

        return buildString {
            append("""<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>$escapedProduct — Profit Report</title>
</head>
<body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #F8FAFC; color: #0F172A; margin: 0; padding: 20px;">
  <div style="max-width: 640px; margin: 0 auto; background: #FFFFFF; border: 1px solid #E2E8F0; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);">
    
    <!-- Header -->
    <div style="background-color: #1A365D; color: #FFFFFF; padding: 20px 24px;">
      <h1 style="margin: 0; font-size: 20px; font-weight: bold; letter-spacing: 0.5px;">Magi<span style="color: #4ADE80;">Q</span></h1>
      <p style="margin: 4px 0 0 0; font-size: 12px; color: #94A3B8;">Know your cost. Own your margin. • $dateStr</p>
    </div>

    <!-- Title Banner -->
    <div style="padding: 20px 24px; border-bottom: 1px solid #E2E8F0;">
      <h2 style="margin: 0; font-size: 18px; color: #1A365D;">$escapedProduct</h2>
""")
            if (escapedSupplier.isNotBlank()) {
                append("""      <p style="margin: 4px 0 0 0; font-size: 14px; color: #64748B;">Supplier: <strong>$escapedSupplier</strong></p>
""")
            }
            append("""      <p style="margin: 4px 0 0 0; font-size: 13px; color: #64748B;">Pack: ${report.unitsPerPack} unit${if (report.unitsPerPack > 1) "s" else ""}/pack | Paid Qty: ${report.quantity} pack${if (report.quantity > 1) "s" else ""}</p>
""")
            if (report.isSupplierOfferEnabled && report.freePacksReceived > 0) {
                append("""      <p style="margin: 6px 0 0 0; font-size: 13px; color: #0F766E; background: #DCFCE7; padding: 6px 10px; border-radius: 6px; display: inline-block;">🎁 Scheme: Buy ${report.offerBuyQuantity}, Get ${report.offerFreeQuantity} Free (+${report.freePacksReceived} free packs, Total: ${report.totalPacksReceived})</p>
""")
            }
            append("""    </div>

    <!-- Profit Hero Card -->
    <div style="background-color: #F0FDF4; border: 1px solid #BBF7D0; margin: 20px 24px; padding: 16px; border-radius: 8px;">
      <table style="width: 100%; border-collapse: collapse;">
        <tr>
          <td style="text-align: center; padding: 8px;">
            <div style="font-size: 12px; color: #166534; font-weight: 600;">PROFIT / PACK</div>
            <div style="font-size: 22px; color: #15803D; font-weight: bold; margin-top: 4px;">+${IndianCurrencyFormatter.formatCurrency(report.profitPerPack)}</div>
          </td>
          <td style="text-align: center; padding: 8px; border-left: 1px solid #DCFCE7; border-right: 1px solid #DCFCE7;">
            <div style="font-size: 12px; color: #166534; font-weight: 600;">PROFIT MARGIN</div>
            <div style="font-size: 22px; color: #15803D; font-weight: bold; margin-top: 4px;">${IndianCurrencyFormatter.formatPercentage(report.profitMarginPercent)}</div>
          </td>
          <td style="text-align: center; padding: 8px;">
            <div style="font-size: 12px; color: #166534; font-weight: 600;">MARKUP</div>
            <div style="font-size: 22px; color: #15803D; font-weight: bold; margin-top: 4px;">${IndianCurrencyFormatter.formatPercentage(report.markupPercent)}</div>
          </td>
        </tr>
      </table>
    </div>

    <!-- Financial Breakdown Table -->
    <div style="padding: 0 24px 20px 24px;">
      <table style="width: 100%; border-collapse: collapse; font-size: 13px;">
        <tr style="border-bottom: 1px solid #E2E8F0;">
          <td style="padding: 8px 0; color: #475569;">Purchase Price (PTR)</td>
          <td style="padding: 8px 0; text-align: right; font-weight: 600;">${IndianCurrencyFormatter.formatCurrency(report.enteredPtr)} (${report.purchasePriceMode.label})</td>
        </tr>
        <tr style="border-bottom: 1px solid #E2E8F0;">
          <td style="padding: 8px 0; color: #475569;">Purchase Tax</td>
          <td style="padding: 8px 0; text-align: right; font-weight: 600;">${IndianCurrencyFormatter.formatPercentage(report.purchaseTaxRate)}</td>
        </tr>
        <tr style="border-bottom: 1px solid #E2E8F0;">
          <td style="padding: 8px 0; color: #475569;">Invoice Total</td>
          <td style="padding: 8px 0; text-align: right; font-weight: 600;">${IndianCurrencyFormatter.formatCurrency(report.invoiceTotal)}</td>
        </tr>
        <tr style="border-bottom: 1px solid #E2E8F0;">
          <td style="padding: 8px 0; color: #475569;">Eligible GST Input Tax Credit</td>
          <td style="padding: 8px 0; text-align: right; font-weight: 600; color: #047857;">${IndianCurrencyFormatter.formatCurrency(report.eligibleInputTaxCredit)}</td>
        </tr>
        <tr style="border-bottom: 1px solid #E2E8F0;">
          <td style="padding: 8px 0; color: #475569;">Effective Economic Cost / Pack</td>
          <td style="padding: 8px 0; text-align: right; font-weight: bold; color: #1A365D;">${IndianCurrencyFormatter.formatCurrency(report.effectiveCostPerPack)}</td>
        </tr>
        <tr style="border-bottom: 1px solid #E2E8F0;">
          <td style="padding: 8px 0; color: #475569;">Selling Price (MRP)</td>
          <td style="padding: 8px 0; text-align: right; font-weight: 600;">${IndianCurrencyFormatter.formatCurrency(report.enteredSellingPrice)} (${report.sellingPriceMode.label})</td>
        </tr>
        <tr style="border-bottom: 1px solid #E2E8F0;">
          <td style="padding: 8px 0; color: #475569;">Net Sales Revenue</td>
          <td style="padding: 8px 0; text-align: right; font-weight: 600;">${IndianCurrencyFormatter.formatCurrency(report.netSalesRevenue)}</td>
        </tr>
        <tr style="border-bottom: 1px solid #E2E8F0;">
          <td style="padding: 8px 0; color: #475569;">Output GST</td>
          <td style="padding: 8px 0; text-align: right; font-weight: 600;">${IndianCurrencyFormatter.formatCurrency(report.outputTax)}</td>
        </tr>
""")
            if (report.unitsPerPack > 1 && report.costPerIndividualUnit != null) {
                append("""        <tr style="border-bottom: 1px solid #E2E8F0; background-color: #F8FAFC;">
          <td style="padding: 8px 0; color: #1A365D; font-weight: 600;">Cost per Individual Unit</td>
          <td style="padding: 8px 0; text-align: right; font-weight: 600;">${IndianCurrencyFormatter.formatCurrency(report.costPerIndividualUnit)}</td>
        </tr>
        <tr style="border-bottom: 1px solid #E2E8F0; background-color: #F8FAFC;">
          <td style="padding: 8px 0; color: #1A365D; font-weight: 600;">Profit per Individual Unit</td>
          <td style="padding: 8px 0; text-align: right; font-weight: bold; color: #15803D;">+${IndianCurrencyFormatter.formatCurrency(report.profitPerIndividualUnit ?: BigDecimal.ZERO)}</td>
        </tr>
""")
            }
            append("""        <tr style="border-top: 2px solid #1A365D; background-color: #F1F5F9;">
          <td style="padding: 10px 0; color: #1A365D; font-weight: bold;">Total Batch Profit (${report.totalPacksReceived} packs)</td>
          <td style="padding: 10px 0; text-align: right; font-weight: bold; font-size: 15px; color: #15803D;">+${IndianCurrencyFormatter.formatCurrency(report.totalBatchProfit)}</td>
        </tr>
      </table>
    </div>

    <!-- Footer -->
    <div style="background-color: #F1F5F9; padding: 14px 24px; text-align: center; border-top: 1px solid #E2E8F0; font-size: 11px; color: #64748B;">
      Generated with <strong>MargiQ</strong> • Know your cost. Own your margin. • <a href="https://github.com/Dr-SuBZeRo/MargiQ" style="color: #0369A1; text-decoration: none;">GitHub</a>
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
    <div style="background-color: #1A365D; color: #FFFFFF; padding: 20px 24px;">
      <h1 style="margin: 0; font-size: 20px; font-weight: bold;">Magi<span style="color: #4ADE80;">Q</span></h1>
      <p style="margin: 4px 0 0 0; font-size: 12px; color: #94A3B8;">Product Comparison Report • $dateStr</p>
    </div>

    <div style="padding: 16px 24px; background-color: #EFF6FF; border-bottom: 1px solid #DBEAFE;">
      <strong style="color: #1E40AF; font-size: 14px;">🏆 ${escapeHtml(report.primaryHighlightSummary)}</strong>
    </div>

    <div style="padding: 20px 24px;">
      <table style="width: 100%; border-collapse: collapse; font-size: 13px;">
        <thead>
          <tr style="border-bottom: 2px solid #CBD5E1; color: #475569;">
            <th style="padding: 8px 0; text-align: left;">Metric</th>
            <th style="padding: 8px 0; text-align: right;">$escapedA</th>
            <th style="padding: 8px 0; text-align: right;">$escapedB</th>
          </tr>
        </thead>
        <tbody>
          <tr style="border-bottom: 1px solid #E2E8F0;">
            <td style="padding: 8px 0; color: #64748B;">Cost / Unit</td>
            <td style="padding: 8px 0; text-align: right; font-weight: bold;">${IndianCurrencyFormatter.formatCurrency(report.costPerUnitA)}</td>
            <td style="padding: 8px 0; text-align: right; font-weight: bold;">${IndianCurrencyFormatter.formatCurrency(report.costPerUnitB)}</td>
          </tr>
          <tr style="border-bottom: 1px solid #E2E8F0;">
            <td style="padding: 8px 0; color: #64748B;">Cost / Pack</td>
            <td style="padding: 8px 0; text-align: right;">${IndianCurrencyFormatter.formatCurrency(report.costPerPackA)}</td>
            <td style="padding: 8px 0; text-align: right;">${IndianCurrencyFormatter.formatCurrency(report.costPerPackB)}</td>
          </tr>
          <tr style="border-bottom: 1px solid #E2E8F0;">
            <td style="padding: 8px 0; color: #64748B;">Selling Price (MRP)</td>
            <td style="padding: 8px 0; text-align: right;">${IndianCurrencyFormatter.formatCurrency(report.sellingPriceA)}</td>
            <td style="padding: 8px 0; text-align: right;">${IndianCurrencyFormatter.formatCurrency(report.sellingPriceB)}</td>
          </tr>
          <tr style="border-bottom: 1px solid #E2E8F0;">
            <td style="padding: 8px 0; color: #64748B;">Profit / Unit</td>
            <td style="padding: 8px 0; text-align: right; font-weight: bold; color: #15803D;">+${IndianCurrencyFormatter.formatCurrency(report.profitPerUnitA)}</td>
            <td style="padding: 8px 0; text-align: right; font-weight: bold; color: #15803D;">+${IndianCurrencyFormatter.formatCurrency(report.profitPerUnitB)}</td>
          </tr>
          <tr style="border-bottom: 1px solid #E2E8F0;">
            <td style="padding: 8px 0; color: #64748B;">Profit Margin</td>
            <td style="padding: 8px 0; text-align: right; font-weight: bold;">${IndianCurrencyFormatter.formatPercentage(report.profitMarginA)}</td>
            <td style="padding: 8px 0; text-align: right; font-weight: bold;">${IndianCurrencyFormatter.formatPercentage(report.profitMarginB)}</td>
          </tr>
          <tr style="border-bottom: 1px solid #E2E8F0;">
            <td style="padding: 8px 0; color: #64748B;">Markup</td>
            <td style="padding: 8px 0; text-align: right;">${IndianCurrencyFormatter.formatPercentage(report.markupA)}</td>
            <td style="padding: 8px 0; text-align: right;">${IndianCurrencyFormatter.formatPercentage(report.markupB)}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div style="background-color: #F1F5F9; padding: 14px 24px; text-align: center; border-top: 1px solid #E2E8F0; font-size: 11px; color: #64748B;">
      Generated with <strong>MargiQ</strong> • Know your cost. Own your margin. • <a href="https://github.com/Dr-SuBZeRo/MargiQ" style="color: #0369A1; text-decoration: none;">GitHub</a>
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
    <div style="background-color: #1A365D; color: #FFFFFF; padding: 20px 24px;">
      <h1 style="margin: 0; font-size: 20px; font-weight: bold;">Magi<span style="color: #4ADE80;">Q</span></h1>
      <p style="margin: 4px 0 0 0; font-size: 12px; color: #94A3B8;">Supplier Comparison Report • $dateStr</p>
    </div>

    <div style="padding: 16px 24px; border-bottom: 1px solid #E2E8F0;">
      <h2 style="margin: 0; font-size: 17px; color: #1A365D;">$escapedProduct</h2>
      <p style="margin: 4px 0 0 0; font-size: 13px; color: #64748B;">MRP: ${IndianCurrencyFormatter.formatCurrency(report.baseSellingPrice)} (${report.baseSellingPriceMode.label}) | Tax: ${IndianCurrencyFormatter.formatPercentage(report.baseSellingTaxRate)} | Pack Size: ${report.unitsPerPack} unit${if (report.unitsPerPack > 1) "s" else ""}</p>
    </div>

    <div style="padding: 16px 24px; background-color: #EFF6FF; border-bottom: 1px solid #DBEAFE;">
      <strong style="color: #1E40AF; font-size: 14px;">🏆 ${escapeHtml(report.primaryHighlightSummary)}</strong>
    </div>

    <div style="padding: 20px 24px;">
      <table style="width: 100%; border-collapse: collapse; font-size: 13px;">
        <thead>
          <tr style="border-bottom: 2px solid #CBD5E1; color: #475569;">
            <th style="padding: 8px 0; text-align: left;">Metric</th>
            <th style="padding: 8px 0; text-align: right;">$escapedA</th>
            <th style="padding: 8px 0; text-align: right;">$escapedB</th>
          </tr>
        </thead>
        <tbody>
          <tr style="border-bottom: 1px solid #E2E8F0;">
            <td style="padding: 8px 0; color: #64748B;">Purchase PTR</td>
            <td style="padding: 8px 0; text-align: right;">${IndianCurrencyFormatter.formatCurrency(report.ptrA)}</td>
            <td style="padding: 8px 0; text-align: right;">${IndianCurrencyFormatter.formatCurrency(report.ptrB)}</td>
          </tr>
          <tr style="border-bottom: 1px solid #E2E8F0;">
            <td style="padding: 8px 0; color: #64748B;">Cost / Unit</td>
            <td style="padding: 8px 0; text-align: right; font-weight: bold;">${IndianCurrencyFormatter.formatCurrency(report.costPerUnitA)}</td>
            <td style="padding: 8px 0; text-align: right; font-weight: bold;">${IndianCurrencyFormatter.formatCurrency(report.costPerUnitB)}</td>
          </tr>
          <tr style="border-bottom: 1px solid #E2E8F0;">
            <td style="padding: 8px 0; color: #64748B;">Cost / Pack</td>
            <td style="padding: 8px 0; text-align: right;">${IndianCurrencyFormatter.formatCurrency(report.costPerPackA)}</td>
            <td style="padding: 8px 0; text-align: right;">${IndianCurrencyFormatter.formatCurrency(report.costPerPackB)}</td>
          </tr>
          <tr style="border-bottom: 1px solid #E2E8F0;">
            <td style="padding: 8px 0; color: #64748B;">Profit / Unit</td>
            <td style="padding: 8px 0; text-align: right; font-weight: bold; color: #15803D;">+${IndianCurrencyFormatter.formatCurrency(report.profitPerUnitA)}</td>
            <td style="padding: 8px 0; text-align: right; font-weight: bold; color: #15803D;">+${IndianCurrencyFormatter.formatCurrency(report.profitPerUnitB)}</td>
          </tr>
          <tr style="border-bottom: 1px solid #E2E8F0;">
            <td style="padding: 8px 0; color: #64748B;">Profit Margin</td>
            <td style="padding: 8px 0; text-align: right; font-weight: bold;">${IndianCurrencyFormatter.formatPercentage(report.profitMarginA)}</td>
            <td style="padding: 8px 0; text-align: right; font-weight: bold;">${IndianCurrencyFormatter.formatPercentage(report.profitMarginB)}</td>
          </tr>
          <tr style="border-bottom: 1px solid #E2E8F0;">
            <td style="padding: 8px 0; color: #64748B;">Markup</td>
            <td style="padding: 8px 0; text-align: right;">${IndianCurrencyFormatter.formatPercentage(report.markupA)}</td>
            <td style="padding: 8px 0; text-align: right;">${IndianCurrencyFormatter.formatPercentage(report.markupB)}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div style="background-color: #F1F5F9; padding: 14px 24px; text-align: center; border-top: 1px solid #E2E8F0; font-size: 11px; color: #64748B;">
      Generated with <strong>MargiQ</strong> • Know your cost. Own your margin. • <a href="https://github.com/Dr-SuBZeRo/MargiQ" style="color: #0369A1; text-decoration: none;">GitHub</a>
    </div>
  </div>
</body>
</html>"""
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

