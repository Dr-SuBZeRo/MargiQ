package com.margiq.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.margiq.domain.backup.BackupConstants
import com.margiq.domain.backup.CalculationBackupDocument
import com.margiq.domain.backup.CalculationHistoryImporter
import com.margiq.domain.backup.CalculationRecordBackup
import com.margiq.domain.backup.ImportMode
import com.margiq.domain.model.PurchaseTaxClassification
import com.margiq.domain.model.TaxPriceMode
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Instrumented Room integration test executing on an Android runtime (emulator or physical device).
 * Verifies that the production [AppDatabase] and [CalculationDao] properly execute [CalculationHistoryImporter]
 * operations against Android framework SQLite.
 *
 * To run:
 * ```powershell
 * ./gradlew connectedDebugAndroidTest
 * ```
 */
@RunWith(AndroidJUnit4::class)
class RoomImporterInstrumentedTest {

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
    fun instrumented_mergeIntoEmptyDatabase_insertsAllRecordsWithAutoGeneratedIds() = runBlocking {
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
        val ids = stored.map { it.id }.sorted()
        assertEquals(listOf(1L, 2L), ids)
    }

    @Test
    fun instrumented_replaceMode_purgesExistingAndRestoresBackup() = runBlocking {
        dao.insertCalculation(createSyntheticEntity(id = 0, productName = "PreExisting 1"))
        assertEquals(1, dao.getAllCalculationsList().size)

        val backupRecord = entityToBackupRecord(createSyntheticEntity(id = 42, productName = "Restored Drug"))
        val document = createDocument(listOf(backupRecord))

        val summary = CalculationHistoryImporter.importBackup(database, document, ImportMode.REPLACE)

        assertEquals(1, summary.importedCount)
        val stored = dao.getAllCalculationsList()
        assertEquals(1, stored.size)
        assertEquals("Restored Drug", stored[0].productName)
        assertEquals(42L, stored[0].id)
    }

    @Test
    fun instrumented_transactionRollback_leavesDatabaseUntouchedOnFailure() = runBlocking {
        dao.insertCalculation(createSyntheticEntity(id = 0, productName = "Baseline Record"))
        assertEquals(1, dao.getAllCalculationsList().size)

        var failed = false
        try {
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
    fun instrumented_futureAutoGeneratedIds_allocateStrictlyGreaterThanImportedIds() = runBlocking {
        val backupRecords = listOf(
            entityToBackupRecord(createSyntheticEntity(id = 50, productName = "Merged 1")),
            entityToBackupRecord(createSyntheticEntity(id = 51, productName = "Merged 2"))
        )
        CalculationHistoryImporter.importBackup(database, createDocument(backupRecords), ImportMode.MERGE)

        val maxMergedId = dao.getAllCalculationsList().maxOf { it.id }
        val newId = dao.insertCalculation(createSyntheticEntity(id = 0, productName = "Fresh User Calculation"))

        assertTrue("Future auto-generated ID ($newId) must be strictly greater than max imported ID ($maxMergedId)", newId > maxMergedId)
    }
}
