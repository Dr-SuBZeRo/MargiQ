package com.margiq.ui.sharing.image

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.margiq.domain.comparison.WinnerSide
import com.margiq.domain.sharing.ShareReport
import com.margiq.domain.util.IndianCurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ShareImageRenderer {

    private const val BITMAP_WIDTH = 1080
    private const val PADDING_X = 54f
    private const val CARD_CORNER_RADIUS = 24f

    fun render(
        context: Context,
        report: ShareReport,
        appearance: ShareImageAppearance
    ): Bitmap {
        val isDark = when (appearance) {
            ShareImageAppearance.DARK -> true
            ShareImageAppearance.LIGHT -> false
            ShareImageAppearance.FOLLOW_APP -> {
                val uiMode = context.resources.configuration.uiMode
                (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            }
        }

        return when (report) {
            is ShareReport.SingleCalculation -> renderSingleCalculation(report, isDark)
            is ShareReport.ProductComparison -> renderProductComparison(report, isDark)
            is ShareReport.SupplierComparison -> renderSupplierComparison(report, isDark)
        }
    }

    private data class Palette(
        val bg: Int,
        val cardBg: Int,
        val cardBorder: Int,
        val textPrimary: Int,
        val textSecondary: Int,
        val headerBg: Int,
        val headerText: Int,
        val brandGreen: Int,
        val heroBg: Int,
        val heroBorder: Int,
        val heroText: Int,
        val heroSubtext: Int,
        val divider: Int,
        val tableAltRow: Int
    )

    private fun getPalette(isDark: Boolean): Palette {
        return if (isDark) {
            Palette(
                bg = Color.parseColor("#0F172A"),
                cardBg = Color.parseColor("#1E293B"),
                cardBorder = Color.parseColor("#334155"),
                textPrimary = Color.parseColor("#F8FAFC"),
                textSecondary = Color.parseColor("#94A3B8"),
                headerBg = Color.parseColor("#111C2D"),
                headerText = Color.parseColor("#F8FAFC"),
                brandGreen = Color.parseColor("#4ADE80"),
                heroBg = Color.parseColor("#14532D"),
                heroBorder = Color.parseColor("#166534"),
                heroText = Color.parseColor("#BBF7D0"),
                heroSubtext = Color.parseColor("#86EFAC"),
                divider = Color.parseColor("#334155"),
                tableAltRow = Color.parseColor("#162032")
            )
        } else {
            Palette(
                bg = Color.parseColor("#F8FAFC"),
                cardBg = Color.parseColor("#FFFFFF"),
                cardBorder = Color.parseColor("#E2E8F0"),
                textPrimary = Color.parseColor("#0F172A"),
                textSecondary = Color.parseColor("#64748B"),
                headerBg = Color.parseColor("#1A365D"),
                headerText = Color.parseColor("#FFFFFF"),
                brandGreen = Color.parseColor("#15803D"),
                heroBg = Color.parseColor("#F0FDF4"),
                heroBorder = Color.parseColor("#BBF7D0"),
                heroText = Color.parseColor("#15803D"),
                heroSubtext = Color.parseColor("#166534"),
                divider = Color.parseColor("#E2E8F0"),
                tableAltRow = Color.parseColor("#F8FAFC")
            )
        }
    }

    private fun renderSingleCalculation(report: ShareReport.SingleCalculation, isDark: Boolean): Bitmap {
        val p = getPalette(isDark)
        val hasUnitsBreakdown = report.unitsPerPack > 1 && report.costPerIndividualUnit != null
        val totalHeight = if (hasUnitsBreakdown) 1780 else 1560

        val bitmap = Bitmap.createBitmap(BITMAP_WIDTH, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(p.bg)

        var y = 0f

        // 1. Top Header Banner
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = p.headerBg }
        canvas.drawRect(0f, 0f, BITMAP_WIDTH.toFloat(), 180f, headerPaint)

        // Logo Wordmark: Magi + green Q
        val magiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.headerText
            textSize = 52f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val qPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isDark) Color.parseColor("#4ADE80") else Color.parseColor("#34D399")
            textSize = 52f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val magiWidth = magiPaint.measureText("Magi")
        canvas.drawText("Magi", PADDING_X, 86f, magiPaint)
        canvas.drawText("Q", PADDING_X + magiWidth, 86f, qPaint)

        // Tagline & Date
        val headerSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        canvas.drawText("Know your cost. Own your margin.", PADDING_X, 130f, headerSubPaint)

        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date(report.timestamp))
        val dateWidth = headerSubPaint.measureText(dateStr)
        canvas.drawText(dateStr, BITMAP_WIDTH - PADDING_X - dateWidth, 130f, headerSubPaint)

        y = 220f

        // 2. Product Title & Pack Info Card
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = p.cardBg }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.cardBorder
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        val contentWidth = BITMAP_WIDTH - (PADDING_X * 2)

        val productTitleCardHeight = if (report.supplierName.isNotBlank()) 150f else 120f
        val productRect = RectF(PADDING_X, y, PADDING_X + contentWidth, y + productTitleCardHeight)
        canvas.drawRoundRect(productRect, CARD_CORNER_RADIUS, CARD_CORNER_RADIUS, cardPaint)
        canvas.drawRoundRect(productRect, CARD_CORNER_RADIUS, CARD_CORNER_RADIUS, borderPaint)

        val prodNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.textPrimary
            textSize = 40f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(report.productName.take(38), PADDING_X + 28f, y + 54f, prodNamePaint)

        val prodSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.textSecondary
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        var subY = y + 96f
        if (report.supplierName.isNotBlank()) {
            canvas.drawText("Supplier: ${report.supplierName.take(30)}  •  Pack: ${report.unitsPerPack} units/pack", PADDING_X + 28f, subY, prodSubPaint)
            subY += 36f
        } else {
            canvas.drawText("Pack Size: ${report.unitsPerPack} unit${if (report.unitsPerPack > 1) "s" else ""}/pack  •  Paid Qty: ${report.quantity} pack${if (report.quantity > 1) "s" else ""}", PADDING_X + 28f, subY, prodSubPaint)
        }

        y += productTitleCardHeight + 24f

        // 3. Hero Profitability Metrics Card (3 columns)
        val heroHeight = 160f
        val heroRect = RectF(PADDING_X, y, PADDING_X + contentWidth, y + heroHeight)
        val heroBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = p.heroBg }
        val heroBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.heroBorder
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(heroRect, CARD_CORNER_RADIUS, CARD_CORNER_RADIUS, heroBgPaint)
        canvas.drawRoundRect(heroRect, CARD_CORNER_RADIUS, CARD_CORNER_RADIUS, heroBorderPaint)

        val colWidth = contentWidth / 3f
        val heroLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.heroSubtext
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val heroValPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.heroText
            textSize = 42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val col1X = PADDING_X + (colWidth * 0.5f)
        val col2X = PADDING_X + (colWidth * 1.5f)
        val col3X = PADDING_X + (colWidth * 2.5f)

        canvas.drawText("PROFIT / PACK", col1X, y + 54f, heroLabelPaint)
        canvas.drawText("+${IndianCurrencyFormatter.formatCurrency(report.profitPerPack)}", col1X, y + 116f, heroValPaint)

        canvas.drawText("PROFIT MARGIN", col2X, y + 54f, heroLabelPaint)
        canvas.drawText(IndianCurrencyFormatter.formatPercentage(report.profitMarginPercent), col2X, y + 116f, heroValPaint)

        canvas.drawText("MARKUP", col3X, y + 54f, heroLabelPaint)
        canvas.drawText(IndianCurrencyFormatter.formatPercentage(report.markupPercent), col3X, y + 116f, heroValPaint)

        y += heroHeight + 24f

        // 4. Financial Breakdown Card
        val breakdownRows = mutableListOf(
            "Purchase Price (PTR)" to "${IndianCurrencyFormatter.formatCurrency(report.enteredPtr)} (${report.purchasePriceMode.label})",
            "Purchase Tax" to "${IndianCurrencyFormatter.formatPercentage(report.purchaseTaxRate)}",
            "Invoice Total" to IndianCurrencyFormatter.formatCurrency(report.invoiceTotal),
            "Eligible Input GST Credit" to IndianCurrencyFormatter.formatCurrency(report.eligibleInputTaxCredit),
            "Effective Cost / Pack" to IndianCurrencyFormatter.formatCurrency(report.effectiveCostPerPack),
            "Selling Price (MRP)" to "${IndianCurrencyFormatter.formatCurrency(report.enteredSellingPrice)} (${report.sellingPriceMode.label})",
            "Selling Tax" to "${IndianCurrencyFormatter.formatPercentage(report.sellingTaxRate)}",
            "Net Sales Revenue" to IndianCurrencyFormatter.formatCurrency(report.netSalesRevenue),
            "Output GST" to IndianCurrencyFormatter.formatCurrency(report.outputTax)
        )

        if (hasUnitsBreakdown) {
            breakdownRows.add("Cost per Individual Unit" to IndianCurrencyFormatter.formatCurrency(report.costPerIndividualUnit ?: java.math.BigDecimal.ZERO))
            breakdownRows.add("Profit per Individual Unit" to "+${IndianCurrencyFormatter.formatCurrency(report.profitPerIndividualUnit ?: java.math.BigDecimal.ZERO)}")
        }

        breakdownRows.add("Total Batch Profit (${report.totalPacksReceived} packs)" to "+${IndianCurrencyFormatter.formatCurrency(report.totalBatchProfit)}")

        val rowHeight = 58f
        val tableHeight = (breakdownRows.size * rowHeight) + 30f
        val tableRect = RectF(PADDING_X, y, PADDING_X + contentWidth, y + tableHeight)
        canvas.drawRoundRect(tableRect, CARD_CORNER_RADIUS, CARD_CORNER_RADIUS, cardPaint)
        canvas.drawRoundRect(tableRect, CARD_CORNER_RADIUS, CARD_CORNER_RADIUS, borderPaint)

        val rowLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.textSecondary
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val rowValPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.textPrimary
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        val highlightRowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.brandGreen
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        var rowY = y + 46f
        for ((idx, row) in breakdownRows.withIndex()) {
            val isLast = idx == breakdownRows.size - 1
            val isUnitProfit = row.first.contains("Profit per Individual Unit")

            if (isLast) {
                // Highlight final total row
                val totalLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = p.textPrimary
                    textSize = 29f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText(row.first, PADDING_X + 28f, rowY, totalLabelPaint)
                canvas.drawText(row.second, PADDING_X + contentWidth - 28f, rowY, highlightRowPaint)
            } else if (isUnitProfit) {
                canvas.drawText(row.first, PADDING_X + 28f, rowY, rowLabelPaint)
                canvas.drawText(row.second, PADDING_X + contentWidth - 28f, rowY, highlightRowPaint)
            } else {
                canvas.drawText(row.first, PADDING_X + 28f, rowY, rowLabelPaint)
                canvas.drawText(row.second, PADDING_X + contentWidth - 28f, rowY, rowValPaint)
            }

            if (!isLast) {
                val divY = rowY + 16f
                val divPaint = Paint().apply {
                    color = p.divider
                    strokeWidth = 1f
                }
                canvas.drawLine(PADDING_X + 20f, divY, PADDING_X + contentWidth - 20f, divY, divPaint)
            }
            rowY += rowHeight
        }

        // 5. Footer Brand Mark
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.textSecondary
            textSize = 22f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Generated with MargiQ  •  Know your cost. Own your margin.", BITMAP_WIDTH / 2f, totalHeight - 48f, footerPaint)

        return bitmap
    }

    private fun renderProductComparison(report: ShareReport.ProductComparison, isDark: Boolean): Bitmap {
        return renderSideBySide(
            title = report.title,
            primaryHighlight = report.primaryHighlightSummary,
            nameA = report.titleA,
            nameB = report.titleB,
            rows = listOf(
                "Cost / Unit" to (IndianCurrencyFormatter.formatCurrency(report.costPerUnitA) to IndianCurrencyFormatter.formatCurrency(report.costPerUnitB)),
                "Cost / Pack" to (IndianCurrencyFormatter.formatCurrency(report.costPerPackA) to IndianCurrencyFormatter.formatCurrency(report.costPerPackB)),
                "Selling Price (MRP)" to (IndianCurrencyFormatter.formatCurrency(report.sellingPriceA) to IndianCurrencyFormatter.formatCurrency(report.sellingPriceB)),
                "Profit / Unit" to ("+${IndianCurrencyFormatter.formatCurrency(report.profitPerUnitA)}" to "+${IndianCurrencyFormatter.formatCurrency(report.profitPerUnitB)}"),
                "Profit Margin" to (IndianCurrencyFormatter.formatPercentage(report.profitMarginA) to IndianCurrencyFormatter.formatPercentage(report.profitMarginB)),
                "Markup" to (IndianCurrencyFormatter.formatPercentage(report.markupA) to IndianCurrencyFormatter.formatPercentage(report.markupB)),
                "Purchase Tax" to (IndianCurrencyFormatter.formatCurrency(report.purchaseTaxA) to IndianCurrencyFormatter.formatCurrency(report.purchaseTaxB)),
                "Selling Tax" to (IndianCurrencyFormatter.formatCurrency(report.sellingTaxA) to IndianCurrencyFormatter.formatCurrency(report.sellingTaxB)),
                "ITC Treatment" to (report.itcTreatmentA to report.itcTreatmentB)
            ),
            winners = listOf(
                "Lowest Cost" to winnerText(report.lowestCostWinner, report.titleA, report.titleB),
                "Highest Profit" to winnerText(report.highestProfitWinner, report.titleA, report.titleB),
                "Highest Margin" to winnerText(report.highestMarginWinner, report.titleA, report.titleB)
            ),
            isDark = isDark,
            timestamp = report.timestamp
        )
    }

    private fun renderSupplierComparison(report: ShareReport.SupplierComparison, isDark: Boolean): Bitmap {
        return renderSideBySide(
            title = report.title,
            primaryHighlight = report.primaryHighlightSummary,
            nameA = report.supplierNameA,
            nameB = report.supplierNameB,
            rows = listOf(
                "Purchase PTR" to ("${IndianCurrencyFormatter.formatCurrency(report.ptrA)} (${report.purchasePriceModeA.label})" to "${IndianCurrencyFormatter.formatCurrency(report.ptrB)} (${report.purchasePriceModeB.label})"),
                "Purchase Tax Rate" to (IndianCurrencyFormatter.formatPercentage(report.purchaseTaxRateA) to IndianCurrencyFormatter.formatPercentage(report.purchaseTaxRateB)),
                "Cost / Pack" to (IndianCurrencyFormatter.formatCurrency(report.costPerPackA) to IndianCurrencyFormatter.formatCurrency(report.costPerPackB)),
                "Cost / Unit" to (IndianCurrencyFormatter.formatCurrency(report.costPerUnitA) to IndianCurrencyFormatter.formatCurrency(report.costPerUnitB)),
                "Profit / Unit" to ("+${IndianCurrencyFormatter.formatCurrency(report.profitPerUnitA)}" to "+${IndianCurrencyFormatter.formatCurrency(report.profitPerUnitB)}"),
                "Profit Margin" to (IndianCurrencyFormatter.formatPercentage(report.profitMarginA) to IndianCurrencyFormatter.formatPercentage(report.profitMarginB)),
                "Markup" to (IndianCurrencyFormatter.formatPercentage(report.markupA) to IndianCurrencyFormatter.formatPercentage(report.markupB))
            ),
            winners = listOf(
                "Lowest Cost" to winnerText(report.lowestCostWinner, report.supplierNameA, report.supplierNameB),
                "Highest Profit" to winnerText(report.highestProfitWinner, report.supplierNameA, report.supplierNameB),
                "Highest Margin" to winnerText(report.highestMarginWinner, report.supplierNameA, report.supplierNameB)
            ),
            isDark = isDark,
            timestamp = report.timestamp
        )
    }

    private fun renderSideBySide(
        title: String,
        primaryHighlight: String,
        nameA: String,
        nameB: String,
        rows: List<Pair<String, Pair<String, String>>>,
        winners: List<Pair<String, String>>,
        isDark: Boolean,
        timestamp: Long
    ): Bitmap {
        val p = getPalette(isDark)
        val totalHeight = 1540
        val bitmap = Bitmap.createBitmap(BITMAP_WIDTH, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(p.bg)

        // Header
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = p.headerBg }
        canvas.drawRect(0f, 0f, BITMAP_WIDTH.toFloat(), 180f, headerPaint)

        val magiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.headerText
            textSize = 52f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val qPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isDark) Color.parseColor("#4ADE80") else Color.parseColor("#34D399")
            textSize = 52f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val magiWidth = magiPaint.measureText("Magi")
        canvas.drawText("Magi", PADDING_X, 86f, magiPaint)
        canvas.drawText("Q", PADDING_X + magiWidth, 86f, qPaint)

        val headerSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        canvas.drawText("Commercial Comparison Report", PADDING_X, 130f, headerSubPaint)

        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date(timestamp))
        val dateWidth = headerSubPaint.measureText(dateStr)
        canvas.drawText(dateStr, BITMAP_WIDTH - PADDING_X - dateWidth, 130f, headerSubPaint)

        var y = 210f
        val contentWidth = BITMAP_WIDTH - (PADDING_X * 2)

        // Highlight Banner Card
        val hlRect = RectF(PADDING_X, y, PADDING_X + contentWidth, y + 100f)
        val hlBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = p.heroBg }
        val hlBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.heroBorder
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(hlRect, CARD_CORNER_RADIUS, CARD_CORNER_RADIUS, hlBgPaint)
        canvas.drawRoundRect(hlRect, CARD_CORNER_RADIUS, CARD_CORNER_RADIUS, hlBorderPaint)

        val hlTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.heroText
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("🏆 $primaryHighlight", PADDING_X + 28f, y + 58f, hlTextPaint)

        y += 124f

        // Winners row (3 cards)
        val wCardWidth = (contentWidth - 24f) / 3f
        val wBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = p.cardBg }
        val wBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.cardBorder
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        val wCatPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.textSecondary
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val wWinnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.brandGreen
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        for ((idx, w) in winners.withIndex()) {
            val wx = PADDING_X + (idx * (wCardWidth + 12f))
            val wRect = RectF(wx, y, wx + wCardWidth, y + 90f)
            canvas.drawRoundRect(wRect, 16f, 16f, wBgPaint)
            canvas.drawRoundRect(wRect, 16f, 16f, wBorderPaint)
            canvas.drawText(w.first, wx + (wCardWidth / 2f), y + 36f, wCatPaint)
            canvas.drawText(w.second.take(16), wx + (wCardWidth / 2f), y + 70f, wWinnerPaint)
        }

        y += 114f

        // Comparison Table Card
        val tableHeight = (rows.size + 1) * 62f + 20f
        val tableRect = RectF(PADDING_X, y, PADDING_X + contentWidth, y + tableHeight)
        canvas.drawRoundRect(tableRect, CARD_CORNER_RADIUS, CARD_CORNER_RADIUS, wBgPaint)
        canvas.drawRoundRect(tableRect, CARD_CORNER_RADIUS, CARD_CORNER_RADIUS, wBorderPaint)

        // Header Row
        val thMetricPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.textSecondary
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val thColPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.textPrimary
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        val col1Right = PADDING_X + contentWidth * 0.68f
        val col2Right = PADDING_X + contentWidth - 28f

        var ty = y + 46f
        canvas.drawText("Metric", PADDING_X + 28f, ty, thMetricPaint)
        canvas.drawText(nameA.take(14), col1Right, ty, thColPaint)
        canvas.drawText(nameB.take(14), col2Right, ty, thColPaint)

        val headerDiv = Paint().apply {
            color = p.cardBorder
            strokeWidth = 2f
        }
        canvas.drawLine(PADDING_X + 20f, ty + 16f, PADDING_X + contentWidth - 20f, ty + 16f, headerDiv)
        ty += 62f

        val tdMetricPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.textSecondary
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val tdValPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.textPrimary
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        val divPaint = Paint().apply {
            color = p.divider
            strokeWidth = 1f
        }

        for ((idx, row) in rows.withIndex()) {
            canvas.drawText(row.first, PADDING_X + 28f, ty, tdMetricPaint)
            canvas.drawText(row.second.first, col1Right, ty, tdValPaint)
            canvas.drawText(row.second.second, col2Right, ty, tdValPaint)

            if (idx < rows.size - 1) {
                canvas.drawLine(PADDING_X + 20f, ty + 16f, PADDING_X + contentWidth - 20f, ty + 16f, divPaint)
            }
            ty += 62f
        }

        // Footer
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.textSecondary
            textSize = 22f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Generated with MargiQ  •  Know your cost. Own your margin.", BITMAP_WIDTH / 2f, totalHeight - 48f, footerPaint)

        return bitmap
    }

    private fun winnerText(winner: WinnerSide, a: String, b: String): String {
        return when (winner) {
            WinnerSide.SIDE_A -> a
            WinnerSide.SIDE_B -> b
            WinnerSide.TIE -> "Tie"
            WinnerSide.NONE -> "None"
        }
    }
}

