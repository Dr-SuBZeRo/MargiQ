package com.margiq.domain.sharing

import com.margiq.domain.calculation.ProfitCalculationEngine
import com.margiq.domain.comparison.ComparisonHighlight
import com.margiq.domain.comparison.ProductComparisonOutcome
import com.margiq.domain.comparison.WinnerSide
import com.margiq.domain.model.CalculationInput
import com.margiq.domain.model.PurchaseTaxClassification
import com.margiq.domain.model.TaxPriceMode
import com.margiq.ui.compare.CompareSideState
import com.margiq.ui.compare.SupplierComparisonBaseProduct
import com.margiq.ui.compare.SupplierOfferSide
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class RichTextShareFormatterTest {

    private val sampleInput = CalculationInput(
        productName = "Augmentin 625 Duo",
        ptr = BigDecimal("150.00"),
        purchaseTaxRate = BigDecimal("12.00"),
        purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
        purchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
        isInputTaxCreditEnabled = true,
        sellingPrice = BigDecimal("220.00"),
        sellingTaxRate = BigDecimal("12.00"),
        sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE,
        unitsPerPack = 10,
        quantity = 5
    )

    private val sampleResult = ProfitCalculationEngine.calculate(sampleInput)

    @Test
    fun testFormatSingleCalculation() {
        val report = ShareReportFactory.fromCalculation(sampleInput, sampleResult, 1000L)
        val formatted = RichTextShareFormatter.format(report)

        assertTrue("Should contain header", formatted.contains("MARGIQ — PROFIT & MARGIN REPORT"))
        assertTrue("Should contain product name", formatted.contains("Augmentin 625 Duo"))
        assertTrue("Should contain PTR with rupee symbol", formatted.contains("₹150.00"))
        assertTrue("Should contain Cost Per Unit", formatted.contains("Cost per Individual Unit"))
        assertTrue("Should contain Profit Margin", formatted.contains("Profit Margin"))
        assertTrue("Should contain Markup", formatted.contains("Markup"))
        assertTrue("Should contain footer tagline", formatted.contains("MargiQ"))
        assertTrue("Should contain tagline", formatted.contains("Know your cost. Own your margin."))
    }

    @Test
    fun testFormatProductComparison() {
        val outcome = ProductComparisonOutcome(
            resultA = sampleResult,
            resultB = sampleResult,
            highlight = ComparisonHighlight(
                lowestCostSide = WinnerSide.SIDE_A,
                highestProfitPerUnitSide = WinnerSide.SIDE_A,
                highestMarginSide = WinnerSide.SIDE_A,
                costDifferencePerUnit = BigDecimal("1.49"),
                costDifferencePerPack = BigDecimal("14.90"),
                costSavingsPercent = BigDecimal("9.31"),
                profitDifferencePerUnit = BigDecimal("0.70"),
                marginDifferencePercent = BigDecimal("3.75"),
                markupDifferencePercent = BigDecimal("5.20")
            ),
            isValid = true,
            validationMessage = null
        )

        val sideA = CompareSideState(productName = "Brand Alpha")
        val sideB = CompareSideState(productName = "Brand Beta")

        val report = ShareReportFactory.fromProductComparison(outcome, sideA, sideB)
        val formatted = RichTextShareFormatter.format(report)

        assertTrue("Should contain comparison title", formatted.contains("MARGIQ — PRODUCT COMPARISON REPORT"))
        assertTrue("Should contain Winner callout", formatted.contains("Brand Alpha"))
        assertTrue("Should contain summary reason", formatted.contains("saves"))
        assertTrue("Should contain Side A title", formatted.contains("Brand Alpha"))
        assertTrue("Should contain Side B title", formatted.contains("Brand Beta"))
        assertTrue("Should contain footer tagline", formatted.contains("Know your cost. Own your margin."))
    }

    @Test
    fun testFormatSupplierComparison() {
        val outcome = ProductComparisonOutcome(
            resultA = sampleResult,
            resultB = sampleResult,
            highlight = ComparisonHighlight(
                lowestCostSide = WinnerSide.SIDE_A,
                highestProfitPerUnitSide = WinnerSide.SIDE_A,
                highestMarginSide = WinnerSide.SIDE_A,
                costDifferencePerUnit = BigDecimal("0.99"),
                costDifferencePerPack = BigDecimal("9.90"),
                costSavingsPercent = BigDecimal("6.39"),
                profitDifferencePerUnit = BigDecimal("0.50"),
                marginDifferencePercent = BigDecimal("2.50"),
                markupDifferencePercent = BigDecimal("3.80")
            ),
            isValid = true,
            validationMessage = null
        )

        val base = SupplierComparisonBaseProduct(
            productName = "Dolo 650",
            sellingPriceText = "30.00",
            sellingTaxRateText = "12.00",
            sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE,
            unitsPerPackText = "15"
        )
        val offerA = SupplierOfferSide(supplierName = "Alpha Pharma", ptrText = "15.00")
        val offerB = SupplierOfferSide(supplierName = "Beta Distributors", ptrText = "16.00")

        val report = ShareReportFactory.fromSupplierComparison(outcome, base, offerA, offerB)
        val formatted = RichTextShareFormatter.format(report)

        assertTrue("Should contain supplier comparison title", formatted.contains("MARGIQ — SUPPLIER COMPARISON REPORT"))
        assertTrue("Should contain base product", formatted.contains("Dolo 650"))
        assertTrue("Should contain supplier names", formatted.contains("Alpha Pharma") && formatted.contains("Beta Distributors"))
        assertTrue("Should contain footer tagline", formatted.contains("Know your cost. Own your margin."))
    }

    @Test
    fun testPromptCanonicalExampleFinancialValues() {
        // Canonically specified test values in prompt:
        // PTR: ₹48.00 (tax-exclusive, 5% recoverable GST), MRP: ₹135.00 (tax-inclusive, 5% GST), unitsPerPack: 10, quantity: 5
        val input = CalculationInput(
            productName = "Sample Product",
            ptr = BigDecimal("48.00"),
            purchaseTaxRate = BigDecimal("5.00"),
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            purchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
            isInputTaxCreditEnabled = true,
            sellingPrice = BigDecimal("135.00"),
            sellingTaxRate = BigDecimal("5.00"),
            sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE,
            unitsPerPack = 10,
            quantity = 5
        )
        val result = ProfitCalculationEngine.calculate(input)
        val report = ShareReportFactory.fromCalculation(input, result, 1000L)
        val formatted = RichTextShareFormatter.format(report)

        // Verify all canonical financial values appear exactly
        assertTrue("Contains PTR ₹48.00", formatted.contains("₹48.00"))
        assertTrue("Contains Purchase GST 5.00%", formatted.contains("5.00%"))
        assertTrue("Contains Invoice Total ₹50.40", formatted.contains("₹50.40"))
        assertTrue("Contains ITC ₹2.40", formatted.contains("₹2.40"))
        assertTrue("Contains MRP ₹135.00", formatted.contains("₹135.00"))
        assertTrue("Contains Net Revenue ₹128.57", formatted.contains("₹128.57"))
        assertTrue("Contains Output GST ₹6.43", formatted.contains("₹6.43"))
        assertTrue("Contains Cost per unit ₹4.80", formatted.contains("₹4.80"))
        assertTrue("Contains Profit per unit +₹8.06", formatted.contains("+₹8.06"))
        assertTrue("Contains Profit per pack +₹80.57", formatted.contains("+₹80.57"))
        assertTrue("Contains Margin 62.67%", formatted.contains("62.67%"))
        assertTrue("Contains Markup 167.86%", formatted.contains("167.86%"))
        assertTrue("Contains Batch profit +₹402.86", formatted.contains("+₹402.86"))

        // Verify WhatsApp formatting
        assertTrue("Contains bold section heading", formatted.contains("*PURCHASE ECONOMICS*"))
        assertTrue("Contains bold section heading", formatted.contains("*SELLING & REVENUE*"))
        assertTrue("Contains bold section heading", formatted.contains("*PROFITABILITY*"))
        assertTrue("Contains box-drawing divider", formatted.contains("━━━━━━━━━━━━━━━━━━━━"))

        // Verify correct brand spelling everywhere
        assertTrue("Contains MargiQ", formatted.contains("MargiQ"))
        org.junit.Assert.assertFalse("Must never contain misspelling MagiQ", formatted.contains("MagiQ"))
    }

    @Test
    fun testProductComparisonWithDifferencesFormatting() {
        val inputA = CalculationInput(
            productName = "Product A",
            ptr = BigDecimal("50.00"),
            purchaseTaxRate = BigDecimal.ZERO,
            sellingPrice = BigDecimal("100.00"),
            sellingTaxRate = BigDecimal.ZERO,
            unitsPerPack = 1
        )
        val inputB = CalculationInput(
            productName = "Product B",
            ptr = BigDecimal("60.00"),
            purchaseTaxRate = BigDecimal.ZERO,
            sellingPrice = BigDecimal("100.00"),
            sellingTaxRate = BigDecimal.ZERO,
            unitsPerPack = 1
        )
        val outcome = com.margiq.domain.comparison.ComparisonEngine.compare(inputA, inputB)
        val sideA = com.margiq.ui.compare.CompareSideState(productName = "Product A", ptrText = "50.00", sellingPriceText = "100.00", unitsPerPackText = "1")
        val sideB = com.margiq.ui.compare.CompareSideState(productName = "Product B", ptrText = "60.00", sellingPriceText = "100.00", unitsPerPackText = "1")
        val report = ShareReportFactory.fromProductComparison(outcome, sideA, sideB)
        val formatted = RichTextShareFormatter.format(report)

        assertTrue("Should contain differences header", formatted.contains("SIDE-BY-SIDE METRICS & DIFFERENCES"))
        assertTrue("Should contain Cost / Unit difference", formatted.contains("• Difference: +₹10.00/unit (₹10.00/unit)"))
        assertTrue("Should contain narrative direction", formatted.contains("➡️ Product A is cheaper by ₹10.00/unit"))
        assertTrue("Should contain Profit / Unit difference", formatted.contains("• Difference: -₹10.00/unit (₹10.00/unit)"))
        assertTrue("Should contain Profit Margin difference in percentage points", formatted.contains("percentage points"))
    }

    @Test
    fun testSupplierComparisonWithDifferencesFormatting() {
        val base = com.margiq.ui.compare.SupplierComparisonBaseProduct(
            productName = "Paracetamol 500",
            sellingPriceText = "150.00",
            sellingTaxRateText = "0.00",
            unitsPerPackText = "10"
        )
        val offerA = com.margiq.ui.compare.SupplierOfferSide(supplierName = "Supplier A", ptrText = "90.00")
        val offerB = com.margiq.ui.compare.SupplierOfferSide(supplierName = "Supplier B", ptrText = "100.00")
        val inputA = offerA.toCalculationInput(base)
        val inputB = offerB.toCalculationInput(base)
        val outcome = com.margiq.domain.comparison.ComparisonEngine.compare(inputA, inputB)
        val report = ShareReportFactory.fromSupplierComparison(outcome, base, offerA, offerB)
        val formatted = RichTextShareFormatter.format(report)

        assertTrue("Should contain supplier offer metrics & differences", formatted.contains("SUPPLIER OFFER METRICS & DIFFERENCES"))
        assertTrue("Should contain PTR difference", formatted.contains("• Difference: +₹10.00 (₹10.00)"))
        assertTrue("Should contain narrative direction", formatted.contains("➡️ Supplier A is cheaper by ₹10.00"))
        assertTrue("Should contain Cost per Unit difference", formatted.contains("➡️ Supplier A is cheaper by ₹1.00/unit"))
    }
}
