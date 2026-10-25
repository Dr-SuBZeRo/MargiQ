package com.margiq.domain.calculation

import com.margiq.domain.model.CalculationInput
import com.margiq.domain.model.PurchaseTaxClassification
import com.margiq.domain.model.TaxPriceMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class ProfitCalculationEngineTest {

    /**
     * Mandatory Regression Test from Section 5:
     * PTR ₹48.00 exclusive 5% GST, ITC enabled.
     * MRP ₹135.00 inclusive 5% GST.
     * Pack: Strip, Unit: Tablet, Units per pack: 10.
     * Purchase quantity: 5 strips. Selling quantity: 5 strips.
     */
    @Test
    fun testMandatorySection5RegressionScenario() {
        val input = CalculationInput(
            productName = "Pharma Strip Test",
            ptr = BigDecimal("48.00"),
            purchaseTaxRate = BigDecimal("5.00"),
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            purchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
            isInputTaxCreditEnabled = true,
            sellingPrice = BigDecimal("135.00"),
            sellingTaxRate = BigDecimal("5.00"),
            sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE,
            quantity = 5,
            packName = "Strip",
            unitName = "Tablet",
            unitsPerPack = 10,
            sellingQuantity = 5
        )

        val result = ProfitCalculationEngine.calculate(input)

        assertTrue("Calculation should be valid", result.isValid)

        // --- Purchase Side per strip ---
        assertEquals(BigDecimal("48.00"), result.taxablePurchaseBase.setScale(2))
        assertEquals(BigDecimal("2.40"), result.purchaseTaxAmount.setScale(2, java.math.RoundingMode.HALF_UP))
        assertEquals(BigDecimal("50.40"), result.roundedPurchaseInvoice)
        assertEquals(BigDecimal("48.00"), result.roundedEconomicCost)

        // --- Multi-strip purchase totals (5 strips) ---
        assertEquals(BigDecimal("252.00"), result.roundedTotalPurchaseInvoice)
        assertEquals(BigDecimal("240.00"), result.roundedTotalEconomicCost)
        assertEquals(BigDecimal("12.00"), result.roundedTotalInputCredit)

        // --- Selling Side per strip ---
        assertEquals(BigDecimal("135.00"), result.roundedCustomerSellingPrice)
        assertEquals(BigDecimal("128.57"), result.roundedNetSalesRevenue)
        assertEquals(BigDecimal("6.43"), result.roundedSellingTaxAmount)

        // --- Units ---
        assertEquals(50, result.totalIndividualUnits)
        assertEquals(50, result.totalIndividualUnitsSold)

        // --- Per Tablet (Individual Unit) ---
        assertEquals(BigDecimal("4.80"), result.roundedCostPerIndividualUnit)
        assertEquals(BigDecimal("13.50"), result.roundedCustomerPricePerIndividualUnit)
        assertEquals(BigDecimal("12.86"), result.roundedNetRevenuePerIndividualUnit)
        assertEquals(BigDecimal("8.06"), result.roundedProfitPerIndividualUnit)

        // --- Per Strip ---
        assertEquals(BigDecimal("80.57"), result.roundedProfitPerUnit)
        assertEquals(BigDecimal("62.67"), result.roundedProfitMargin)
        assertEquals(BigDecimal("167.86"), result.roundedMarkup)

        // --- For Five Strips Sold ---
        assertEquals(BigDecimal("675.00"), result.roundedTotalCustomerCollection)
        assertEquals(BigDecimal("642.86"), result.roundedTotalNetSalesRevenue)
        assertEquals(BigDecimal("32.14"), result.roundedTotalOutputTax)
        assertEquals(BigDecimal("402.86"), result.roundedTotalProfit)
    }

    @Test
    fun testVariousPackSizes_1_10_15_30_100() {
        val packSizes = listOf(1, 10, 15, 30, 100)

        for (size in packSizes) {
            val input = CalculationInput(
                ptr = BigDecimal("60.00"),
                purchaseTaxRate = BigDecimal("5.00"),
                purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
                purchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
                isInputTaxCreditEnabled = true,
                sellingPrice = BigDecimal("150.00"),
                sellingTaxRate = BigDecimal("5.00"),
                sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE,
                quantity = 4,
                packName = "Box",
                unitName = "Piece",
                unitsPerPack = size,
                sellingQuantity = 4
            )

            val result = ProfitCalculationEngine.calculate(input)
            assertTrue(result.isValid)
            assertEquals(4 * size, result.totalIndividualUnits)
            assertEquals(4 * size, result.totalIndividualUnitsSold)

            // Cost per individual unit = 60.00 / size
            val expectedCostPerUnit = BigDecimal("60.00").divide(BigDecimal(size), 2, java.math.RoundingMode.HALF_UP)
            assertEquals(expectedCostPerUnit, result.roundedCostPerIndividualUnit)

            // In full precision, unit profit times size exactly equals pack profit
            val fullPrecisionPackProfit = result.profitPerIndividualUnit.multiply(BigDecimal(size))
                .setScale(2, java.math.RoundingMode.HALF_UP)
            assertEquals(result.roundedProfitPerUnit, fullPrecisionPackProfit)

            // Explicit expected rounded profit per individual unit without ₹1 tolerance
            val expectedUnitProfits = mapOf(
                1 to BigDecimal("82.86"),
                10 to BigDecimal("8.29"),
                15 to BigDecimal("5.52"),
                30 to BigDecimal("2.76"),
                100 to BigDecimal("0.83")
            )
            assertEquals(expectedUnitProfits[size], result.roundedProfitPerIndividualUnit)
        }
    }

    @Test
    fun testLiveUpdateBehavior_ChangingUnitsPerPackAndQuantity() {
        // Base: 5 strips × 10 tablets = 50 tablets
        val baseInput = CalculationInput(
            quantity = 5,
            unitsPerPack = 10,
            packName = "Strip",
            unitName = "Tablet"
        )
        val baseResult = ProfitCalculationEngine.calculate(baseInput)
        assertEquals(50, baseResult.totalIndividualUnits)

        // Changing Units per pack from 10 to 15 updates total to 75 tablets without changing strips
        val updatedPackSizeInput = baseInput.copy(unitsPerPack = 15)
        val updatedPackResult = ProfitCalculationEngine.calculate(updatedPackSizeInput)
        assertEquals(5, updatedPackResult.quantity)
        assertEquals(75, updatedPackResult.totalIndividualUnits)

        // Changing purchase quantity from 5 to 8 strips with pack size 10 updates total to 80 tablets
        val updatedQtyInput = baseInput.copy(quantity = 8)
        val updatedQtyResult = ProfitCalculationEngine.calculate(updatedQtyInput)
        assertEquals(8, updatedQtyResult.quantity)
        assertEquals(80, updatedQtyResult.totalIndividualUnits)
    }

    @Test
    fun testDecoupledSellingQuantity() {
        // Purchase 10 packs, but only sell 3 packs
        val input = CalculationInput(
            ptr = BigDecimal("100.00"),
            purchaseTaxRate = BigDecimal("0.00"),
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            sellingPrice = BigDecimal("150.00"),
            sellingTaxRate = BigDecimal("0.00"),
            sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE,
            quantity = 10,
            packName = "Bottle",
            unitName = "ml",
            unitsPerPack = 100,
            sellingQuantity = 3
        )

        val result = ProfitCalculationEngine.calculate(input)
        assertTrue(result.isValid)

        // Units
        assertEquals(1000, result.totalIndividualUnits)
        assertEquals(300, result.totalIndividualUnitsSold)

        // Purchase total is for 10 bottles
        assertEquals(BigDecimal("1000.00"), result.roundedTotalPurchaseInvoice)
        assertEquals(BigDecimal("1000.00"), result.roundedTotalEconomicCost)

        // Selling total and profit are for 3 bottles sold
        assertEquals(BigDecimal("50.00"), result.roundedProfitPerUnit)
        assertEquals(BigDecimal("450.00"), result.roundedTotalCustomerCollection)
        assertEquals(BigDecimal("150.00"), result.roundedTotalProfit)
    }

    @Test
    fun testNonRecoverableTaxScenario() {
        val input = CalculationInput(
            productName = "Sample Non-recoverable",
            ptr = BigDecimal("48.00"),
            purchaseTaxRate = BigDecimal("5.00"),
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            purchaseTaxClassification = PurchaseTaxClassification.NON_RECOVERABLE,
            isInputTaxCreditEnabled = false,
            sellingPrice = BigDecimal("135.00"),
            sellingTaxRate = BigDecimal("5.00"),
            sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE,
            quantity = 1,
            unitsPerPack = 10
        )

        val result = ProfitCalculationEngine.calculate(input)

        assertTrue(result.isValid)
        assertEquals(BigDecimal("0.00"), result.roundedEligibleInputTaxCredit)
        assertEquals(BigDecimal("50.40"), result.roundedEconomicCost)
        assertEquals(BigDecimal("128.57"), result.roundedNetSalesRevenue)
        assertEquals(BigDecimal("78.17"), result.roundedProfitPerUnit)
        assertEquals(BigDecimal("7.82"), result.roundedProfitPerIndividualUnit)
        assertEquals(BigDecimal("60.80"), result.roundedProfitMargin)
        assertEquals(BigDecimal("155.10"), result.roundedMarkup)
        assertEquals(BigDecimal("39.20"), result.roundedCostPercentage)
    }

    @Test
    fun testInputTaxCreditDisabledForRecoverableTax() {
        val input = CalculationInput(
            productName = "Sample ITC Disabled",
            ptr = BigDecimal("48.00"),
            purchaseTaxRate = BigDecimal("5.00"),
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            purchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
            isInputTaxCreditEnabled = false,
            sellingPrice = BigDecimal("135.00"),
            sellingTaxRate = BigDecimal("5.00"),
            sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE,
            quantity = 1,
            unitsPerPack = 10
        )

        val result = ProfitCalculationEngine.calculate(input)

        assertTrue(result.isValid)
        assertEquals(BigDecimal("0.00"), result.roundedEligibleInputTaxCredit)
        assertEquals(BigDecimal("50.40"), result.roundedEconomicCost)
        assertEquals(BigDecimal("78.17"), result.roundedProfitPerUnit)
        assertEquals(BigDecimal("7.82"), result.roundedProfitPerIndividualUnit)
    }

    @Test
    fun testTaxInclusivePurchasePrice() {
        val input = CalculationInput(
            ptr = BigDecimal("105.00"),
            purchaseTaxRate = BigDecimal("5.00"),
            purchasePriceMode = TaxPriceMode.TAX_INCLUSIVE,
            purchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
            isInputTaxCreditEnabled = true,
            sellingPrice = BigDecimal("150.00"),
            sellingTaxRate = BigDecimal("0.00"),
            sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE,
            quantity = 1,
            unitsPerPack = 5
        )

        val result = ProfitCalculationEngine.calculate(input)

        assertEquals(BigDecimal("100.00"), result.taxablePurchaseBase.setScale(2))
        assertEquals(BigDecimal("5.00"), result.roundedEligibleInputTaxCredit)
        assertEquals(BigDecimal("105.00"), result.roundedPurchaseInvoice)
        assertEquals(BigDecimal("100.00"), result.roundedEconomicCost)
        assertEquals(BigDecimal("20.00"), result.roundedCostPerIndividualUnit)
    }

    @Test
    fun testTaxExclusiveSellingPrice() {
        val input = CalculationInput(
            ptr = BigDecimal("100.00"),
            purchaseTaxRate = BigDecimal("0.00"),
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            sellingPrice = BigDecimal("200.00"),
            sellingTaxRate = BigDecimal("18.00"),
            sellingPriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            quantity = 1,
            unitsPerPack = 2
        )

        val result = ProfitCalculationEngine.calculate(input)

        assertEquals(BigDecimal("200.00"), result.roundedNetSalesRevenue)
        assertEquals(BigDecimal("36.00"), result.roundedSellingTaxAmount)
        assertEquals(BigDecimal("236.00"), result.roundedCustomerSellingPrice)
        assertEquals(BigDecimal("100.00"), result.roundedProfitPerUnit)
        assertEquals(BigDecimal("50.00"), result.roundedProfitMargin)
        assertEquals(BigDecimal("100.00"), result.roundedMarkup)
        assertEquals(BigDecimal("50.00"), result.roundedProfitPerIndividualUnit)
    }

    @Test
    fun testZeroTaxRates() {
        val input = CalculationInput(
            ptr = BigDecimal("100.00"),
            purchaseTaxRate = BigDecimal("0.00"),
            purchasePriceMode = TaxPriceMode.TAX_INCLUSIVE,
            sellingPrice = BigDecimal("150.00"),
            sellingTaxRate = BigDecimal("0.00"),
            sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE,
            quantity = 1,
            unitsPerPack = 10
        )

        val result = ProfitCalculationEngine.calculate(input)

        assertEquals(BigDecimal("100.00"), result.roundedEconomicCost)
        assertEquals(BigDecimal("150.00"), result.roundedNetSalesRevenue)
        assertEquals(BigDecimal("50.00"), result.roundedProfitPerUnit)
        assertEquals(BigDecimal("33.33"), result.roundedProfitMargin)
        assertEquals(BigDecimal("50.00"), result.roundedMarkup)
        assertEquals(BigDecimal("5.00"), result.roundedProfitPerIndividualUnit)
    }

    @Test
    fun testBreakEvenCalculation() {
        val input = CalculationInput(
            ptr = BigDecimal("100.00"),
            purchaseTaxRate = BigDecimal("0.00"),
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            sellingPrice = BigDecimal("100.00"),
            sellingTaxRate = BigDecimal("0.00"),
            sellingPriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            quantity = 1,
            unitsPerPack = 10
        )

        val result = ProfitCalculationEngine.calculate(input)

        assertTrue(result.isBreakEven)
        assertFalse(result.isLoss)
        assertEquals(BigDecimal("0.00"), result.roundedProfitPerUnit)
        assertEquals(BigDecimal("0.00"), result.roundedProfitPerIndividualUnit)
        assertEquals(BigDecimal("0.00"), result.roundedProfitMargin)
        assertEquals(BigDecimal("0.00"), result.roundedMarkup)
    }

    @Test
    fun testNegativeProfitLossScenario() {
        val input = CalculationInput(
            ptr = BigDecimal("120.00"),
            purchaseTaxRate = BigDecimal("0.00"),
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            sellingPrice = BigDecimal("100.00"),
            sellingTaxRate = BigDecimal("0.00"),
            sellingPriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            quantity = 1,
            unitsPerPack = 10
        )

        val result = ProfitCalculationEngine.calculate(input)

        assertTrue(result.isLoss)
        assertFalse(result.isBreakEven)
        assertEquals(BigDecimal("-20.00"), result.roundedProfitPerUnit)
        assertEquals(BigDecimal("-2.00"), result.roundedProfitPerIndividualUnit)
        assertEquals(BigDecimal("-20.00"), result.roundedProfitMargin)
        assertEquals(BigDecimal("-16.67"), result.roundedMarkup)
    }

    @Test
    fun testZeroAndInvalidPackSizes() {
        // Zero pack size
        val zeroPackSize = CalculationInput(unitsPerPack = 0)
        val resultZeroPack = ProfitCalculationEngine.calculate(zeroPackSize)
        assertFalse(resultZeroPack.isValid)
        assertTrue(resultZeroPack.validationMessage?.contains("Units per pack") == true)

        // Negative pack size
        val negPackSize = CalculationInput(unitsPerPack = -5)
        val resultNegPack = ProfitCalculationEngine.calculate(negPackSize)
        assertFalse(resultNegPack.isValid)
        assertTrue(resultNegPack.validationMessage?.contains("Units per pack") == true)

        // Negative selling quantity
        val negSellingQty = CalculationInput(sellingQuantity = -1)
        val resultNegSelling = ProfitCalculationEngine.calculate(negSellingQty)
        assertFalse(resultNegSelling.isValid)

        // Negative prices
        val negativePrice = CalculationInput(ptr = BigDecimal("-10.00"))
        val resultNegative = ProfitCalculationEngine.calculate(negativePrice)
        assertFalse(resultNegative.isValid)

        val negativeTax = CalculationInput(purchaseTaxRate = BigDecimal("-5.00"))
        val resultTax = ProfitCalculationEngine.calculate(negativeTax)
        assertFalse(resultTax.isValid)

        val zeroQty = CalculationInput(quantity = 0)
        val resultZeroQty = ProfitCalculationEngine.calculate(zeroQty)
        assertFalse(resultZeroQty.isValid)
    }

    @Test
    fun testZeroSalesRevenueHandledWithoutDivisionByZero() {
        val input = CalculationInput(
            ptr = BigDecimal("50.00"),
            purchaseTaxRate = BigDecimal("0.00"),
            sellingPrice = BigDecimal("0.00"),
            sellingTaxRate = BigDecimal("0.00"),
            quantity = 1,
            unitsPerPack = 10
        )

        val result = ProfitCalculationEngine.calculate(input)

        assertTrue(result.isValid)
        assertEquals(BigDecimal("-50.00"), result.roundedProfitPerUnit)
        assertNull(result.profitMargin)
        assertNotNull(result.markup)
    }

    @Test
    fun testZeroEconomicCostHandledWithoutDivisionByZero() {
        val input = CalculationInput(
            ptr = BigDecimal("0.00"),
            purchaseTaxRate = BigDecimal("0.00"),
            sellingPrice = BigDecimal("100.00"),
            sellingTaxRate = BigDecimal("0.00"),
            quantity = 1,
            unitsPerPack = 10
        )

        val result = ProfitCalculationEngine.calculate(input)

        assertTrue(result.isValid)
        assertEquals(BigDecimal("100.00"), result.roundedProfitPerUnit)
        assertNotNull(result.profitMargin)
        assertNull(result.markup)
    }

    @Test
    fun testZeroPacksSold() {
        val input = CalculationInput(
            ptr = BigDecimal("50.00"),
            purchaseTaxRate = BigDecimal("5.00"),
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            purchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
            isInputTaxCreditEnabled = true,
            sellingPrice = BigDecimal("100.00"),
            sellingTaxRate = BigDecimal("5.00"),
            sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE,
            quantity = 10,
            packName = "Box",
            unitName = "Vial",
            unitsPerPack = 10,
            sellingQuantity = 0
        )

        val result = ProfitCalculationEngine.calculate(input)
        assertTrue("Zero selling quantity must be valid", result.isValid)

        // Per-pack unit economic metrics remain valid pricing references
        assertEquals(BigDecimal("50.00"), result.roundedEconomicCost)
        assertEquals(BigDecimal("95.24"), result.roundedNetSalesRevenue)
        assertEquals(BigDecimal("45.24"), result.roundedProfitPerUnit)

        // Multi-pack realized sales totals must be ZERO
        assertEquals(0, result.sellingQuantity)
        assertEquals(0, result.totalIndividualUnitsSold)
        assertEquals(BigDecimal("0.00"), result.roundedTotalCustomerCollection)
        assertEquals(BigDecimal("0.00"), result.roundedTotalNetSalesRevenue)
        assertEquals(BigDecimal("0.00"), result.roundedTotalOutputTax)
        assertEquals(BigDecimal("0.00"), result.roundedTotalProfit)

        // Purchase totals reflect full batch purchased
        assertEquals(10, result.quantity)
        assertEquals(100, result.totalIndividualUnits)
        assertEquals(BigDecimal("525.00"), result.roundedTotalPurchaseInvoice)
        assertEquals(BigDecimal("500.00"), result.roundedTotalEconomicCost)
        assertEquals(BigDecimal("25.00"), result.roundedTotalInputCredit)

        // Remaining inventory equals total purchased inventory
        assertEquals(10, result.remainingPacks)
        assertEquals(100, result.remainingIndividualUnits)
    }

    @Test
    fun testPartialSalesQuantityDifferentFromPurchaseQuantity() {
        val input = CalculationInput(
            ptr = BigDecimal("80.00"),
            purchaseTaxRate = BigDecimal("12.00"),
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            purchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
            isInputTaxCreditEnabled = true,
            sellingPrice = BigDecimal("150.00"),
            sellingTaxRate = BigDecimal("12.00"),
            sellingPriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            quantity = 20,
            packName = "Pack",
            unitName = "Sachet",
            unitsPerPack = 5,
            sellingQuantity = 7
        )

        val result = ProfitCalculationEngine.calculate(input)
        assertTrue(result.isValid)

        assertEquals(20, result.quantity)
        assertEquals(7, result.sellingQuantity)
        assertEquals(100, result.totalIndividualUnits)
        assertEquals(35, result.totalIndividualUnitsSold)
        assertEquals(13, result.remainingPacks)
        assertEquals(65, result.remainingIndividualUnits)

        // Economics per pack
        assertEquals(BigDecimal("80.00"), result.roundedEconomicCost)
        assertEquals(BigDecimal("150.00"), result.roundedNetSalesRevenue)
        assertEquals(BigDecimal("70.00"), result.roundedProfitPerUnit)

        // Realized profit is 7 packs sold * 70.00 = 490.00
        assertEquals(BigDecimal("490.00"), result.roundedTotalProfit)
        // Customer collection is 7 packs * (150 + 18) = 7 * 168 = 1176.00
        assertEquals(BigDecimal("1176.00"), result.roundedTotalCustomerCollection)
        // Net revenue is 7 packs * 150 = 1050.00
        assertEquals(BigDecimal("1050.00"), result.roundedTotalNetSalesRevenue)
        // Output tax is 7 packs * 18 = 126.00
        assertEquals(BigDecimal("126.00"), result.roundedTotalOutputTax)
    }

    @Test
    fun testVeryLargeQuantityInputsAndOverflowGuards() {
        // Safe large quantity within integer bounds
        val largeSafeInput = CalculationInput(
            quantity = 100_000,
            unitsPerPack = 10_000,
            sellingQuantity = 50_000
        )
        val safeResult = ProfitCalculationEngine.calculate(largeSafeInput)
        assertTrue(safeResult.isValid)
        assertEquals(1_000_000_000, safeResult.totalIndividualUnits)
        assertEquals(500_000_000, safeResult.totalIndividualUnitsSold)
        assertEquals(500_000_000, safeResult.remainingIndividualUnits)
        assertEquals(50_000, safeResult.remainingPacks)

        // Overflow: quantity * unitsPerPack > Int.MAX_VALUE
        val overflowInput = CalculationInput(
            quantity = Int.MAX_VALUE,
            unitsPerPack = 2,
            sellingQuantity = 1
        )
        val overflowResult = ProfitCalculationEngine.calculate(overflowInput)
        assertFalse("Multiplication exceeding Int.MAX_VALUE must be rejected", overflowResult.isValid)
        assertTrue(
            overflowResult.validationMessage?.contains("overflow") == true ||
            overflowResult.validationMessage?.contains("limit") == true
        )

        // Selling quantity overflow
        val sellingOverflowInput = CalculationInput(
            quantity = 10,
            unitsPerPack = 10,
            sellingQuantity = Int.MAX_VALUE
        )
        val sellingOverflowResult = ProfitCalculationEngine.calculate(sellingOverflowInput)
        assertFalse(sellingOverflowResult.isValid)
    }

    @Test
    fun testFullPrecisionCalculationsPreventPrematureRoundingErrors() {
        val input = CalculationInput(
            ptr = BigDecimal("33.33"),
            purchaseTaxRate = BigDecimal("18.00"),
            purchasePriceMode = TaxPriceMode.TAX_INCLUSIVE,
            purchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
            isInputTaxCreditEnabled = true,
            sellingPrice = BigDecimal("49.99"),
            sellingTaxRate = BigDecimal("18.00"),
            sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE,
            quantity = 100,
            unitsPerPack = 3,
            sellingQuantity = 100
        )

        val result = ProfitCalculationEngine.calculate(input)
        assertTrue(result.isValid)

        // Full precision prevents premature rounding discrepancy
        assertEquals(BigDecimal("1411.86"), result.roundedTotalProfit)
        assertEquals(BigDecimal("4236.44"), result.roundedTotalNetSalesRevenue)
        assertEquals(BigDecimal("2824.58"), result.roundedTotalEconomicCost)
    }

    @Test
    fun testNegativeSellingQuantityIsRejected() {
        val input = CalculationInput(sellingQuantity = -1)
        val result = ProfitCalculationEngine.calculate(input)
        assertFalse(result.isValid)
        assertEquals("Selling quantity cannot be negative.", result.validationMessage)
    }

    // ==========================================
    // MargiQ v1.2.0 Supplier Offer Regression Tests
    // ==========================================

    @Test
    fun testSupplierOfferDisabled_BaselineCalculationsUnchanged() {
        val disabledOfferInput = CalculationInput(
            ptr = BigDecimal("100.00"),
            purchaseTaxRate = BigDecimal("5.00"),
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            purchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
            isInputTaxCreditEnabled = true,
            sellingPrice = BigDecimal("150.00"),
            sellingTaxRate = BigDecimal("5.00"),
            sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE,
            quantity = 5,
            unitsPerPack = 10,
            isSupplierOfferEnabled = false,
            offerBuyQuantity = 1,
            offerFreeQuantity = 1,
            supplierDiscountPercent = BigDecimal.ZERO
        )

        val result = ProfitCalculationEngine.calculate(disabledOfferInput)
        assertTrue(result.isValid)
        assertEquals(0, result.freePacksReceived)
        assertEquals(5, result.totalPacksReceived)
        assertEquals(BigDecimal("100.00"), result.roundedEconomicCost)
        assertEquals(BigDecimal("100.00"), result.roundedEffectiveCostPerReceivedPack)
        assertEquals(BigDecimal("10.00"), result.roundedCostPerIndividualUnit)
    }

    @Test
    fun testSupplierOffer_Buy1Get1Free_Phase2Scenario_RecoverableAndNonRecoverable() {
        // Recoverable GST with ITC enabled
        val recoverableInput = CalculationInput(
            ptr = BigDecimal("100.00"),
            purchaseTaxRate = BigDecimal("5.00"),
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            purchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
            isInputTaxCreditEnabled = true,
            sellingPrice = BigDecimal("150.00"),
            sellingTaxRate = BigDecimal("5.00"),
            sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE,
            quantity = 1,
            unitsPerPack = 10,
            isSupplierOfferEnabled = true,
            offerBuyQuantity = 1,
            offerFreeQuantity = 1,
            supplierDiscountPercent = BigDecimal.ZERO
        )

        val recResult = ProfitCalculationEngine.calculate(recoverableInput)
        assertTrue(recResult.isValid)
        assertEquals(1, recResult.quantity)
        assertEquals(1, recResult.freePacksReceived)
        assertEquals(2, recResult.totalPacksReceived)

        // Paid purchase value before GST: ₹100, GST = ₹5, Supplier invoice total = ₹105
        assertEquals(BigDecimal("100.00"), recResult.taxablePurchaseBase.setScale(2))
        assertEquals(BigDecimal("5.00"), recResult.purchaseTaxAmount.setScale(2))
        assertEquals(BigDecimal("105.00"), recResult.roundedPurchaseInvoice)
        assertEquals(BigDecimal("105.00"), recResult.roundedTotalPurchaseInvoice)

        // Effective pre-GST cost per received pack when ₹5 purchase GST is fully recoverable: ₹50.00
        assertEquals(BigDecimal("50.00"), recResult.roundedEffectiveCostPerReceivedPack)
        assertEquals(BigDecimal("5.00"), recResult.roundedCostPerIndividualUnit)

        // Selling economics per pack: Net revenue = 150 / 1.05 = 142.86
        assertEquals(BigDecimal("142.86"), recResult.roundedNetSalesRevenue)
        assertEquals(BigDecimal("92.86"), recResult.roundedProfitPerUnit)
        assertEquals(BigDecimal("9.29"), recResult.roundedProfitPerIndividualUnit)
        assertEquals(BigDecimal("185.71"), recResult.roundedTotalProfit)

        // Non-recoverable GST (no ITC)
        val nonRecInput = recoverableInput.copy(
            purchaseTaxClassification = PurchaseTaxClassification.NON_RECOVERABLE,
            isInputTaxCreditEnabled = false
        )
        val nonRecResult = ProfitCalculationEngine.calculate(nonRecInput)
        assertTrue(nonRecResult.isValid)
        // Total effective purchase cost per received pack, including non-recoverable GST: ₹52.50
        assertEquals(BigDecimal("52.50"), nonRecResult.roundedEffectiveCostPerReceivedPack)
        assertEquals(BigDecimal("5.25"), nonRecResult.roundedCostPerIndividualUnit)
        assertEquals(BigDecimal("90.36"), nonRecResult.roundedProfitPerUnit)
        assertEquals(BigDecimal("9.04"), nonRecResult.roundedProfitPerIndividualUnit)
        assertEquals(BigDecimal("180.71"), nonRecResult.roundedTotalProfit)
    }

    @Test
    fun testSupplierOffer_Buy10Get2Free_MultipleCycles_10_and_20_PaidPacks() {
        val baseOfferInput = CalculationInput(
            ptr = BigDecimal("100.00"),
            purchaseTaxRate = BigDecimal("0.00"),
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            sellingPrice = BigDecimal("150.00"),
            sellingTaxRate = BigDecimal("0.00"),
            sellingPriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            unitsPerPack = 10,
            isSupplierOfferEnabled = true,
            offerBuyQuantity = 10,
            offerFreeQuantity = 2,
            supplierDiscountPercent = BigDecimal.ZERO
        )

        // Cycle 1: Paid quantity = 10 packs -> 2 free packs earned -> 12 total packs
        val tenPaidInput = baseOfferInput.copy(quantity = 10)
        val tenResult = ProfitCalculationEngine.calculate(tenPaidInput)
        assertTrue(tenResult.isValid)
        assertEquals(10, tenResult.quantity)
        assertEquals(2, tenResult.freePacksReceived)
        assertEquals(12, tenResult.totalPacksReceived)
        // 1000 total outlay / 12 = 83.3333333333
        assertEquals(BigDecimal("83.33"), tenResult.roundedEffectiveCostPerReceivedPack)

        // Cycle 2: Paid quantity = 20 packs -> 4 free packs earned -> 24 total packs
        val twentyPaidInput = baseOfferInput.copy(quantity = 20)
        val twentyResult = ProfitCalculationEngine.calculate(twentyPaidInput)
        assertTrue(twentyResult.isValid)
        assertEquals(20, twentyResult.quantity)
        assertEquals(4, twentyResult.freePacksReceived)
        assertEquals(24, twentyResult.totalPacksReceived)
        // 2000 total outlay / 24 = 83.3333333333
        assertEquals(BigDecimal("83.33"), twentyResult.roundedEffectiveCostPerReceivedPack)
    }

    @Test
    fun testSupplierOffer_IncompleteOfferCycle_OnlyCompleteCyclesEarnFreePacks() {
        // Buy 10, Get 2 Free: but only paid 5 packs (incomplete cycle)
        val incompleteInput = CalculationInput(
            ptr = BigDecimal("100.00"),
            quantity = 5,
            isSupplierOfferEnabled = true,
            offerBuyQuantity = 10,
            offerFreeQuantity = 2
        )
        val result = ProfitCalculationEngine.calculate(incompleteInput)
        assertTrue(result.isValid)
        assertEquals(5, result.quantity)
        assertEquals(0, result.freePacksReceived)
        assertEquals(5, result.totalPacksReceived)

        // Paid 15 packs: 1 complete cycle (2 free packs earned), 5 remainder packs do not earn bonus
        val fifteenInput = incompleteInput.copy(quantity = 15)
        val fifteenResult = ProfitCalculationEngine.calculate(fifteenInput)
        assertTrue(fifteenResult.isValid)
        assertEquals(15, fifteenResult.quantity)
        assertEquals(2, fifteenResult.freePacksReceived)
        assertEquals(17, fifteenResult.totalPacksReceived)
    }

    @Test
    fun testSupplierOffer_ZeroFreeQuantityBehavesLikeNoFreeBenefit() {
        val zeroFreeInput = CalculationInput(
            quantity = 10,
            isSupplierOfferEnabled = true,
            offerBuyQuantity = 5,
            offerFreeQuantity = 0
        )
        val result = ProfitCalculationEngine.calculate(zeroFreeInput)
        assertTrue(result.isValid)
        assertEquals(0, result.freePacksReceived)
        assertEquals(10, result.totalPacksReceived)
    }

    @Test
    fun testSupplierOffer_InvalidBuyQuantityAndNegativeFreeQuantityRejected() {
        // Buy quantity <= 0 when offer enabled
        val zeroBuyInput = CalculationInput(
            isSupplierOfferEnabled = true,
            offerBuyQuantity = 0,
            offerFreeQuantity = 1
        )
        val zeroBuyResult = ProfitCalculationEngine.calculate(zeroBuyInput)
        assertFalse(zeroBuyResult.isValid)
        assertEquals("Buy quantity must be greater than zero when the offer is enabled.", zeroBuyResult.validationMessage)

        // Free quantity < 0
        val negFreeInput = CalculationInput(
            isSupplierOfferEnabled = true,
            offerBuyQuantity = 10,
            offerFreeQuantity = -2
        )
        val negFreeResult = ProfitCalculationEngine.calculate(negFreeInput)
        assertFalse(negFreeResult.isValid)
        assertEquals("Free quantity cannot be negative.", negFreeResult.validationMessage)
    }

    // ==========================================
    // MargiQ v1.2.0 Supplier Discount Tests
    // ==========================================

    @Test
    fun testSupplierDiscount_ZeroDiscountMatchesBaseline() {
        val zeroDiscInput = CalculationInput(
            ptr = BigDecimal("100.00"),
            purchaseTaxRate = BigDecimal("5.00"),
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            isSupplierDiscountEnabled = true,
            supplierDiscountPercent = BigDecimal.ZERO
        )
        val result = ProfitCalculationEngine.calculate(zeroDiscInput)
        assertTrue(result.isValid)
        assertEquals(BigDecimal("100.00"), result.taxablePurchaseBase.setScale(2))
        assertEquals(BigDecimal("5.00"), result.purchaseTaxAmount.setScale(2))
        assertEquals(BigDecimal("105.00"), result.roundedPurchaseInvoice)
    }

    @Test
    fun testSupplierDiscount_WhenDisabled_StoredPercentageIgnored() {
        val input = CalculationInput(
            ptr = BigDecimal("100.00"),
            quantity = 10,
            purchaseTaxRate = BigDecimal("5.00"),
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            purchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
            isInputTaxCreditEnabled = true,
            isSupplierDiscountEnabled = false,
            supplierDiscountPercent = BigDecimal("15.00")
        )
        val result = ProfitCalculationEngine.calculate(input)
        assertTrue(result.isValid)
        assertEquals(BigDecimal("1000.00"), result.roundedGrossPaidPurchaseValue)
        assertEquals(BigDecimal("0.00"), result.roundedSupplierDiscountAmount)
        assertEquals(BigDecimal("1050.00"), result.roundedTotalPurchaseInvoice)
        assertEquals(BigDecimal("1000.00"), result.roundedTotalEconomicCost)
    }

    @Test
    fun testSupplierDiscount_10PercentDiscountOnTaxExclusivePTR() {
        // PTR: ₹100, Paid Qty: 10, Supplier discount: 10%, Purchase GST: 5%
        // Gross paid value = ₹1,000, Discount = ₹100, Net taxable base = ₹900, GST = ₹45, Invoice = ₹945
        val input = CalculationInput(
            ptr = BigDecimal("100.00"),
            quantity = 10,
            purchaseTaxRate = BigDecimal("5.00"),
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            purchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
            isInputTaxCreditEnabled = true,
            isSupplierDiscountEnabled = true,
            supplierDiscountPercent = BigDecimal("10.00")
        )

        val result = ProfitCalculationEngine.calculate(input)
        assertTrue(result.isValid)
        assertEquals(BigDecimal("1000.00"), result.roundedGrossPaidPurchaseValue)
        assertEquals(BigDecimal("100.00"), result.roundedSupplierDiscountAmount)
        assertEquals(BigDecimal("90.00"), result.taxablePurchaseBase.setScale(2))
        assertEquals(BigDecimal("4.50"), result.purchaseTaxAmount.setScale(2))
        assertEquals(BigDecimal("94.50"), result.roundedPurchaseInvoice)
        assertEquals(BigDecimal("945.00"), result.roundedTotalPurchaseInvoice)
        assertEquals(BigDecimal("900.00"), result.roundedTotalEconomicCost)
        assertEquals(BigDecimal("45.00"), result.roundedTotalInputCredit)
    }

    @Test
    fun testSupplierDiscount_TaxInclusivePTR_CorrectTaxExtraction() {
        // PTR: ₹105 (tax-inclusive 5% GST), 10% discount
        // Discounted gross price = 105 * 0.90 = 94.50
        // Net taxable base = 94.50 / 1.05 = 90.00
        // Purchase GST = 94.50 - 90.00 = 4.50
        // Supplier invoice total = 94.50
        val input = CalculationInput(
            ptr = BigDecimal("105.00"),
            quantity = 1,
            purchaseTaxRate = BigDecimal("5.00"),
            purchasePriceMode = TaxPriceMode.TAX_INCLUSIVE,
            purchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
            isInputTaxCreditEnabled = true,
            isSupplierDiscountEnabled = true,
            supplierDiscountPercent = BigDecimal("10.00")
        )

        val result = ProfitCalculationEngine.calculate(input)
        assertTrue(result.isValid)
        assertEquals(BigDecimal("90.00"), result.taxablePurchaseBase.setScale(2))
        assertEquals(BigDecimal("4.50"), result.purchaseTaxAmount.setScale(2))
        assertEquals(BigDecimal("94.50"), result.roundedPurchaseInvoice)
        assertEquals(BigDecimal("90.00"), result.roundedEconomicCost)
    }

    @Test
    fun testSupplierDiscount_100PercentDiscountHandledWithoutCrashOrDivisionByZero() {
        val input = CalculationInput(
            ptr = BigDecimal("100.00"),
            purchaseTaxRate = BigDecimal("5.00"),
            isSupplierDiscountEnabled = true,
            supplierDiscountPercent = BigDecimal("100.00"),
            sellingPrice = BigDecimal("150.00")
        )

        val result = ProfitCalculationEngine.calculate(input)
        assertTrue(result.isValid)
        assertEquals(BigDecimal("0.00"), result.roundedEconomicCost)
        assertEquals(BigDecimal("0.00"), result.roundedPurchaseInvoice)
        assertEquals(BigDecimal("0.00"), result.roundedEffectiveCostPerReceivedPack)
        assertNull(result.markup) // Division by zero economic cost produces null markup safely
        assertNotNull(result.profitMargin)
    }

    @Test
    fun testSupplierDiscount_NegativeAndOver100PercentRejected() {
        val negInput = CalculationInput(
            isSupplierDiscountEnabled = true,
            supplierDiscountPercent = BigDecimal("-5.00")
        )
        val negResult = ProfitCalculationEngine.calculate(negInput)
        assertFalse(negResult.isValid)
        assertEquals("Supplier discount must be between 0% and 100%.", negResult.validationMessage)

        val overInput = CalculationInput(
            isSupplierDiscountEnabled = true,
            supplierDiscountPercent = BigDecimal("105.00")
        )
        val overResult = ProfitCalculationEngine.calculate(overInput)
        assertFalse(overResult.isValid)
        assertEquals("Supplier discount must be between 0% and 100%.", overResult.validationMessage)
    }

    @Test
    fun testCombinedSupplierDiscountAndBuyXGetYFree_Phase3Scenario() {
        // PHASE 3 Master Example:
        // PTR: ₹100 per paid pack, tax-exclusive
        // Paid purchase quantity: 10 packs
        // Supplier discount: 10%
        // Purchase GST: 5%
        // Buy X, Get Y Free: Buy 10, Get 2 Free
        // Selling price: ₹150, tax-inclusive 5% sales GST, unitsPerPack = 10
        val input = CalculationInput(
            ptr = BigDecimal("100.00"),
            quantity = 10,
            purchaseTaxRate = BigDecimal("5.00"),
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            purchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
            isInputTaxCreditEnabled = true,
            isSupplierOfferEnabled = true,
            offerBuyQuantity = 10,
            offerFreeQuantity = 2,
            isSupplierDiscountEnabled = true,
            supplierDiscountPercent = BigDecimal("10.00"),
            sellingPrice = BigDecimal("150.00"),
            sellingTaxRate = BigDecimal("5.00"),
            sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE,
            unitsPerPack = 10
        )

        val result = ProfitCalculationEngine.calculate(input)
        assertTrue(result.isValid)

        // Gross paid-goods value = 10 × ₹100 = ₹1,000
        assertEquals(BigDecimal("1000.00"), result.roundedGrossPaidPurchaseValue)
        // Supplier discount = 10% × ₹1,000 = ₹100
        assertEquals(BigDecimal("100.00"), result.roundedSupplierDiscountAmount)
        // Net taxable purchase value = ₹900
        assertEquals(BigDecimal("900.00"), result.totalEconomicPurchaseCost.setScale(2))
        // Purchase GST = 5% × ₹900 = ₹45
        assertEquals(BigDecimal("45.00"), result.totalOutputTax.setScale(2, java.math.RoundingMode.HALF_UP).let { result.roundedTotalInputCredit })
        // Supplier invoice total = ₹945
        assertEquals(BigDecimal("945.00"), result.roundedTotalPurchaseInvoice)
        // Free packs earned = 2, Total packs received = 12
        assertEquals(2, result.freePacksReceived)
        assertEquals(12, result.totalPacksReceived)

        // Effective purchase cost per received pack = 900 / 12 = ₹75.00
        assertEquals(BigDecimal("75.00"), result.roundedEffectiveCostPerReceivedPack)
        // Effective cost per unit = 75 / 10 = ₹7.50
        assertEquals(BigDecimal("7.50"), result.roundedCostPerIndividualUnit)

        // Net sales revenue per pack = 150 / 1.05 = 142.86
        assertEquals(BigDecimal("142.86"), result.roundedNetSalesRevenue)
        // Profit per received pack = 142.857... - 75.00 = 67.86
        assertEquals(BigDecimal("67.86"), result.roundedProfitPerUnit)
        // Profit per unit = 6.79
        assertEquals(BigDecimal("6.79"), result.roundedProfitPerIndividualUnit)
        // Total profit for batch (12 received packs) = 12 * 67.857... = ₹814.29
        assertEquals(BigDecimal("814.29"), result.roundedTotalProfit)
        // Customer collection (12 * 150) = ₹1,800.00
        assertEquals(BigDecimal("1800.00"), result.roundedTotalCustomerCollection)
    }

    @Test
    fun testOfferIntegerOverflowGuards() {
        val overflowInput = CalculationInput(
            quantity = Int.MAX_VALUE,
            isSupplierOfferEnabled = true,
            offerBuyQuantity = 1,
            offerFreeQuantity = 1
        )
        val overflowResult = ProfitCalculationEngine.calculate(overflowInput)
        assertFalse(overflowResult.isValid)
        assertTrue(overflowResult.validationMessage?.contains("overflow") == true || overflowResult.validationMessage?.contains("limit") == true)
    }
}

