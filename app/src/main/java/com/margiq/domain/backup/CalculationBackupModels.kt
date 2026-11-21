package com.margiq.domain.backup

import com.margiq.data.local.CalculationEntity

/**
 * Metadata and specification constants for MargiQ Calculation History Backups.
 */
object BackupConstants {
    const val BACKUP_FORMAT_IDENTIFIER = "com.margiq.calculation-backup"
    const val CURRENT_SCHEMA_VERSION = 1
    const val DEFAULT_MIME_TYPE = "application/json"
    const val DEFAULT_FILE_EXTENSION = ".json"
    const val SUGGESTED_FILE_PREFIX = "MargiQ_Backup_"
    const val MAX_SUPPORTED_BACKUP_SIZE_BYTES = 10 * 1024 * 1024L // 10 MB sanity limit
}

/**
 * Top-level versioned container for exported calculation records.
 */
data class CalculationBackupDocument(
    val format: String = BackupConstants.BACKUP_FORMAT_IDENTIFIER,
    val schemaVersion: Int = BackupConstants.CURRENT_SCHEMA_VERSION,
    val appVersion: String,
    val exportedAt: String,
    val recordCount: Int,
    val records: List<CalculationRecordBackup>
)

/**
 * Data Transfer Object representing an individual calculation record in the backup.
 * All numerical and monetary figures are serialized as exact decimal strings to prevent
 * binary floating-point rounding inaccuracies.
 */
data class CalculationRecordBackup(
    val id: Long = 0,
    val productName: String,
    val supplierName: String = "",
    val timestamp: Long,
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

    // Supplier Offers & Discounts
    val isSupplierDiscountEnabled: Boolean = false,
    val isSupplierOfferEnabled: Boolean = false,
    val offerBuyQuantity: Int = 1,
    val offerFreeQuantity: Int = 0,
    val freePacksReceived: Int = 0,
    val totalPacksReceived: Int = 1,
    val supplierDiscountPercent: String = "0.00",
    val supplierDiscountAmount: String = "0.00",
    val effectiveCostPerReceivedPack: String = "0.00",

    // Economic & Margin Outputs
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
    /**
     * Converts this backup record to a Room [CalculationEntity].
     *
     * @param assignNewId If true, sets [CalculationEntity.id] to 0 so Room auto-generates a primary key.
     *                    If false, retains the original record [id].
     */
    fun toEntity(assignNewId: Boolean = false): CalculationEntity {
        return CalculationEntity(
            id = if (assignNewId) 0 else id,
            productName = productName,
            supplierName = supplierName,
            timestamp = timestamp,
            ptr = ptr,
            purchaseTaxRate = purchaseTaxRate,
            purchasePriceMode = purchasePriceMode,
            purchaseTaxClassification = purchaseTaxClassification,
            isInputTaxCreditEnabled = isInputTaxCreditEnabled,
            sellingPrice = sellingPrice,
            sellingTaxRate = sellingTaxRate,
            sellingPriceMode = sellingPriceMode,
            quantity = quantity,
            packName = packName,
            unitName = unitName,
            unitsPerPack = unitsPerPack,
            sellingQuantity = sellingQuantity,
            costPerIndividualUnit = costPerIndividualUnit,
            revenuePerIndividualUnit = revenuePerIndividualUnit,
            profitPerIndividualUnit = profitPerIndividualUnit,
            isSupplierDiscountEnabled = isSupplierDiscountEnabled,
            isSupplierOfferEnabled = isSupplierOfferEnabled,
            offerBuyQuantity = offerBuyQuantity,
            offerFreeQuantity = offerFreeQuantity,
            freePacksReceived = freePacksReceived,
            totalPacksReceived = totalPacksReceived,
            supplierDiscountPercent = supplierDiscountPercent,
            supplierDiscountAmount = supplierDiscountAmount,
            effectiveCostPerReceivedPack = effectiveCostPerReceivedPack,
            economicPurchaseCost = economicPurchaseCost,
            netSalesRevenue = netSalesRevenue,
            profitPerUnit = profitPerUnit,
            profitMargin = profitMargin,
            markup = markup,
            costPercentage = costPercentage,
            totalProfit = totalProfit,
            totalPurchaseInvoiceTotal = totalPurchaseInvoiceTotal,
            totalSellingCustomerTotal = totalSellingCustomerTotal,
            calculationBasis = calculationBasis
        )
    }

    companion object {
        fun fromEntity(entity: CalculationEntity): CalculationRecordBackup {
            return CalculationRecordBackup(
                id = entity.id,
                productName = entity.productName,
                supplierName = entity.supplierName,
                timestamp = entity.timestamp,
                ptr = entity.ptr,
                purchaseTaxRate = entity.purchaseTaxRate,
                purchasePriceMode = entity.purchasePriceMode,
                purchaseTaxClassification = entity.purchaseTaxClassification,
                isInputTaxCreditEnabled = entity.isInputTaxCreditEnabled,
                sellingPrice = entity.sellingPrice,
                sellingTaxRate = entity.sellingTaxRate,
                sellingPriceMode = entity.sellingPriceMode,
                quantity = entity.quantity,
                packName = entity.packName,
                unitName = entity.unitName,
                unitsPerPack = entity.unitsPerPack,
                sellingQuantity = entity.sellingQuantity,
                costPerIndividualUnit = entity.costPerIndividualUnit,
                revenuePerIndividualUnit = entity.revenuePerIndividualUnit,
                profitPerIndividualUnit = entity.profitPerIndividualUnit,
                isSupplierDiscountEnabled = entity.isSupplierDiscountEnabled,
                isSupplierOfferEnabled = entity.isSupplierOfferEnabled,
                offerBuyQuantity = entity.offerBuyQuantity,
                offerFreeQuantity = entity.offerFreeQuantity,
                freePacksReceived = entity.freePacksReceived,
                totalPacksReceived = entity.totalPacksReceived,
                supplierDiscountPercent = entity.supplierDiscountPercent,
                supplierDiscountAmount = entity.supplierDiscountAmount,
                effectiveCostPerReceivedPack = entity.effectiveCostPerReceivedPack,
                economicPurchaseCost = entity.economicPurchaseCost,
                netSalesRevenue = entity.netSalesRevenue,
                profitPerUnit = entity.profitPerUnit,
                profitMargin = entity.profitMargin,
                markup = entity.markup,
                costPercentage = entity.costPercentage,
                totalProfit = entity.totalProfit,
                totalPurchaseInvoiceTotal = entity.totalPurchaseInvoiceTotal,
                totalSellingCustomerTotal = entity.totalSellingCustomerTotal,
                calculationBasis = entity.calculationBasis
            )
        }
    }
}
