package com.example.profitmargincalculator.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.profitmargincalculator.domain.model.PurchaseTaxClassification
import com.example.profitmargincalculator.domain.model.TaxPriceMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.math.BigDecimal

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "profit_calculator_preferences")

interface IPreferencesRepository {
    val userPreferencesFlow: Flow<UserPreferences>
    suspend fun updateThemePreference(preference: ThemePreference)
    suspend fun updateDynamicColor(enabled: Boolean)
    suspend fun updateDefaults(
        purchaseTaxRate: BigDecimal,
        sellingTaxRate: BigDecimal,
        purchasePriceMode: TaxPriceMode,
        sellingPriceMode: TaxPriceMode,
        purchaseTaxClassification: PurchaseTaxClassification,
        inputTaxCreditEnabled: Boolean,
        quantity: Int
    )
    suspend fun resetDefaults()
}

class PreferencesRepository(private val dataStore: DataStore<Preferences>) : IPreferencesRepository {

    private object PreferencesKeys {
        val THEME_PREFERENCE = stringPreferencesKey("theme_preference")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val DEFAULT_PURCHASE_TAX_RATE = stringPreferencesKey("default_purchase_tax_rate")
        val DEFAULT_SELLING_TAX_RATE = stringPreferencesKey("default_selling_tax_rate")
        val DEFAULT_PURCHASE_PRICE_MODE = stringPreferencesKey("default_purchase_price_mode")
        val DEFAULT_SELLING_PRICE_MODE = stringPreferencesKey("default_selling_price_mode")
        val DEFAULT_PURCHASE_TAX_CLASSIFICATION = stringPreferencesKey("default_purchase_tax_classification")
        val DEFAULT_INPUT_TAX_CREDIT_ENABLED = booleanPreferencesKey("default_input_tax_credit_enabled")
        val DEFAULT_QUANTITY = intPreferencesKey("default_quantity")
    }

    override val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeString = preferences[PreferencesKeys.THEME_PREFERENCE] ?: ThemePreference.SYSTEM.name
            val themePreference = try {
                ThemePreference.valueOf(themeString)
            } catch (_: Exception) {
                ThemePreference.SYSTEM
            }

            val dynamicColor = preferences[PreferencesKeys.DYNAMIC_COLOR] ?: true

            val purchaseTaxRateStr = preferences[PreferencesKeys.DEFAULT_PURCHASE_TAX_RATE] ?: "5.00"
            val purchaseTaxRate = try {
                BigDecimal(purchaseTaxRateStr)
            } catch (_: Exception) {
                BigDecimal("5.00")
            }

            val sellingTaxRateStr = preferences[PreferencesKeys.DEFAULT_SELLING_TAX_RATE] ?: "5.00"
            val sellingTaxRate = try {
                BigDecimal(sellingTaxRateStr)
            } catch (_: Exception) {
                BigDecimal("5.00")
            }

            val purchaseModeStr = preferences[PreferencesKeys.DEFAULT_PURCHASE_PRICE_MODE] ?: TaxPriceMode.TAX_EXCLUSIVE.name
            val purchasePriceMode = try {
                TaxPriceMode.valueOf(purchaseModeStr)
            } catch (_: Exception) {
                TaxPriceMode.TAX_EXCLUSIVE
            }

            val sellingModeStr = preferences[PreferencesKeys.DEFAULT_SELLING_PRICE_MODE] ?: TaxPriceMode.TAX_INCLUSIVE.name
            val sellingPriceMode = try {
                TaxPriceMode.valueOf(sellingModeStr)
            } catch (_: Exception) {
                TaxPriceMode.TAX_INCLUSIVE
            }

            val classificationStr = preferences[PreferencesKeys.DEFAULT_PURCHASE_TAX_CLASSIFICATION] ?: PurchaseTaxClassification.RECOVERABLE_GST.name
            val purchaseTaxClassification = try {
                PurchaseTaxClassification.valueOf(classificationStr)
            } catch (_: Exception) {
                PurchaseTaxClassification.RECOVERABLE_GST
            }

            val itcEnabled = preferences[PreferencesKeys.DEFAULT_INPUT_TAX_CREDIT_ENABLED] ?: true
            val quantity = preferences[PreferencesKeys.DEFAULT_QUANTITY] ?: 1

            UserPreferences(
                themePreference = themePreference,
                dynamicColor = dynamicColor,
                defaultPurchaseTaxRate = purchaseTaxRate,
                defaultSellingTaxRate = sellingTaxRate,
                defaultPurchasePriceMode = purchasePriceMode,
                defaultSellingPriceMode = sellingPriceMode,
                defaultPurchaseTaxClassification = purchaseTaxClassification,
                defaultInputTaxCreditEnabled = itcEnabled,
                defaultQuantity = quantity
            )
        }

    override suspend fun updateThemePreference(preference: ThemePreference) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_PREFERENCE] = preference.name
        }
    }

    override suspend fun updateDynamicColor(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLOR] = enabled
        }
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
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_PURCHASE_TAX_RATE] = purchaseTaxRate.toPlainString()
            preferences[PreferencesKeys.DEFAULT_SELLING_TAX_RATE] = sellingTaxRate.toPlainString()
            preferences[PreferencesKeys.DEFAULT_PURCHASE_PRICE_MODE] = purchasePriceMode.name
            preferences[PreferencesKeys.DEFAULT_SELLING_PRICE_MODE] = sellingPriceMode.name
            preferences[PreferencesKeys.DEFAULT_PURCHASE_TAX_CLASSIFICATION] = purchaseTaxClassification.name
            preferences[PreferencesKeys.DEFAULT_INPUT_TAX_CREDIT_ENABLED] = inputTaxCreditEnabled
            preferences[PreferencesKeys.DEFAULT_QUANTITY] = quantity
        }
    }

    override suspend fun resetDefaults() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_PURCHASE_TAX_RATE] = "5.00"
            preferences[PreferencesKeys.DEFAULT_SELLING_TAX_RATE] = "5.00"
            preferences[PreferencesKeys.DEFAULT_PURCHASE_PRICE_MODE] = TaxPriceMode.TAX_EXCLUSIVE.name
            preferences[PreferencesKeys.DEFAULT_SELLING_PRICE_MODE] = TaxPriceMode.TAX_INCLUSIVE.name
            preferences[PreferencesKeys.DEFAULT_PURCHASE_TAX_CLASSIFICATION] = PurchaseTaxClassification.RECOVERABLE_GST.name
            preferences[PreferencesKeys.DEFAULT_INPUT_TAX_CREDIT_ENABLED] = true
            preferences[PreferencesKeys.DEFAULT_QUANTITY] = 1
        }
    }
}
