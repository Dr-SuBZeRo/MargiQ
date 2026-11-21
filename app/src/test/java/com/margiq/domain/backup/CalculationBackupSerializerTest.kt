package com.margiq.domain.backup

import com.margiq.data.local.CalculationEntity
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class CalculationBackupSerializerTest {

    private fun createSampleEntity(
        id: Long = 1L,
        productName: String = "Augmentin 625 Duo",
        supplierName: String = "GlaxoSmithKline",
        ptr: String = "158.45",
        sellingPrice: String = "201.71",
        profitMargin: String? = "21.45",
        markup: String? = "27.30",
        costPercentage: String? = "78.55"
    ): CalculationEntity {
        return CalculationEntity(
            id = id,
            productName = productName,
            supplierName = supplierName,
            timestamp = 1732180000000L,
            ptr = ptr,
            purchaseTaxRate = "12.00",
            purchasePriceMode = "TAX_EXCLUSIVE",
            purchaseTaxClassification = "RECOVERABLE_GST",
            isInputTaxCreditEnabled = true,
            sellingPrice = sellingPrice,
            sellingTaxRate = "12.00",
            sellingPriceMode = "TAX_INCLUSIVE",
            quantity = 10,
            packName = "Strip",
            unitName = "Tablet",
            unitsPerPack = 10,
            sellingQuantity = 1,
            costPerIndividualUnit = "15.85",
            revenuePerIndividualUnit = "18.01",
            profitPerIndividualUnit = "2.16",
            isSupplierDiscountEnabled = true,
            isSupplierOfferEnabled = false,
            offerBuyQuantity = 1,
            offerFreeQuantity = 0,
            freePacksReceived = 0,
            totalPacksReceived = 10,
            supplierDiscountPercent = "2.50",
            supplierDiscountAmount = "39.61",
            effectiveCostPerReceivedPack = "154.49",
            economicPurchaseCost = "1544.89",
            netSalesRevenue = "1800.98",
            profitPerUnit = "25.61",
            profitMargin = profitMargin,
            markup = markup,
            costPercentage = costPercentage,
            totalProfit = "256.09",
            totalPurchaseInvoiceTotal = "1730.28",
            totalSellingCustomerTotal = "2017.10",
            calculationBasis = "MargiQ Domain Calculation"
        )
    }

    @Test
    fun serializeEntities_emptyList_createsValidDocumentWithZeroRecords() {
        val jsonString = CalculationBackupSerializer.serializeEntities(
            entities = emptyList(),
            appVersion = "2.3.0",
            exportedAt = Instant.parse("2026-11-21T08:30:00Z")
        )

        val root = JSONObject(jsonString)
        assertEquals(BackupConstants.BACKUP_FORMAT_IDENTIFIER, root.getString("format"))
        assertEquals(BackupConstants.CURRENT_SCHEMA_VERSION, root.getInt("schemaVersion"))
        assertEquals("2.3.0", root.getString("appVersion"))
        assertEquals("2026-11-21T08:30:00Z", root.getString("exportedAt"))
        assertEquals(0, root.getInt("recordCount"))
        assertEquals(0, root.getJSONArray("records").length())
    }

    @Test
    fun serializeEntities_singleRecord_preservesAllFieldsAndPrecision() {
        val entity = createSampleEntity()
        val jsonString = CalculationBackupSerializer.serializeEntities(
            entities = listOf(entity),
            appVersion = "2.3.0",
            exportedAt = Instant.parse("2026-11-21T08:30:00Z")
        )

        val root = JSONObject(jsonString)
        assertEquals(1, root.getInt("recordCount"))

        val records = root.getJSONArray("records")
        assertEquals(1, records.length())

        val recordJson = records.getJSONObject(0)
        assertEquals(1L, recordJson.getLong("id"))
        assertEquals("Augmentin 625 Duo", recordJson.getString("productName"))
        assertEquals("GlaxoSmithKline", recordJson.getString("supplierName"))
        assertEquals(1732180000000L, recordJson.getLong("timestamp"))
        assertEquals("158.45", recordJson.getString("ptr"))
        assertEquals("12.00", recordJson.getString("purchaseTaxRate"))
        assertEquals("TAX_EXCLUSIVE", recordJson.getString("purchasePriceMode"))
        assertEquals("RECOVERABLE_GST", recordJson.getString("purchaseTaxClassification"))
        assertTrue(recordJson.getBoolean("isInputTaxCreditEnabled"))
        assertEquals("201.71", recordJson.getString("sellingPrice"))
        assertEquals("12.00", recordJson.getString("sellingTaxRate"))
        assertEquals("TAX_INCLUSIVE", recordJson.getString("sellingPriceMode"))
        assertEquals(10, recordJson.getInt("quantity"))
        assertEquals("Strip", recordJson.getString("packName"))
        assertEquals("Tablet", recordJson.getString("unitName"))
        assertEquals(10, recordJson.getInt("unitsPerPack"))
        assertEquals("15.85", recordJson.getString("costPerIndividualUnit"))
        assertEquals("18.01", recordJson.getString("revenuePerIndividualUnit"))
        assertEquals("2.16", recordJson.getString("profitPerIndividualUnit"))
        assertTrue(recordJson.getBoolean("isSupplierDiscountEnabled"))
        assertFalse(recordJson.getBoolean("isSupplierOfferEnabled"))
        assertEquals("2.50", recordJson.getString("supplierDiscountPercent"))
        assertEquals("39.61", recordJson.getString("supplierDiscountAmount"))
        assertEquals("154.49", recordJson.getString("effectiveCostPerReceivedPack"))
        assertEquals("1544.89", recordJson.getString("economicPurchaseCost"))
        assertEquals("1800.98", recordJson.getString("netSalesRevenue"))
        assertEquals("25.61", recordJson.getString("profitPerUnit"))
        assertEquals("21.45", recordJson.getString("profitMargin"))
        assertEquals("27.30", recordJson.getString("markup"))
        assertEquals("78.55", recordJson.getString("costPercentage"))
        assertEquals("256.09", recordJson.getString("totalProfit"))
        assertEquals("1730.28", recordJson.getString("totalPurchaseInvoiceTotal"))
        assertEquals("2017.10", recordJson.getString("totalSellingCustomerTotal"))
        assertEquals("MargiQ Domain Calculation", recordJson.getString("calculationBasis"))
    }

    @Test
    fun serializeEntities_multipleRecords_preservesOrderAndCount() {
        val entities = listOf(
            createSampleEntity(id = 101L, productName = "Product Alpha"),
            createSampleEntity(id = 102L, productName = "Product Beta"),
            createSampleEntity(id = 103L, productName = "Product Gamma")
        )

        val jsonString = CalculationBackupSerializer.serializeEntities(
            entities = entities,
            appVersion = "2.3.0"
        )

        val root = JSONObject(jsonString)
        assertEquals(3, root.getInt("recordCount"))
        val records = root.getJSONArray("records")
        assertEquals(3, records.length())
        assertEquals("Product Alpha", records.getJSONObject(0).getString("productName"))
        assertEquals("Product Beta", records.getJSONObject(1).getString("productName"))
        assertEquals("Product Gamma", records.getJSONObject(2).getString("productName"))
    }

    @Test
    fun serializeEntities_handlesUnicodeAndSpecialCharacters() {
        val entity = createSampleEntity(
            productName = "Amoxicillin & Clavulanate (₹ Special \"500mg/125mg\")",
            supplierName = "Dr. Reddy's & Sun Pharma <Special-Line> / தமிழ்"
        )

        val jsonString = CalculationBackupSerializer.serializeEntities(
            entities = listOf(entity),
            appVersion = "2.3.0"
        )

        val root = JSONObject(jsonString)
        val recordJson = root.getJSONArray("records").getJSONObject(0)
        assertEquals("Amoxicillin & Clavulanate (₹ Special \"500mg/125mg\")", recordJson.getString("productName"))
        assertEquals("Dr. Reddy's & Sun Pharma <Special-Line> / தமிழ்", recordJson.getString("supplierName"))
    }

    @Test
    fun serializeEntities_nullableMetrics_serializedAsExplicitNull() {
        val entity = createSampleEntity(
            profitMargin = null,
            markup = null,
            costPercentage = null
        )

        val jsonString = CalculationBackupSerializer.serializeEntities(
            entities = listOf(entity),
            appVersion = "2.3.0"
        )

        val root = JSONObject(jsonString)
        val recordJson = root.getJSONArray("records").getJSONObject(0)
        assertTrue(recordJson.isNull("profitMargin"))
        assertTrue(recordJson.isNull("markup"))
        assertTrue(recordJson.isNull("costPercentage"))
    }

    @Test
    fun serializeEntities_largeCurrencyNumbers_preservesExactDecimalPrecision() {
        val entity = createSampleEntity(
            ptr = "9999999999.9999",
            sellingPrice = "12345678901.8888"
        )

        val jsonString = CalculationBackupSerializer.serializeEntities(
            entities = listOf(entity),
            appVersion = "2.3.0"
        )

        val root = JSONObject(jsonString)
        val recordJson = root.getJSONArray("records").getJSONObject(0)
        assertEquals("9999999999.9999", recordJson.getString("ptr"))
        assertEquals("12345678901.8888", recordJson.getString("sellingPrice"))
    }
}
