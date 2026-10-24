package com.margiq.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.margiq.data.preferences.IPreferencesRepository
import com.margiq.data.preferences.ThemePreference
import com.margiq.data.preferences.UserPreferences
import com.margiq.domain.model.PurchaseTaxClassification
import com.margiq.domain.model.TaxPriceMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal

class SettingsViewModel(
    private val preferencesRepository: IPreferencesRepository
) : ViewModel() {

    private val _showResetDefaultsDialog = MutableStateFlow(false)
    private val _userMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.userPreferencesFlow,
        _showResetDefaultsDialog,
        _userMessage
    ) { prefs, showDialog, message ->
        SettingsUiState(
            preferences = prefs,
            showResetDefaultsConfirmDialog = showDialog,
            userMessage = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

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
}
