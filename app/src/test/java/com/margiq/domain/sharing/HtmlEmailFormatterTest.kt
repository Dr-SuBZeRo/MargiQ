package com.margiq.domain.sharing

import com.margiq.domain.calculation.ProfitCalculationEngine
import com.margiq.domain.comparison.ComparisonHighlight
import com.margiq.domain.comparison.ProductComparisonOutcome
import com.margiq.domain.comparison.WinnerSide
import com.margiq.domain.model.CalculationInput
import com.margiq.domain.model.PurchaseTaxClassification
import com.margiq.domain.model.TaxPriceMode
import com.margiq.ui.compare.CompareSideState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class HtmlEmailFormatterTest {

    private val sampleInput = CalculationInput(
        productName = "Cold & Flu <Safe> \"Caps\"",
        ptr = BigDecimal("50.00"),
        purchaseTaxRate = BigDecimal("18.00"),
        purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
        purchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
        isInputTaxCreditEnabled = true,
        sellingPrice = BigDecimal("100.00"),
        sellingTaxRate = BigDecimal("18.00"),
        sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE,
        unitsPerPack = 10,
        quantity = 5
    )

    private val sampleResult = ProfitCalculationEngine.calculate(sampleInput)

    @Test
    fun testSingleCalculationHtmlAndEscaping() {
        val report = ShareReportFactory.fromCalculation(sampleInput, sampleResult, 1000L)
        val email = HtmlEmailFormatter.format(report)

        assertTrue("Subject contains brand and product", email.subject.contains("MargiQ Report"))
        assertTrue("HTML body contains DOCTYPE", email.htmlBody.contains("<!DOCTYPE html>"))
        assertTrue("HTML body contains escaped &amp; or &lt;", email.htmlBody.contains("&amp;") || email.htmlBody.contains("&lt;Safe&gt;"))
        assertFalse("Raw unescaped tag should not exist", email.htmlBody.contains("<Safe>"))
        assertTrue("Plain text fallback present", email.plainTextFallback.isNotBlank())
        assertTrue("Plain text fallback contains tagline", email.plainTextFallback.contains("Know your cost. Own your margin."))
    }

    @Test
    fun testComparisonHtmlFormatting() {
        val outcome = ProductComparisonOutcome(
            resultA = sampleResult,
            resultB = sampleResult,
            highlight = ComparisonHighlight(
                lowestCostSide = WinnerSide.SIDE_A,
                highestProfitPerUnitSide = WinnerSide.SIDE_A,
                highestMarginSide = WinnerSide.SIDE_A,
                costDifferencePerUnit = BigDecimal("0.60"),
                costDifferencePerPack = BigDecimal("6.00"),
                costSavingsPercent = BigDecimal("9.23"),
                profitDifferencePerUnit = BigDecimal("0.50"),
                marginDifferencePercent = BigDecimal("5.38"),
                markupDifferencePercent = BigDecimal("7.00")
            ),
            isValid = true,
            validationMessage = null
        )

        val sideA = CompareSideState(productName = "Product <A>")
        val sideB = CompareSideState(productName = "Product & B")

        val report = ShareReportFactory.fromProductComparison(outcome, sideA, sideB)
        val email = HtmlEmailFormatter.format(report)

        assertTrue(email.subject.contains("Product Comparison"))
        assertTrue(email.htmlBody.contains("&lt;A&gt;"))
        assertTrue(email.htmlBody.contains("&amp; B"))
        assertTrue(email.htmlBody.contains("table"))
        assertTrue(email.plainTextFallback.contains("Product <A>"))
    }
}
