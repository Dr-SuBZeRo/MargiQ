package com.margiq.ui.calculator

import com.margiq.data.local.CalculationDao
import com.margiq.data.local.CalculationEntity
import com.margiq.data.preferences.ThemePreference
import com.margiq.data.preferences.UserPreferences
import com.margiq.data.repository.CalculationRepository
import com.margiq.domain.model.PurchaseTaxClassification
import com.margiq.domain.model.TaxPriceMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class CalculatorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeDao: FakeCalculationDao
    private lateinit var calculationRepository: CalculationRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeCalculationDao()
        calculationRepository = CalculationRepository(fakeDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialUiStateMatchesDefaultReferenceValues() {
        val fakePrefs = FakePreferencesRepository()
        val viewModel = CalculatorViewModel(calculationRepository, fakePrefs)

        val state = viewModel.uiState.value
        assertEquals("Sample Product", state.productName)
        assertEquals("48.00", state.ptrText)
        assertEquals("5.00", state.purchaseTaxRateText)
        assertEquals("135.00", state.sellingPriceText)
        assertEquals("5.00", state.sellingTaxRateText)
        assertEquals(TaxPriceMode.TAX_EXCLUSIVE, state.purchasePriceMode)
        assertEquals(TaxPriceMode.TAX_INCLUSIVE, state.sellingPriceMode)
        assertEquals(PurchaseTaxClassification.RECOVERABLE_GST, state.purchaseTaxClassification)
        assertTrue(state.isInputTaxCreditEnabled)

        // Pack & Unit Defaults
        assertEquals("10", state.unitsPerPackText)
        assertEquals("5", state.quantityText)
        assertEquals(50, state.calculationResult.totalIndividualUnits)

        // Supplier Details & Offer/Discount Defaults
        assertEquals("", state.supplierName)
        assertFalse(state.isSupplierOfferEnabled)
        assertFalse(state.isSupplierDiscountEnabled)
        assertEquals("0.00", state.supplierDiscountText)

        // Check reference calculation results
        assertEquals(BigDecimal("80.57"), state.calculationResult.roundedProfitPerUnit)
        assertEquals(BigDecimal("8.06"), state.calculationResult.roundedProfitPerIndividualUnit)
        assertEquals(BigDecimal("4.80"), state.calculationResult.roundedCostPerIndividualUnit)
        assertEquals(BigDecimal("62.67"), state.calculationResult.roundedProfitMargin)
        assertEquals(BigDecimal("167.86"), state.calculationResult.roundedMarkup)
        assertEquals(BigDecimal("402.86"), state.calculationResult.roundedTotalProfit)
    }

    @Test
    fun testChangingUnitsPerPackUpdatesLiveCalculation() {
        val fakePrefs = FakePreferencesRepository()
        val viewModel = CalculatorViewModel(calculationRepository, fakePrefs)

        viewModel.onUnitsPerPackChanged("20")
        viewModel.onQuantityChanged("3")

        val state = viewModel.uiState.value
        assertEquals("20", state.unitsPerPackText)
        assertEquals(60, state.calculationResult.totalIndividualUnits)
        assertEquals(BigDecimal("4.03"), state.calculationResult.roundedProfitPerIndividualUnit)
    }

    @Test
    fun testChangingInputUpdatesLiveCalculation() {
        val fakePrefs = FakePreferencesRepository()
        val viewModel = CalculatorViewModel(calculationRepository, fakePrefs)

        viewModel.onPtrChanged("100.00")
        viewModel.onPurchaseTaxRateChanged("0.00")
        viewModel.onSellingPriceChanged("150.00")
        viewModel.onSellingTaxRateChanged("0.00")
        viewModel.onUnitsPerPackChanged("10")

        val state = viewModel.uiState.value
        assertEquals(BigDecimal("50.00"), state.calculationResult.roundedProfitPerUnit)
        assertEquals(BigDecimal("5.00"), state.calculationResult.roundedProfitPerIndividualUnit)
        assertEquals(BigDecimal("10.00"), state.calculationResult.roundedCostPerIndividualUnit)
        assertEquals(BigDecimal("33.33"), state.calculationResult.roundedProfitMargin)
        assertEquals(BigDecimal("50.00"), state.calculationResult.roundedMarkup)
    }

    @Test
    fun testSupplierOfferToggleAndInputsUpdateLiveCalculation() {
        val fakePrefs = FakePreferencesRepository()
        val viewModel = CalculatorViewModel(calculationRepository, fakePrefs)

        viewModel.onPtrChanged("100.00")
        viewModel.onPurchaseTaxRateChanged("5.00")
        viewModel.onSellingPriceChanged("150.00")
        viewModel.onSellingTaxRateChanged("5.00")
        viewModel.onQuantityChanged("1")
        viewModel.onUnitsPerPackChanged("10")

        // Enable Buy 1 Get 1 Free offer
        viewModel.onSupplierOfferToggled(true)
        viewModel.onOfferBuyQuantityChanged("1")
        viewModel.onOfferFreeQuantityChanged("1")

        val state = viewModel.uiState.value
        assertTrue(state.isSupplierOfferEnabled)
        assertEquals(1, state.calculationResult.freePacksReceived)
        assertEquals(2, state.calculationResult.totalPacksReceived)
        assertEquals(BigDecimal("50.00"), state.calculationResult.roundedEffectiveCostPerReceivedPack)
        assertEquals(BigDecimal("5.00"), state.calculationResult.roundedCostPerIndividualUnit)
        assertEquals(BigDecimal("92.86"), state.calculationResult.roundedProfitPerUnit)
    }

    @Test
    fun testSupplierDiscountChangesUpdateLiveCalculation() {
        val fakePrefs = FakePreferencesRepository()
        val viewModel = CalculatorViewModel(calculationRepository, fakePrefs)

        viewModel.onPtrChanged("100.00")
        viewModel.onPurchaseTaxRateChanged("5.00")
        viewModel.onQuantityChanged("10")
        viewModel.onSupplierDiscountToggled(true)
        viewModel.onSupplierDiscountChanged("10.00")

        val state = viewModel.uiState.value
        assertTrue(state.isSupplierDiscountEnabled)
        assertEquals(BigDecimal("1000.00"), state.calculationResult.roundedGrossPaidPurchaseValue)
        assertEquals(BigDecimal("100.00"), state.calculationResult.roundedSupplierDiscountAmount)
        assertEquals(BigDecimal("945.00"), state.calculationResult.roundedTotalPurchaseInvoice)
        assertEquals(BigDecimal("900.00"), state.calculationResult.roundedTotalEconomicCost)
    }

    @Test
    fun testDisabledSupplierDiscountIgnoresStoredPercentage() {
        val fakePrefs = FakePreferencesRepository()
        val viewModel = CalculatorViewModel(calculationRepository, fakePrefs)

        viewModel.onPtrChanged("100.00")
        viewModel.onPurchaseTaxRateChanged("5.00")
        viewModel.onQuantityChanged("10")
        // Store percentage but leave toggle disabled
        viewModel.onSupplierDiscountToggled(false)
        viewModel.onSupplierDiscountChanged("15.00")

        val state = viewModel.uiState.value
        assertFalse(state.isSupplierDiscountEnabled)
        assertEquals(BigDecimal("1000.00"), state.calculationResult.roundedGrossPaidPurchaseValue)
        assertEquals(BigDecimal("0.00"), state.calculationResult.roundedSupplierDiscountAmount)
        assertEquals(BigDecimal("1050.00"), state.calculationResult.roundedTotalPurchaseInvoice)
        assertEquals(BigDecimal("1000.00"), state.calculationResult.roundedTotalEconomicCost)
    }

    @Test
    fun testBothSupplierDiscountAndOfferOperateIndependentlyAndSimultaneously() {
        val fakePrefs = FakePreferencesRepository()
        val viewModel = CalculatorViewModel(calculationRepository, fakePrefs)

        viewModel.onPtrChanged("100.00")
        viewModel.onPurchaseTaxRateChanged("5.00")
        viewModel.onSellingPriceChanged("150.00")
        viewModel.onSellingTaxRateChanged("5.00")
        viewModel.onQuantityChanged("10")
        viewModel.onUnitsPerPackChanged("10")

        // Enable both
        viewModel.onSupplierDiscountToggled(true)
        viewModel.onSupplierDiscountChanged("10.00")
        viewModel.onSupplierOfferToggled(true)
        viewModel.onOfferBuyQuantityChanged("10")
        viewModel.onOfferFreeQuantityChanged("2")

        val state = viewModel.uiState.value
        assertTrue(state.isSupplierDiscountEnabled)
        assertTrue(state.isSupplierOfferEnabled)
        assertEquals(2, state.calculationResult.freePacksReceived)
        assertEquals(12, state.calculationResult.totalPacksReceived)
        // 10 packs * 100 = 1000 gross. Discount 10% = 100. Net taxable = 900. GST 5% = 45. Invoice = 945.
        assertEquals(BigDecimal("100.00"), state.calculationResult.roundedSupplierDiscountAmount)
        assertEquals(BigDecimal("945.00"), state.calculationResult.roundedTotalPurchaseInvoice)
        // Total economic cost = 900 (since ITC enabled). Effective cost per 12 received packs = 900 / 12 = 75.00
        assertEquals(BigDecimal("75.00"), state.calculationResult.roundedEffectiveCostPerReceivedPack)
    }

    @Test
    fun testSavingCalculationPersistsToRepository() = runTest(testDispatcher) {
        val fakePrefs = FakePreferencesRepository()
        val viewModel = CalculatorViewModel(calculationRepository, fakePrefs)

        viewModel.onProductNameChanged("Paracetamol 650 Strip")
        viewModel.onSupplierNameChanged("Apex Distributorship")
        viewModel.onUnitsPerPackChanged("15")
        viewModel.onQuantityChanged("4")
        viewModel.onSupplierDiscountToggled(true)
        viewModel.onSupplierDiscountChanged("5.00")
        viewModel.onSupplierOfferToggled(true)
        viewModel.onOfferBuyQuantityChanged("2")
        viewModel.onOfferFreeQuantityChanged("1")

        viewModel.saveCalculation()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.currentSavedId)
        assertEquals("Calculation saved to history", state.userMessage)
        assertEquals(1, fakeDao.items.size)
        val saved = fakeDao.items[0]
        assertEquals("Paracetamol 650 Strip", saved.productName)
        assertEquals("Apex Distributorship", saved.supplierName)
        assertEquals(15, saved.unitsPerPack)
        assertEquals(4, saved.quantity)
        assertTrue(saved.isSupplierOfferEnabled)
        assertEquals(2, saved.offerBuyQuantity)
        assertEquals(1, saved.offerFreeQuantity)
        assertTrue(saved.isSupplierDiscountEnabled)
        assertEquals("5.00", saved.supplierDiscountPercent)
    }

    @Test
    fun testLoadingCalculationRestoresOfferAndDiscount() {
        val fakePrefs = FakePreferencesRepository()
        val viewModel = CalculatorViewModel(calculationRepository, fakePrefs)

        val entity = CalculationEntity(
            id = 42,
            productName = "Amoxicillin 500mg",
            supplierName = "MedPlus Pharma",
            ptr = "100.00",
            purchaseTaxRate = "5.00",
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE.name,
            purchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST.name,
            isInputTaxCreditEnabled = true,
            sellingPrice = "150.00",
            sellingTaxRate = "5.00",
            sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE.name,
            quantity = 10,
            unitsPerPack = 10,
            isSupplierOfferEnabled = true,
            offerBuyQuantity = 10,
            offerFreeQuantity = 2,
            freePacksReceived = 2,
            totalPacksReceived = 12,
            isSupplierDiscountEnabled = true,
            supplierDiscountPercent = "10.00",
            supplierDiscountAmount = "100.00",
            effectiveCostPerReceivedPack = "75.00",
            economicPurchaseCost = "75.00",
            netSalesRevenue = "142.86",
            profitPerUnit = "67.86",
            profitMargin = "47.50",
            markup = "90.48",
            costPercentage = "52.50",
            totalProfit = "814.29",
            totalPurchaseInvoiceTotal = "945.00",
            totalSellingCustomerTotal = "1800.00",
            calculationBasis = "Profit after excluding eligible GST credit"
        )

        viewModel.loadCalculation(entity)

        val state = viewModel.uiState.value
        assertEquals("Amoxicillin 500mg", state.productName)
        assertEquals("MedPlus Pharma", state.supplierName)
        assertEquals("10", state.unitsPerPackText)
        assertEquals("10", state.quantityText)
        assertTrue(state.isSupplierOfferEnabled)
        assertEquals("10", state.offerBuyQuantityText)
        assertEquals("2", state.offerFreeQuantityText)
        assertTrue(state.isSupplierDiscountEnabled)
        assertEquals("10.00", state.supplierDiscountText)
        assertEquals(12, state.calculationResult.totalPacksReceived)
        assertEquals(BigDecimal("75.00"), state.calculationResult.roundedEffectiveCostPerReceivedPack)
        assertEquals(42L, state.currentSavedId)
    }

    @Test
    fun testRestoringExampleScenario() {
        val fakePrefs = FakePreferencesRepository()
        val viewModel = CalculatorViewModel(calculationRepository, fakePrefs)

        viewModel.onPtrChanged("20.00")
        viewModel.onSupplierOfferToggled(true)
        viewModel.onSupplierDiscountChanged("15.00")
        viewModel.restoreExampleScenario()

        val state = viewModel.uiState.value
        assertEquals("48.00", state.ptrText)
        assertEquals("135.00", state.sellingPriceText)
        assertEquals("10", state.unitsPerPackText)
        assertFalse(state.isSupplierOfferEnabled)
        assertEquals("0.00", state.supplierDiscountText)
        assertEquals(BigDecimal("80.57"), state.calculationResult.roundedProfitPerUnit)
    }

    @Test
    fun testClearForm() {
        val fakePrefs = FakePreferencesRepository()
        val viewModel = CalculatorViewModel(calculationRepository, fakePrefs)

        viewModel.clearForm()
        val state = viewModel.uiState.value
        assertEquals("", state.ptrText)
        assertEquals("", state.sellingPriceText)
        assertFalse(state.isSupplierOfferEnabled)
        assertEquals("0.00", state.supplierDiscountText)
    }

    private class FakeCalculationDao : CalculationDao {
        val items = mutableListOf<CalculationEntity>()
        private var idCounter = 1L

        override fun getAllCalculations(): Flow<List<CalculationEntity>> = flowOf(items)
        override fun searchCalculations(query: String): Flow<List<CalculationEntity>> =
            flowOf(items.filter { it.productName.contains(query, ignoreCase = true) || it.supplierName.contains(query, ignoreCase = true) })
        override suspend fun getCalculationById(id: Long): CalculationEntity? = items.find { it.id == id }
        override suspend fun insertCalculation(entity: CalculationEntity): Long {
            val assignedId = if (entity.id == 0L) idCounter++ else entity.id
            val copy = entity.copy(id = assignedId)
            items.removeAll { it.id == assignedId }
            items.add(copy)
            return assignedId
        }
        override suspend fun updateCalculation(entity: CalculationEntity): Int {
            items.removeAll { it.id == entity.id }
            items.add(entity)
            return 1
        }
        override suspend fun deleteCalculation(entity: CalculationEntity): Int {
            return if (items.remove(entity)) 1 else 0
        }
        override suspend fun deleteById(id: Long): Int {
            return if (items.removeAll { it.id == id }) 1 else 0
        }
        override suspend fun clearAllCalculations(): Int {
            val count = items.size
            items.clear()
            return count
        }
    }

    private class FakePreferencesRepository : com.margiq.data.preferences.IPreferencesRepository {
        var prefs = UserPreferences()
        override val userPreferencesFlow: Flow<UserPreferences>
            get() = flowOf(prefs)

        override suspend fun updateThemePreference(preference: ThemePreference) {
            prefs = prefs.copy(themePreference = preference)
        }
        override suspend fun updateDynamicColor(enabled: Boolean) {
            prefs = prefs.copy(dynamicColor = enabled)
        }
        override suspend fun updateDefaults(
            purchaseTaxRate: BigDecimal,
            sellingTaxRate: BigDecimal,
            purchasePriceMode: TaxPriceMode,
            sellingPriceMode: TaxPriceMode,
            purchaseTaxClassification: PurchaseTaxClassification,
            inputTaxCreditEnabled: Boolean,
            quantity: Int
        ) {
            prefs = prefs.copy(
                defaultPurchaseTaxRate = purchaseTaxRate,
                defaultSellingTaxRate = sellingTaxRate,
                defaultPurchasePriceMode = purchasePriceMode,
                defaultSellingPriceMode = sellingPriceMode,
                defaultPurchaseTaxClassification = purchaseTaxClassification,
                defaultInputTaxCreditEnabled = inputTaxCreditEnabled,
                defaultQuantity = quantity
            )
        }
        override suspend fun resetDefaults() {
            prefs = UserPreferences()
        }
    }

    @Test
    fun testLoadCalculationFromHistoryDoesNotMutateSavedRecord() = runTest {
        val fakePrefs = FakePreferencesRepository()
        val viewModel = CalculatorViewModel(calculationRepository, fakePrefs)

        val entity = CalculationEntity(
            id = 42,
            productName = "Saved Azithromycin",
            supplierName = "Cipla",
            timestamp = 100000L,
            ptr = "80.00",
            purchaseTaxRate = "12.00",
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE.name,
            purchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST.name,
            isInputTaxCreditEnabled = true,
            sellingPrice = "180.00",
            sellingTaxRate = "12.00",
            sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE.name,
            quantity = 2,
            packName = "Strip",
            unitName = "Tablet",
            unitsPerPack = 6,
            sellingQuantity = 2,
            economicPurchaseCost = "160.00",
            netSalesRevenue = "321.43",
            profitPerUnit = "161.43",
            profitMargin = "50.22",
            markup = "100.89",
            costPercentage = "49.78",
            totalProfit = "161.43",
            totalPurchaseInvoiceTotal = "179.20",
            totalSellingCustomerTotal = "360.00",
            calculationBasis = "Profit after excluding eligible GST credit"
        )

        // Load into calculator
        viewModel.loadCalculation(entity)
        val state = viewModel.uiState.value

        assertEquals("Saved Azithromycin", state.productName)
        assertEquals("Cipla", state.supplierName)
        assertEquals("80.00", state.ptrText)
        assertEquals("180.00", state.sellingPriceText)
        assertEquals(6, state.unitsPerPackText.toInt())
        assertEquals(42L, state.currentSavedId)

        // Verify original entity remains unchanged
        assertEquals("Saved Azithromycin", entity.productName)
        assertEquals("80.00", entity.ptr)
        assertEquals(42L, entity.id)
    }
}
