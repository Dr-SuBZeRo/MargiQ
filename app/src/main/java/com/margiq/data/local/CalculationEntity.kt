package com.margiq.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.margiq.domain.model.CalculationInput
import com.margiq.domain.model.CalculationResult
import com.margiq.domain.model.PurchaseTaxClassification
import com.margiq.domain.model.TaxPriceMode
import java.math.BigDecimal

@Entity(tableName = "calculations")
data class CalculationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productName: String,
    val supplierName: String = "",
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

    // Pack & Unit Details
    val packName: String = "Pack",
    val unitName: String = "Unit",
    val unitsPerPack: Int = 1,
    val sellingQuantity: Int = 1,
    val costPerIndividualUnit: String = "0.00",
    val revenuePerIndividualUnit: String = "0.00",
    val profitPerIndividualUnit: String = "0.00",

    // Supplier Offers & Discounts (v1.2.0 & v2.0.1)
    val isSupplierDiscountEnabled: Boolean = false,
    val isSupplierOfferEnabled: Boolean = false,
    val offerBuyQuantity: Int = 1,
    val offerFreeQuantity: Int = 0,
    val freePacksReceived: Int = 0,
    val totalPacksReceived: Int = 1,
    val supplierDiscountPercent: String = "0.00",
    val supplierDiscountAmount: String = "0.00",
    val effectiveCostPerReceivedPack: String = "0.00",

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
        val discountEnabled = isSupplierDiscountEnabled || (try { BigDecimal(supplierDiscountPercent) > BigDecimal.ZERO } catch (_: Exception) { false })
        return CalculationInput(
            productName = productName,
            supplierName = supplierName,
            ptr = BigDecimal(ptr),
            purchaseTaxRate = BigDecimal(purchaseTaxRate),
            purchasePriceMode = TaxPriceMode.valueOf(purchasePriceMode),
            purchaseTaxClassification = PurchaseTaxClassification.valueOf(purchaseTaxClassification),
            isInputTaxCreditEnabled = isInputTaxCreditEnabled,
            sellingPrice = BigDecimal(sellingPrice),
            sellingTaxRate = BigDecimal(sellingTaxRate),
            sellingPriceMode = TaxPriceMode.valueOf(sellingPriceMode),
            quantity = quantity,
            packName = packName,
            unitName = unitName,
            unitsPerPack = unitsPerPack,
            sellingQuantity = sellingQuantity,
            isSupplierDiscountEnabled = discountEnabled,
            supplierDiscountPercent = try { BigDecimal(supplierDiscountPercent) } catch (_: Exception) { BigDecimal.ZERO },
            isSupplierOfferEnabled = isSupplierOfferEnabled,
            offerBuyQuantity = offerBuyQuantity,
            offerFreeQuantity = offerFreeQuantity
        )
    }

    companion object {
        fun fromInputAndResult(input: CalculationInput, result: CalculationResult, id: Long = 0): CalculationEntity {
            return CalculationEntity(
                id = id,
                productName = input.productName.ifBlank { "Untitled Calculation" },
                supplierName = input.supplierName,
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
                packName = result.packName,
                unitName = result.unitName,
                unitsPerPack = result.unitsPerPack,
                sellingQuantity = result.sellingQuantity,
                costPerIndividualUnit = result.roundedCostPerIndividualUnit.toPlainString(),
                revenuePerIndividualUnit = result.roundedNetRevenuePerIndividualUnit.toPlainString(),
                profitPerIndividualUnit = result.roundedProfitPerIndividualUnit.toPlainString(),
                isSupplierDiscountEnabled = result.isSupplierDiscountEnabled,
                isSupplierOfferEnabled = result.isSupplierOfferEnabled,
                offerBuyQuantity = result.offerBuyQuantity,
                offerFreeQuantity = result.offerFreeQuantity,
                freePacksReceived = result.freePacksReceived,
                totalPacksReceived = result.totalPacksReceived,
                supplierDiscountPercent = result.supplierDiscountPercent.toPlainString(),
                supplierDiscountAmount = result.roundedSupplierDiscountAmount.toPlainString(),
                effectiveCostPerReceivedPack = result.roundedEffectiveCostPerReceivedPack.toPlainString(),
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
