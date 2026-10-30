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
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ShareImageRenderer {

    private const val BITMAP_WIDTH = 1080
    private const val PADDING_X = 54f
    private const val CARD_CORNER_RADIUS = 20f

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
                bg = Color.parseColor("#0B0F19"),
                cardBg = Color.parseColor("#151D2E"),
                cardBorder = Color.parseColor("#253248"),
                textPrimary = Color.parseColor("#F8FAFC"),
                textSecondary = Color.parseColor("#94A3B8"),
                headerBg = Color.parseColor("#0F172A"),
                headerText = Color.parseColor("#F8FAFC"),
                brandGreen = Color.parseColor("#10B981"),
                heroBg = Color.parseColor("#064E3B"),
                heroBorder = Color.parseColor("#047857"),
                heroText = Color.parseColor("#D1FAE5"),
                heroSubtext = Color.parseColor("#A7F3D0"),
                divider = Color.parseColor("#1E293B"),
                tableAltRow = Color.parseColor("#101827")
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
                brandGreen = Color.parseColor("#10B981"),
                heroBg = Color.parseColor("#ECFDF5"),
                heroBorder = Color.parseColor("#A7F3D0"),
                heroText = Color.parseColor("#047857"),
                heroSubtext = Color.parseColor("#065F46"),
                divider = Color.parseColor("#E2E8F0"),
                tableAltRow = Color.parseColor("#F8FAFC")
            )
        }
    }

    private fun renderSingleCalculation(report: ShareReport.SingleCalculation, isDark: Boolean): Bitmap {
        val p = getPalette(isDark)
        val hasUnitsBreakdown = report.unitsPerPack > 1 && report.costPerIndividualUnit != null
        val totalUnits = report.unitsPerPack * report.quantity

        // Compute dynamic height based on content
        val headerH = 160f
        var productCardH = 110f
        if (report.supplierName.isNotBlank()) productCardH += 34f
        if (report.isSupplierOfferEnabled && report.freePacksReceived > 0) productCardH += 38f
        if (report.isSupplierDiscountEnabled && report.supplierDiscountPercent > BigDecimal.ZERO) productCardH += 34f

        val heroCardH = 135f
        val breakdownRowsCount = 10
        val breakdownCardH = 46f + (breakdownRowsCount * 44f) + 16f
        val unitsCardH = if (hasUnitsBreakdown) 150f else 0f
        val batchCardH = 88f
        val footerH = 64f
        val spacing = 20f

        var calculatedHeight = headerH + spacing + productCardH + spacing + heroCardH + spacing + breakdownCardH + spacing + batchCardH + footerH + 40f
        if (hasUnitsBreakdown) {
            calculatedHeight += spacing + unitsCardH
        }

        val totalHeight = calculatedHeight.toInt()
        val bitmap = Bitmap.createBitmap(BITMAP_WIDTH, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(p.bg)

        // 1. Header with MargiQ Wordmark
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = p.headerBg }
        canvas.drawRect(0f, 0f, BITMAP_WIDTH.toFloat(), headerH, headerPaint)

        val margiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.headerText
            textSize = 52f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val qPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.brandGreen
            textSize = 52f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val margiWidth = margiPaint.measureText("Margi")
        canvas.drawText("Margi", PADDING_X, 86f, margiPaint)
        canvas.drawText("Q", PADDING_X + margiWidth, 86f, qPaint)

        val taglinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isDark) Color.parseColor("#94A3B8") else Color.parseColor("#CBD5E1")
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        }
        canvas.drawText("Know your cost. Own your margin.", PADDING_X, 126f, taglinePaint)

        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateText = dateFormat.format(Date(report.timestamp))
        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isDark) Color.parseColor("#94A3B8") else Color.parseColor("#CBD5E1")
            textSize = 20f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(dateText, BITMAP_WIDTH - PADDING_X, 126f, datePaint)

        var y = headerH + spacing
        val cardWidth = BITMAP_WIDTH - 2 * PADDING_X

        // 2. Product Summary Card
        drawCard(canvas, PADDING_X, y, cardWidth, productCardH, p)
        var cardInnerY = y + 40f

        val prodNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.textPrimary
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(report.productName.take(38), PADDING_X + 24f, cardInnerY, prodNamePaint)
        cardInnerY += 34f

        if (report.supplierName.isNotBlank()) {
            val suppPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = p.textSecondary
                textSize = 22f
            }
            canvas.drawText("Supplier: ${report.supplierName}", PADDING_X + 24f, cardInnerY, suppPaint)
            cardInnerY += 32f
        }

        val packInfoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.textSecondary
            textSize = 22f
        }
        canvas.drawText("Pack Size: ${report.unitsPerPack} units/pack  •  Paid Qty: ${report.quantity} packs  •  Total Units: $totalUnits", PADDING_X + 24f, cardInnerY, packInfoPaint)
        cardInnerY += 34f

        if (report.isSupplierOfferEnabled && report.freePacksReceived > 0) {
            val schemePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = p.brandGreen
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("🎁 Scheme: Buy ${report.offerBuyQuantity}, Get ${report.offerFreeQuantity} Free (+${report.freePacksReceived} free packs, Total: ${report.totalPacksReceived})", PADDING_X + 24f, cardInnerY, schemePaint)
        }

        y += productCardH + spacing

        // 3. Profit Hero Section
        val heroRect = RectF(PADDING_X, y, PADDING_X + cardWidth, y + heroCardH)
        val heroBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = p.heroBg }
        val heroBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.heroBorder
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(heroRect, CARD_CORNER_RADIUS, CARD_CORNER_RADIUS, heroBgPaint)
        canvas.drawRoundRect(heroRect, CARD_CORNER_RADIUS, CARD_CORNER_RADIUS, heroBorderPaint)

        val colW = cardWidth / 3f
        val heroLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.heroSubtext
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val heroValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.heroText
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        // Col 1: Profit/Pack
        canvas.drawText("PROFIT / PACK", PADDING_X + colW * 0.5f, y + 46f, heroLabelPaint)
        canvas.drawText(formatSignedCurrency(report.profitPerPack), PADDING_X + colW * 0.5f, y + 96f, heroValuePaint)

        // Divider
        val heroDivPaint = Paint().apply {
            color = p.heroBorder
            strokeWidth = 1.5f
        }
        canvas.drawLine(PADDING_X + colW, y + 20f, PADDING_X + colW, y + heroCardH - 20f, heroDivPaint)

        // Col 2: Profit Margin
        canvas.drawText("PROFIT MARGIN", PADDING_X + colW * 1.5f, y + 46f, heroLabelPaint)
        canvas.drawText(IndianCurrencyFormatter.formatPercentage(report.profitMarginPercent), PADDING_X + colW * 1.5f, y + 96f, heroValuePaint)

        // Divider
        canvas.drawLine(PADDING_X + colW * 2f, y + 20f, PADDING_X + colW * 2f, y + heroCardH - 20f, heroDivPaint)

        // Col 3: Markup
        canvas.drawText("MARKUP", PADDING_X + colW * 2.5f, y + 46f, heroLabelPaint)
        canvas.drawText(IndianCurrencyFormatter.formatPercentage(report.markupPercent), PADDING_X + colW * 2.5f, y + 96f, heroValuePaint)

        y += heroCardH + spacing

        // 4. Financial Breakdown Card
        drawCard(canvas, PADDING_X, y, cardWidth, breakdownCardH, p)
        val secTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.textPrimary
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("PURCHASE & SELLING BREAKDOWN", PADDING_X + 24f, y + 36f, secTitlePaint)

        val breakdownRows = listOf(
            "Purchase Price (PTR)" to "${IndianCurrencyFormatter.formatCurrency(report.enteredPtr)} (${report.purchasePriceMode.label})",
            "Purchase GST Rate" to IndianCurrencyFormatter.formatPercentage(report.purchaseTaxRate),
            "Invoice Total" to IndianCurrencyFormatter.formatCurrency(report.invoiceTotal),
            "Eligible Input GST Credit (ITC)" to IndianCurrencyFormatter.formatCurrency(report.eligibleInputTaxCredit),
            "Effective Cost per Pack" to IndianCurrencyFormatter.formatCurrency(report.effectiveCostPerPack),
            "Selling Price (MRP)" to "${IndianCurrencyFormatter.formatCurrency(report.enteredSellingPrice)} (${report.sellingPriceMode.label})",
            "Selling GST Rate" to IndianCurrencyFormatter.formatPercentage(report.sellingTaxRate),
            "Customer Selling Price" to IndianCurrencyFormatter.formatCurrency(report.customerSellingPrice),
            "Net Sales Revenue" to IndianCurrencyFormatter.formatCurrency(report.netSalesRevenue),
            "Output GST" to IndianCurrencyFormatter.formatCurrency(report.outputTax)
        )

        var rowY = y + 54f
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.textSecondary
            textSize = 21f
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.textPrimary
            textSize = 21f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        val altRowPaint = Paint().apply { color = p.tableAltRow }
        val rowDivPaint = Paint().apply {
            color = p.divider
            strokeWidth = 1f
        }

        breakdownRows.forEachIndexed { i, (lbl, v) ->
            if (i % 2 == 1) {
                canvas.drawRect(PADDING_X + 10f, rowY, PADDING_X + cardWidth - 10f, rowY + 44f, altRowPaint)
            }
            canvas.drawText(lbl, PADDING_X + 24f, rowY + 30f, labelPaint)
            canvas.drawText(v, PADDING_X + cardWidth - 24f, rowY + 30f, valuePaint)
            canvas.drawLine(PADDING_X + 20f, rowY + 44f, PADDING_X + cardWidth - 20f, rowY + 44f, rowDivPaint)
            rowY += 44f
        }

        y += breakdownCardH + spacing

        // 5. Individual Units Breakdown (if unitsPerPack > 1)
        if (hasUnitsBreakdown) {
            drawCard(canvas, PADDING_X, y, cardWidth, unitsCardH, p)
            canvas.drawText("PER-UNIT BREAKDOWN (1 Pack = ${report.unitsPerPack} Units)", PADDING_X + 24f, y + 36f, secTitlePaint)

            var uRowY = y + 50f
            val unitRows = mutableListOf(
                "Cost per Individual Unit" to IndianCurrencyFormatter.formatCurrency(report.costPerIndividualUnit)
            )
            report.customerPricePerIndividualUnit?.let {
                unitRows.add("MRP per Individual Unit" to IndianCurrencyFormatter.formatCurrency(it))
            }
            report.profitPerIndividualUnit?.let {
                unitRows.add("Profit per Individual Unit" to formatSignedCurrency(it))
            }

            unitRows.forEachIndexed { i, (lbl, v) ->
                if (i % 2 == 1) {
                    canvas.drawRect(PADDING_X + 10f, uRowY, PADDING_X + cardWidth - 10f, uRowY + 44f, altRowPaint)
                }
                canvas.drawText(lbl, PADDING_X + 24f, uRowY + 30f, labelPaint)
                canvas.drawText(v, PADDING_X + cardWidth - 24f, uRowY + 30f, valuePaint)
                canvas.drawLine(PADDING_X + 20f, uRowY + 44f, PADDING_X + cardWidth - 20f, uRowY + 44f, rowDivPaint)
                uRowY += 44f
            }
            y += unitsCardH + spacing
        }

        // 6. Total Batch Profit Card
        drawCard(canvas, PADDING_X, y, cardWidth, batchCardH, p)
        val batchLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.textPrimary
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val batchValPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.brandGreen
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("Total Batch Profit (${report.totalPacksReceived} packs)", PADDING_X + 24f, y + 54f, batchLabelPaint)
        canvas.drawText(formatSignedCurrency(report.totalBatchProfit), PADDING_X + cardWidth - 24f, y + 56f, batchValPaint)

        y += batchCardH + spacing

        // 7. Footer
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.textSecondary
            textSize = 20f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Generated with MargiQ  •  Know your cost. Own your margin.", BITMAP_WIDTH / 2f, y + 28f, footerPaint)

        return bitmap
    }

    private fun renderProductComparison(report: ShareReport.ProductComparison, isDark: Boolean): Bitmap {
        return renderSideBySide(
            title = "PRODUCT COMPARISON",
            primaryHighlight = report.primaryHighlightSummary,
            nameA = report.titleA,
            nameB = report.titleB,
            rows = listOf(
                "Cost per Unit" to (IndianCurrencyFormatter.formatCurrency(report.costPerUnitA) to IndianCurrencyFormatter.formatCurrency(report.costPerUnitB)),
                "Cost per Pack" to (IndianCurrencyFormatter.formatCurrency(report.costPerPackA) to IndianCurrencyFormatter.formatCurrency(report.costPerPackB)),
                "Selling Price (MRP)" to (IndianCurrencyFormatter.formatCurrency(report.sellingPriceA) to IndianCurrencyFormatter.formatCurrency(report.sellingPriceB)),
                "Profit per Unit" to (formatSignedCurrency(report.profitPerUnitA) to formatSignedCurrency(report.profitPerUnitB)),
                "Profit Margin" to (IndianCurrencyFormatter.formatPercentage(report.profitMarginA) to IndianCurrencyFormatter.formatPercentage(report.profitMarginB)),
                "Markup" to (IndianCurrencyFormatter.formatPercentage(report.markupA) to IndianCurrencyFormatter.formatPercentage(report.markupB)),
                "Purchase GST" to (IndianCurrencyFormatter.formatCurrency(report.purchaseTaxA) to IndianCurrencyFormatter.formatCurrency(report.purchaseTaxB)),
                "Selling GST" to (IndianCurrencyFormatter.formatCurrency(report.sellingTaxA) to IndianCurrencyFormatter.formatCurrency(report.sellingTaxB)),
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
            title = "SUPPLIER COMPARISON — ${report.productName.take(24)}",
            primaryHighlight = report.primaryHighlightSummary,
            nameA = report.supplierNameA,
            nameB = report.supplierNameB,
            rows = listOf(
                "Purchase Price (PTR)" to (IndianCurrencyFormatter.formatCurrency(report.ptrA) to IndianCurrencyFormatter.formatCurrency(report.ptrB)),
                "Purchase GST Rate" to (IndianCurrencyFormatter.formatPercentage(report.purchaseTaxRateA) to IndianCurrencyFormatter.formatPercentage(report.purchaseTaxRateB)),
                "Cost per Unit" to (IndianCurrencyFormatter.formatCurrency(report.costPerUnitA) to IndianCurrencyFormatter.formatCurrency(report.costPerUnitB)),
                "Cost per Pack" to (IndianCurrencyFormatter.formatCurrency(report.costPerPackA) to IndianCurrencyFormatter.formatCurrency(report.costPerPackB)),
                "Profit per Unit" to (formatSignedCurrency(report.profitPerUnitA) to formatSignedCurrency(report.profitPerUnitB)),
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
        val headerH = 160f
        val highlightH = 80f
        val winnersH = 110f
        val tableH = 50f + (rows.size * 48f) + 16f
        val footerH = 64f
        val spacing = 20f

        val totalHeight = (headerH + spacing + highlightH + spacing + winnersH + spacing + tableH + footerH + 40f).toInt()
        val bitmap = Bitmap.createBitmap(BITMAP_WIDTH, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(p.bg)

        // Header
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = p.headerBg }
        canvas.drawRect(0f, 0f, BITMAP_WIDTH.toFloat(), headerH, headerPaint)

        val margiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.headerText
            textSize = 52f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val qPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.brandGreen
            textSize = 52f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val margiWidth = margiPaint.measureText("Margi")
        canvas.drawText("Margi", PADDING_X, 86f, margiPaint)
        canvas.drawText("Q", PADDING_X + margiWidth, 86f, qPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isDark) Color.parseColor("#94A3B8") else Color.parseColor("#CBD5E1")
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(title, PADDING_X, 126f, titlePaint)

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val dateText = dateFormat.format(Date(timestamp))
        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isDark) Color.parseColor("#94A3B8") else Color.parseColor("#CBD5E1")
            textSize = 20f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(dateText, BITMAP_WIDTH - PADDING_X, 126f, datePaint)

        var y = headerH + spacing
        val cardWidth = BITMAP_WIDTH - 2 * PADDING_X

        // Highlight Card
        val hlRect = RectF(PADDING_X, y, PADDING_X + cardWidth, y + highlightH)
        val hlPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isDark) Color.parseColor("#1E3A5F") else Color.parseColor("#EFF6FF")
        }
        val hlBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isDark) Color.parseColor("#2563EB") else Color.parseColor("#BFDBFE")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(hlRect, CARD_CORNER_RADIUS, CARD_CORNER_RADIUS, hlPaint)
        canvas.drawRoundRect(hlRect, CARD_CORNER_RADIUS, CARD_CORNER_RADIUS, hlBorder)

        val hlTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isDark) Color.parseColor("#93C5FD") else Color.parseColor("#1E40AF")
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("🏆  $primaryHighlight", PADDING_X + 24f, y + 48f, hlTextPaint)

        y += highlightH + spacing

        // Winners Card
        drawCard(canvas, PADDING_X, y, cardWidth, winnersH, p)
        val wColW = cardWidth / winners.size
        val wLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.textSecondary
            textSize = 18f
            textAlign = Paint.Align.CENTER
        }
        val wValPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.brandGreen
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        winners.forEachIndexed { i, (cat, win) ->
            val cx = PADDING_X + (i + 0.5f) * wColW
            canvas.drawText(cat, cx, y + 40f, wLabelPaint)
            canvas.drawText(win, cx, y + 80f, wValPaint)
            if (i > 0) {
                val divX = PADDING_X + i * wColW
                val divPaint = Paint().apply {
                    color = p.divider
                    strokeWidth = 1f
                }
                canvas.drawLine(divX, y + 16f, divX, y + winnersH - 16f, divPaint)
            }
        }

        y += winnersH + spacing

        // Comparison Table Card
        drawCard(canvas, PADDING_X, y, cardWidth, tableH, p)
        val thPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.textPrimary
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val thRightA = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.textPrimary
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        val thRightB = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.textPrimary
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        val colA_X = PADDING_X + cardWidth * 0.65f
        val colB_X = PADDING_X + cardWidth - 24f

        canvas.drawText("Metric", PADDING_X + 24f, y + 36f, thPaint)
        canvas.drawText(nameA.take(14), colA_X, y + 36f, thRightA)
        canvas.drawText(nameB.take(14), colB_X, y + 36f, thRightB)

        var tRowY = y + 50f
        val lblPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.textSecondary
            textSize = 20f
        }
        val valPaintA = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.textPrimary
            textSize = 20f
            textAlign = Paint.Align.RIGHT
        }
        val valPaintB = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.textPrimary
            textSize = 20f
            textAlign = Paint.Align.RIGHT
        }
        val altRowPaint = Paint().apply { color = p.tableAltRow }
        val rowDivPaint = Paint().apply {
            color = p.divider
            strokeWidth = 1f
        }

        rows.forEachIndexed { i, (lbl, vals) ->
            if (i % 2 == 1) {
                canvas.drawRect(PADDING_X + 10f, tRowY, PADDING_X + cardWidth - 10f, tRowY + 48f, altRowPaint)
            }
            canvas.drawText(lbl, PADDING_X + 24f, tRowY + 32f, lblPaint)
            canvas.drawText(vals.first, colA_X, tRowY + 32f, valPaintA)
            canvas.drawText(vals.second, colB_X, tRowY + 32f, valPaintB)
            canvas.drawLine(PADDING_X + 20f, tRowY + 48f, PADDING_X + cardWidth - 20f, tRowY + 48f, rowDivPaint)
            tRowY += 48f
        }

        y += tableH + spacing

        // Footer
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.textSecondary
            textSize = 20f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Generated with MargiQ  •  Know your cost. Own your margin.", BITMAP_WIDTH / 2f, y + 28f, footerPaint)

        return bitmap
    }

    private fun drawCard(canvas: Canvas, x: Float, y: Float, w: Float, h: Float, p: Palette) {
        val rect = RectF(x, y, x + w, y + h)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = p.cardBg }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = p.cardBorder
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        canvas.drawRoundRect(rect, CARD_CORNER_RADIUS, CARD_CORNER_RADIUS, bgPaint)
        canvas.drawRoundRect(rect, CARD_CORNER_RADIUS, CARD_CORNER_RADIUS, borderPaint)
    }

    private fun winnerText(winner: WinnerSide, a: String, b: String): String {
        return when (winner) {
            WinnerSide.SIDE_A -> a
            WinnerSide.SIDE_B -> b
            WinnerSide.TIE -> "Tie"
            WinnerSide.NONE -> "-"
        }
    }

    private fun formatSignedCurrency(amount: BigDecimal): String {
        val formatted = IndianCurrencyFormatter.formatCurrency(amount)
        return if (amount > BigDecimal.ZERO) "+$formatted" else formatted
    }
}
