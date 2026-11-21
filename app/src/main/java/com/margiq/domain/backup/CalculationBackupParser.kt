package com.margiq.domain.backup

import com.margiq.domain.model.PurchaseTaxClassification
import com.margiq.domain.model.TaxPriceMode
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal

/**
 * Result of parsing a backup JSON document.
 */
sealed class BackupParseResult {
    data class Success(val document: CalculationBackupDocument) : BackupParseResult()
    data class Error(val message: String) : BackupParseResult()
}

/**
 * Validates and parses raw JSON backup documents into strongly-typed [CalculationBackupDocument] models.
 */
object CalculationBackupParser {

    /**
     * Parses and validates a JSON backup string.
     * Enforces all-or-nothing validation: any schema violation, missing required field,
     * invalid number, or unknown enum terminates parsing with a descriptive error.
     */
    fun parse(jsonString: String): BackupParseResult {
        val trimmed = jsonString.trim()
        if (trimmed.isEmpty()) {
            return BackupParseResult.Error("Backup content is empty.")
        }

        if (trimmed.length > BackupConstants.MAX_SUPPORTED_BACKUP_SIZE_BYTES) {
            return BackupParseResult.Error("Backup file exceeds maximum supported size of 10 MB.")
        }

        val root = try {
            JSONObject(trimmed)
        } catch (e: Exception) {
            return BackupParseResult.Error("Malformed JSON: ${e.localizedMessage ?: "Syntax error"}")
        }

        val format = root.optString("format", "")
        if (format != BackupConstants.BACKUP_FORMAT_IDENTIFIER) {
            return BackupParseResult.Error("Unrecognized format '$format'. Expected '${BackupConstants.BACKUP_FORMAT_IDENTIFIER}'.")
        }

        val schemaVersion = root.optInt("schemaVersion", -1)
        if (schemaVersion != BackupConstants.CURRENT_SCHEMA_VERSION) {
            return BackupParseResult.Error("Unsupported schema version $schemaVersion. This MargiQ build supports schema version ${BackupConstants.CURRENT_SCHEMA_VERSION}.")
        }

        val appVersion = root.optString("appVersion", "Unknown")
        val exportedAt = root.optString("exportedAt", "")

        val recordsArray = root.optJSONArray("records")
            ?: return BackupParseResult.Error("Missing 'records' array in backup document.")

        val declaredCount = root.optInt("recordCount", recordsArray.length())
        val records = mutableListOf<CalculationRecordBackup>()

        for (i in 0 until recordsArray.length()) {
            val recordObj = recordsArray.optJSONObject(i)
                ?: return BackupParseResult.Error("Record at index $i is not a valid JSON object.")

            val parsedRecord = parseAndValidateRecord(recordObj, i)
            when (parsedRecord) {
                is RecordParseResult.Success -> records.add(parsedRecord.record)
                is RecordParseResult.Error -> return BackupParseResult.Error("Record #${i + 1}: ${parsedRecord.message}")
            }
        }

        val document = CalculationBackupDocument(
            format = format,
            schemaVersion = schemaVersion,
            appVersion = appVersion,
            exportedAt = exportedAt,
            recordCount = declaredCount,
            records = records
        )

        return BackupParseResult.Success(document)
    }

    private sealed class RecordParseResult {
        data class Success(val record: CalculationRecordBackup) : RecordParseResult()
        data class Error(val message: String) : RecordParseResult()
    }

