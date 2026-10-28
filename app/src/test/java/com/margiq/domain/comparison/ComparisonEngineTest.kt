package com.margiq.domain.comparison

import com.margiq.domain.model.CalculationInput
import com.margiq.domain.model.PurchaseTaxClassification
import com.margiq.domain.model.TaxPriceMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class ComparisonEngineTest {

    @Test
    fun testIdenticalInputsYieldTie() {
        val inputA = CalculationInput.defaultExample()
        val inputB = CalculationInput.defaultExample()

        val outcome = ComparisonEngine.compare(inputA, inputB)
        assertTrue(outcome.isValid)
        assertNotNull(outcome.highlight)
        val hl = outcome.highlight!!

        assertEquals(WinnerSide.TIE, hl.lowestCostSide)
        assertEquals(WinnerSide.TIE, hl.highestProfitPerUnitSide)
        assertEquals(WinnerSide.TIE, hl.highestMarginSide)
        assertEquals(0, hl.costDifferencePerUnit.compareTo(BigDecimal.ZERO))
        assertEquals(0, hl.profitDifferencePerUnit.compareTo(BigDecimal.ZERO))
    }

    @Test
    fun testLowerCostAndHigherProfitSeparation() {
        // Product A: Low cost, low selling price
        val inputA = CalculationInput(
            productName = "Product A",
            ptr = BigDecimal("40.00"),
            purchaseTaxRate = BigDecimal("5.00"),
            sellingPrice = BigDecimal("60.00"),
            sellingTaxRate = BigDecimal("5.00"),
            unitsPerPack = 10
        )

        // Product B: Higher cost, but much higher selling price
        val inputB = CalculationInput(
            productName = "Product B",
            ptr = BigDecimal("80.00"),
            purchaseTaxRate = BigDecimal("5.00"),
            sellingPrice = BigDecimal("200.00"),
            sellingTaxRate = BigDecimal("5.00"),
            unitsPerPack = 10
        )

        val outcome = ComparisonEngine.compare(inputA, inputB)
        assertTrue(outcome.isValid)
        val hl = outcome.highlight!!

        // Product A has lower cost
        assertEquals(WinnerSide.SIDE_A, hl.lowestCostSide)
        // Product B has higher profit per unit and higher margin
        assertEquals(WinnerSide.SIDE_B, hl.highestProfitPerUnitSide)
        assertEquals(WinnerSide.SIDE_B, hl.highestMarginSide)
        assertTrue(hl.costDifferencePerUnit > BigDecimal.ZERO)
        assertTrue(hl.profitDifferencePerUnit > BigDecimal.ZERO)
    }

    @Test
    fun testNormalizedPackSizeComparison() {
        // Supplier A offers 10 units per pack at PTR 100 (Cost per unit = 10.00)
        val offerA = CalculationInput(
            productName = "Tablets",
            supplierName = "Supplier A",
            ptr = BigDecimal("100.00"),
            purchaseTaxRate = BigDecimal.ZERO,
            sellingPrice = BigDecimal("150.00"),
            sellingTaxRate = BigDecimal.ZERO,
            unitsPerPack = 10
        )

        // Supplier B offers 20 units per pack at PTR 160 (Cost per unit = 8.00)
        val offerB = CalculationInput(
            productName = "Tablets",
            supplierName = "Supplier B",
            ptr = BigDecimal("160.00"),
            purchaseTaxRate = BigDecimal.ZERO,
            sellingPrice = BigDecimal("300.00"),
            sellingTaxRate = BigDecimal.ZERO,
            unitsPerPack = 20
        )

        val outcome = ComparisonEngine.compare(offerA, offerB)
        assertTrue(outcome.isValid)
        val hl = outcome.highlight!!

        // Supplier B is cheaper per unit despite higher pack price!
        assertEquals(WinnerSide.SIDE_B, hl.lowestCostSide)
        assertEquals(BigDecimal("2.00"), hl.costDifferencePerUnit)
        assertEquals(BigDecimal("20.00"), hl.costSavingsPercent)
    }

    @Test
    fun testSupplierDiscountAndOfferComparison() {
        // Supplier A: Flat PTR 100 with 10% discount -> Effective 90
        val offerA = CalculationInput(
            productName = "Syrup",
            supplierName = "Wholesaler A",
            ptr = BigDecimal("100.00"),
            purchaseTaxRate = BigDecimal.ZERO,
            isSupplierDiscountEnabled = true,
            supplierDiscountPercent = BigDecimal("10.00"),
            sellingPrice = BigDecimal("150.00"),
            sellingTaxRate = BigDecimal.ZERO,
            unitsPerPack = 1
        )

        // Supplier B: PTR 100 with Buy 10 Get 2 Free (12 bottles for 1000 = 83.33/bottle)
        val offerB = CalculationInput(
            productName = "Syrup",
            supplierName = "Wholesaler B",
            ptr = BigDecimal("100.00"),
            purchaseTaxRate = BigDecimal.ZERO,
            quantity = 10,
            isSupplierOfferEnabled = true,
            offerBuyQuantity = 10,
            offerFreeQuantity = 2,
            sellingPrice = BigDecimal("150.00"),
            sellingTaxRate = BigDecimal.ZERO,
            unitsPerPack = 1
        )

        val outcome = ComparisonEngine.compare(offerA, offerB)
        assertTrue(outcome.isValid)
        val hl = outcome.highlight!!

        // Supplier B is cheaper due to free goods
        assertEquals(WinnerSide.SIDE_B, hl.lowestCostSide)
    }

    @Test
    fun testInvalidInputsHandledSafely() {
        val invalidA = CalculationInput(
            productName = "Invalid",
            ptr = BigDecimal("-10.00")
        )
        val validB = CalculationInput.defaultExample()

        val outcome = ComparisonEngine.compare(invalidA, validB)
        assertFalse(outcome.isValid)
        assertTrue(outcome.validationMessage?.contains("cannot be negative") == true)
    }

    @Test
    fun testDifferingSellingTaxRatesComparison() {
        // Product A: PTR 50, Selling MRP 100 with 5% GST -> Net selling = 95.24, Profit = 45.24
        val inputA = CalculationInput(
            productName = "Product A",
            ptr = BigDecimal("50.00"),
            purchaseTaxRate = BigDecimal("5.00"),
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            purchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
            isInputTaxCreditEnabled = true,
            sellingPrice = BigDecimal("100.00"),
            sellingTaxRate = BigDecimal("5.00"),
            sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE,
            unitsPerPack = 1
        )

        // Product B: PTR 50, Selling MRP 100 with 18% GST -> Net selling = 84.75, Profit = 34.75
        val inputB = CalculationInput(
            productName = "Product B",
            ptr = BigDecimal("50.00"),
            purchaseTaxRate = BigDecimal("5.00"),
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            purchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
            isInputTaxCreditEnabled = true,
            sellingPrice = BigDecimal("100.00"),
            sellingTaxRate = BigDecimal("18.00"),
            sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE,
            unitsPerPack = 1
        )

        val outcome = ComparisonEngine.compare(inputA, inputB)
        assertTrue(outcome.isValid)
        val hl = outcome.highlight!!

        // Costs are equal
        assertEquals(WinnerSide.TIE, hl.lowestCostSide)
        // Product A yields higher profit and margin because lower tax slab leaves more net revenue
        assertEquals(WinnerSide.SIDE_A, hl.highestProfitPerUnitSide)
        assertEquals(WinnerSide.SIDE_A, hl.highestMarginSide)
    }

    @Test
    fun testTaxInclusiveVsExclusiveSellingPriceComparison() {
        // Product A: Selling price 100 TAX_EXCLUSIVE (+5% tax = customer pays 105, net revenue 100)
        val inputA = CalculationInput(
            productName = "Product A",
            ptr = BigDecimal("50.00"),
            purchaseTaxRate = BigDecimal.ZERO,
            sellingPrice = BigDecimal("100.00"),
            sellingTaxRate = BigDecimal("5.00"),
            sellingPriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            unitsPerPack = 1
        )

        // Product B: Selling price 100 TAX_INCLUSIVE (customer pays 100, net revenue 95.24)
        val inputB = CalculationInput(
            productName = "Product B",
            ptr = BigDecimal("50.00"),
            purchaseTaxRate = BigDecimal.ZERO,
            sellingPrice = BigDecimal("100.00"),
            sellingTaxRate = BigDecimal("5.00"),
            sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE,
            unitsPerPack = 1
        )

        val outcome = ComparisonEngine.compare(inputA, inputB)
        assertTrue(outcome.isValid)
        val hl = outcome.highlight!!

        assertEquals(WinnerSide.SIDE_A, hl.highestProfitPerUnitSide)
        assertEquals(WinnerSide.SIDE_A, hl.highestMarginSide)
    }

    @Test
    fun testPurchaseItcEligibilityComparison() {
        // Product A: 12% GST with ITC Claimed (economic cost = 100)
        val inputA = CalculationInput(
            productName = "Product A",
            ptr = BigDecimal("100.00"),
            purchaseTaxRate = BigDecimal("12.00"),
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            purchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
            isInputTaxCreditEnabled = true,
            sellingPrice = BigDecimal("150.00"),
            sellingTaxRate = BigDecimal.ZERO,
            unitsPerPack = 1
        )

        // Product B: 12% GST with Non-recoverable tax (economic cost = 112)
        val inputB = CalculationInput(
            productName = "Product B",
            ptr = BigDecimal("100.00"),
            purchaseTaxRate = BigDecimal("12.00"),
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            purchaseTaxClassification = PurchaseTaxClassification.NON_RECOVERABLE,
            isInputTaxCreditEnabled = false,
            sellingPrice = BigDecimal("150.00"),
            sellingTaxRate = BigDecimal.ZERO,
            unitsPerPack = 1
        )

        val outcome = ComparisonEngine.compare(inputA, inputB)
        assertTrue(outcome.isValid)
        val hl = outcome.highlight!!

        // Product A has lower economic cost because ITC is credited
        assertEquals(WinnerSide.SIDE_A, hl.lowestCostSide)
        assertEquals(WinnerSide.SIDE_A, hl.highestProfitPerUnitSide)
    }
}
