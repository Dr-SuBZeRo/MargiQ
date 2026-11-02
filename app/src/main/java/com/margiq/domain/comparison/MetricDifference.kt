package com.margiq.domain.comparison

import com.margiq.domain.util.IndianCurrencyFormatter
import java.math.BigDecimal
import java.math.RoundingMode

enum class MetricDifferenceType {
    CURRENCY,
    PERCENTAGE_POINTS,
    INTEGER,
    RATIO
}

enum class FavourabilityRule {
    LOWER_IS_BETTER,
    HIGHER_IS_BETTER,
    NEUTRAL
}

data class MetricDifference(
    val metricKey: String,
    val metricName: String,
    val rawA: BigDecimal?,
    val rawB: BigDecimal?,
    val type: MetricDifferenceType,
    val favourabilityRule: FavourabilityRule = FavourabilityRule.NEUTRAL,
    val unitSuffix: String = ""
) {
    val isDefined: Boolean
        get() = rawA != null && rawB != null

    val isTie: Boolean
        get() = isDefined && rawA!!.compareTo(rawB!!) == 0

    /**
     * Signed difference convention: Difference = Option B - Option A
     * Positive means Option B has the higher numerical value.
     * Negative means Option A has the higher numerical value.
     */
    val signedDifference: BigDecimal?
        get() = if (isDefined) rawB!!.subtract(rawA!!) else null

    val absoluteDifference: BigDecimal?
        get() = signedDifference?.abs()

    val roundedSignedDifference: BigDecimal?
        get() = signedDifference?.setScale(2, RoundingMode.HALF_UP)

    val roundedAbsoluteDifference: BigDecimal?
        get() = absoluteDifference?.setScale(2, RoundingMode.HALF_UP)

    val higherSide: WinnerSide
        get() {
            val a = rawA ?: return WinnerSide.NONE
            val b = rawB ?: return WinnerSide.NONE
            return when {
                a > b -> WinnerSide.SIDE_A
                b > a -> WinnerSide.SIDE_B
                else -> WinnerSide.TIE
            }
        }

    val lowerSide: WinnerSide
        get() {
            val a = rawA ?: return WinnerSide.NONE
            val b = rawB ?: return WinnerSide.NONE
            return when {
                a < b -> WinnerSide.SIDE_A
                b < a -> WinnerSide.SIDE_B
                else -> WinnerSide.TIE
            }
        }

    val favourableSide: WinnerSide
        get() = when (favourabilityRule) {
            FavourabilityRule.LOWER_IS_BETTER -> lowerSide
            FavourabilityRule.HIGHER_IS_BETTER -> higherSide
            FavourabilityRule.NEUTRAL -> if (isTie) WinnerSide.TIE else WinnerSide.NONE
        }

    fun formatAbsolute(): String {
        val abs = roundedAbsoluteDifference ?: return "N/A"
        return when (type) {
            MetricDifferenceType.CURRENCY -> {
                val formatted = IndianCurrencyFormatter.formatCurrency(abs)
                if (unitSuffix.isNotBlank()) "$formatted$unitSuffix" else formatted
            }
            MetricDifferenceType.PERCENTAGE_POINTS -> {
                "${abs.toPlainString()} percentage points"
            }
            MetricDifferenceType.INTEGER -> {
                val intVal = abs.stripTrailingZeros().toPlainString()
                if (unitSuffix.isNotBlank()) "$intVal$unitSuffix" else intVal
            }
            MetricDifferenceType.RATIO -> {
                val str = abs.toPlainString()
                if (unitSuffix.isNotBlank()) "$str$unitSuffix" else str
            }
        }
    }

    fun formatCompactAbsolute(): String {
        val abs = roundedAbsoluteDifference ?: return "N/A"
        return when (type) {
            MetricDifferenceType.CURRENCY -> {
                val formatted = IndianCurrencyFormatter.formatCurrency(abs)
                if (unitSuffix.isNotBlank()) "$formatted$unitSuffix" else formatted
            }
            MetricDifferenceType.PERCENTAGE_POINTS -> {
                "${abs.toPlainString()} % pts"
            }
            MetricDifferenceType.INTEGER -> {
                val intVal = abs.stripTrailingZeros().toPlainString()
                if (unitSuffix.isNotBlank()) "$intVal$unitSuffix" else intVal
            }
            MetricDifferenceType.RATIO -> {
                val str = abs.toPlainString()
                if (unitSuffix.isNotBlank()) "$str$unitSuffix" else str
            }
        }
    }

    fun formatSigned(): String {
        val signed = roundedSignedDifference ?: return "N/A"
        val sign = if (signed > BigDecimal.ZERO) "+" else if (signed < BigDecimal.ZERO) "-" else ""
        val abs = signed.abs()
        return when (type) {
            MetricDifferenceType.CURRENCY -> {
                val formatted = IndianCurrencyFormatter.formatCurrency(abs)
                val str = "$sign$formatted"
                if (unitSuffix.isNotBlank()) "$str$unitSuffix" else str
            }
            MetricDifferenceType.PERCENTAGE_POINTS -> {
                "$sign${abs.toPlainString()} percentage points"
            }
            MetricDifferenceType.INTEGER -> {
                val intVal = abs.stripTrailingZeros().toPlainString()
                val str = "$sign$intVal"
                if (unitSuffix.isNotBlank()) "$str$unitSuffix" else str
            }
            MetricDifferenceType.RATIO -> {
                val str = "$sign${abs.toPlainString()}"
                if (unitSuffix.isNotBlank()) "$str$unitSuffix" else str
            }
        }
    }

    fun formatCompactSigned(): String {
        val signed = roundedSignedDifference ?: return "N/A"
        val sign = if (signed > BigDecimal.ZERO) "+" else if (signed < BigDecimal.ZERO) "-" else ""
        val abs = signed.abs()
        return when (type) {
            MetricDifferenceType.CURRENCY -> {
                val formatted = IndianCurrencyFormatter.formatCurrency(abs)
                val str = "$sign$formatted"
                if (unitSuffix.isNotBlank()) "$str$unitSuffix" else str
            }
            MetricDifferenceType.PERCENTAGE_POINTS -> {
                "$sign${abs.toPlainString()} % pts"
            }
            MetricDifferenceType.INTEGER -> {
                val intVal = abs.stripTrailingZeros().toPlainString()
                val str = "$sign$intVal"
                if (unitSuffix.isNotBlank()) "$str$unitSuffix" else str
            }
            MetricDifferenceType.RATIO -> {
                val str = "$sign${abs.toPlainString()}"
                if (unitSuffix.isNotBlank()) "$str$unitSuffix" else str
            }
        }
    }

    fun formatCompactSummary(labelA: String, labelB: String): String {
        if (!isDefined) return "Difference: N/A"
        if (isTie) return "Difference: Identical ($labelA = $labelB)"

        val absStr = formatCompactAbsolute()
        return when (favourabilityRule) {
            FavourabilityRule.LOWER_IS_BETTER -> {
                val lowerName = if (lowerSide == WinnerSide.SIDE_A) labelA else labelB
                val higherName = if (higherSide == WinnerSide.SIDE_A) labelA else labelB
                "Diff: $absStr — $lowerName is cheaper ($higherName costs more)"
            }
            FavourabilityRule.HIGHER_IS_BETTER -> {
                val winnerName = if (higherSide == WinnerSide.SIDE_A) labelA else labelB
                "Diff: $absStr — $winnerName has higher $metricName"
            }
            FavourabilityRule.NEUTRAL -> {
                val higherName = if (higherSide == WinnerSide.SIDE_A) labelA else labelB
                "Diff: $absStr — $higherName is higher"
            }
        }
    }

    fun formatNarrative(labelA: String, labelB: String): String {
        if (!isDefined) return "Comparison unavailable"
        if (isTie) return "Both $labelA and $labelB are equal"

        val absStr = formatAbsolute()
        return when (favourabilityRule) {
            FavourabilityRule.LOWER_IS_BETTER -> {
                val cheaper = if (lowerSide == WinnerSide.SIDE_A) labelA else labelB
                "$cheaper is cheaper by $absStr"
            }
            FavourabilityRule.HIGHER_IS_BETTER -> {
                val higher = if (higherSide == WinnerSide.SIDE_A) labelA else labelB
                "$higher has higher $metricName by $absStr"
            }
            FavourabilityRule.NEUTRAL -> {
                val higher = if (higherSide == WinnerSide.SIDE_A) labelA else labelB
                "$higher is higher by $absStr"
            }
        }
    }
}

