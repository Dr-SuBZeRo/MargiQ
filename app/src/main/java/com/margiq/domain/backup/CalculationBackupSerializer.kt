package com.margiq.domain.backup

import com.margiq.data.local.CalculationEntity
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

/**
 * Serializes calculation history into versioned, schema-compliant JSON documents.
 */
object CalculationBackupSerializer {

    /**
     * Serializes a list of [CalculationEntity] records into a formatted JSON backup string.
     *
     * @param entities The calculation records to export from the local database.
     * @param appVersion The current MargiQ application version (e.g. "2.3.0").
     * @param exportedAt The timestamp of export (defaults to current UTC instant).
     * @param indentSpaces Indentation for pretty-printing (default: 2 spaces). Set to 0 for compact output.
     * @return Valid, versioned JSON string conforming to Schema Version 1.
     */
    fun serializeEntities(
        entities: List<CalculationEntity>,
        appVersion: String,
        exportedAt: Instant = Instant.now(),
        indentSpaces: Int = 2
    ): String {
        val backupRecords = entities.map { CalculationRecordBackup.fromEntity(it) }
        val document = CalculationBackupDocument(
            format = BackupConstants.BACKUP_FORMAT_IDENTIFIER,
            schemaVersion = BackupConstants.CURRENT_SCHEMA_VERSION,
            appVersion = appVersion,
            exportedAt = exportedAt.toString(),
            recordCount = backupRecords.size,
            records = backupRecords
        )
        return serializeDocument(document, indentSpaces)
    }

    /**
     * Serializes a [CalculationBackupDocument] directly to a JSON string.
     */
    fun serializeDocument(document: CalculationBackupDocument, indentSpaces: Int = 2): String {
        val root = JSONObject()
        root.put("format", document.format)
        root.put("schemaVersion", document.schemaVersion)
        root.put("appVersion", document.appVersion)
        root.put("exportedAt", document.exportedAt)
        root.put("recordCount", document.recordCount)

        val recordsArray = JSONArray()
        for (record in document.records) {
            recordsArray.put(serializeRecord(record))
        }
        root.put("records", recordsArray)

        return if (indentSpaces > 0) root.toString(indentSpaces) else root.toString()
    }

    /**
     * Serializes an individual [CalculationRecordBackup] to a [JSONObject].
     */
    fun serializeRecord(record: CalculationRecordBackup): JSONObject {
        val obj = JSONObject()
        obj.put("id", record.id)
        obj.put("productName", record.productName)
        obj.put("supplierName", record.supplierName)
        obj.put("timestamp", record.timestamp)
        obj.put("ptr", record.ptr)
        obj.put("purchaseTaxRate", record.purchaseTaxRate)
        obj.put("purchasePriceMode", record.purchasePriceMode)
        obj.put("purchaseTaxClassification", record.purchaseTaxClassification)
        obj.put("isInputTaxCreditEnabled", record.isInputTaxCreditEnabled)
        obj.put("sellingPrice", record.sellingPrice)
        obj.put("sellingTaxRate", record.sellingTaxRate)
        obj.put("sellingPriceMode", record.sellingPriceMode)
        obj.put("quantity", record.quantity)

        // Pack & Unit Details
        obj.put("packName", record.packName)
        obj.put("unitName", record.unitName)
        obj.put("unitsPerPack", record.unitsPerPack)
        obj.put("sellingQuantity", record.sellingQuantity)
        obj.put("costPerIndividualUnit", record.costPerIndividualUnit)
        obj.put("revenuePerIndividualUnit", record.revenuePerIndividualUnit)
        obj.put("profitPerIndividualUnit", record.profitPerIndividualUnit)

        // Supplier Offers & Discounts
        obj.put("isSupplierDiscountEnabled", record.isSupplierDiscountEnabled)
        obj.put("isSupplierOfferEnabled", record.isSupplierOfferEnabled)
        obj.put("offerBuyQuantity", record.offerBuyQuantity)
        obj.put("offerFreeQuantity", record.offerFreeQuantity)
        obj.put("freePacksReceived", record.freePacksReceived)
        obj.put("totalPacksReceived", record.totalPacksReceived)
        obj.put("supplierDiscountPercent", record.supplierDiscountPercent)
        obj.put("supplierDiscountAmount", record.supplierDiscountAmount)
        obj.put("effectiveCostPerReceivedPack", record.effectiveCostPerReceivedPack)

        // Economic & Margin Outputs
        obj.put("economicPurchaseCost", record.economicPurchaseCost)
        obj.put("netSalesRevenue", record.netSalesRevenue)
        obj.put("profitPerUnit", record.profitPerUnit)

        // Nullable percentage metrics
        obj.put("profitMargin", record.profitMargin ?: JSONObject.NULL)
        obj.put("markup", record.markup ?: JSONObject.NULL)
        obj.put("costPercentage", record.costPercentage ?: JSONObject.NULL)

        obj.put("totalProfit", record.totalProfit)
        obj.put("totalPurchaseInvoiceTotal", record.totalPurchaseInvoiceTotal)
        obj.put("totalSellingCustomerTotal", record.totalSellingCustomerTotal)
        obj.put("calculationBasis", record.calculationBasis)

        return obj
    }
}
