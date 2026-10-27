package com.margiq.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.margiq.domain.model.CalculationInput
import com.margiq.domain.model.PurchaseTaxClassification
import com.margiq.domain.model.TaxPriceMode
import java.math.BigDecimal

/**
 * Entity representing a reusable product in the shared Product Library.
 * Persists all calculation inputs required to reconstruct or compare product economics.
 */
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productName: String,
    val supplierName: String = "",
    val ptr: String = "0.00",
    val purchaseTaxRate: String = "5.00",
    val purchasePriceMode: String = "TAX_EXCLUSIVE",
    val purchaseTaxClassification: String = "RECOVERABLE_GST",
    val isInputTaxCreditEnabled: Boolean = true,
    val sellingPrice: String = "0.00",
    val sellingTaxRate: String = "5.00",
    val sellingPriceMode: String = "TAX_INCLUSIVE",
    val quantity: Int = 1,
    val packName: String = "Pack",
    val unitName: String = "Unit",
    val unitsPerPack: Int = 1,
    val sellingQuantity: Int = 1,
    val isSupplierDiscountEnabled: Boolean = false,
    val supplierDiscountPercent: String = "0.00",
    val isSupplierOfferEnabled: Boolean = false,
    val offerBuyQuantity: Int = 1,
    val offerFreeQuantity: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toCalculationInput(): CalculationInput {
        val discountEnabled = isSupplierDiscountEnabled || (try { BigDecimal(supplierDiscountPercent) > BigDecimal.ZERO } catch (_: Exception) { false })
        return CalculationInput(
            productName = productName,
            supplierName = supplierName,
            ptr = try { BigDecimal(ptr) } catch (_: Exception) { BigDecimal.ZERO },
            purchaseTaxRate = try { BigDecimal(purchaseTaxRate) } catch (_: Exception) { BigDecimal("5.00") },
            purchasePriceMode = try { TaxPriceMode.valueOf(purchasePriceMode) } catch (_: Exception) { TaxPriceMode.TAX_EXCLUSIVE },
            purchaseTaxClassification = try { PurchaseTaxClassification.valueOf(purchaseTaxClassification) } catch (_: Exception) { PurchaseTaxClassification.RECOVERABLE_GST },
            isInputTaxCreditEnabled = isInputTaxCreditEnabled,
            sellingPrice = try { BigDecimal(sellingPrice) } catch (_: Exception) { BigDecimal.ZERO },
            sellingTaxRate = try { BigDecimal(sellingTaxRate) } catch (_: Exception) { BigDecimal("5.00") },
            sellingPriceMode = try { TaxPriceMode.valueOf(sellingPriceMode) } catch (_: Exception) { TaxPriceMode.TAX_INCLUSIVE },
            quantity = if (quantity > 0) quantity else 1,
            packName = packName.ifBlank { "Pack" },
            unitName = unitName.ifBlank { "Unit" },
            unitsPerPack = if (unitsPerPack > 0) unitsPerPack else 1,
            sellingQuantity = if (sellingQuantity > 0) sellingQuantity else 1,
            isSupplierDiscountEnabled = discountEnabled,
            supplierDiscountPercent = try { BigDecimal(supplierDiscountPercent) } catch (_: Exception) { BigDecimal.ZERO },
            isSupplierOfferEnabled = isSupplierOfferEnabled,
            offerBuyQuantity = if (offerBuyQuantity > 0) offerBuyQuantity else 1,
            offerFreeQuantity = if (offerFreeQuantity >= 0) offerFreeQuantity else 0
        )
    }

    companion object {
        fun fromCalculationInput(input: CalculationInput, id: Long = 0, createdAt: Long = System.currentTimeMillis()): ProductEntity {
            return ProductEntity(
                id = id,
                productName = input.productName.ifBlank { "Untitled Product" },
                supplierName = input.supplierName,
                ptr = input.ptr.toPlainString(),
                purchaseTaxRate = input.purchaseTaxRate.toPlainString(),
                purchasePriceMode = input.purchasePriceMode.name,
                purchaseTaxClassification = input.purchaseTaxClassification.name,
                isInputTaxCreditEnabled = input.isInputTaxCreditEnabled,
                sellingPrice = input.sellingPrice.toPlainString(),
                sellingTaxRate = input.sellingTaxRate.toPlainString(),
                sellingPriceMode = input.sellingPriceMode.name,
                quantity = input.quantity,
                packName = input.packName,
                unitName = input.unitName,
                unitsPerPack = input.unitsPerPack,
                sellingQuantity = input.sellingQuantity,
                isSupplierDiscountEnabled = input.isSupplierDiscountEnabled,
                supplierDiscountPercent = input.supplierDiscountPercent.toPlainString(),
                isSupplierOfferEnabled = input.isSupplierOfferEnabled,
                offerBuyQuantity = input.offerBuyQuantity,
                offerFreeQuantity = input.offerFreeQuantity,
                createdAt = createdAt,
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}
