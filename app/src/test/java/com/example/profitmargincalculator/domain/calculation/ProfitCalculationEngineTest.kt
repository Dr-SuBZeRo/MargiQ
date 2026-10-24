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

    @Test
    fun testDefaultReferenceCalculation() {
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
            quantity = 1
        )

        val result = ProfitCalculationEngine.calculate(input)

        assertTrue(result.isValid)
        // Purchase side
        assertEquals(BigDecimal("48.00"), result.taxablePurchaseBase.setScale(2))
        assertEquals(BigDecimal("2.40"), result.roundedEligibleInputTaxCredit)
        assertEquals(BigDecimal("50.40"), result.roundedPurchaseInvoice)
        assertEquals(BigDecimal("48.00"), result.roundedEconomicCost)

        // Selling side
        assertEquals(BigDecimal("135.00"), result.roundedCustomerSellingPrice)
        assertEquals(BigDecimal("128.57"), result.roundedNetSalesRevenue)
        assertEquals(BigDecimal("6.43"), result.roundedSellingTaxAmount)

        // Profit & metrics
        assertEquals(BigDecimal("80.57"), result.roundedProfitPerUnit)
        assertEquals(BigDecimal("62.67"), result.roundedProfitMargin)
        assertEquals(BigDecimal("167.86"), result.roundedMarkup)
        assertEquals(BigDecimal("37.33"), result.roundedCostPercentage)

        // Totals for quantity = 1
        assertEquals(BigDecimal("80.57"), result.roundedTotalProfit)
        assertEquals(BigDecimal("50.40"), result.roundedTotalPurchaseInvoice)
        assertEquals(BigDecimal("135.00"), result.roundedTotalCustomerCollection)

        // Simple cash comparison
        assertEquals(BigDecimal("84.60"), result.simpleCashProfitPerUnit.setScale(2, java.math.RoundingMode.HALF_UP))
        assertEquals(BigDecimal("62.67"), result.simpleCashProfitMargin?.setScale(2, java.math.RoundingMode.HALF_UP))
    }

    @Test
    fun testNonRecoverableTaxScenario() {
        // When purchase tax is non-recoverable or ITC is disabled, tax is added to economic cost
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
            quantity = 1
        )

        val result = ProfitCalculationEngine.calculate(input)

        assertTrue(result.isValid)
        assertEquals(BigDecimal("0.00"), result.roundedEligibleInputTaxCredit)
        assertEquals(BigDecimal("50.40"), result.roundedEconomicCost)
        assertEquals(BigDecimal("128.57"), result.roundedNetSalesRevenue)
        assertEquals(BigDecimal("78.17"), result.roundedProfitPerUnit)
        assertEquals(BigDecimal("60.80"), result.roundedProfitMargin)
        assertEquals(BigDecimal("155.10"), result.roundedMarkup)
        assertEquals(BigDecimal("39.20"), result.roundedCostPercentage)
    }

    @Test
    fun testInputTaxCreditDisabledForRecoverableTax() {
        // Tax is classified as recoverable GST, but user has disabled the ITC toggle
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
            quantity = 1
        )

        val result = ProfitCalculationEngine.calculate(input)

        assertTrue(result.isValid)
        assertEquals(BigDecimal("0.00"), result.roundedEligibleInputTaxCredit)
        assertEquals(BigDecimal("50.40"), result.roundedEconomicCost)
        assertEquals(BigDecimal("78.17"), result.roundedProfitPerUnit)
    }

    @Test
    fun testTaxInclusivePurchasePrice() {
        // PTR = ₹105.00 tax inclusive at 5% tax -> base = ₹100.00, tax = ₹5.00
        val input = CalculationInput(
            ptr = BigDecimal("105.00"),
            purchaseTaxRate = BigDecimal("5.00"),
            purchasePriceMode = TaxPriceMode.TAX_INCLUSIVE,
            purchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
            isInputTaxCreditEnabled = true,
            sellingPrice = BigDecimal("150.00"),
            sellingTaxRate = BigDecimal("0.00"),
            sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE,
            quantity = 1
        )

        val result = ProfitCalculationEngine.calculate(input)

        assertEquals(BigDecimal("100.00"), result.taxablePurchaseBase.setScale(2))
        assertEquals(BigDecimal("5.00"), result.roundedEligibleInputTaxCredit)
        assertEquals(BigDecimal("105.00"), result.roundedPurchaseInvoice)
        assertEquals(BigDecimal("100.00"), result.roundedEconomicCost)
    }

    @Test
    fun testTaxExclusiveSellingPrice() {
        // Selling price = ₹200.00 exclusive at 18% tax -> net revenue = ₹200.00, customer pays ₹236.00
        val input = CalculationInput(
            ptr = BigDecimal("100.00"),
            purchaseTaxRate = BigDecimal("0.00"),
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            sellingPrice = BigDecimal("200.00"),
            sellingTaxRate = BigDecimal("18.00"),
            sellingPriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            quantity = 1
        )

        val result = ProfitCalculationEngine.calculate(input)

        assertEquals(BigDecimal("200.00"), result.roundedNetSalesRevenue)
        assertEquals(BigDecimal("36.00"), result.roundedSellingTaxAmount)
        assertEquals(BigDecimal("236.00"), result.roundedCustomerSellingPrice)
        assertEquals(BigDecimal("100.00"), result.roundedProfitPerUnit)
        assertEquals(BigDecimal("50.00"), result.roundedProfitMargin)
        assertEquals(BigDecimal("100.00"), result.roundedMarkup)
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
            quantity = 1
        )

        val result = ProfitCalculationEngine.calculate(input)

        assertEquals(BigDecimal("100.00"), result.roundedEconomicCost)
        assertEquals(BigDecimal("150.00"), result.roundedNetSalesRevenue)
        assertEquals(BigDecimal("50.00"), result.roundedProfitPerUnit)
        assertEquals(BigDecimal("33.33"), result.roundedProfitMargin)
        assertEquals(BigDecimal("50.00"), result.roundedMarkup)
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
            quantity = 1
        )

        val result = ProfitCalculationEngine.calculate(input)

        assertTrue(result.isBreakEven)
        assertFalse(result.isLoss)
        assertEquals(BigDecimal("0.00"), result.roundedProfitPerUnit)
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
            quantity = 1
        )

        val result = ProfitCalculationEngine.calculate(input)

        assertTrue(result.isLoss)
        assertFalse(result.isBreakEven)
        assertEquals(BigDecimal("-20.00"), result.roundedProfitPerUnit)
        assertEquals(BigDecimal("-20.00"), result.roundedProfitMargin)
        assertEquals(BigDecimal("-16.67"), result.roundedMarkup)
    }

    @Test
    fun testMultipleUnitsCalculation() {
        val input = CalculationInput(
            ptr = BigDecimal("48.00"),
            purchaseTaxRate = BigDecimal("5.00"),
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            sellingPrice = BigDecimal("135.00"),
            sellingTaxRate = BigDecimal("5.00"),
            sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE,
            quantity = 10
        )

        val result = ProfitCalculationEngine.calculate(input)

        assertEquals(BigDecimal("80.57"), result.roundedProfitPerUnit)
        assertEquals(BigDecimal("805.71"), result.roundedTotalProfit)
        assertEquals(BigDecimal("504.00"), result.roundedTotalPurchaseInvoice)
        assertEquals(BigDecimal("1350.00"), result.roundedTotalCustomerCollection)
        assertEquals(BigDecimal("24.00"), result.roundedTotalInputCredit)
        assertEquals(BigDecimal("64.29"), result.roundedTotalOutputTax)
    }

    @Test
    fun testZeroSalesRevenueHandledWithoutDivisionByZero() {
        val input = CalculationInput(
            ptr = BigDecimal("50.00"),
            purchaseTaxRate = BigDecimal("0.00"),
            sellingPrice = BigDecimal("0.00"),
            sellingTaxRate = BigDecimal("0.00"),
            quantity = 1
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
            quantity = 1
        )

        val result = ProfitCalculationEngine.calculate(input)

        assertTrue(result.isValid)
        assertEquals(BigDecimal("100.00"), result.roundedProfitPerUnit)
        assertNotNull(result.profitMargin)
        assertNull(result.markup)
    }

    @Test
    fun testValidationEdgeCases() {
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
    fun testLargePrecisionValues() {
        val input = CalculationInput(
            ptr = BigDecimal("12345678.95"),
            purchaseTaxRate = BigDecimal("18.00"),
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            sellingPrice = BigDecimal("23456789.50"),
            sellingTaxRate = BigDecimal("18.00"),
            sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE,
            quantity = 5
        )

        val result = ProfitCalculationEngine.calculate(input)
        assertTrue(result.isValid)
        assertTrue(result.totalProfit > BigDecimal.ZERO)
    }
}
