package com.example.profitmargincalculator.ui.calculator

import com.example.profitmargincalculator.data.local.CalculationDao
import com.example.profitmargincalculator.data.local.CalculationEntity
import com.example.profitmargincalculator.data.preferences.ThemePreference
import com.example.profitmargincalculator.data.preferences.UserPreferences
import com.example.profitmargincalculator.data.repository.CalculationRepository
import com.example.profitmargincalculator.domain.model.PurchaseTaxClassification
import com.example.profitmargincalculator.domain.model.TaxPriceMode
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
        assertEquals("Strip", state.packName)
        assertEquals("Tablet", state.unitName)
        assertEquals("10", state.unitsPerPackText)
        assertEquals("5", state.quantityText)
        assertEquals(50, state.calculationResult.totalIndividualUnits)

        // Check reference calculation results
        assertEquals(BigDecimal("80.57"), state.calculationResult.roundedProfitPerUnit)
        assertEquals(BigDecimal("8.06"), state.calculationResult.roundedProfitPerIndividualUnit)
        assertEquals(BigDecimal("4.80"), state.calculationResult.roundedCostPerIndividualUnit)
        assertEquals(BigDecimal("62.67"), state.calculationResult.roundedProfitMargin)
        assertEquals(BigDecimal("167.86"), state.calculationResult.roundedMarkup)
        assertEquals(BigDecimal("402.86"), state.calculationResult.roundedTotalProfit)
    }

    @Test
    fun testChangingPackAndUnitUpdatesLiveCalculation() {
        val fakePrefs = FakePreferencesRepository()
        val viewModel = CalculatorViewModel(calculationRepository, fakePrefs)

        // Change pack name to Box, unit to Piece, unitsPerPack to 20, quantity to 3
        viewModel.onPackNameChanged("Box")
        viewModel.onUnitNameChanged("Piece")
        viewModel.onUnitsPerPackChanged("20")
        viewModel.onQuantityChanged("3")
        viewModel.onSellingQuantityChanged("3")

        val state = viewModel.uiState.value
        assertEquals("Box", state.packName)
        assertEquals("Piece", state.unitName)
        assertEquals("20", state.unitsPerPackText)
        assertEquals(60, state.calculationResult.totalIndividualUnits)
        assertEquals(60, state.calculationResult.totalIndividualUnitsSold)
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
    fun testSavingCalculationPersistsToRepository() = runTest(testDispatcher) {
        val fakePrefs = FakePreferencesRepository()
        val viewModel = CalculatorViewModel(calculationRepository, fakePrefs)

        viewModel.onProductNameChanged("Paracetamol 650 Strip")
        viewModel.onPackNameChanged("Strip")
        viewModel.onUnitNameChanged("Tablet")
        viewModel.onUnitsPerPackChanged("15")
        viewModel.onQuantityChanged("4")

        viewModel.saveCalculation()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.currentSavedId)
        assertEquals("Calculation saved to history", state.userMessage)
        assertEquals(1, fakeDao.items.size)
        val saved = fakeDao.items[0]
        assertEquals("Paracetamol 650 Strip", saved.productName)
        assertEquals("Strip", saved.packName)
        assertEquals("Tablet", saved.unitName)
        assertEquals(15, saved.unitsPerPack)
        assertEquals(4, saved.quantity)
    }

    @Test
    fun testLoadingCalculationRestoresPackInformation() {
        val fakePrefs = FakePreferencesRepository()
        val viewModel = CalculatorViewModel(calculationRepository, fakePrefs)

        val entity = CalculationEntity(
            id = 42,
            productName = "Amoxicillin 500mg",
            ptr = "90.00",
            purchaseTaxRate = "12.00",
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE.name,
            purchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST.name,
            isInputTaxCreditEnabled = true,
            sellingPrice = "180.00",
            sellingTaxRate = "12.00",
            sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE.name,
            quantity = 6,
            packName = "Strip",
            unitName = "Capsule",
            unitsPerPack = 10,
            sellingQuantity = 6,
            costPerIndividualUnit = "9.00",
            revenuePerIndividualUnit = "16.07",
            profitPerIndividualUnit = "7.07",
            economicPurchaseCost = "90.00",
            netSalesRevenue = "160.71",
            profitPerUnit = "70.71",
            profitMargin = "44.00",
            markup = "78.57",
            costPercentage = "56.00",
            totalProfit = "424.26",
            totalPurchaseInvoiceTotal = "604.80",
            totalSellingCustomerTotal = "1080.00",
            calculationBasis = "Profit after excluding eligible GST credit"
        )

        viewModel.loadCalculation(entity)

        val state = viewModel.uiState.value
        assertEquals("Amoxicillin 500mg", state.productName)
        assertEquals("Strip", state.packName)
        assertEquals("Capsule", state.unitName)
        assertEquals("10", state.unitsPerPackText)
        assertEquals("6", state.quantityText)
        assertEquals(60, state.calculationResult.totalIndividualUnits)
        assertEquals(42L, state.currentSavedId)
    }

    @Test
    fun testRestoringExampleScenario() {
        val fakePrefs = FakePreferencesRepository()
        val viewModel = CalculatorViewModel(calculationRepository, fakePrefs)

        viewModel.onPtrChanged("20.00")
        viewModel.restoreExampleScenario()

        val state = viewModel.uiState.value
        assertEquals("48.00", state.ptrText)
        assertEquals("135.00", state.sellingPriceText)
        assertEquals("Strip", state.packName)
        assertEquals("Tablet", state.unitName)
        assertEquals("10", state.unitsPerPackText)
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
    }

    @Test
    fun testZeroSellingQuantityDoesNotFallBackToPurchaseQuantity() {
        val fakePrefs = FakePreferencesRepository()
        val viewModel = CalculatorViewModel(calculationRepository, fakePrefs)

        viewModel.onQuantityChanged("5")
        viewModel.onSellingQuantityChanged("0")

        val state = viewModel.uiState.value
        assertTrue(state.calculationResult.isValid)
        assertEquals(0, state.calculationResult.sellingQuantity)
        assertEquals(0, state.calculationResult.totalIndividualUnitsSold)
        assertEquals(BigDecimal("0.00"), state.calculationResult.roundedTotalProfit)
        assertEquals(BigDecimal("0.00"), state.calculationResult.roundedTotalCustomerCollection)
        assertEquals(5, state.calculationResult.remainingPacks)
        assertEquals(50, state.calculationResult.remainingIndividualUnits)
    }

    @Test
    fun testBlankSellingQuantityDoesNotFallBackToPurchaseQuantity() {
        val fakePrefs = FakePreferencesRepository()
        val viewModel = CalculatorViewModel(calculationRepository, fakePrefs)

        viewModel.onQuantityChanged("5")
        viewModel.onSellingQuantityChanged("")

        val state = viewModel.uiState.value
        assertTrue(state.calculationResult.isValid)
        assertEquals(0, state.calculationResult.sellingQuantity)
        assertEquals(0, state.calculationResult.totalIndividualUnitsSold)
        assertEquals(BigDecimal("0.00"), state.calculationResult.roundedTotalProfit)
        assertEquals(5, state.calculationResult.remainingPacks)
        assertEquals(50, state.calculationResult.remainingIndividualUnits)
    }

    @Test
    fun testNegativeSellingQuantityYieldsInvalidCalculation() {
        val fakePrefs = FakePreferencesRepository()
        val viewModel = CalculatorViewModel(calculationRepository, fakePrefs)

        viewModel.onSellingQuantityChanged("-2")

        val state = viewModel.uiState.value
        org.junit.Assert.assertFalse(state.calculationResult.isValid)
        assertEquals("Selling quantity cannot be negative.", state.calculationResult.validationMessage)
    }

    private class FakeCalculationDao : CalculationDao {
        val items = mutableListOf<CalculationEntity>()
        private var idCounter = 1L

        override fun getAllCalculations(): Flow<List<CalculationEntity>> = flowOf(items)
        override fun searchCalculations(query: String): Flow<List<CalculationEntity>> = flowOf(items.filter { it.productName.contains(query) })
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

    private class FakePreferencesRepository : com.example.profitmargincalculator.data.preferences.IPreferencesRepository {
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
}
