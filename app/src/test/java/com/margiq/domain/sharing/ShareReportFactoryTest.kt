package com.margiq.domain.sharing

import com.margiq.data.local.CalculationEntity
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class ShareReportFactoryTest {

    private val sampleInput = CalculationInput(
        productName = "Amoxicillin 500mg",
        ptr = BigDecimal("45.00"),
        purchaseTaxRate = BigDecimal("12.00"),
        purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
        purchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
        isInputTaxCreditEnabled = true,
        sellingPrice = BigDecimal("120.00"),
        sellingTaxRate = BigDecimal("12.00"),
        sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE,
        unitsPerPack = 10,
        quantity = 5
    )

    private val sampleResult = ProfitCalculationEngine.calculate(sampleInput)

    @Test
    fun testFromCalculation() {
        val report = ShareReportFactory.fromCalculation(sampleInput, sampleResult, 1000L)
        assertEquals("Amoxicillin 500mg", report.productName)
        assertEquals(BigDecimal("45.00"), report.enteredPtr)
        assertEquals(BigDecimal("120.00"), report.enteredSellingPrice)
        assertEquals(10, report.unitsPerPack)
        assertNotNull(report.profitPerPack)
        assertNotNull(report.profitMarginPercent)
    }

    @Test
    fun testFromHistory() {
        val entity = CalculationEntity.fromInputAndResult(sampleInput, sampleResult, id = 42L)
        val report = ShareReportFactory.fromHistory(entity)
        assertEquals("Amoxicillin 500mg", report.productName)
        assertEquals(BigDecimal("45.00"), report.enteredPtr)
        assertEquals(10, report.unitsPerPack)
        assertNotNull(report.effectiveCostPerPack)
    }

    @Test
    fun testFromProductComparison() {
        val outcome = ProductComparisonOutcome(
            resultA = sampleResult,
            resultB = sampleResult,
            highlight = ComparisonHighlight(
                lowestCostSide = WinnerSide.SIDE_A,
                highestProfitPerUnitSide = WinnerSide.SIDE_A,
                highestMarginSide = WinnerSide.SIDE_A,
                costDifferencePerUnit = BigDecimal("0.85"),
                costDifferencePerPack = BigDecimal("8.50"),
                costSavingsPercent = BigDecimal("16.35"),
                profitDifferencePerUnit = BigDecimal("1.10"),
                marginDifferencePercent = BigDecimal("9.38"),
                markupDifferencePercent = BigDecimal("15.20")
            ),
            isValid = true,
            validationMessage = null
        )

        val sideA = CompareSideState(productName = "Brand Alpha")
        val sideB = CompareSideState(productName = "Brand Beta")

        val report = ShareReportFactory.fromProductComparison(outcome, sideA, sideB)
        assertEquals("Brand Alpha", report.titleA)
        assertEquals("Brand Beta", report.titleB)
        assertTrue(report.primaryHighlightSummary.contains("saves"))
        assertEquals(WinnerSide.SIDE_A, report.lowestCostWinner)
    }

    @Test
    fun testFromSupplierComparison() {
        val outcome = ProductComparisonOutcome(
            resultA = sampleResult,
            resultB = sampleResult,
            highlight = ComparisonHighlight(
                lowestCostSide = WinnerSide.SIDE_A,
                highestProfitPerUnitSide = WinnerSide.SIDE_A,
                highestMarginSide = WinnerSide.SIDE_A,
                costDifferencePerUnit = BigDecimal("0.45"),
                costDifferencePerPack = BigDecimal("4.50"),
                costSavingsPercent = BigDecimal("9.38"),
                profitDifferencePerUnit = BigDecimal("0.50"),
                marginDifferencePercent = BigDecimal("5.00"),
                markupDifferencePercent = BigDecimal("7.50")
            ),
            isValid = true,
            validationMessage = null
        )

        val base = SupplierComparisonBaseProduct(
            productName = "Cetirizine 10mg",
            sellingPriceText = "50.00",
            sellingTaxRateText = "12.00",
            sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE,
            unitsPerPackText = "10"
        )
        val offerA = SupplierOfferSide(supplierName = "Supplier Alpha", ptrText = "20.00")
        val offerB = SupplierOfferSide(supplierName = "Supplier Beta", ptrText = "22.00")

        val report = ShareReportFactory.fromSupplierComparison(outcome, base, offerA, offerB)
        assertEquals("Cetirizine 10mg", report.productName)
        assertEquals("Supplier Alpha", report.supplierNameA)
        assertEquals("Supplier Beta", report.supplierNameB)
        assertTrue(report.primaryHighlightSummary.contains("saves"))
    }
}
