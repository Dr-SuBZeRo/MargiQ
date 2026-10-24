package com.example.profitmargincalculator.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.profitmargincalculator.domain.model.CalculationInput
import com.example.profitmargincalculator.domain.model.CalculationResult
import com.example.profitmargincalculator.domain.model.PurchaseTaxClassification
import com.example.profitmargincalculator.domain.model.TaxPriceMode
import java.math.BigDecimal

@Entity(tableName = "calculations")
data class CalculationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val ptr: String,
    val purchaseTaxRate: String,
    val purchasePriceMode: String,
    val purchaseTaxClassification: String,
    val isInputTaxCreditEnabled: Boolean,
    val sellingPrice: String,
    val sellingTaxRate: String,
    val sellingPriceMode: String,
    val quantity: Int,

    val economicPurchaseCost: String,
    val netSalesRevenue: String,
    val profitPerUnit: String,
    val profitMargin: String?,
    val markup: String?,
    val costPercentage: String?,
    val totalProfit: String,
    val totalPurchaseInvoiceTotal: String,
    val totalSellingCustomerTotal: String,
    val calculationBasis: String
) {
    fun toCalculationInput(): CalculationInput {
        return CalculationInput(
            productName = productName,
            ptr = BigDecimal(ptr),
            purchaseTaxRate = BigDecimal(purchaseTaxRate),
            purchasePriceMode = TaxPriceMode.valueOf(purchasePriceMode),
            purchaseTaxClassification = PurchaseTaxClassification.valueOf(purchaseTaxClassification),
            isInputTaxCreditEnabled = isInputTaxCreditEnabled,
            sellingPrice = BigDecimal(sellingPrice),
            sellingTaxRate = BigDecimal(sellingTaxRate),
            sellingPriceMode = TaxPriceMode.valueOf(sellingPriceMode),
            quantity = quantity
        )
    }

    companion object {
        fun fromInputAndResult(input: CalculationInput, result: CalculationResult, id: Long = 0): CalculationEntity {
            return CalculationEntity(
                id = id,
                productName = input.productName.ifBlank { "Untitled Calculation" },
                timestamp = System.currentTimeMillis(),
                ptr = input.ptr.toPlainString(),
                purchaseTaxRate = input.purchaseTaxRate.toPlainString(),
                purchasePriceMode = input.purchasePriceMode.name,
                purchaseTaxClassification = input.purchaseTaxClassification.name,
                isInputTaxCreditEnabled = input.isInputTaxCreditEnabled,
                sellingPrice = input.sellingPrice.toPlainString(),
                sellingTaxRate = input.sellingTaxRate.toPlainString(),
                sellingPriceMode = input.sellingPriceMode.name,
                quantity = input.quantity,
                economicPurchaseCost = result.roundedEconomicCost.toPlainString(),
                netSalesRevenue = result.roundedNetSalesRevenue.toPlainString(),
                profitPerUnit = result.roundedProfitPerUnit.toPlainString(),
                profitMargin = result.roundedProfitMargin?.toPlainString(),
                markup = result.roundedMarkup?.toPlainString(),
                costPercentage = result.roundedCostPercentage?.toPlainString(),
                totalProfit = result.roundedTotalProfit.toPlainString(),
                totalPurchaseInvoiceTotal = result.roundedTotalPurchaseInvoice.toPlainString(),
                totalSellingCustomerTotal = result.roundedTotalCustomerCollection.toPlainString(),
                calculationBasis = result.calculationBasis
            )
        }
    }
}
