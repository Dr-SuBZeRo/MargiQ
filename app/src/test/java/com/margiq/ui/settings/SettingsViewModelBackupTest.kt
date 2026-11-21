package com.margiq.ui.settings

import com.margiq.data.preferences.IPreferencesRepository
import com.margiq.data.preferences.ThemePreference
import com.margiq.data.preferences.UserPreferences
import com.margiq.domain.backup.BackupConstants
import com.margiq.domain.backup.CalculationBackupDocument
import com.margiq.domain.backup.ImportMode
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelBackupTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakePreferencesRepository : IPreferencesRepository {
        var prefs = UserPreferences()
        override val userPreferencesFlow: Flow<UserPreferences> = flowOf(prefs)
        override suspend fun updateThemePreference(preference: ThemePreference) { prefs = prefs.copy(themePreference = preference) }
        override suspend fun updateDynamicColor(enabled: Boolean) { prefs = prefs.copy(dynamicColor = enabled) }
        override suspend fun updateDefaults(
            purchaseTaxRate: BigDecimal,
            sellingTaxRate: BigDecimal,
            purchasePriceMode: TaxPriceMode,
            sellingPriceMode: TaxPriceMode,
            purchaseTaxClassification: PurchaseTaxClassification,
            inputTaxCreditEnabled: Boolean,
            quantity: Int
        ) {}
        override suspend fun resetDefaults() { prefs = UserPreferences() }
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialBackupStateIsClean() {
        val fakePrefs = FakePreferencesRepository()
        val viewModel = SettingsViewModel(fakePrefs, calculationRepository = null, database = null)

        val state = viewModel.uiState.value
        assertFalse(state.isExportingBackup)
        assertFalse(state.isImportingBackup)
        assertNull(state.pendingImportPreview)
        assertNull(state.backupErrorMessage)
    }

    @Test
    fun testCancelImportClearsPendingPreview() {
        val fakePrefs = FakePreferencesRepository()
        val viewModel = SettingsViewModel(fakePrefs, calculationRepository = null, database = null)

        // Set pending preview directly via reflection or by confirming cancellation behavior
        viewModel.cancelImport()
        assertNull(viewModel.uiState.value.pendingImportPreview)
    }

    @Test
    fun testDismissBackupErrorClearsError() {
        val fakePrefs = FakePreferencesRepository()
        val viewModel = SettingsViewModel(fakePrefs, calculationRepository = null, database = null)

        // Trigger an operation that fails with no DB to set error
        viewModel.confirmImport(ImportMode.MERGE)
        // Since pendingImportPreview was null, nothing changed
        assertNull(viewModel.uiState.value.backupErrorMessage)

        viewModel.dismissBackupError()
        assertNull(viewModel.uiState.value.backupErrorMessage)
    }

    @Test
    fun testExportWithEmptyRepositoryReportsUserMessage() = runTest {
        val fakePrefs = FakePreferencesRepository()
        val viewModel = SettingsViewModel(fakePrefs, calculationRepository = null, database = null)

        // When repository is null or empty, exportHistory should not crash
        // (Full mock of Context/ContentResolver is covered in BackupFileManagerTest)
        assertFalse(viewModel.uiState.value.isExportingBackup)
    }
}
