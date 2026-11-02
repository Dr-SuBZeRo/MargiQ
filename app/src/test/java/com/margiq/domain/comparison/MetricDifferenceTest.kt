package com.margiq.domain.comparison

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class MetricDifferenceTest {

    @Test
    fun testPositiveDifferenceOptionBHigher() {
        // Option A: 5.00, Option B: 6.00 -> Difference (B - A) = +1.00
        val diff = MetricDifference(
            metricKey = "cost_per_unit",
            metricName = "Cost / Unit",
            rawA = BigDecimal("5.00"),
            rawB = BigDecimal("6.00"),
            type = MetricDifferenceType.CURRENCY,
            favourabilityRule = FavourabilityRule.LOWER_IS_BETTER,
            unitSuffix = "/unit"
        )

        assertTrue(diff.isDefined)
        assertFalse(diff.isTie)
        assertEquals(BigDecimal("1.00"), diff.signedDifference)
        assertEquals(BigDecimal("1.00"), diff.absoluteDifference)
        assertEquals(WinnerSide.SIDE_B, diff.higherSide)
        assertEquals(WinnerSide.SIDE_A, diff.lowerSide)
        // Since lower is better, Option A is the favourable side
        assertEquals(WinnerSide.SIDE_A, diff.favourableSide)
        assertEquals("₹1.00/unit", diff.formatAbsolute())
        assertEquals("+₹1.00/unit", diff.formatSigned())
        assertEquals("Option A is cheaper by ₹1.00/unit", diff.formatNarrative("Option A", "Option B"))
    }

    @Test
    fun testNegativeDifferenceOptionAHigher() {
        // Option A: 10.00, Option B: 8.00 -> Difference (B - A) = -2.00
        val diff = MetricDifference(
            metricKey = "profit_per_unit",
            metricName = "Profit / Unit",
            rawA = BigDecimal("10.00"),
            rawB = BigDecimal("8.00"),
            type = MetricDifferenceType.CURRENCY,
            favourabilityRule = FavourabilityRule.HIGHER_IS_BETTER,
            unitSuffix = "/unit"
        )

        assertTrue(diff.isDefined)
        assertFalse(diff.isTie)
        assertEquals(BigDecimal("-2.00"), diff.signedDifference)
        assertEquals(BigDecimal("2.00"), diff.absoluteDifference)
        assertEquals(WinnerSide.SIDE_A, diff.higherSide)
        assertEquals(WinnerSide.SIDE_B, diff.lowerSide)
        // Since higher is better, Option A is favourable
        assertEquals(WinnerSide.SIDE_A, diff.favourableSide)
        assertEquals("₹2.00/unit", diff.formatAbsolute())
        assertEquals("-₹2.00/unit", diff.formatSigned())
        assertEquals("Option A has higher Profit / Unit by ₹2.00/unit", diff.formatNarrative("Option A", "Option B"))
    }

    @Test
    fun testZeroDifferenceTie() {
        // Option A: 50.00, Option B: 50.00 -> Difference = 0.00
        val diff = MetricDifference(
            metricKey = "selling_price",
            metricName = "Selling Price",
            rawA = BigDecimal("50.00"),
            rawB = BigDecimal("50.00"),
            type = MetricDifferenceType.CURRENCY,
            favourabilityRule = FavourabilityRule.NEUTRAL
        )

        assertTrue(diff.isDefined)
        assertTrue(diff.isTie)
        assertEquals(0, diff.signedDifference!!.compareTo(BigDecimal.ZERO))
        assertEquals(0, diff.absoluteDifference!!.compareTo(BigDecimal.ZERO))
        assertEquals(WinnerSide.TIE, diff.higherSide)
        assertEquals(WinnerSide.TIE, diff.lowerSide)
        assertEquals(WinnerSide.TIE, diff.favourableSide)
        assertEquals("Both Product A and Product B are equal", diff.formatNarrative("Product A", "Product B"))
    }

    @Test
    fun testPercentagePointsHandlingNotRelativePercent() {
        // Option A: 30.00%, Option B: 25.00% -> Difference = -5.00 percentage points
        val diff = MetricDifference(
            metricKey = "profit_margin",
            metricName = "Profit Margin",
            rawA = BigDecimal("30.00"),
            rawB = BigDecimal("25.00"),
            type = MetricDifferenceType.PERCENTAGE_POINTS,
            favourabilityRule = FavourabilityRule.HIGHER_IS_BETTER
        )

        assertEquals(BigDecimal("-5.00"), diff.signedDifference)
        assertEquals(BigDecimal("5.00"), diff.absoluteDifference)
        assertEquals("5.00 percentage points", diff.formatAbsolute())
        assertEquals("-5.00 percentage points", diff.formatSigned())
        assertEquals("5.00 % pts", diff.formatCompactAbsolute())
        assertEquals("-5.00 % pts", diff.formatCompactSigned())
        // Crucial requirement: Must state percentage points, never label as 5%
        assertTrue(diff.formatAbsolute().contains("percentage points"))
        assertFalse(diff.formatAbsolute().contains("5.00%"))
        assertEquals("Option A has higher Profit Margin by 5.00 percentage points", diff.formatNarrative("Option A", "Option B"))
    }

    @Test
    fun testMarkupPositivePercentagePoints() {
        // Option A: 50.00%, Option B: 60.00% -> Difference = +10.00 percentage points
        val diff = MetricDifference(
            metricKey = "markup",
            metricName = "Markup",
            rawA = BigDecimal("50.00"),
            rawB = BigDecimal("60.00"),
            type = MetricDifferenceType.PERCENTAGE_POINTS,
            favourabilityRule = FavourabilityRule.HIGHER_IS_BETTER
        )

        assertEquals(BigDecimal("10.00"), diff.signedDifference)
        assertEquals("10.00 percentage points", diff.formatAbsolute())
        assertEquals("+10.00 percentage points", diff.formatSigned())
        assertEquals(WinnerSide.SIDE_B, diff.favourableSide)
        assertEquals("Option B has higher Markup by 10.00 percentage points", diff.formatNarrative("Option A", "Option B"))
    }

    @Test
    fun testNegativeProfitLossScenario() {
        // Option A: -₹5.00 (loss), Option B: +₹2.00 (profit)
        val diff = MetricDifference(
            metricKey = "profit_per_unit",
            metricName = "Profit / Unit",
            rawA = BigDecimal("-5.00"),
            rawB = BigDecimal("2.00"),
            type = MetricDifferenceType.CURRENCY,
            favourabilityRule = FavourabilityRule.HIGHER_IS_BETTER,
            unitSuffix = "/unit"
        )

        assertEquals(BigDecimal("7.00"), diff.signedDifference)
        assertEquals(BigDecimal("7.00"), diff.absoluteDifference)
        assertEquals(WinnerSide.SIDE_B, diff.higherSide)
        assertEquals(WinnerSide.SIDE_B, diff.favourableSide)
        assertEquals("Option B has higher Profit / Unit by ₹7.00/unit", diff.formatNarrative("Option A", "Option B"))
    }

    @Test
    fun testNeutralFavourabilitySellingPrice() {
        // Option A: 100.00, Option B: 120.00
        val diff = MetricDifference(
            metricKey = "selling_price",
            metricName = "Selling Price",
            rawA = BigDecimal("100.00"),
            rawB = BigDecimal("120.00"),
            type = MetricDifferenceType.CURRENCY,
            favourabilityRule = FavourabilityRule.NEUTRAL
        )

        assertEquals(WinnerSide.SIDE_B, diff.higherSide)
        assertEquals(WinnerSide.SIDE_A, diff.lowerSide)
        // Neutral metrics do not declare a winner side
        assertEquals(WinnerSide.NONE, diff.favourableSide)
        assertEquals("Option B is higher by ₹20.00", diff.formatNarrative("Option A", "Option B").let {
            // Narrative should explain higher side
            assertTrue(it.contains("Option B is higher"))
            it
        })
    }

    @Test
    fun testNullOrUnavailableOptionalMetrics() {
        val diff = MetricDifference(
            metricKey = "profit_margin",
            metricName = "Profit Margin",
            rawA = null,
            rawB = BigDecimal("20.00"),
            type = MetricDifferenceType.PERCENTAGE_POINTS,
            favourabilityRule = FavourabilityRule.HIGHER_IS_BETTER
        )

        assertFalse(diff.isDefined)
        assertFalse(diff.isTie)
        assertNull(diff.signedDifference)
        assertNull(diff.absoluteDifference)
        assertEquals(WinnerSide.NONE, diff.higherSide)
        assertEquals(WinnerSide.NONE, diff.favourableSide)
        assertEquals("N/A", diff.formatAbsolute())
        assertEquals("N/A", diff.formatSigned())
        assertEquals("Comparison unavailable", diff.formatNarrative("Option A", "Option B"))
    }

    @Test
    fun testHighPrecisionBigDecimalUnroundedDifferenceCalculation() {
        // Unrounded PTR: 72.1329 vs 60.11075
        val rawA = BigDecimal("72.1329")
        val rawB = BigDecimal("60.11075")

        val diff = MetricDifference(
            metricKey = "cost_per_unit",
            metricName = "Cost / Unit",
            rawA = rawA,
            rawB = rawB,
            type = MetricDifferenceType.CURRENCY,
            favourabilityRule = FavourabilityRule.LOWER_IS_BETTER,
            unitSuffix = "/unit"
        )

        // Exact unrounded signed difference: 60.11075 - 72.1329 = -12.02215
        assertEquals(BigDecimal("-12.02215"), diff.signedDifference)
        // Exact unrounded absolute difference: 12.02215
        assertEquals(BigDecimal("12.02215"), diff.absoluteDifference)
        // Rounding happens only at presentation / display: 12.02
        assertEquals(BigDecimal("-12.02"), diff.roundedSignedDifference)
        assertEquals(BigDecimal("12.02"), diff.roundedAbsoluteDifference)
        assertEquals("₹12.02/unit", diff.formatAbsolute())
        assertEquals("-₹12.02/unit", diff.formatSigned())
        assertEquals(WinnerSide.SIDE_B, diff.favourableSide)
    }

    @Test
    fun testNeutralRuleProducesNoWinnerSideWhenValuesDifferButTieWhenEqual() {
        // Unequal values under NEUTRAL rule
        val diffUnequal = MetricDifference(
            metricKey = "supplier_discount_percent",
            metricName = "Supplier Discount %",
            rawA = BigDecimal("5.00"),
            rawB = BigDecimal("20.00"),
            type = MetricDifferenceType.PERCENTAGE_POINTS,
            favourabilityRule = FavourabilityRule.NEUTRAL
        )
        assertEquals(WinnerSide.SIDE_B, diffUnequal.higherSide)
        assertEquals(WinnerSide.SIDE_A, diffUnequal.lowerSide)
        assertEquals(WinnerSide.NONE, diffUnequal.favourableSide)
        assertEquals("+15.00 percentage points", diffUnequal.formatSigned())
        assertEquals("+15.00 % pts", diffUnequal.formatCompactSigned())
        assertEquals("Supplier B is higher by 15.00 percentage points", diffUnequal.formatNarrative("Supplier A", "Supplier B"))

        // Equal values under NEUTRAL rule
        val diffEqual = MetricDifference(
            metricKey = "supplier_discount_percent",
            metricName = "Supplier Discount %",
            rawA = BigDecimal("10.00"),
            rawB = BigDecimal("10.00"),
            type = MetricDifferenceType.PERCENTAGE_POINTS,
            favourabilityRule = FavourabilityRule.NEUTRAL
        )
        assertTrue(diffEqual.isTie)
        assertEquals(WinnerSide.TIE, diffEqual.higherSide)
        assertEquals(WinnerSide.TIE, diffEqual.favourableSide)
        assertEquals("Both Supplier A and Supplier B are equal", diffEqual.formatNarrative("Supplier A", "Supplier B"))
    }

    @Test
    fun testLargeCurrencyValuesWithIndianFormatting() {
        val diff = MetricDifference(
            metricKey = "revenue",
            metricName = "Revenue",
            rawA = BigDecimal("1000000.00"), // 10 Lakhs
            rawB = BigDecimal("1250000.00"), // 12.5 Lakhs
            type = MetricDifferenceType.CURRENCY,
            favourabilityRule = FavourabilityRule.NEUTRAL
        )
        assertEquals(BigDecimal("250000.00"), diff.signedDifference)
        assertEquals("₹2,50,000.00", diff.formatAbsolute())
        assertEquals("+₹2,50,000.00", diff.formatSigned())
    }
}

