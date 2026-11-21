package com.margiq.domain.backup

import com.margiq.data.local.CalculationEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class CalculationBackupParserTest {

    private fun createValidSampleJson(
        format: String = BackupConstants.BACKUP_FORMAT_IDENTIFIER,
        schemaVersion: Int = BackupConstants.CURRENT_SCHEMA_VERSION,
        productName: String = "Augmentin 625 Duo",
        ptr: String = "158.45",
        sellingPrice: String = "201.71",
        quantity: Int = 10,
        purchasePriceMode: String = "TAX_EXCLUSIVE",
        timestamp: Long = 1732180000000L
    ): String {
        return """
        {
          "format": "$format",
          "schemaVersion": $schemaVersion,
          "appVersion": "2.3.0",
          "exportedAt": "2026-11-21T08:30:00Z",
          "recordCount": 1,
          "records": [
            {
              "id": 1,
              "productName": "$productName",
              "supplierName": "GlaxoSmithKline",
              "timestamp": $timestamp,
              "ptr": "$ptr",
              "purchaseTaxRate": "12.00",
              "purchasePriceMode": "$purchasePriceMode",
              "purchaseTaxClassification": "RECOVERABLE_GST",
              "isInputTaxCreditEnabled": true,
              "sellingPrice": "$sellingPrice",
              "sellingTaxRate": "12.00",
              "sellingPriceMode": "TAX_INCLUSIVE",
              "quantity": $quantity,
              "packName": "Strip",
              "unitName": "Tablet",
              "unitsPerPack": 10,
              "sellingQuantity": 1,
              "costPerIndividualUnit": "15.85",
              "revenuePerIndividualUnit": "18.01",
              "profitPerIndividualUnit": "2.16",
              "isSupplierDiscountEnabled": true,
              "isSupplierOfferEnabled": false,
              "offerBuyQuantity": 1,
              "offerFreeQuantity": 0,
              "freePacksReceived": 0,
              "totalPacksReceived": 10,
              "supplierDiscountPercent": "2.50",
              "supplierDiscountAmount": "39.61",
              "effectiveCostPerReceivedPack": "154.49",
              "economicPurchaseCost": "1544.89",
              "netSalesRevenue": "1800.98",
              "profitPerUnit": "25.61",
              "profitMargin": "21.45",
              "markup": "27.30",
              "costPercentage": "78.55",
              "totalProfit": "256.09",
              "totalPurchaseInvoiceTotal": "1730.28",
              "totalSellingCustomerTotal": "2017.10",
              "calculationBasis": "MargiQ Domain Calculation"
            }
          ]
        }
        """.trimIndent()
    }

    @Test
    fun parse_validDocument_succeedsWithCorrectMetadataAndRecords() {
        val json = createValidSampleJson()
        val result = CalculationBackupParser.parse(json)

        assertTrue(result is BackupParseResult.Success)
        val doc = (result as BackupParseResult.Success).document
        assertEquals(BackupConstants.BACKUP_FORMAT_IDENTIFIER, doc.format)
        assertEquals(1, doc.schemaVersion)
        assertEquals("2.3.0", doc.appVersion)
        assertEquals("2026-11-21T08:30:00Z", doc.exportedAt)
        assertEquals(1, doc.recordCount)
        assertEquals(1, doc.records.size)

        val record = doc.records.first()
        assertEquals("Augmentin 625 Duo", record.productName)
        assertEquals("158.45", record.ptr)
        assertEquals("201.71", record.sellingPrice)
        assertEquals(10, record.quantity)
    }

    @Test
    fun parse_emptyString_returnsError() {
        val result = CalculationBackupParser.parse("")
        assertTrue(result is BackupParseResult.Error)
        assertTrue((result as BackupParseResult.Error).message.contains("empty"))
    }

    @Test
    fun parse_malformedJson_returnsDescriptiveError() {
        val result = CalculationBackupParser.parse("{ format: broken json ...")
        assertTrue(result is BackupParseResult.Error)
        assertTrue((result as BackupParseResult.Error).message.contains("Malformed JSON"))
    }

    @Test
    fun parse_wrongFormatIdentifier_returnsError() {
        val json = createValidSampleJson(format = "com.otherapp.backup")
        val result = CalculationBackupParser.parse(json)
        assertTrue(result is BackupParseResult.Error)
        assertTrue((result as BackupParseResult.Error).message.contains("Unrecognized format"))
    }

    @Test
    fun parse_unsupportedSchemaVersion_returnsError() {
        val json = createValidSampleJson(schemaVersion = 99)
        val result = CalculationBackupParser.parse(json)
        assertTrue(result is BackupParseResult.Error)
        assertTrue((result as BackupParseResult.Error).message.contains("Unsupported schema version 99"))
    }

    @Test
    fun parse_missingRecordsArray_returnsError() {
        val json = """
        {
          "format": "${BackupConstants.BACKUP_FORMAT_IDENTIFIER}",
          "schemaVersion": 1,
          "appVersion": "2.3.0",
          "exportedAt": "2026-11-21T08:30:00Z"
        }
        """.trimIndent()
        val result = CalculationBackupParser.parse(json)
        assertTrue(result is BackupParseResult.Error)
        assertTrue((result as BackupParseResult.Error).message.contains("Missing 'records' array"))
    }

    @Test
    fun parse_recordWithEmptyProductName_returnsDescriptiveError() {
        val json = createValidSampleJson(productName = "   ")
        val result = CalculationBackupParser.parse(json)
        assertTrue(result is BackupParseResult.Error)
        assertTrue((result as BackupParseResult.Error).message.contains("productName is required"))
    }

    @Test
    fun parse_recordWithInvalidPtr_returnsDescriptiveError() {
        val json = createValidSampleJson(ptr = "invalid-ptr-amount")
        val result = CalculationBackupParser.parse(json)
        assertTrue(result is BackupParseResult.Error)
        assertTrue((result as BackupParseResult.Error).message.contains("Invalid purchase price (PTR)"))
    }

    @Test
    fun parse_recordWithInvalidEnum_returnsDescriptiveError() {
        val json = createValidSampleJson(purchasePriceMode = "NON_EXISTENT_MODE")
        val result = CalculationBackupParser.parse(json)
        assertTrue(result is BackupParseResult.Error)
        assertTrue((result as BackupParseResult.Error).message.contains("Invalid purchasePriceMode"))
    }

    @Test
    fun parse_recordWithZeroQuantity_returnsDescriptiveError() {
        val json = createValidSampleJson(quantity = 0)
        val result = CalculationBackupParser.parse(json)
        assertTrue(result is BackupParseResult.Error)
        assertTrue((result as BackupParseResult.Error).message.contains("quantity must be at least 1"))
    }

    @Test
    fun parse_recordWithInvalidTimestamp_returnsDescriptiveError() {
        val json = createValidSampleJson(timestamp = -5L)
        val result = CalculationBackupParser.parse(json)
        assertTrue(result is BackupParseResult.Error)
        assertTrue((result as BackupParseResult.Error).message.contains("Invalid or missing timestamp"))
    }

    @Test
    fun roundTrip_serializeAndParse_preservesAllDataIdentically() {
        val originalEntity = CalculationEntity(
            id = 42L,
            productName = "Telmisartan 40mg",
            supplierName = "Cipla Healthcare",
            timestamp = 1732185555000L,
            ptr = "85.50",
            purchaseTaxRate = "5.00",
            purchasePriceMode = "TAX_EXCLUSIVE",
            purchaseTaxClassification = "RECOVERABLE_GST",
            isInputTaxCreditEnabled = true,
            sellingPrice = "115.00",
            sellingTaxRate = "5.00",
            sellingPriceMode = "TAX_INCLUSIVE",
            quantity = 25,
            packName = "Box",
            unitName = "Tablet",
            unitsPerPack = 30,
            sellingQuantity = 1,
            costPerIndividualUnit = "2.85",
            revenuePerIndividualUnit = "3.65",
            profitPerIndividualUnit = "0.80",
            isSupplierDiscountEnabled = true,
            isSupplierOfferEnabled = true,
            offerBuyQuantity = 20,
            offerFreeQuantity = 5,
            freePacksReceived = 5,
            totalPacksReceived = 30,
            supplierDiscountPercent = "3.00",
            supplierDiscountAmount = "64.13",
            effectiveCostPerReceivedPack = "69.11",
            economicPurchaseCost = "2073.38",
            netSalesRevenue = "2738.10",
            profitPerUnit = "22.16",
            profitMargin = "24.28",
            markup = "32.06",
            costPercentage = "75.72",
            totalProfit = "664.72",
            totalPurchaseInvoiceTotal = "2177.04",
            totalSellingCustomerTotal = "2875.00",
            calculationBasis = "MargiQ Domain Calculation"
        )

        // 1. Serialize
        val jsonString = CalculationBackupSerializer.serializeEntities(
            entities = listOf(originalEntity),
            appVersion = "2.3.0",
            exportedAt = Instant.parse("2026-11-21T08:35:00Z")
        )

        // 2. Parse
        val parseResult = CalculationBackupParser.parse(jsonString)
        assertTrue(parseResult is BackupParseResult.Success)

        val parsedRecord = (parseResult as BackupParseResult.Success).document.records.first()

        // 3. Convert to entity
        val reconstructedEntity = parsedRecord.toEntity(assignNewId = false)

        // 4. Verify round-trip equivalence
        assertEquals(originalEntity, reconstructedEntity)
    }

    @Test
    fun signatureGeneration_deterministicAndMatchesForEquivalentInputs() {
        val entity1 = CalculationEntity(
            id = 1L,
            productName = "  Paracetamol 650  ",
            supplierName = "Cipla",
            timestamp = 1732180000000L,
            ptr = "48.00",
            purchaseTaxRate = "5.00",
            purchasePriceMode = "TAX_EXCLUSIVE",
            purchaseTaxClassification = "RECOVERABLE_GST",
            isInputTaxCreditEnabled = true,
            sellingPrice = "60.00",
            sellingTaxRate = "5.00",
            sellingPriceMode = "TAX_INCLUSIVE",
            quantity = 10,
            packName = "Strip",
            unitName = "Tablet",
            unitsPerPack = 10,
            sellingQuantity = 1,
            costPerIndividualUnit = "4.80",
            revenuePerIndividualUnit = "5.71",
            profitPerIndividualUnit = "0.91",
            economicPurchaseCost = "480.00",
            netSalesRevenue = "571.43",
            profitPerUnit = "9.14",
            profitMargin = "16.00",
            markup = "19.05",
            costPercentage = "84.00",
            totalProfit = "91.43",
            totalPurchaseInvoiceTotal = "504.00",
            totalSellingCustomerTotal = "600.00",
            calculationBasis = "MargiQ Domain Calculation"
        )

        val entity2 = entity1.copy(
            id = 999L, // different database id
            productName = "paracetamol 650" // different case / whitespace
        )

        val sig1 = CalculationHistoryImporter.createSignature(entity1)
        val sig2 = CalculationHistoryImporter.createSignature(entity2)

        assertEquals(sig1, sig2)
    }

    @Test
    fun fingerprint_distinguishesRecordsDifferingInTaxRatesOrSchemeOffers() {
        val baseEntity = CalculationEntity(
            id = 1L,
            productName = "Paracetamol 650",
            supplierName = "Cipla",
            timestamp = 1732180000000L,
            ptr = "48.00",
            purchaseTaxRate = "5.00",
            purchasePriceMode = "TAX_EXCLUSIVE",
            purchaseTaxClassification = "RECOVERABLE_GST",
            isInputTaxCreditEnabled = true,
            sellingPrice = "60.00",
            sellingTaxRate = "5.00",
            sellingPriceMode = "TAX_INCLUSIVE",
            quantity = 10,
            packName = "Strip",
            unitName = "Tablet",
            unitsPerPack = 10,
            sellingQuantity = 1,
            costPerIndividualUnit = "4.80",
            revenuePerIndividualUnit = "5.71",
            profitPerIndividualUnit = "0.91",
            economicPurchaseCost = "480.00",
            netSalesRevenue = "571.43",
            profitPerUnit = "9.14",
            profitMargin = "16.00",
            markup = "19.05",
            costPercentage = "84.00",
            totalProfit = "91.43",
            totalPurchaseInvoiceTotal = "504.00",
            totalSellingCustomerTotal = "600.00",
            calculationBasis = "MargiQ Domain Calculation"
        )

        val baseFingerprint = CalculationHistoryImporter.computeContentFingerprint(baseEntity)

        // 1. Differing in tax rate
        val diffTaxRate = baseEntity.copy(purchaseTaxRate = "18.00")
        val diffTaxFingerprint = CalculationHistoryImporter.computeContentFingerprint(diffTaxRate)
        assertTrue(baseFingerprint != diffTaxFingerprint)

        // 2. Differing in ITC eligibility
        val diffItc = baseEntity.copy(isInputTaxCreditEnabled = false)
        val diffItcFingerprint = CalculationHistoryImporter.computeContentFingerprint(diffItc)
        assertTrue(baseFingerprint != diffItcFingerprint)

        // 3. Differing in Supplier Scheme / Buy-Free offer
        val diffOffer = baseEntity.copy(isSupplierOfferEnabled = true, offerBuyQuantity = 10, offerFreeQuantity = 1)
        val diffOfferFingerprint = CalculationHistoryImporter.computeContentFingerprint(diffOffer)
        assertTrue(baseFingerprint != diffOfferFingerprint)

        // 4. Differing in Supplier Discount
        val diffDiscount = baseEntity.copy(isSupplierDiscountEnabled = true, supplierDiscountPercent = "5.00")
        val diffDiscountFingerprint = CalculationHistoryImporter.computeContentFingerprint(diffDiscount)
        assertTrue(baseFingerprint != diffDiscountFingerprint)

        // 5. Differing in Tax Mode
        val diffTaxMode = baseEntity.copy(purchasePriceMode = "TAX_INCLUSIVE")
        val diffTaxModeFingerprint = CalculationHistoryImporter.computeContentFingerprint(diffTaxMode)
        assertTrue(baseFingerprint != diffTaxModeFingerprint)
    }

    @Test
    fun fingerprint_textContainingPipe_doesNotCorruptOrCollide() {
        val base = CalculationHistoryImporterTest.createSyntheticEntity(1L, "Paracetamol 650", "Cipla")
        val withPipe = base.copy(productName = "Paracetamol | 650 | Special")
        val fpBase = CalculationHistoryImporter.computeContentFingerprint(base)
        val fpWithPipe = CalculationHistoryImporter.computeContentFingerprint(withPipe)

        assertTrue(fpBase != fpWithPipe)
        assertEquals(64, fpWithPipe.length) // valid SHA-256 hex string
    }

    @Test
    fun fingerprint_literalNullText_doesNotCollideWithNullField() {
        val baseWithNull = CalculationHistoryImporterTest.createSyntheticEntity(1L, "Crocin", "GSK").copy(
            profitMargin = null,
            markup = null,
            costPercentage = null
        )
        val baseWithLiteralNull = baseWithNull.copy(
            profitMargin = "<null>",
            markup = null,
            costPercentage = null
        )

        val fpNull = CalculationHistoryImporter.computeContentFingerprint(baseWithNull)
        val fpLiteralNull = CalculationHistoryImporter.computeContentFingerprint(baseWithLiteralNull)

        assertTrue(fpNull != fpLiteralNull)
    }

    @Test
    fun fingerprint_emptyStringVsNull_areStrictlyDistinguished() {
        val baseWithNull = CalculationHistoryImporterTest.createSyntheticEntity(1L, "Crocin", "GSK").copy(
            profitMargin = null
        )
        val baseWithEmpty = baseWithNull.copy(
            profitMargin = ""
        )

        val fpNull = CalculationHistoryImporter.computeContentFingerprint(baseWithNull)
        val fpEmpty = CalculationHistoryImporter.computeContentFingerprint(baseWithEmpty)

        assertTrue(fpNull != fpEmpty)
    }

    @Test
    fun fingerprint_unicodeAndMultibyteStrings_preservesExactEncoding() {
        val baseAscii = CalculationHistoryImporterTest.createSyntheticEntity(1L, "Paracetamol", "Cipla")
        val multibyte = baseAscii.copy(
            productName = "पैरासिटामोल 650 — பாராசிட்டமால் ₹ \uD83D\uDC8A",
            supplierName = "சிப்லா இந்தியா \u2122"
        )

        val fpAscii = CalculationHistoryImporter.computeContentFingerprint(baseAscii)
        val fpMultibyte = CalculationHistoryImporter.computeContentFingerprint(multibyte)

        assertTrue(fpAscii != fpMultibyte)
        // Determinism check: same entity generates identical fingerprint
        val fpMultibyteAgain = CalculationHistoryImporter.computeContentFingerprint(multibyte)
        assertEquals(fpMultibyte, fpMultibyteAgain)
    }

    @Test
    fun fingerprint_boundaryShiftCombination_doesNotCollide() {
        // Delimiter-based encoding could collide when delimiters exist in fields
        // TLV length-prefixed encoding prevents boundary shifting between fields
        val recordA = CalculationHistoryImporterTest.createSyntheticEntity(1L, "Dolo", "650|Cipla")
        val recordB = CalculationHistoryImporterTest.createSyntheticEntity(1L, "Dolo|650", "Cipla")

        val fpA = CalculationHistoryImporter.computeContentFingerprint(recordA)
        val fpB = CalculationHistoryImporter.computeContentFingerprint(recordB)

        assertTrue(fpA != fpB)
    }

    @Test
    fun fingerprint_decimalPrecisionAndScalePolicy_normalizesEquivalentDecimals() {
        val record1 = CalculationHistoryImporterTest.createSyntheticEntity(1L, "Aspirin", "Bayer").copy(
            ptr = "100.00",
            sellingPrice = "120.000",
            supplierDiscountPercent = "0.00"
        )
        val record2 = record1.copy(
            ptr = "100.0",
            sellingPrice = "120",
            supplierDiscountPercent = "0"
        )
        val recordDifferent = record1.copy(
            ptr = "100.01"
        )

        val fp1 = CalculationHistoryImporter.computeContentFingerprint(record1)
        val fp2 = CalculationHistoryImporter.computeContentFingerprint(record2)
        val fpDiff = CalculationHistoryImporter.computeContentFingerprint(recordDifferent)

        assertEquals("Equivalent decimals with different scale should produce the same fingerprint", fp1, fp2)
        assertTrue("Different numerical values must produce different fingerprints", fp1 != fpDiff)
    }

    @Test
    fun fingerprint_arbitraryPrecisionDecimals_preservedWithoutDoubleLoss() {
        val highPrecision = CalculationHistoryImporterTest.createSyntheticEntity(1L, "PrecisionDrug", "Pharma").copy(
            ptr = "1234567.890123456789123456",
            sellingPrice = "9876543.210987654321987654"
        )
        val highPrecisionPerturbed = highPrecision.copy(
            ptr = "1234567.890123456789123457" // last digit differed
        )

        val fp1 = CalculationHistoryImporter.computeContentFingerprint(highPrecision)
        val fp2 = CalculationHistoryImporter.computeContentFingerprint(highPrecisionPerturbed)

        assertTrue("High precision differences must not be lost to IEEE-754 float rounding", fp1 != fp2)
    }

    @Test
    fun fingerprint_fieldCompletenessAudit_failsIfNewEntityFieldIsAddedWithoutExplicitFingerprintDecision() {
        // Audited list of all 38 material calculation fields accounted for in CanonicalFingerprintEncoder tags 1..38
        val auditedFingerprintFields = setOf(
            "productName", "supplierName", "timestamp", "ptr", "purchaseTaxRate",
            "purchasePriceMode", "purchaseTaxClassification", "isInputTaxCreditEnabled",
            "sellingPrice", "sellingTaxRate", "sellingPriceMode", "quantity",
            "packName", "unitName", "unitsPerPack", "sellingQuantity",
            "costPerIndividualUnit", "revenuePerIndividualUnit", "profitPerIndividualUnit",
            "isSupplierDiscountEnabled", "isSupplierOfferEnabled", "offerBuyQuantity",
            "offerFreeQuantity", "freePacksReceived", "totalPacksReceived",
            "supplierDiscountPercent", "supplierDiscountAmount", "effectiveCostPerReceivedPack",
            "economicPurchaseCost", "netSalesRevenue", "profitPerUnit", "profitMargin",
            "markup", "costPercentage", "totalProfit", "totalPurchaseInvoiceTotal",
            "totalSellingCustomerTotal", "calculationBasis"
        )

        val entityDeclaredFields = CalculationEntity::class.java.declaredFields
            .filter { !java.lang.reflect.Modifier.isStatic(it.modifiers) && !it.isSynthetic }
            .map { it.name }
            .toSet()

        val contentFields = entityDeclaredFields - "id"

        assertEquals(
            "CalculationEntity must have exactly 38 content fields encoded in the TLV canonical fingerprint",
            38,
            contentFields.size
        )
        assertEquals(
            "Every field in CalculationEntity must be explicitly audited and accounted for in CanonicalFingerprintEncoder",
            auditedFingerprintFields,
            contentFields
        )
    }
}
