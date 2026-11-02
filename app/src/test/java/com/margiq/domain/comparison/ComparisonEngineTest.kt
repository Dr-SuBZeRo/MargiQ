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

    @Test
    fun testProductComparisonDifferencesDetailed() {
        // Product A: Cost 40.00, Selling 60.00 -> Profit 20.00, Margin 33.33%
        val inputA = CalculationInput(
            productName = "Product A",
            ptr = BigDecimal("40.00"),
            purchaseTaxRate = BigDecimal.ZERO,
            sellingPrice = BigDecimal("60.00"),
            sellingTaxRate = BigDecimal.ZERO,
            unitsPerPack = 1
        )

        // Product B: Cost 30.00, Selling 60.00 -> Profit 30.00, Margin 50.00%
        val inputB = CalculationInput(
            productName = "Product B",
            ptr = BigDecimal("30.00"),
            purchaseTaxRate = BigDecimal.ZERO,
            sellingPrice = BigDecimal("60.00"),
            sellingTaxRate = BigDecimal.ZERO,
            unitsPerPack = 1
        )

        val outcome = ComparisonEngine.compare(inputA, inputB)
        assertTrue(outcome.isValid)
        assertNotNull(outcome.differences)
        val diffs = outcome.differences!!

        // Cost / Unit: Option A = 40.00, Option B = 30.00 -> Difference (B - A) = -10.00
        assertEquals(0, BigDecimal("-10.00").compareTo(diffs.costPerUnit.signedDifference))
        assertEquals(BigDecimal("-10.00"), diffs.costPerUnit.roundedSignedDifference)
        assertEquals(0, BigDecimal("10.00").compareTo(diffs.costPerUnit.absoluteDifference))
        assertEquals(BigDecimal("10.00"), diffs.costPerUnit.roundedAbsoluteDifference)
        assertEquals(WinnerSide.SIDE_B, diffs.costPerUnit.lowerSide)
        assertEquals(WinnerSide.SIDE_B, diffs.costPerUnit.favourableSide)
        assertEquals("-₹10.00/unit", diffs.costPerUnit.formatSigned())
        assertEquals("Option B is cheaper by ₹10.00/unit", diffs.costPerUnit.formatNarrative("Option A", "Option B"))

        // Profit / Unit: Option A = 20.00, Option B = 30.00 -> Difference (B - A) = +10.00
        assertEquals(0, BigDecimal("10.00").compareTo(diffs.profitPerUnit.signedDifference))
        assertEquals(BigDecimal("10.00"), diffs.profitPerUnit.roundedSignedDifference)
        assertEquals(WinnerSide.SIDE_B, diffs.profitPerUnit.higherSide)
        assertEquals(WinnerSide.SIDE_B, diffs.profitPerUnit.favourableSide)
        assertEquals("+₹10.00/unit", diffs.profitPerUnit.formatSigned())

        // Margin: Option A = 33.33%, Option B = 50.00% -> Difference = +16.67 percentage points
        assertEquals(BigDecimal("16.67"), diffs.profitMargin.roundedSignedDifference)
        assertTrue(diffs.profitMargin.formatSigned().contains("percentage points"))
        assertEquals(WinnerSide.SIDE_B, diffs.profitMargin.favourableSide)

        // Selling Price: Tie
        assertTrue(diffs.customerSellingPrice.isTie)
        assertEquals(0, diffs.customerSellingPrice.signedDifference!!.compareTo(BigDecimal.ZERO))
    }

    @Test
    fun testDifferentUnitsPerPackNormalization() {
        // Product A: 10 tablets/pack at PTR ₹100 (₹10/unit)
        val inputA = CalculationInput(
            productName = "Tablets 10s",
            ptr = BigDecimal("100.00"),
            purchaseTaxRate = BigDecimal.ZERO,
            sellingPrice = BigDecimal("150.00"),
            sellingTaxRate = BigDecimal.ZERO,
            unitsPerPack = 10
        )

        // Product B: 20 tablets/pack at PTR ₹160 (₹8/unit)
        val inputB = CalculationInput(
            productName = "Tablets 20s",
            ptr = BigDecimal("160.00"),
            purchaseTaxRate = BigDecimal.ZERO,
            sellingPrice = BigDecimal("300.00"),
            sellingTaxRate = BigDecimal.ZERO,
            unitsPerPack = 20
        )

        val outcome = ComparisonEngine.compare(inputA, inputB)
        assertTrue(outcome.isValid)
        val diffs = outcome.differences!!

        // Must normalize: areUnitsPerPackEqual is false
        assertFalse(diffs.areUnitsPerPackEqual)

        // Pack cost has different bases, so favourability rule is NEUTRAL to prevent false conclusions
        assertEquals(FavourabilityRule.NEUTRAL, diffs.costPerPack.favourabilityRule)
        assertEquals(WinnerSide.NONE, diffs.costPerPack.favourableSide)

        // Cost per individual unit is normalized and accurately detects Option B as cheaper
        assertEquals(FavourabilityRule.LOWER_IS_BETTER, diffs.costPerUnit.favourabilityRule)
        assertEquals(WinnerSide.SIDE_B, diffs.costPerUnit.favourableSide)
        assertEquals(BigDecimal("-2.00"), diffs.costPerUnit.roundedSignedDifference)
        assertEquals("₹2.00/unit", diffs.costPerUnit.formatAbsolute())
    }

    @Test
    fun testSupplierComparisonDifferences() {
        // Supplier Alpha: PTR 100 with 10% discount -> Effective cost 90
        val offerA = CalculationInput(
            productName = "Syrup",
            supplierName = "Alpha",
            ptr = BigDecimal("100.00"),
            purchaseTaxRate = BigDecimal.ZERO,
            isSupplierDiscountEnabled = true,
            supplierDiscountPercent = BigDecimal("10.00"),
            sellingPrice = BigDecimal("150.00"),
            sellingTaxRate = BigDecimal.ZERO,
            unitsPerPack = 1
        )

        // Supplier Beta: PTR 95 flat
        val offerB = CalculationInput(
            productName = "Syrup",
            supplierName = "Beta",
            ptr = BigDecimal("95.00"),
            purchaseTaxRate = BigDecimal.ZERO,
            isSupplierDiscountEnabled = false,
            sellingPrice = BigDecimal("150.00"),
            sellingTaxRate = BigDecimal.ZERO,
            unitsPerPack = 1
        )

        val outcome = ComparisonEngine.compare(offerA, offerB)
        assertTrue(outcome.isValid)
        val diffs = outcome.differences!!

        // PTR difference (95 - 100 = -5.00)
        assertEquals(0, BigDecimal("-5.00").compareTo(diffs.ptr.signedDifference))
        assertEquals(BigDecimal("-5.00"), diffs.ptr.roundedSignedDifference)
        assertEquals(WinnerSide.SIDE_B, diffs.ptr.lowerSide)

        // Discount difference (0 - 10 = -10 percentage points)
        assertEquals(0, BigDecimal("-10.00").compareTo(diffs.supplierDiscountPercent.signedDifference))
        assertEquals(BigDecimal("-10.00"), diffs.supplierDiscountPercent.roundedSignedDifference)
        assertEquals(WinnerSide.SIDE_A, diffs.supplierDiscountPercent.higherSide)

        // Effective Cost per unit (Beta 95 vs Alpha 90 = +5.00)
        assertEquals(0, BigDecimal("5.00").compareTo(diffs.costPerUnit.signedDifference))
        assertEquals(BigDecimal("5.00"), diffs.costPerUnit.roundedSignedDifference)
        assertEquals(WinnerSide.SIDE_A, diffs.costPerUnit.favourableSide)
        assertEquals("Alpha is cheaper by ₹5.00/unit", diffs.costPerUnit.formatNarrative("Alpha", "Beta"))
    }

    @Test
    fun testBogoComparisonReflectedInDifferences() {
        // Option A: PTR 100, 1 paid, 0 free
        val inputA = CalculationInput(
            productName = "Standard",
            ptr = BigDecimal("100.00"),
            purchaseTaxRate = BigDecimal.ZERO,
            sellingPrice = BigDecimal("150.00"),
            sellingTaxRate = BigDecimal.ZERO,
            unitsPerPack = 1,
            quantity = 1
        )

        // Option B: Buy 5 Get 1 Free at PTR 100 -> Cost/unit = 500 / 6 = 83.33
        val inputB = CalculationInput(
            productName = "Offer",
            ptr = BigDecimal("100.00"),
            purchaseTaxRate = BigDecimal.ZERO,
            quantity = 5,
            isSupplierOfferEnabled = true,
            offerBuyQuantity = 5,
            offerFreeQuantity = 1,
            sellingPrice = BigDecimal("150.00"),
            sellingTaxRate = BigDecimal.ZERO,
            unitsPerPack = 1
        )

        val outcome = ComparisonEngine.compare(inputA, inputB)
        assertTrue(outcome.isValid)
        val diffs = outcome.differences!!

        assertEquals(BigDecimal("1"), diffs.freePacksReceived.signedDifference)
        assertEquals(BigDecimal("5"), diffs.totalPacksReceived.signedDifference)
        assertEquals(WinnerSide.SIDE_B, diffs.freePacksReceived.higherSide)
        assertEquals(WinnerSide.NONE, diffs.freePacksReceived.favourableSide)
        assertEquals(WinnerSide.SIDE_B, diffs.totalPacksReceived.higherSide)
        assertEquals(WinnerSide.NONE, diffs.totalPacksReceived.favourableSide)
        assertEquals(WinnerSide.SIDE_B, diffs.costPerUnit.favourableSide)
        // Option B costs ~16.67 less per unit
        assertEquals(BigDecimal("-16.67"), diffs.costPerUnit.roundedSignedDifference)
    }

    @Test
    fun testHigherDiscountDoesNotAutomaticallyEstablishFavourability() {
        // Supplier A: PTR 100 with 5% discount -> Effective cost 95, selling 200 -> profit 105, margin 52.5%
        val inputA = CalculationInput(
            productName = "Product A",
            supplierName = "Supplier A",
            ptr = BigDecimal("100.00"),
            purchaseTaxRate = BigDecimal.ZERO,
            isSupplierDiscountEnabled = true,
            supplierDiscountPercent = BigDecimal("5.00"),
            sellingPrice = BigDecimal("200.00"),
            sellingTaxRate = BigDecimal.ZERO,
            unitsPerPack = 1,
            quantity = 1
        )

        // Supplier B: PTR 200 with 20% discount -> Effective cost 160, selling 200 -> profit 40, margin 20.0%
        // Supplier B has 4x higher discount % and higher discount amount, but is commercially worse!
        val inputB = CalculationInput(
            productName = "Product B",
            supplierName = "Supplier B",
            ptr = BigDecimal("200.00"),
            purchaseTaxRate = BigDecimal.ZERO,
            isSupplierDiscountEnabled = true,
            supplierDiscountPercent = BigDecimal("20.00"),
            sellingPrice = BigDecimal("200.00"),
            sellingTaxRate = BigDecimal.ZERO,
            unitsPerPack = 1,
            quantity = 1
        )

        val outcome = ComparisonEngine.compare(inputA, inputB)
        assertTrue(outcome.isValid)
        val diffs = outcome.differences!!

        // Discount % difference: 20 - 5 = +15 percentage points
        assertEquals(0, BigDecimal("15.00").compareTo(diffs.supplierDiscountPercent.signedDifference))
        assertEquals(WinnerSide.SIDE_B, diffs.supplierDiscountPercent.higherSide)
        // Regression requirement: Higher discount does NOT establish favourable side (must be NONE)
        assertEquals(WinnerSide.NONE, diffs.supplierDiscountPercent.favourableSide)
        assertEquals(FavourabilityRule.NEUTRAL, diffs.supplierDiscountPercent.favourabilityRule)
        assertEquals("Supplier B is higher by 15.00 percentage points", diffs.supplierDiscountPercent.formatNarrative("Supplier A", "Supplier B"))

        // Discount amount difference: 40 - 5 = +35
        assertEquals(WinnerSide.SIDE_B, diffs.supplierDiscountAmount.higherSide)
        assertEquals(WinnerSide.NONE, diffs.supplierDiscountAmount.favourableSide)
        assertEquals(FavourabilityRule.NEUTRAL, diffs.supplierDiscountAmount.favourabilityRule)

        // Commercially, Supplier A is cheaper and more profitable:
        assertEquals(WinnerSide.SIDE_A, diffs.costPerUnit.favourableSide)
        assertEquals(WinnerSide.SIDE_A, diffs.profitPerUnit.favourableSide)
        assertEquals(WinnerSide.SIDE_A, diffs.profitMargin.favourableSide)
    }

    @Test
    fun testFreePacksAndTotalPacksDoNotEstablishCommercialFavourability() {
        // Option A: PTR 100, 10 paid, 0 free -> 10 total packs, Cost/unit = 100
        val inputA = CalculationInput(
            productName = "Option A",
            ptr = BigDecimal("100.00"),
            purchaseTaxRate = BigDecimal.ZERO,
            sellingPrice = BigDecimal("250.00"),
            sellingTaxRate = BigDecimal.ZERO,
            unitsPerPack = 1,
            quantity = 10
        )

        // Option B: PTR 250, Buy 5 Get 1 Free, 10 paid -> 2 free packs -> 12 total packs, Cost/unit = 2500 / 12 = 208.33
        // Option B receives more free packs (2 vs 0) and more total packs (12 vs 10), but is far more expensive!
        val inputB = CalculationInput(
            productName = "Option B",
            ptr = BigDecimal("250.00"),
            purchaseTaxRate = BigDecimal.ZERO,
            quantity = 10,
            isSupplierOfferEnabled = true,
            offerBuyQuantity = 5,
            offerFreeQuantity = 1,
            sellingPrice = BigDecimal("250.00"),
            sellingTaxRate = BigDecimal.ZERO,
            unitsPerPack = 1
        )

        val outcome = ComparisonEngine.compare(inputA, inputB)
        assertTrue(outcome.isValid)
        val diffs = outcome.differences!!

        // Free packs: Option B has higher count, but NEUTRAL favourability
        assertEquals(BigDecimal("2"), diffs.freePacksReceived.signedDifference)
        assertEquals(WinnerSide.SIDE_B, diffs.freePacksReceived.higherSide)
        assertEquals(WinnerSide.NONE, diffs.freePacksReceived.favourableSide)
        assertEquals(FavourabilityRule.NEUTRAL, diffs.freePacksReceived.favourabilityRule)

        // Total packs received: Option B has higher count, but NEUTRAL favourability
        assertEquals(BigDecimal("2"), diffs.totalPacksReceived.signedDifference)
        assertEquals(WinnerSide.SIDE_B, diffs.totalPacksReceived.higherSide)
        assertEquals(WinnerSide.NONE, diffs.totalPacksReceived.favourableSide)
        assertEquals(FavourabilityRule.NEUTRAL, diffs.totalPacksReceived.favourabilityRule)

        // Commercial favourability correctly goes to Option A (Cost 100 vs 208.33)
        assertEquals(WinnerSide.SIDE_A, diffs.costPerUnit.favourableSide)
    }

    @Test
    fun testDoubleLossScenarioIdentifiesSmallerLossAsFavourable() {
        // Product A: Cost 100, Selling 80 -> Profit -20 (loss)
        val inputA = CalculationInput(
            productName = "A",
            ptr = BigDecimal("100.00"),
            purchaseTaxRate = BigDecimal.ZERO,
            sellingPrice = BigDecimal("80.00"),
            sellingTaxRate = BigDecimal.ZERO,
            unitsPerPack = 1
        )

        // Product B: Cost 100, Selling 90 -> Profit -10 (smaller loss)
        val inputB = CalculationInput(
            productName = "B",
            ptr = BigDecimal("100.00"),
            purchaseTaxRate = BigDecimal.ZERO,
            sellingPrice = BigDecimal("90.00"),
            sellingTaxRate = BigDecimal.ZERO,
            unitsPerPack = 1
        )

        val outcome = ComparisonEngine.compare(inputA, inputB)
        assertTrue(outcome.isValid)
        val diffs = outcome.differences!!

        // Profit / Unit difference: -10 - (-20) = +10.00
        assertEquals(0, BigDecimal("10.00").compareTo(diffs.profitPerUnit.signedDifference))
        assertEquals(WinnerSide.SIDE_B, diffs.profitPerUnit.higherSide)
        assertEquals(WinnerSide.SIDE_B, diffs.profitPerUnit.favourableSide)
    }
}