data class ComparisonDifferences(
    // Cost and purchase metrics
    val costPerUnit: MetricDifference,
    val costPerPack: MetricDifference,
    val ptr: MetricDifference,
    val purchaseTaxAmount: MetricDifference,
    val purchaseTaxRate: MetricDifference,
    val supplierDiscountPercent: MetricDifference,
    val supplierDiscountAmount: MetricDifference,
    val eligibleItc: MetricDifference,

    // Selling metrics
    val customerSellingPrice: MetricDifference,
    val sellingTaxAmount: MetricDifference,
    val sellingTaxRate: MetricDifference,
    val netSalesRevenue: MetricDifference,

    // Profitability metrics
    val profitPerUnit: MetricDifference,
    val profitPerPack: MetricDifference,
    val profitMargin: MetricDifference,
    val markup: MetricDifference,
    val batchProfit: MetricDifference?,

    // Quantity and offer metrics
    val unitsPerPack: MetricDifference,
    val totalPacksReceived: MetricDifference,
    val freePacksReceived: MetricDifference,

    // Base normalization indicator
    val areUnitsPerPackEqual: Boolean = true
) {
    val keyDifferences: List<MetricDifference>
        get() = listOfNotNull(
            costPerUnit,
            profitPerUnit,
            profitMargin,
            markup,
            costPerPack,
            customerSellingPrice
        )

    val allDetailedDifferences: List<MetricDifference>
        get() = listOfNotNull(
            costPerUnit,
            costPerPack,
            ptr,
            purchaseTaxRate,
            purchaseTaxAmount,
            supplierDiscountPercent,
            supplierDiscountAmount,
            eligibleItc,
            customerSellingPrice,
            sellingTaxRate,
            sellingTaxAmount,
            netSalesRevenue,
            profitPerUnit,
            profitPerPack,
            profitMargin,
            markup,
            batchProfit,
            unitsPerPack,
            totalPacksReceived,
            freePacksReceived
        )
}

