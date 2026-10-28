package com.margiq.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.margiq.data.preferences.IPreferencesRepository
import com.margiq.data.preferences.ThemePreference
import com.margiq.data.preferences.UserPreferences
import com.margiq.data.updater.DefaultGitHubUpdateService
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
    private val updateService: IGitHubUpdateService = DefaultGitHubUpdateService()
) : ViewModel() {

    private val _showResetDefaultsDialog = MutableStateFlow(false)
    private val _userMessage = MutableStateFlow<String?>(null)
    private val _updateCheckState = MutableStateFlow<UpdateCheckState>(UpdateCheckState.Idle)

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.userPreferencesFlow,
        _showResetDefaultsDialog,
        _userMessage,
        _updateCheckState
    ) { prefs, showDialog, message, updateState ->
        SettingsUiState(
            preferences = prefs,
            showResetDefaultsConfirmDialog = showDialog,
            userMessage = message,
            updateCheckState = updateState
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
