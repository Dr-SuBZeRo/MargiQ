package com.margiq.domain.sharing

import com.margiq.domain.calculation.ProfitCalculationEngine
import com.margiq.domain.comparison.ComparisonEngine
import com.margiq.domain.comparison.ComparisonHighlight
import com.margiq.domain.comparison.ProductComparisonOutcome
import com.margiq.domain.comparison.WinnerSide
import com.margiq.domain.model.CalculationInput
import com.margiq.domain.model.PurchaseTaxClassification
import com.margiq.domain.model.TaxPriceMode
import com.margiq.ui.compare.CompareSideState
import com.margiq.ui.compare.SupplierComparisonBaseProduct
import com.margiq.ui.compare.SupplierOfferSide
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

    @Test
    fun testBrandStylingAndTableStructure() {
        val report = ShareReportFactory.fromCalculation(sampleInput, sampleResult, 1000L)
        val email = HtmlEmailFormatter.format(report)

        assertTrue("Contains HTML table", email.htmlBody.contains("<table"))
        assertTrue("Contains table header", email.htmlBody.contains("<th"))
        assertTrue("Contains table cell", email.htmlBody.contains("<td"))
        assertTrue("Contains MargiQ brand in HTML", email.htmlBody.contains("Margi<span"))
        assertFalse("Must never contain MagiQ", email.htmlBody.contains("MagiQ"))
        assertFalse("Must never contain Magi<span", email.htmlBody.contains("Magi<span"))

        // Financial values consistency
        assertTrue("HTML contains effective cost", email.htmlBody.contains("Effective Cost per Pack"))
        assertTrue("Plain text fallback contains bold headings", email.plainTextFallback.contains("*PROFITABILITY*"))
    }

    @Test
    fun testProductComparisonWithDifferencesFormatting() {
        val inputA = sampleInput.copy(ptr = BigDecimal("50.00"), sellingPrice = BigDecimal("100.00"))
        val inputB = sampleInput.copy(ptr = BigDecimal("60.00"), sellingPrice = BigDecimal("100.00"))
        val outcome = ComparisonEngine.compare(inputA, inputB)

        val sideA = CompareSideState(productName = "Product Alpha")
        val sideB = CompareSideState(productName = "Product Beta")

        val report = ShareReportFactory.fromProductComparison(outcome, sideA, sideB)
        val email = HtmlEmailFormatter.format(report)

        assertTrue("Subject contains Product Comparison", email.subject.contains("Product Comparison"))
        assertTrue("HTML contains Difference column header", email.htmlBody.contains("Difference (B - A)"))
        assertTrue("HTML indicates cheaper side for Cost per Unit", email.htmlBody.contains("Product Alpha cheaper"))
        assertTrue("Plain text fallback contains differences section", email.plainTextFallback.contains("SIDE-BY-SIDE METRICS & DIFFERENCES"))
        assertTrue("Plain text fallback contains percentage points", email.plainTextFallback.contains("percentage points") || email.plainTextFallback.contains("pts"))
    }

    @Test
    fun testSupplierComparisonWithDifferencesFormatting() {
        val base = SupplierComparisonBaseProduct(
            productName = "Paracetamol 500",
            sellingPriceText = "120.00",
            sellingTaxRateText = "12.00",
            unitsPerPackText = "10"
        )
        val offerA = SupplierOfferSide(
            supplierName = "Apex Pharma",
            ptrText = "60.00",
            purchaseTaxRateText = "12.00"
        )
        val offerB = SupplierOfferSide(
            supplierName = "Zenith Meds",
            ptrText = "70.00",
            purchaseTaxRateText = "12.00"
        )

        val inputA = offerA.toCalculationInput(base)
        val inputB = offerB.toCalculationInput(base)
        val outcome = ComparisonEngine.compare(inputA, inputB)

        val report = ShareReportFactory.fromSupplierComparison(outcome, base, offerA, offerB)
        val email = HtmlEmailFormatter.format(report)

        assertTrue("Subject contains Supplier Comparison", email.subject.contains("Supplier Comparison"))
        assertTrue("HTML contains product name", email.htmlBody.contains("Paracetamol 500"))
        assertTrue("HTML contains Difference column header", email.htmlBody.contains("Difference (B - A)"))
        assertTrue("HTML contains Apex Pharma cheaper", email.htmlBody.contains("Apex Pharma cheaper"))
        assertTrue("HTML contains Purchase PTR row", email.htmlBody.contains("Purchase PTR"))
        assertTrue("Plain text fallback contains supplier comparison", email.plainTextFallback.contains("SUPPLIER COMPARISON"))
    }
}
