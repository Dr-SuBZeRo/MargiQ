package com.margiq.ui.compare

import com.margiq.data.local.CalculationEntity
import com.margiq.domain.comparison.WinnerSide
import com.margiq.domain.model.CalculationInput
import com.margiq.domain.model.CalculationResult
import com.margiq.domain.model.PurchaseTaxClassification
import com.margiq.domain.model.TaxPriceMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class CompareViewModelTest {

    private lateinit var viewModel: CompareViewModel

    @Before
    fun setUp() {
        viewModel = CompareViewModel()
    }

    @Test
    fun testSwitchTabs() {
        viewModel.selectTab(CompareTab.SUPPLIERS)
        assertEquals(CompareTab.SUPPLIERS, viewModel.uiState.value.activeTab)

        viewModel.selectTab(CompareTab.PRODUCTS)
        assertEquals(CompareTab.PRODUCTS, viewModel.uiState.value.activeTab)
    }

    @Test
    fun testLoadCalculationEntityIntoSidesAndCompare() {
        val inputA = CalculationInput(
            productName = "Product A",
            supplierName = "Supplier 1",
            ptr = BigDecimal("40.00"),
            purchaseTaxRate = BigDecimal("5.00"),
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            purchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
            isInputTaxCreditEnabled = true,
            sellingPrice = BigDecimal("100.00"),
            sellingTaxRate = BigDecimal("5.00"),
            sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE,
            quantity = 1,
            unitsPerPack = 10
        )
        val dummyResultA = CalculationResult.invalid("dummy")
        val entityA = CalculationEntity.fromInputAndResult(inputA, dummyResultA, id = 101)

        val inputB = CalculationInput(
            productName = "Product B",
            supplierName = "Supplier 2",
            ptr = BigDecimal("50.00"),
            purchaseTaxRate = BigDecimal("5.00"),
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
            purchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
            isInputTaxCreditEnabled = true,
            sellingPrice = BigDecimal("100.00"),
            sellingTaxRate = BigDecimal("12.00"),
            sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE,
            quantity = 1,
            unitsPerPack = 10
        )
        val entityB = CalculationEntity.fromInputAndResult(inputB, dummyResultA, id = 102)

        viewModel.loadCalculationIntoSideA(entityA)
        viewModel.loadCalculationIntoSideB(entityB)

        val outcome = viewModel.uiState.value.productsComparison
        assertNotNull(outcome)
        assertTrue(outcome!!.isValid)
        // Product A has lower purchase cost
        assertEquals(WinnerSide.SIDE_A, outcome.highlight!!.lowestCostSide)
        // Product A also has 5% GST on selling vs 12% for B, so A yields higher profit and margin
        assertEquals(WinnerSide.SIDE_A, outcome.highlight!!.highestProfitPerUnitSide)
        assertEquals(WinnerSide.SIDE_A, outcome.highlight!!.highestMarginSide)
    }

    @Test
    fun testSellingTaxInclusiveVsExclusiveComparison() {
        // Both have identical PTR 50, but Side A has 100 Tax-Exclusive selling (customer pays 105, net revenue 100)
        // Side B has 100 Tax-Inclusive selling (net revenue ~95.24)
        viewModel.updateSideA {
            it.copy(
                ptrText = "50.00",
                sellingPriceText = "100.00",
                sellingTaxRateText = "5.00",
                sellingPriceMode = TaxPriceMode.TAX_EXCLUSIVE
            )
        }
        viewModel.updateSideB {
            it.copy(
                ptrText = "50.00",
                sellingPriceText = "100.00",
                sellingTaxRateText = "5.00",
                sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE
            )
        }

        val outcome = viewModel.uiState.value.productsComparison
        assertNotNull(outcome)
        assertTrue(outcome!!.isValid)
        // Same cost
        assertEquals(WinnerSide.TIE, outcome.highlight!!.lowestCostSide)
        // Side A has higher net revenue, hence higher profit and margin
        assertEquals(WinnerSide.SIDE_A, outcome.highlight!!.highestProfitPerUnitSide)
        assertEquals(WinnerSide.SIDE_A, outcome.highlight!!.highestMarginSide)
    }

    @Test
    fun testSupplierOffersWithSellingTaxConfiguration() {
        viewModel.updateSupplierBase {
            it.copy(
                productName = "Amoxicillin",
                sellingPriceText = "200.00",
                sellingTaxRateText = "5.00",
                sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE,
                unitsPerPackText = "10"
            )
        }
        viewModel.updateSupplierA {
            it.copy(
                supplierName = "Distributor Alpha",
                ptrText = "90.00",
                isSupplierDiscountEnabled = true,
                supplierDiscountText = "5.00"
            )
        }
        viewModel.updateSupplierB {
            it.copy(
                supplierName = "Distributor Beta",
                ptrText = "100.00",
                isSupplierOfferEnabled = true,
                offerBuyQuantityText = "10",
                offerFreeQuantityText = "2"
            )
        }

        val outcome = viewModel.uiState.value.suppliersComparison
        assertNotNull(outcome)
        assertTrue(outcome!!.isValid)
        // Supplier B receives 12 packs for price of 10 -> effective PTR ~83.33 < Supplier A 90 - 5% (85.50)
        assertEquals(WinnerSide.SIDE_B, outcome.highlight!!.lowestCostSide)
        assertEquals(WinnerSide.SIDE_B, outcome.highlight!!.highestProfitPerUnitSide)
    }

    @Test
    fun testIncompleteOrZeroInputsHandledGracefully() {
        viewModel.updateSideA { it.copy(ptrText = "0.00", sellingPriceText = "0.00") }
        val outcome = viewModel.uiState.value.productsComparison
        assertNotNull(outcome)
        // Zero selling price is handled safely by calculation engine (returns null profit margin without throwing)
        assertTrue(outcome!!.isValid)
    }

    @Test
    fun testInvalidNegativeInputsHandledGracefully() {
        viewModel.updateSideA { it.copy(ptrText = "-10.00") }
        val outcome = viewModel.uiState.value.productsComparison
        assertNotNull(outcome)
        assertFalse(outcome!!.isValid)
        assertTrue(outcome.validationMessage!!.contains("negative"))
    }

    @Test
    fun testNoComparisonRecordsWrittenToPersistence() {
        // CompareViewModel operates purely on transient UI state and does not hold repositories
        assertNotNull(viewModel.uiState.value)
        viewModel.updateSideA { it.copy(productName = "Temporary Drug A", ptrText = "50.00") }
        viewModel.updateSideB { it.copy(productName = "Temporary Drug B", ptrText = "60.00") }

        // Comparison outcomes are evaluated in memory without any persistent database mutation
        val outcome = viewModel.uiState.value.productsComparison
        assertNotNull(outcome)
        assertEquals("Temporary Drug A", viewModel.uiState.value.productSideA.productName)
    }

    @Test
    fun testCompareSuppliersUnitsPerPackOneMatchesScreenshot() {
        viewModel.updateSupplierBase {
            it.copy(
                productName = "Sucralox-O",
                sellingPriceText = "135.00",
                sellingTaxRateText = "0.00",
                sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE,
                unitsPerPackText = "1"
            )
        }
        viewModel.updateSupplierA {
            it.copy(
                supplierName = "Supplier Alpha",
                ptrText = "42.00",
                purchaseTaxRateText = "5.00",
                purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
                isInputTaxCreditEnabled = false,
                isSupplierDiscountEnabled = false,
                isSupplierOfferEnabled = false
            )
        }
        viewModel.updateSupplierB {
            it.copy(
                supplierName = "Supplier Beta",
                ptrText = "45.00",
                purchaseTaxRateText = "5.00",
                purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
                isInputTaxCreditEnabled = false,
                isSupplierDiscountEnabled = false,
                isSupplierOfferEnabled = false
            )
        }

        val outcome = viewModel.uiState.value.suppliersComparison
        assertNotNull(outcome)
        assertTrue(outcome!!.isValid)

        val resA = outcome.resultA
        val resB = outcome.resultB

        // Supplier Alpha: PTR 42 + 5% tax (2.10) with No ITC = Cost per Pack 44.10
        // Units/Pack = 1, so Cost per Unit MUST BE 44.10 (not 4.41!)
        assertEquals(BigDecimal("44.10"), resA.roundedEffectiveCostPerReceivedPack)
        assertEquals(BigDecimal("44.10"), resA.roundedCostPerIndividualUnit)

        // Supplier Beta: PTR 45 + 5% tax (2.25) with No ITC = Cost per Pack 47.25
        // Units/Pack = 1, so Cost per Unit MUST BE 47.25 (not 4.73!)
        assertEquals(BigDecimal("47.25"), resB.roundedEffectiveCostPerReceivedPack)
        assertEquals(BigDecimal("47.25"), resB.roundedCostPerIndividualUnit)

        val hl = outcome.highlight!!
        assertEquals(WinnerSide.SIDE_A, hl.lowestCostSide)
        assertEquals(BigDecimal("3.15"), hl.costDifferencePerPack)
        assertEquals(BigDecimal("3.15"), hl.costDifferencePerUnit)
        assertEquals(BigDecimal("6.67"), hl.costSavingsPercent)
    }

    @Test
    fun testCompareSuppliersUnitsPerPackTen() {
        viewModel.updateSupplierBase {
            it.copy(
                productName = "Sucralox-O",
                sellingPriceText = "135.00",
                sellingTaxRateText = "0.00",
                sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE,
                unitsPerPackText = "10"
            )
        }
        viewModel.updateSupplierA {
            it.copy(
                supplierName = "Supplier Alpha",
                ptrText = "42.00",
                purchaseTaxRateText = "5.00",
                purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
                isInputTaxCreditEnabled = false,
                isSupplierDiscountEnabled = false,
                isSupplierOfferEnabled = false
            )
        }
        viewModel.updateSupplierB {
            it.copy(
                supplierName = "Supplier Beta",
                ptrText = "45.00",
                purchaseTaxRateText = "5.00",
                purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
                isInputTaxCreditEnabled = false,
                isSupplierDiscountEnabled = false,
                isSupplierOfferEnabled = false
            )
        }

        val outcome = viewModel.uiState.value.suppliersComparison
        assertNotNull(outcome)
        assertTrue(outcome!!.isValid)

        val resA = outcome.resultA
        val resB = outcome.resultB

        // Cost per Pack remains 44.10 and 47.25
        assertEquals(BigDecimal("44.10"), resA.roundedEffectiveCostPerReceivedPack)
        assertEquals(BigDecimal("47.25"), resB.roundedEffectiveCostPerReceivedPack)

        // Cost per Unit is divided by 10
        assertEquals(BigDecimal("4.41"), resA.roundedCostPerIndividualUnit)
        assertEquals(BigDecimal("4.73"), resB.roundedCostPerIndividualUnit)

        val hl = outcome.highlight!!
        assertEquals(BigDecimal("3.15"), hl.costDifferencePerPack)
        assertEquals(BigDecimal("0.32"), hl.costDifferencePerUnit)
    }

    @Test
    fun testChangingUnitsPerPackUpdatesImmediately() {
        viewModel.updateSupplierBase {
            it.copy(
                productName = "Item Test",
                sellingPriceText = "100.00",
                unitsPerPackText = "1"
            )
        }
        viewModel.updateSupplierA {
            it.copy(
                ptrText = "42.00",
                purchaseTaxRateText = "5.00",
                purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
                isInputTaxCreditEnabled = false,
                isSupplierDiscountEnabled = false,
                isSupplierOfferEnabled = false
            )
        }

        var outcome = viewModel.uiState.value.suppliersComparison
        assertEquals(BigDecimal("44.10"), outcome!!.resultA.roundedCostPerIndividualUnit)

        // Change to 10
        viewModel.updateSupplierBase { it.copy(unitsPerPackText = "10") }
        outcome = viewModel.uiState.value.suppliersComparison
        assertEquals(BigDecimal("4.41"), outcome!!.resultA.roundedCostPerIndividualUnit)

        // Change to 5
        viewModel.updateSupplierBase { it.copy(unitsPerPackText = "5") }
        outcome = viewModel.uiState.value.suppliersComparison
        assertEquals(BigDecimal("8.82"), outcome!!.resultA.roundedCostPerIndividualUnit)
    }

    @Test
    fun testSupplierAAndBDoNotLeakPackSizesOrValues() {
        viewModel.updateSupplierBase {
            it.copy(
                productName = "Common Base Drug",
                sellingPriceText = "150.00",
                unitsPerPackText = "4"
            )
        }
        viewModel.updateSupplierA {
            it.copy(
                supplierName = "Alpha",
                ptrText = "40.00",
                purchaseTaxRateText = "0.00",
                purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
                isSupplierDiscountEnabled = false,
                isSupplierOfferEnabled = false
            )
        }
        viewModel.updateSupplierB {
            it.copy(
                supplierName = "Beta",
                ptrText = "80.00",
                purchaseTaxRateText = "0.00",
                purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
                isSupplierDiscountEnabled = false,
                isSupplierOfferEnabled = false
            )
        }

        val outcome = viewModel.uiState.value.suppliersComparison
        assertNotNull(outcome)

        // Both offers evaluate with unitsPerPack = 4
        assertEquals(BigDecimal("10.00"), outcome!!.resultA.roundedCostPerIndividualUnit) // 40 / 4
        assertEquals(BigDecimal("20.00"), outcome.resultB.roundedCostPerIndividualUnit) // 80 / 4
    }

    @Test
    fun testCompareProductsIndependentPackSizes() {
        viewModel.updateSideA {
            it.copy(
                productName = "Product A (Pack of 1)",
                ptrText = "42.00",
                purchaseTaxRateText = "5.00",
                purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
                isInputTaxCreditEnabled = false,
                unitsPerPackText = "1"
            )
        }
        viewModel.updateSideB {
            it.copy(
                productName = "Product B (Pack of 10)",
                ptrText = "42.00",
                purchaseTaxRateText = "5.00",
                purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
                isInputTaxCreditEnabled = false,
                unitsPerPackText = "10"
            )
        }

        val outcome = viewModel.uiState.value.productsComparison
        assertNotNull(outcome)
        assertTrue(outcome!!.isValid)

        // Side A cost per unit is 44.10, Side B cost per unit is 4.41
        assertEquals(BigDecimal("44.10"), outcome.resultA.roundedCostPerIndividualUnit)
        assertEquals(BigDecimal("4.41"), outcome.resultB.roundedCostPerIndividualUnit)
        assertEquals(WinnerSide.SIDE_B, outcome.highlight!!.lowestCostSide)
    }

    @Test
    fun testSupplierOffersDiscountFreeGoodsAndItc() {
        viewModel.updateSupplierBase {
            it.copy(
                productName = "Commercial Medicine",
                sellingPriceText = "100.00",
                unitsPerPackText = "1"
            )
        }
        // Offer A: PTR 100, 5% cash discount, 5% purchase tax, ITC claimed (tax is credited, effective cost = 95.00)
        viewModel.updateSupplierA {
            it.copy(
                ptrText = "100.00",
                purchaseTaxRateText = "5.00",
                purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
                isInputTaxCreditEnabled = true,
                isSupplierDiscountEnabled = true,
                supplierDiscountText = "5.00"
            )
        }
        // Offer B: PTR 100, 5% purchase tax, ITC claimed, Buy 10 Get 2 Free (12 packs for 1000 - ITC credited = 1000/12 = 83.33)
        viewModel.updateSupplierB {
            it.copy(
                ptrText = "100.00",
                purchaseTaxRateText = "5.00",
                purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE,
                isInputTaxCreditEnabled = true,
                isSupplierOfferEnabled = true,
                offerBuyQuantityText = "10",
                offerFreeQuantityText = "2"
            )
        }

        val outcome = viewModel.uiState.value.suppliersComparison
        assertNotNull(outcome)
        assertTrue(outcome!!.isValid)

        assertEquals(BigDecimal("95.00"), outcome.resultA.roundedCostPerIndividualUnit)
        assertEquals(BigDecimal("83.33"), outcome.resultB.roundedCostPerIndividualUnit)
        assertEquals(WinnerSide.SIDE_B, outcome.highlight!!.lowestCostSide)
    }
}
