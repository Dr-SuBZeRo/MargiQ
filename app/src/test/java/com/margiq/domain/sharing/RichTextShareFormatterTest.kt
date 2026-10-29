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

        assertTrue("Should contain header", formatted.contains("MargiQ — Profit & Margin Report"))
        assertTrue("Should contain product name", formatted.contains("Augmentin 625 Duo"))
        assertTrue("Should contain PTR with rupee symbol", formatted.contains("₹150.00"))
        assertTrue("Should contain Cost Per Unit", formatted.contains("Cost Per Unit"))
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

        assertTrue("Should contain comparison title", formatted.contains("Product Comparison Report"))
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

        assertTrue("Should contain supplier comparison title", formatted.contains("Supplier Comparison Report"))
        assertTrue("Should contain base product", formatted.contains("Dolo 650"))
        assertTrue("Should contain supplier names", formatted.contains("Alpha Pharma") && formatted.contains("Beta Distributors"))
        assertTrue("Should contain footer tagline", formatted.contains("Know your cost. Own your margin."))
    }
}
