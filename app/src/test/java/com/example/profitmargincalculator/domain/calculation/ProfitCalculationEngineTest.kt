package com.example.profitmargincalculator.domain.calculation

import com.example.profitmargincalculator.domain.model.CalculationInput
import com.example.profitmargincalculator.domain.model.PurchaseTaxClassification
import com.example.profitmargincalculator.domain.model.TaxPriceMode
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
}