    private fun parseAndValidateRecord(obj: JSONObject, index: Int): RecordParseResult {
        val id = obj.optLong("id", 0L)
        val productName = obj.optString("productName", "").trim()
        if (productName.isEmpty()) {
            return RecordParseResult.Error("productName is required and cannot be empty.")
        }

        val supplierName = obj.optString("supplierName", "")
        val timestamp = obj.optLong("timestamp", -1L)
        if (timestamp <= 0L) {
            return RecordParseResult.Error("Invalid or missing timestamp ($timestamp).")
        }

        val ptr = obj.optString("ptr", "")
        if (!isValidDecimal(ptr)) {
            return RecordParseResult.Error("Invalid purchase price (PTR): '$ptr'.")
        }

        val purchaseTaxRate = obj.optString("purchaseTaxRate", "")
        if (!isValidDecimal(purchaseTaxRate)) {
            return RecordParseResult.Error("Invalid purchase tax rate: '$purchaseTaxRate'.")
        }

        val purchasePriceModeStr = obj.optString("purchasePriceMode", "")
        if (!isValidEnum<TaxPriceMode>(purchasePriceModeStr)) {
            return RecordParseResult.Error("Invalid purchasePriceMode: '$purchasePriceModeStr'.")
        }

        val purchaseTaxClassificationStr = obj.optString("purchaseTaxClassification", "")
        if (!isValidEnum<PurchaseTaxClassification>(purchaseTaxClassificationStr)) {
            return RecordParseResult.Error("Invalid purchaseTaxClassification: '$purchaseTaxClassificationStr'.")
        }

        val isInputTaxCreditEnabled = obj.optBoolean("isInputTaxCreditEnabled", true)

        val sellingPrice = obj.optString("sellingPrice", "")
        if (!isValidDecimal(sellingPrice)) {
            return RecordParseResult.Error("Invalid selling price: '$sellingPrice'.")
        }

        val sellingTaxRate = obj.optString("sellingTaxRate", "")
        if (!isValidDecimal(sellingTaxRate)) {
            return RecordParseResult.Error("Invalid selling tax rate: '$sellingTaxRate'.")
        }

        val sellingPriceModeStr = obj.optString("sellingPriceMode", "")
        if (!isValidEnum<TaxPriceMode>(sellingPriceModeStr)) {
            return RecordParseResult.Error("Invalid sellingPriceMode: '$sellingPriceModeStr'.")
        }

        val quantity = obj.optInt("quantity", 0)
        if (quantity < 1) {
            return RecordParseResult.Error("quantity must be at least 1 (found $quantity).")
        }

        val packName = obj.optString("packName", "Pack").ifBlank { "Pack" }
        val unitName = obj.optString("unitName", "Unit").ifBlank { "Unit" }
        val unitsPerPack = obj.optInt("unitsPerPack", 1).coerceAtLeast(1)
        val sellingQuantity = obj.optInt("sellingQuantity", 1).coerceAtLeast(1)

        val costPerIndividualUnit = obj.optString("costPerIndividualUnit", "0.00")
        val revenuePerIndividualUnit = obj.optString("revenuePerIndividualUnit", "0.00")
        val profitPerIndividualUnit = obj.optString("profitPerIndividualUnit", "0.00")

        val isSupplierDiscountEnabled = obj.optBoolean("isSupplierDiscountEnabled", false)
        val isSupplierOfferEnabled = obj.optBoolean("isSupplierOfferEnabled", false)
        val offerBuyQuantity = obj.optInt("offerBuyQuantity", 1).coerceAtLeast(1)
        val offerFreeQuantity = obj.optInt("offerFreeQuantity", 0).coerceAtLeast(0)
        val freePacksReceived = obj.optInt("freePacksReceived", 0).coerceAtLeast(0)
        val totalPacksReceived = obj.optInt("totalPacksReceived", quantity).coerceAtLeast(1)

        val supplierDiscountPercent = obj.optString("supplierDiscountPercent", "0.00")
        val supplierDiscountAmount = obj.optString("supplierDiscountAmount", "0.00")
        val effectiveCostPerReceivedPack = obj.optString("effectiveCostPerReceivedPack", "0.00")

        val economicPurchaseCost = obj.optString("economicPurchaseCost", "")
        if (!isValidDecimal(economicPurchaseCost)) {
            return RecordParseResult.Error("Invalid economicPurchaseCost: '$economicPurchaseCost'.")
        }

        val netSalesRevenue = obj.optString("netSalesRevenue", "")
        if (!isValidDecimal(netSalesRevenue)) {
            return RecordParseResult.Error("Invalid netSalesRevenue: '$netSalesRevenue'.")
        }

        val profitPerUnit = obj.optString("profitPerUnit", "")
        if (!isValidDecimal(profitPerUnit)) {
            return RecordParseResult.Error("Invalid profitPerUnit: '$profitPerUnit'.")
        }

        val profitMargin = if (obj.isNull("profitMargin")) null else obj.optString("profitMargin")
        val markup = if (obj.isNull("markup")) null else obj.optString("markup")
        val costPercentage = if (obj.isNull("costPercentage")) null else obj.optString("costPercentage")

        val totalProfit = obj.optString("totalProfit", "0.00")
        val totalPurchaseInvoiceTotal = obj.optString("totalPurchaseInvoiceTotal", "0.00")
        val totalSellingCustomerTotal = obj.optString("totalSellingCustomerTotal", "0.00")
        val calculationBasis = obj.optString("calculationBasis", "MargiQ Domain Calculation")

        val record = CalculationRecordBackup(
            id = id,
            productName = productName,
            supplierName = supplierName,
            timestamp = timestamp,
            ptr = ptr,
            purchaseTaxRate = purchaseTaxRate,
            purchasePriceMode = purchasePriceModeStr,
            purchaseTaxClassification = purchaseTaxClassificationStr,
            isInputTaxCreditEnabled = isInputTaxCreditEnabled,
            sellingPrice = sellingPrice,
            sellingTaxRate = sellingTaxRate,
            sellingPriceMode = sellingPriceModeStr,
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

        return RecordParseResult.Success(record)
    }

    private fun isValidDecimal(str: String): Boolean {
        if (str.isBlank()) return false
        return try {
            BigDecimal(str.trim())
            true
        } catch (_: Exception) {
            false
        }
    }

    private inline fun <reified T : Enum<T>> isValidEnum(str: String): Boolean {
        if (str.isBlank()) return false
        return try {
            java.lang.Enum.valueOf(T::class.java, str.trim())
            true
        } catch (_: Exception) {
            false
        }
    }
}
