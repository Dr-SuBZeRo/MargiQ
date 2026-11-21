package com.margiq.domain.backup

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import com.margiq.data.local.AppDatabase
import com.margiq.data.local.CalculationDao
import com.margiq.data.local.CalculationEntity
import com.margiq.domain.model.PurchaseTaxClassification
import com.margiq.domain.model.TaxPriceMode
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProductionRoomDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: CalculationDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.calculationDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    private fun createSyntheticEntity(
        id: Long = 0,
        productName: String = "Augmentin 625 Duo",
        supplierName: String = "GlaxoSmithKline",
        timestamp: Long = 1732180000000L,
        ptr: String = "158.45",
        purchaseTaxRate: String = "12.00",
        purchasePriceMode: String = TaxPriceMode.TAX_EXCLUSIVE.name,
        purchaseTaxClassification: String = PurchaseTaxClassification.RECOVERABLE_GST.name,
        isInputTaxCreditEnabled: Boolean = true,
        sellingPrice: String = "201.71",
        sellingTaxRate: String = "12.00",
        sellingPriceMode: String = TaxPriceMode.TAX_INCLUSIVE.name,
        quantity: Int = 10,
        profitMargin: String? = "21.45"
    ): CalculationEntity {
        return CalculationEntity(
            id = id,
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
            markup = "27.30",
            costPercentage = "78.55",
            totalProfit = "256.09",
            totalPurchaseInvoiceTotal = "1730.28",
            totalSellingCustomerTotal = "2017.10",
            calculationBasis = "MargiQ Domain Calculation"
        )
    }

    private fun entityToBackupRecord(entity: CalculationEntity): CalculationRecordBackup {
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

    private fun createDocument(records: List<CalculationRecordBackup>): CalculationBackupDocument {
        return CalculationBackupDocument(
            format = BackupConstants.BACKUP_FORMAT_IDENTIFIER,
            schemaVersion = BackupConstants.CURRENT_SCHEMA_VERSION,
            appVersion = "2.3.0",
            exportedAt = "2026-11-21T08:30:00Z",
            recordCount = records.size,
            records = records
        )
    }

    @Test
    fun productionRoom_mergeIntoEmptyDatabase_insertsAllRecordsWithAutoGeneratedIds() = runTest {
        val backupRecords = listOf(
            entityToBackupRecord(createSyntheticEntity(id = 100, productName = "Product Alpha")),
            entityToBackupRecord(createSyntheticEntity(id = 101, productName = "Product Beta"))
        )
        val document = createDocument(backupRecords)

        val summary = CalculationHistoryImporter.importBackup(database, document, ImportMode.MERGE)

        assertEquals(2, summary.importedCount)
        assertEquals(0, summary.skippedCount)

        val stored = dao.getAllCalculationsList()
        assertEquals(2, stored.size)
        // Auto-generated sequential IDs starting from 1
        val ids = stored.map { it.id }.sorted()
        assertEquals(listOf(1L, 2L), ids)
    }

    @Test
    fun productionRoom_mergeWithIdenticalRecords_skipsDuplicatesSafely() = runTest {
        val original = createSyntheticEntity(id = 0, productName = "Identical Drug")
        val insertedId = dao.insertCalculation(original)
        assertTrue(insertedId > 0)

        val backupRecord = entityToBackupRecord(original.copy(id = 999))
        val document = createDocument(listOf(backupRecord))

        val summary = CalculationHistoryImporter.importBackup(database, document, ImportMode.MERGE)

        assertEquals(0, summary.importedCount)
        assertEquals(1, summary.skippedCount)

        val stored = dao.getAllCalculationsList()
        assertEquals(1, stored.size)
        assertEquals(insertedId, stored[0].id)
    }

    @Test
    fun productionRoom_mergeWithMeaningfulDifferences_doesNotSkip() = runTest {
        val existing = createSyntheticEntity(
            id = 0,
            productName = "Paracetamol",
            ptr = "50.00",
            purchaseTaxRate = "5.00"
        )
        dao.insertCalculation(existing)

        // Differing only in purchaseTaxRate
        val candidate = createSyntheticEntity(
            id = 55,
            productName = "Paracetamol",
            ptr = "50.00",
            purchaseTaxRate = "12.00"
        )
        val document = createDocument(listOf(entityToBackupRecord(candidate)))

        val summary = CalculationHistoryImporter.importBackup(database, document, ImportMode.MERGE)

        assertEquals(1, summary.importedCount)
        assertEquals(0, summary.skippedCount)

        val stored = dao.getAllCalculationsList()
        assertEquals(2, stored.size)
    }

    @Test
    fun productionRoom_replaceMode_purgesExistingAndRestoresBackup() = runTest {
        dao.insertCalculation(createSyntheticEntity(id = 0, productName = "PreExisting 1"))
        dao.insertCalculation(createSyntheticEntity(id = 0, productName = "PreExisting 2"))
        assertEquals(2, dao.getAllCalculationsList().size)

        val backupRecord = entityToBackupRecord(createSyntheticEntity(id = 42, productName = "Restored Drug"))
        val document = createDocument(listOf(backupRecord))

        val summary = CalculationHistoryImporter.importBackup(database, document, ImportMode.REPLACE)

        assertEquals(1, summary.importedCount)
        assertEquals(0, summary.skippedCount)

        val stored = dao.getAllCalculationsList()
        assertEquals(1, stored.size)
        assertEquals("Restored Drug", stored[0].productName)
        assertEquals(42L, stored[0].id)
    }

    @Test
    fun productionRoom_replaceWithDuplicateBackupIds_resequencesIdsSafely() = runTest {
        val record1 = entityToBackupRecord(createSyntheticEntity(id = 1, productName = "Product Alpha"))
        val record2 = entityToBackupRecord(createSyntheticEntity(id = 1, productName = "Product Beta")) // duplicate id in backup
        val document = createDocument(listOf(record1, record2))

        val summary = CalculationHistoryImporter.importBackup(database, document, ImportMode.REPLACE)

        assertEquals(2, summary.importedCount)
        val stored = dao.getAllCalculationsList()
        assertEquals(2, stored.size)
        val ids = stored.map { it.id }.distinct()
        assertEquals(2, ids.size) // both received distinct auto-generated IDs
    }

    @Test
    fun productionRoom_replaceWithZeroOrInvalidIds_resequencesIdsSafely() = runTest {
        val record1 = entityToBackupRecord(createSyntheticEntity(id = 0, productName = "Zero ID Drug"))
        val record2 = entityToBackupRecord(createSyntheticEntity(id = -5, productName = "Negative ID Drug"))
        val document = createDocument(listOf(record1, record2))

        val summary = CalculationHistoryImporter.importBackup(database, document, ImportMode.REPLACE)

        assertEquals(2, summary.importedCount)
        val stored = dao.getAllCalculationsList()
        assertEquals(2, stored.size)
        assertTrue(stored.all { it.id > 0 })
    }

    @Test
    fun productionRoom_repeatedImports_secondImportSkipsAll() = runTest {
        val records = listOf(
            entityToBackupRecord(createSyntheticEntity(productName = "Batch A")),
            entityToBackupRecord(createSyntheticEntity(productName = "Batch B"))
        )
        val document = createDocument(records)

        val firstSummary = CalculationHistoryImporter.importBackup(database, document, ImportMode.MERGE)
        assertEquals(2, firstSummary.importedCount)
        assertEquals(0, firstSummary.skippedCount)

        val secondSummary = CalculationHistoryImporter.importBackup(database, document, ImportMode.MERGE)
        assertEquals(0, secondSummary.importedCount)
        assertEquals(2, secondSummary.skippedCount)

        assertEquals(2, dao.getAllCalculationsList().size)
    }

    @Test
    fun productionRoom_transactionRollback_leavesDatabaseUntouchedOnFailure() = runTest {
        dao.insertCalculation(createSyntheticEntity(id = 0, productName = "Baseline Record"))
        assertEquals(1, dao.getAllCalculationsList().size)

        var failed = false
        try {
            // Simulate a failure inside the database transaction
            database.withTransaction {
                dao.insertCalculation(createSyntheticEntity(id = 0, productName = "Mid Transaction"))
                throw IOException("Simulated I/O failure during multi-step restore")
            }
        } catch (_: IOException) {
            failed = true
        }

        assertTrue(failed)
        val stored = dao.getAllCalculationsList()
        assertEquals(1, stored.size)
        assertEquals("Baseline Record", stored[0].productName)
    }

    @Test
    fun productionRoom_preservesEveryCalculationFieldExactly() = runTest {
        val original = createSyntheticEntity(
            id = 77L,
            productName = "Precision Antibiotic 500mg",
            supplierName = "Sun Pharma Ltd",
            ptr = "123.45",
            purchaseTaxRate = "18.00",
            purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE.name,
            purchaseTaxClassification = PurchaseTaxClassification.RECOVERABLE_GST.name,
            isInputTaxCreditEnabled = true,
            sellingPrice = "165.80",
            sellingTaxRate = "18.00",
            sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE.name,
            quantity = 25,
            profitMargin = "19.85"
        )
        val document = createDocument(listOf(entityToBackupRecord(original)))

        CalculationHistoryImporter.importBackup(database, document, ImportMode.REPLACE)

        val stored = dao.getAllCalculationsList().first()
        assertEquals(original.productName, stored.productName)
        assertEquals(original.supplierName, stored.supplierName)
        assertEquals(original.ptr, stored.ptr)
        assertEquals(original.purchaseTaxRate, stored.purchaseTaxRate)
        assertEquals(original.purchasePriceMode, stored.purchasePriceMode)
        assertEquals(original.purchaseTaxClassification, stored.purchaseTaxClassification)
        assertEquals(original.isInputTaxCreditEnabled, stored.isInputTaxCreditEnabled)
        assertEquals(original.sellingPrice, stored.sellingPrice)
        assertEquals(original.sellingTaxRate, stored.sellingTaxRate)
        assertEquals(original.sellingPriceMode, stored.sellingPriceMode)
        assertEquals(original.quantity, stored.quantity)
        assertEquals(original.profitMargin, stored.profitMargin)
        assertEquals(original.costPerIndividualUnit, stored.costPerIndividualUnit)
        assertEquals(original.revenuePerIndividualUnit, stored.revenuePerIndividualUnit)
        assertEquals(original.totalProfit, stored.totalProfit)
    }

    @Test
    fun productionRoom_futureAutoGeneratedIds_allocateStrictlyGreaterThanImportedIds() = runTest {
        // 1. MERGE mode test
        val backupRecords = listOf(
            entityToBackupRecord(createSyntheticEntity(id = 50, productName = "Merged 1")),
            entityToBackupRecord(createSyntheticEntity(id = 51, productName = "Merged 2"))
        )
        CalculationHistoryImporter.importBackup(database, createDocument(backupRecords), ImportMode.MERGE)

        val maxMergedId = dao.getAllCalculationsList().maxOf { it.id }

        // 2. User creates a new calculation after the import
        val newCalculation = createSyntheticEntity(id = 0, productName = "Fresh User Calculation")
        val newId = dao.insertCalculation(newCalculation)

        assertTrue(
            "Future auto-generated ID ($newId) must be strictly greater than max imported ID ($maxMergedId)",
            newId > maxMergedId
        )
    }

    @Test
    fun productionRoom_databaseRemainsUsableAfterFailedImport() = runTest {
        dao.insertCalculation(createSyntheticEntity(id = 0, productName = "Pre Existing"))

        try {
            database.withTransaction {
                dao.insertCalculation(createSyntheticEntity(id = 0, productName = "Will Fail"))
                throw RuntimeException("Forced failure")
            }
        } catch (_: RuntimeException) {
            // expected
        }

        // Verify subsequent normal database operations succeed
        val subsequentId = dao.insertCalculation(createSyntheticEntity(id = 0, productName = "Subsequent Success"))
        assertTrue(subsequentId > 0)
        assertEquals(2, dao.getAllCalculationsList().size)
    }
}
