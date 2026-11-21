package com.margiq.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.margiq.data.local.AppDatabase
import com.margiq.data.preferences.IPreferencesRepository
import com.margiq.data.preferences.ThemePreference
import com.margiq.data.preferences.UserPreferences
import com.margiq.data.repository.CalculationRepository
import com.margiq.data.updater.DefaultGitHubUpdateService
import com.margiq.domain.backup.BackupFileManager
import com.margiq.domain.backup.BackupParseResult
import com.margiq.domain.backup.CalculationBackupParser
import com.margiq.domain.backup.CalculationBackupSerializer
import com.margiq.domain.backup.CalculationHistoryImporter
import com.margiq.domain.backup.ImportMode
import com.margiq.domain.model.PurchaseTaxClassification
import com.margiq.domain.model.TaxPriceMode
import com.margiq.domain.updater.ApkAssetSelection
import com.margiq.domain.updater.GitHubUpdateResult
import com.margiq.domain.updater.IGitHubUpdateService
import com.margiq.domain.updater.SemanticVersion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal

class SettingsViewModel(
    private val preferencesRepository: IPreferencesRepository,
    private val calculationRepository: CalculationRepository? = null,
    private val database: AppDatabase? = null,
    private val updateService: IGitHubUpdateService = DefaultGitHubUpdateService()
) : ViewModel() {

    private val _showResetDefaultsDialog = MutableStateFlow(false)
    private val _userMessage = MutableStateFlow<String?>(null)
    private val _updateCheckState = MutableStateFlow<UpdateCheckState>(UpdateCheckState.Idle)
    private val _isExportingBackup = MutableStateFlow(false)
    private val _isImportingBackup = MutableStateFlow(false)
    private val _pendingImportPreview = MutableStateFlow<BackupImportPreview?>(null)
    private val _backupErrorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.userPreferencesFlow,
        _showResetDefaultsDialog,
        _userMessage,
        _updateCheckState,
        _isExportingBackup,
        _isImportingBackup,
        _pendingImportPreview,
        _backupErrorMessage
    ) { args: Array<Any?> ->
        SettingsUiState(
            preferences = args[0] as UserPreferences,
            showResetDefaultsConfirmDialog = args[1] as Boolean,
            userMessage = args[2] as String?,
            updateCheckState = args[3] as UpdateCheckState,
            isExportingBackup = args[4] as Boolean,
            isImportingBackup = args[5] as Boolean,
            pendingImportPreview = args[6] as BackupImportPreview?,
            backupErrorMessage = args[7] as String?
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun checkForUpdates(installedVersionName: String) {
        if (_updateCheckState.value is UpdateCheckState.Checking) return

        viewModelScope.launch {
            _updateCheckState.value = UpdateCheckState.Checking
            val result = updateService.fetchLatestRelease()
            _updateCheckState.value = processUpdateResult(result, installedVersionName)
        }
    }

    fun dismissUpdateState() {
        _updateCheckState.value = UpdateCheckState.Idle
    }

    fun setThemePreference(theme: ThemePreference) {
        viewModelScope.launch {
            preferencesRepository.updateThemePreference(theme)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateDynamicColor(enabled)
        }
    }

    fun updateDefaults(
        purchaseTaxRate: BigDecimal,
        sellingTaxRate: BigDecimal,
        purchasePriceMode: TaxPriceMode,
        sellingPriceMode: TaxPriceMode,
        purchaseTaxClassification: PurchaseTaxClassification,
        inputTaxCreditEnabled: Boolean,
        quantity: Int
    ) {
        viewModelScope.launch {
            preferencesRepository.updateDefaults(
                purchaseTaxRate = purchaseTaxRate,
                sellingTaxRate = sellingTaxRate,
                purchasePriceMode = purchasePriceMode,
                sellingPriceMode = sellingPriceMode,
                purchaseTaxClassification = purchaseTaxClassification,
                inputTaxCreditEnabled = inputTaxCreditEnabled,
                quantity = quantity
            )
            _userMessage.value = "Calculator defaults updated"
        }
    }

    fun requestResetDefaults() {
        _showResetDefaultsDialog.value = true
    }

    fun cancelResetDefaults() {
        _showResetDefaultsDialog.value = false
    }

    fun confirmResetDefaults() {
        viewModelScope.launch {
            preferencesRepository.resetDefaults()
            _showResetDefaultsDialog.value = false
            _userMessage.value = "Calculator defaults reset to standard"
        }
    }

    fun dismissUserMessage() {
        _userMessage.value = null
    }

    fun exportHistory(context: Context, destinationUri: Uri, appVersion: String) {
        if (_isExportingBackup.value) return
        viewModelScope.launch {
            _isExportingBackup.value = true
            try {
                val entities = calculationRepository?.getAllCalculationsList() ?: emptyList()
                if (entities.isEmpty()) {
                    _userMessage.value = "No calculation records to export."
                    return@launch
                }
                val json = CalculationBackupSerializer.serializeEntities(entities, appVersion)
                val result = BackupFileManager.writeJsonToUri(context, destinationUri, json)
                if (result.isSuccess) {
                    _userMessage.value = "Exported ${entities.size} calculation${if (entities.size > 1) "s" else ""} successfully."
                } else {
                    _backupErrorMessage.value = "Failed to export: ${result.exceptionOrNull()?.localizedMessage ?: "Write error"}"
                }
            } catch (e: Exception) {
                _backupErrorMessage.value = "Export error: ${e.localizedMessage ?: "Unexpected error"}"
            } finally {
                _isExportingBackup.value = false
            }
        }
    }

    fun prepareImport(context: Context, sourceUri: Uri) {
        if (_isImportingBackup.value) return
        viewModelScope.launch {
            _isImportingBackup.value = true
            try {
                val readResult = BackupFileManager.readJsonFromUri(context, sourceUri)
                if (readResult.isFailure) {
                    _backupErrorMessage.value = "Could not read backup file: ${readResult.exceptionOrNull()?.localizedMessage ?: "Read error"}"
                    return@launch
                }
                val jsonContent = readResult.getOrThrow()
                when (val parseResult = CalculationBackupParser.parse(jsonContent)) {
                    is BackupParseResult.Error -> {
                        _backupErrorMessage.value = parseResult.message
                    }
                    is BackupParseResult.Success -> {
                        val existingCount = calculationRepository?.getAllCalculationsList()?.size ?: 0
                        _pendingImportPreview.value = BackupImportPreview(
                            document = parseResult.document,
                            existingRecordCount = existingCount
                        )
                    }
                }
            } catch (e: Exception) {
                _backupErrorMessage.value = "Import error: ${e.localizedMessage ?: "Unexpected error"}"
            } finally {
                _isImportingBackup.value = false
            }
        }
    }

    fun confirmImport(mode: ImportMode) {
        val preview = _pendingImportPreview.value ?: return
        val db = database
        if (db == null) {
            _backupErrorMessage.value = "Database connection unavailable for import."
            _pendingImportPreview.value = null
            return
        }
        viewModelScope.launch {
            _isImportingBackup.value = true
            try {
                val summary = CalculationHistoryImporter.importBackup(db, preview.document, mode)
                _pendingImportPreview.value = null
                _userMessage.value = when (mode) {
                    ImportMode.REPLACE -> "History replaced with ${summary.importedCount} calculation${if (summary.importedCount > 1) "s" else ""}."
                    ImportMode.MERGE -> {
                        if (summary.skippedCount > 0) {
                            "Imported ${summary.importedCount} calculation${if (summary.importedCount > 1) "s" else ""} (${summary.skippedCount} duplicate${if (summary.skippedCount > 1) "s" else ""} skipped)."
                        } else {
                            "Imported ${summary.importedCount} calculation${if (summary.importedCount > 1) "s" else ""} successfully."
                        }
                    }
                }
            } catch (e: Exception) {
                _backupErrorMessage.value = "Database import failed: ${e.localizedMessage ?: "Transaction error"}"
            } finally {
                _isImportingBackup.value = false
            }
        }
    }

    fun cancelImport() {
        _pendingImportPreview.value = null
    }

    fun dismissBackupError() {
        _backupErrorMessage.value = null
    }

    companion object {
        /**
         * Pure evaluation of an update check result against the installed version.
         */
        fun processUpdateResult(
            result: GitHubUpdateResult,
            installedVersionName: String
        ): UpdateCheckState {
            return when (result) {
                is GitHubUpdateResult.Success -> {
                    val release = result.release
                    val installedSemver = SemanticVersion.parse(installedVersionName)
                    val releaseSemver = SemanticVersion.parse(release.tagName)

                    if (installedSemver != null && releaseSemver != null) {
                        if (releaseSemver > installedSemver) {
                            val apkSelection = release.resolveApkAsset()
                            UpdateCheckState.UpdateAvailable(
                                latestVersion = releaseSemver.normalizedString,
                                releaseName = release.name.ifBlank { release.tagName },
                                releaseNotes = release.body,
                                releasePageUrl = release.htmlUrl,
                                apkAsset = (apkSelection as? ApkAssetSelection.Selected)?.asset,
                                isAmbiguousApk = apkSelection is ApkAssetSelection.Ambiguous
                            )
                        } else {
                            UpdateCheckState.UpToDate(
                                currentVersion = installedVersionName,
                                releasePageUrl = release.htmlUrl
                            )
                        }
                    } else {
                        // Tag could not be parsed reliably
                        UpdateCheckState.UncertainVersion(
                            rawTag = release.tagName,
                            releaseName = release.name,
                            releasePageUrl = release.htmlUrl
                        )
                    }
                }
                is GitHubUpdateResult.HttpError -> {
                    UpdateCheckState.Error(
                        message = result.message,
                        isOffline = false
                    )
                }
                is GitHubUpdateResult.NetworkError -> {
                    UpdateCheckState.Error(
                        message = result.message,
                        isOffline = result.isOffline
                    )
                }
                is GitHubUpdateResult.ParseError -> {
                    UpdateCheckState.Error(
                        message = result.message,
                        isOffline = false
                    )
                }
            }
        }
    }
}
