package com.example.profitmargincalculator.ui.calculator

import com.example.profitmargincalculator.data.local.CalculationDao
import com.example.profitmargincalculator.data.local.CalculationEntity
import com.example.profitmargincalculator.data.preferences.PreferencesRepository
import com.example.profitmargincalculator.data.preferences.ThemePreference
import com.example.profitmargincalculator.data.preferences.UserPreferences
import com.example.profitmargincalculator.data.repository.CalculationRepository
import com.example.profitmargincalculator.domain.model.PurchaseTaxClassification
import com.example.profitmargincalculator.domain.model.TaxPriceMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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

        // Check reference calculation results
        assertEquals(BigDecimal("80.57"), state.calculationResult.roundedProfitPerUnit)
        assertEquals(BigDecimal("62.67"), state.calculationResult.roundedProfitMargin)
        assertEquals(BigDecimal("167.86"), state.calculationResult.roundedMarkup)
    }

    @Test
    fun testChangingInputUpdatesLiveCalculation() {
        val fakePrefs = FakePreferencesRepository()
        val viewModel = CalculatorViewModel(calculationRepository, fakePrefs)

        viewModel.onPtrChanged("100.00")
        viewModel.onPurchaseTaxRateChanged("0.00")
        viewModel.onSellingPriceChanged("150.00")
        viewModel.onSellingTaxRateChanged("0.00")

        val state = viewModel.uiState.value
        assertEquals(BigDecimal("50.00"), state.calculationResult.roundedProfitPerUnit)
        assertEquals(BigDecimal("33.33"), state.calculationResult.roundedProfitMargin)
        assertEquals(BigDecimal("50.00"), state.calculationResult.roundedMarkup)
    }

    @Test
    fun testSavingCalculationPersistsToRepository() = runTest(testDispatcher) {
        val fakePrefs = FakePreferencesRepository()
        val viewModel = CalculatorViewModel(calculationRepository, fakePrefs)

        viewModel.onProductNameChanged("Test Pharma Product")
        viewModel.saveCalculation()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.currentSavedId)
        assertEquals("Calculation saved to history", state.userMessage)
        assertEquals(1, fakeDao.items.size)
        assertEquals("Test Pharma Product", fakeDao.items[0].productName)
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
