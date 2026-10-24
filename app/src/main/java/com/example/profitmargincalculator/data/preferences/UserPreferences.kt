package com.example.profitmargincalculator.data.preferences

import com.example.profitmargincalculator.domain.model.CalculationInput
import com.example.profitmargincalculator.domain.model.PurchaseTaxClassification
import com.example.profitmargincalculator.domain.model.TaxPriceMode
import java.math.BigDecimal

data class UserPreferences(
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val dynamicColor: Boolean = true,
    val defaultPurchaseTaxRate: BigDecimal = BigDecimal("5.00"),
    val defaultSellingTaxRate: BigDecimal = BigDecimal("5.00"),
    val defaultPurchasePriceMode: TaxPriceMode = TaxPriceMode.TAX_EXCLUSIVE,
    val defaultSellingPriceMode: TaxPriceMode = TaxPriceMode.TAX_INCLUSIVE,
    val defaultPurchaseTaxClassification: PurchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST,
    val defaultInputTaxCreditEnabled: Boolean = true,
    val defaultQuantity: Int = 1
) {
    fun toInitialCalculationInput(productName: String = "Sample Product"): CalculationInput {
        return CalculationInput(
            productName = productName,
            ptr = BigDecimal("48.00"),
            purchaseTaxRate = defaultPurchaseTaxRate,
            purchasePriceMode = defaultPurchasePriceMode,
            purchaseTaxClassification = defaultPurchaseTaxClassification,
            isInputTaxCreditEnabled = defaultInputTaxCreditEnabled,
            sellingPrice = BigDecimal("135.00"),
            sellingTaxRate = defaultSellingTaxRate,
            sellingPriceMode = defaultSellingPriceMode,
            quantity = defaultQuantity
        )
    }
}
