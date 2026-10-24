package com.example.profitmargincalculator.domain.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object IndianCurrencyFormatter {

    private val inrLocale = Locale.forLanguageTag("en-IN")
    private val decimalSymbols = DecimalFormatSymbols(inrLocale).apply {
        currencySymbol = "₹"
    }

    /**
     * Formats a BigDecimal to Indian Rupee representation (e.g., ₹1,23,456.78).
     */
    fun formatCurrency(amount: BigDecimal?): String {
        if (amount == null) return "₹0.00"
        val isNegative = amount < BigDecimal.ZERO
        val absAmount = amount.abs().setScale(2, RoundingMode.HALF_UP)
        val formatted = formatIndianGrouping(absAmount)
        return if (isNegative) "-₹$formatted" else "₹$formatted"
    }

    /**
     * Formats a percentage BigDecimal (e.g. 62.67%).
     */
    fun formatPercentage(percentage: BigDecimal?): String {
        if (percentage == null) return "N/A"
        val rounded = percentage.setScale(2, RoundingMode.HALF_UP)
        return "$rounded%"
    }

    /**
     * Standard Indian number grouping: 3 digits for first thousand, then groups of 2.
     */
    fun formatIndianGrouping(amount: BigDecimal): String {
        val scaled = amount.setScale(2, RoundingMode.HALF_UP).toPlainString()
        val parts = scaled.split(".")
        val integerPart = parts[0]
        val fractionPart = if (parts.size > 1) parts[1] else "00"

        if (integerPart.length <= 3) {
            return "$integerPart.$fractionPart"
        }

        val lastThree = integerPart.substring(integerPart.length - 3)
        val remaining = integerPart.substring(0, integerPart.length - 3)

        val grouped = StringBuilder()
        var count = 0
        for (i in remaining.length - 1 downTo 0) {
            grouped.insert(0, remaining[i])
            count++
            if (count % 2 == 0 && i != 0) {
                grouped.insert(0, ',')
            }
        }

        return "$grouped,$lastThree.$fractionPart"
    }

    /**
     * Safely parse user input string into BigDecimal.
     */
    fun parseInput(value: String): BigDecimal? {
        val clean = value.replace(",", "").replace("₹", "").replace("%", "").trim()
        if (clean.isEmpty()) return null
        return try {
            BigDecimal(clean)
        } catch (_: Exception) {
            null
        }
    }
}
