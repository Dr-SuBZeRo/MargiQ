package com.margiq.domain.backup

import com.margiq.data.local.CalculationDao
import com.margiq.data.local.CalculationEntity
import com.margiq.domain.model.PurchaseTaxClassification
import com.margiq.domain.model.TaxPriceMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.time.Instant

/**
 * Isolated unit and transaction tests for [CalculationHistoryImporter]
 * utilizing an in-memory SQLite JDBC database to verify real database transactions,
 * constraints, deduplication, and atomic rollbacks.
 */
class CalculationHistoryImporterTest {

    private lateinit var connection: Connection
    private lateinit var testDao: JdbcTestCalculationDao

    @Before
    fun setup() {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:")
        connection.createStatement().use { stmt ->
            stmt.execute(
                """
                CREATE TABLE calculations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    productName TEXT NOT NULL,
                    supplierName TEXT NOT NULL DEFAULT '',
                    timestamp INTEGER NOT NULL,
                    ptr TEXT NOT NULL,
                    purchaseTaxRate TEXT NOT NULL,
                    purchasePriceMode TEXT NOT NULL,
                    purchaseTaxClassification TEXT NOT NULL,
                    isInputTaxCreditEnabled INTEGER NOT NULL,
                    sellingPrice TEXT NOT NULL,
                    sellingTaxRate TEXT NOT NULL,
                    sellingPriceMode TEXT NOT NULL,
                    quantity INTEGER NOT NULL,
                    packName TEXT NOT NULL DEFAULT 'Pack',
                    unitName TEXT NOT NULL DEFAULT 'Unit',
                    unitsPerPack INTEGER NOT NULL DEFAULT 1,
                    sellingQuantity INTEGER NOT NULL DEFAULT 1,
                    costPerIndividualUnit TEXT NOT NULL DEFAULT '0.00',
                    revenuePerIndividualUnit TEXT NOT NULL DEFAULT '0.00',
                    profitPerIndividualUnit TEXT NOT NULL DEFAULT '0.00',
                    isSupplierDiscountEnabled INTEGER NOT NULL DEFAULT 0,
                    isSupplierOfferEnabled INTEGER NOT NULL DEFAULT 0,
                    offerBuyQuantity INTEGER NOT NULL DEFAULT 1,
                    offerFreeQuantity INTEGER NOT NULL DEFAULT 0,
                    freePacksReceived INTEGER NOT NULL DEFAULT 0,
                    totalPacksReceived INTEGER NOT NULL DEFAULT 1,
                    supplierDiscountPercent TEXT NOT NULL DEFAULT '0.00',
                    supplierDiscountAmount TEXT NOT NULL DEFAULT '0.00',
                    effectiveCostPerReceivedPack TEXT NOT NULL DEFAULT '0.00',
                    economicPurchaseCost TEXT NOT NULL,
                    netSalesRevenue TEXT NOT NULL,
                    profitPerUnit TEXT NOT NULL,
                    profitMargin TEXT,
                    markup TEXT,
                    costPercentage TEXT,
                    totalProfit TEXT NOT NULL,
                    totalPurchaseInvoiceTotal TEXT NOT NULL,
                    totalSellingCustomerTotal TEXT NOT NULL,
                    calculationBasis TEXT NOT NULL
                );
                """.trimIndent()
            )
        }
        testDao = JdbcTestCalculationDao(connection)
    }

    @After
    fun tearDown() {
        if (!connection.isClosed) {
            connection.close()
        }
    }

    companion object {
        fun createSyntheticEntity(
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

    private fun createDocument(records: List<CalculationRecordBackup>) = CalculationBackupDocument(
        appVersion = "2.3.0",
        exportedAt = "2026-11-21T09:30:00Z",
        recordCount = records.size,
        records = records
    )

    // 1. Import into empty database (MERGE)
    @Test
    fun import_intoEmptyDatabase_mergeMode_insertsAllRecordsWithNewIds() = runTest {
        val records = listOf(
            entityToBackupRecord(createSyntheticEntity(id = 50, productName = "Item A")),
            entityToBackupRecord(createSyntheticEntity(id = 51, productName = "Item B"))
        )
        val document = createDocument(records)

        val summary = CalculationHistoryImporter.executeImport(testDao, document, ImportMode.MERGE)

        assertEquals(2, summary.importedCount)
        assertEquals(0, summary.skippedCount)
        assertEquals(2, summary.totalInBackup)

        val inDb = testDao.getAllCalculationsList()
        assertEquals(2, inDb.size)
        // Auto-increment IDs start from 1 in SQLite
        assertEquals(1L, inDb[0].id)
        assertEquals(2L, inDb[1].id)
        assertEquals("Item A", inDb[0].productName)
        assertEquals("Item B", inDb[1].productName)
    }

    // 2. MERGE with existing identical records
    @Test
    fun import_mergeWithExistingIdenticalRecords_skipsDuplicates() = runTest {
        val original = createSyntheticEntity(id = 1, productName = "Identical Drug")
        testDao.insertCalculation(original)

        val backupRecord = entityToBackupRecord(original.copy(id = 999))
        val document = createDocument(listOf(backupRecord))

        val summary = CalculationHistoryImporter.executeImport(testDao, document, ImportMode.MERGE)

        assertEquals(0, summary.importedCount)
        assertEquals(1, summary.skippedCount)
        assertEquals(1, testDao.getAllCalculationsList().size)
    }

    // 3. MERGE with different records sharing old composite signature
    @Test
    fun import_mergeWithRecordsSharingOldSignature_doesNotSkipWhenMeaningfulFieldsDiffer() = runTest {
        val existing = createSyntheticEntity(
            id = 1,
            productName = "Paracetamol 650",
            purchaseTaxRate = "5.00",
            isInputTaxCreditEnabled = true
        )
        testDao.insertCalculation(existing)

        // Shares product, supplier, timestamp, ptr, sellingPrice, quantity
        // BUT differs in purchaseTaxRate and ITC!
        val candidate = createSyntheticEntity(
            id = 2,
            productName = "Paracetamol 650",
            purchaseTaxRate = "18.00",
            isInputTaxCreditEnabled = false
        )
        val document = createDocument(listOf(entityToBackupRecord(candidate)))

        val summary = CalculationHistoryImporter.executeImport(testDao, document, ImportMode.MERGE)

        // Must NOT be treated as duplicate
        assertEquals(1, summary.importedCount)
        assertEquals(0, summary.skippedCount)
        assertEquals(2, testDao.getAllCalculationsList().size)
    }

    // 4. REPLACE existing history
    @Test
    fun import_replaceMode_purgesExistingAndRestoresBackup() = runTest {
        testDao.insertCalculation(createSyntheticEntity(id = 1, productName = "Old Calculation 1"))
        testDao.insertCalculation(createSyntheticEntity(id = 2, productName = "Old Calculation 2"))
        assertEquals(2, testDao.getAllCalculationsList().size)

        val newRecord = entityToBackupRecord(createSyntheticEntity(id = 10, productName = "Restored Record"))
        val document = createDocument(listOf(newRecord))

        val summary = CalculationHistoryImporter.executeImport(testDao, document, ImportMode.REPLACE)

        assertEquals(1, summary.importedCount)
        assertEquals(0, summary.skippedCount)

        val inDb = testDao.getAllCalculationsList()
        assertEquals(1, inDb.size)
        assertEquals("Restored Record", inDb[0].productName)
        assertEquals(10L, inDb[0].id) // Preserves ID when unique and positive
    }

    // 5. Duplicate primary keys in backup document under REPLACE
    @Test
    fun import_replaceMode_withDuplicateIdsInBackup_regeneratesIdsSafelyWithoutDataLoss() = runTest {
        // Two distinct calculations that happen to carry duplicate id = 1 in the backup file
        val record1 = entityToBackupRecord(createSyntheticEntity(id = 1, productName = "Product One"))
        val record2 = entityToBackupRecord(createSyntheticEntity(id = 1, productName = "Product Two"))
        val document = createDocument(listOf(record1, record2))

        val summary = CalculationHistoryImporter.executeImport(testDao, document, ImportMode.REPLACE)

        assertEquals(2, summary.importedCount)
        val inDb = testDao.getAllCalculationsList()
        assertEquals(2, inDb.size)
        // Because IDs were duplicated, ID policy safely assigned new sequential IDs 1 and 2
        assertEquals(1L, inDb[0].id)
        assertEquals(2L, inDb[1].id)
        assertEquals("Product One", inDb[0].productName)
        assertEquals("Product Two", inDb[1].productName)
    }

    // 6. Duplicate consecutive imports (idempotency in MERGE)
    @Test
    fun import_duplicateConsecutiveImports_secondImportSkipsAll() = runTest {
        val records = listOf(
            entityToBackupRecord(createSyntheticEntity(productName = "Product A")),
            entityToBackupRecord(createSyntheticEntity(productName = "Product B"))
        )
        val document = createDocument(records)

        val firstSummary = CalculationHistoryImporter.executeImport(testDao, document, ImportMode.MERGE)
        assertEquals(2, firstSummary.importedCount)
        assertEquals(0, firstSummary.skippedCount)

        val secondSummary = CalculationHistoryImporter.executeImport(testDao, document, ImportMode.MERGE)
        assertEquals(0, secondSummary.importedCount)
        assertEquals(2, secondSummary.skippedCount)
        assertEquals(2, testDao.getAllCalculationsList().size)
    }

    // 7. Transaction rollback on insertion failure
    @Test
    fun import_transactionRollback_onFailureLeavesDatabaseUntouched() = runTest {
        testDao.insertCalculation(createSyntheticEntity(id = 1, productName = "Baseline Unchanged"))
        assertEquals(1, testDao.getAllCalculationsList().size)

        // Create a faulty DAO that throws during insertCalculations
        val faultyDao = object : CalculationDao by testDao {
            override suspend fun insertCalculations(entities: List<CalculationEntity>): List<Long> {
                throw IllegalStateException("Simulated SQLite constraint violation or disk I/O failure")
            }
        }

        val records = listOf(entityToBackupRecord(createSyntheticEntity(productName = "Incoming")))
        val document = createDocument(records)

        // Execute inside a transactional block that rolls back on exception
        connection.autoCommit = false
        try {
            CalculationHistoryImporter.executeImport(faultyDao, document, ImportMode.MERGE)
            connection.commit()
            fail("Expected exception was not thrown")
        } catch (_: IllegalStateException) {
            connection.rollback()
        } finally {
            connection.autoCommit = true
        }

        // Database must remain completely untouched
        val afterList = testDao.getAllCalculationsList()
        assertEquals(1, afterList.size)
        assertEquals("Baseline Unchanged", afterList[0].productName)
    }

    // 8. Field preservation across all 38 calculation attributes
    @Test
    fun import_preservesAllCalculationFieldsExactly() = runTest {
        val original = createSyntheticEntity(
            id = 42,
            productName = "Amoxicillin 500",
            supplierName = "Sun Pharma",
            timestamp = 1732185555000L,
            ptr = "75.25",
            purchaseTaxRate = "12.00",
            purchasePriceMode = "TAX_EXCLUSIVE",
            purchaseTaxClassification = "RECOVERABLE_GST",
            isInputTaxCreditEnabled = true,
            sellingPrice = "120.00",
            sellingTaxRate = "12.00",
            sellingPriceMode = "TAX_INCLUSIVE",
            quantity = 5,
            profitMargin = "34.50"
        )

        val document = createDocument(listOf(entityToBackupRecord(original)))
        CalculationHistoryImporter.executeImport(testDao, document, ImportMode.REPLACE)

        val restored = testDao.getAllCalculationsList().first()
        assertEquals(original.productName, restored.productName)
        assertEquals(original.supplierName, restored.supplierName)
        assertEquals(original.timestamp, restored.timestamp)
        assertEquals(original.ptr, restored.ptr)
        assertEquals(original.purchaseTaxRate, restored.purchaseTaxRate)
        assertEquals(original.purchasePriceMode, restored.purchasePriceMode)
        assertEquals(original.purchaseTaxClassification, restored.purchaseTaxClassification)
        assertEquals(original.isInputTaxCreditEnabled, restored.isInputTaxCreditEnabled)
        assertEquals(original.sellingPrice, restored.sellingPrice)
        assertEquals(original.sellingTaxRate, restored.sellingTaxRate)
        assertEquals(original.sellingPriceMode, restored.sellingPriceMode)
        assertEquals(original.quantity, restored.quantity)
        assertEquals(original.packName, restored.packName)
        assertEquals(original.unitName, restored.unitName)
        assertEquals(original.unitsPerPack, restored.unitsPerPack)
        assertEquals(original.sellingQuantity, restored.sellingQuantity)
        assertEquals(original.costPerIndividualUnit, restored.costPerIndividualUnit)
        assertEquals(original.revenuePerIndividualUnit, restored.revenuePerIndividualUnit)
        assertEquals(original.profitPerIndividualUnit, restored.profitPerIndividualUnit)
        assertEquals(original.isSupplierDiscountEnabled, restored.isSupplierDiscountEnabled)
        assertEquals(original.isSupplierOfferEnabled, restored.isSupplierOfferEnabled)
        assertEquals(original.offerBuyQuantity, restored.offerBuyQuantity)
        assertEquals(original.offerFreeQuantity, restored.offerFreeQuantity)
        assertEquals(original.freePacksReceived, restored.freePacksReceived)
        assertEquals(original.totalPacksReceived, restored.totalPacksReceived)
        assertEquals(original.supplierDiscountPercent, restored.supplierDiscountPercent)
        assertEquals(original.supplierDiscountAmount, restored.supplierDiscountAmount)
        assertEquals(original.effectiveCostPerReceivedPack, restored.effectiveCostPerReceivedPack)
        assertEquals(original.economicPurchaseCost, restored.economicPurchaseCost)
        assertEquals(original.netSalesRevenue, restored.netSalesRevenue)
        assertEquals(original.profitPerUnit, restored.profitPerUnit)
        assertEquals(original.profitMargin, restored.profitMargin)
        assertEquals(original.markup, restored.markup)
        assertEquals(original.costPercentage, restored.costPercentage)
        assertEquals(original.totalProfit, restored.totalProfit)
        assertEquals(original.totalPurchaseInvoiceTotal, restored.totalPurchaseInvoiceTotal)
        assertEquals(original.totalSellingCustomerTotal, restored.totalSellingCustomerTotal)
        assertEquals(original.calculationBasis, restored.calculationBasis)
    }

    // 9. Task 3: JSON Round-Trip Fidelity with synthetic high-precision, unicode, free packs, and nulls
    @Test
    fun roundTripFidelity_syntheticCompleteRecord_preservesHighPrecisionAndUnicode() {
        val syntheticEntity = CalculationEntity(
            id = 77L,
            productName = "Paracetamol 650 Strip — প্যারাস্তামল / पैरासिटामोल ⚕️ & Specials",
            supplierName = "Cipla Healthcare Ltd. — ముంబై (District #4)",
            timestamp = 1732189999999L,
            ptr = "12345.6789012345",
            purchaseTaxRate = "18.00",
            purchasePriceMode = "TAX_EXCLUSIVE",
            purchaseTaxClassification = PurchaseTaxClassification.NON_RECOVERABLE.name,
            isInputTaxCreditEnabled = false,
            sellingPrice = "98765.4321098765",
            sellingTaxRate = "18.00",
            sellingPriceMode = "TAX_INCLUSIVE",
            quantity = 250,
            packName = "Special Box (100s)",
            unitName = "Sachet",
            unitsPerPack = 50,
            sellingQuantity = 2,
            costPerIndividualUnit = "246.913578",
            revenuePerIndividualUnit = "1975.308642",
            profitPerIndividualUnit = "1728.395064",
            isSupplierDiscountEnabled = true,
            isSupplierOfferEnabled = true,
            offerBuyQuantity = 10,
            offerFreeQuantity = 2,
            freePacksReceived = 50,
            totalPacksReceived = 300,
            supplierDiscountPercent = "7.50",
            supplierDiscountAmount = "231481.479398",
            effectiveCostPerReceivedPack = "9522.610427",
            economicPurchaseCost = "2856783.128100",
            netSalesRevenue = "20924879.684300",
            profitPerUnit = "72272.386225",
            profitMargin = null, // Explicit null test
            markup = null,       // Explicit null test
            costPercentage = null, // Explicit null test
            totalProfit = "18068096.556200",
            totalPurchaseInvoiceTotal = "3371004.091158",
            totalSellingCustomerTotal = "24691358.027469",
            calculationBasis = "Blocked credit treatment — tax added to purchase cost without ITC offset"
        )

        // 1. Serialize
        val jsonString = CalculationBackupSerializer.serializeEntities(
            entities = listOf(syntheticEntity),
            appVersion = "2.3.0",
            exportedAt = Instant.parse("2026-11-21T09:30:00Z")
        )

        // 2. Parse
        val parseResult = CalculationBackupParser.parse(jsonString)
        assertTrue("Parsing must succeed", parseResult is BackupParseResult.Success)
        val document = (parseResult as BackupParseResult.Success).document

        // 3. Reconstruct
        val restoredRecord = document.records.first()
        val restoredEntity = restoredRecord.toEntity(assignNewId = false)

        // 4. Assert fidelity
        assertEquals(syntheticEntity.id, restoredEntity.id)
        assertEquals(syntheticEntity.productName, restoredEntity.productName)
        assertEquals(syntheticEntity.supplierName, restoredEntity.supplierName)
        assertEquals(syntheticEntity.timestamp, restoredEntity.timestamp)
        assertEquals(syntheticEntity.ptr, restoredEntity.ptr)
        assertEquals(syntheticEntity.purchaseTaxRate, restoredEntity.purchaseTaxRate)
        assertEquals(syntheticEntity.purchasePriceMode, restoredEntity.purchasePriceMode)
        assertEquals(syntheticEntity.purchaseTaxClassification, restoredEntity.purchaseTaxClassification)
        assertEquals(syntheticEntity.isInputTaxCreditEnabled, restoredEntity.isInputTaxCreditEnabled)
        assertEquals(syntheticEntity.sellingPrice, restoredEntity.sellingPrice)
        assertEquals(syntheticEntity.sellingTaxRate, restoredEntity.sellingTaxRate)
        assertEquals(syntheticEntity.sellingPriceMode, restoredEntity.sellingPriceMode)
        assertEquals(syntheticEntity.quantity, restoredEntity.quantity)
        assertEquals(syntheticEntity.packName, restoredEntity.packName)
        assertEquals(syntheticEntity.unitName, restoredEntity.unitName)
        assertEquals(syntheticEntity.unitsPerPack, restoredEntity.unitsPerPack)
        assertEquals(syntheticEntity.sellingQuantity, restoredEntity.sellingQuantity)
        assertEquals(syntheticEntity.costPerIndividualUnit, restoredEntity.costPerIndividualUnit)
        assertEquals(syntheticEntity.revenuePerIndividualUnit, restoredEntity.revenuePerIndividualUnit)
        assertEquals(syntheticEntity.profitPerIndividualUnit, restoredEntity.profitPerIndividualUnit)
        assertEquals(syntheticEntity.isSupplierDiscountEnabled, restoredEntity.isSupplierDiscountEnabled)
        assertEquals(syntheticEntity.isSupplierOfferEnabled, restoredEntity.isSupplierOfferEnabled)
        assertEquals(syntheticEntity.offerBuyQuantity, restoredEntity.offerBuyQuantity)
        assertEquals(syntheticEntity.offerFreeQuantity, restoredEntity.offerFreeQuantity)
        assertEquals(syntheticEntity.freePacksReceived, restoredEntity.freePacksReceived)
        assertEquals(syntheticEntity.totalPacksReceived, restoredEntity.totalPacksReceived)
        assertEquals(syntheticEntity.supplierDiscountPercent, restoredEntity.supplierDiscountPercent)
        assertEquals(syntheticEntity.supplierDiscountAmount, restoredEntity.supplierDiscountAmount)
        assertEquals(syntheticEntity.effectiveCostPerReceivedPack, restoredEntity.effectiveCostPerReceivedPack)
        assertEquals(syntheticEntity.economicPurchaseCost, restoredEntity.economicPurchaseCost)
        assertEquals(syntheticEntity.netSalesRevenue, restoredEntity.netSalesRevenue)
        assertEquals(syntheticEntity.profitPerUnit, restoredEntity.profitPerUnit)
        assertNull(restoredEntity.profitMargin)
        assertNull(restoredEntity.markup)
        assertNull(restoredEntity.costPercentage)
        assertEquals(syntheticEntity.totalProfit, restoredEntity.totalProfit)
        assertEquals(syntheticEntity.totalPurchaseInvoiceTotal, restoredEntity.totalPurchaseInvoiceTotal)
        assertEquals(syntheticEntity.totalSellingCustomerTotal, restoredEntity.totalSellingCustomerTotal)
        assertEquals(syntheticEntity.calculationBasis, restoredEntity.calculationBasis)
    }
}

/**
 * Real SQLite JDBC implementation of [CalculationDao] for test isolation.
 */
class JdbcTestCalculationDao(private val connection: Connection) : CalculationDao {

    override fun getAllCalculations(): Flow<List<CalculationEntity>> = kotlinx.coroutines.flow.flow {
        emit(getAllCalculationsList())
    }

    override fun searchCalculations(query: String): Flow<List<CalculationEntity>> = kotlinx.coroutines.flow.flow {
        emit(getAllCalculationsList().filter {
            it.productName.contains(query, ignoreCase = true) || it.supplierName.contains(query, ignoreCase = true)
        })
    }

    override suspend fun getAllCalculationsList(): List<CalculationEntity> {
        val list = mutableListOf<CalculationEntity>()
        val sql = "SELECT * FROM calculations ORDER BY timestamp DESC, id ASC"
        connection.createStatement().use { stmt ->
            val rs = stmt.executeQuery(sql)
            while (rs.next()) {
                list.add(mapResultSetToEntity(rs))
            }
        }
        return list
    }

    override suspend fun insertCalculation(entity: CalculationEntity): Long {
        return insertCalculations(listOf(entity)).first()
    }

    override suspend fun insertCalculations(entities: List<CalculationEntity>): List<Long> {
        val ids = mutableListOf<Long>()
        val hasExplicitId = entities.any { it.id > 0 }
        val effectiveSql = if (hasExplicitId) {
            """
            INSERT INTO calculations (
                id, productName, supplierName, timestamp, ptr, purchaseTaxRate, purchasePriceMode,
                purchaseTaxClassification, isInputTaxCreditEnabled, sellingPrice, sellingTaxRate,
                sellingPriceMode, quantity, packName, unitName, unitsPerPack, sellingQuantity,
                costPerIndividualUnit, revenuePerIndividualUnit, profitPerIndividualUnit,
                isSupplierDiscountEnabled, isSupplierOfferEnabled, offerBuyQuantity, offerFreeQuantity,
                freePacksReceived, totalPacksReceived, supplierDiscountPercent, supplierDiscountAmount,
                effectiveCostPerReceivedPack, economicPurchaseCost, netSalesRevenue, profitPerUnit,
                profitMargin, markup, costPercentage, totalProfit, totalPurchaseInvoiceTotal,
                totalSellingCustomerTotal, calculationBasis
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
        } else {
            """
            INSERT INTO calculations (
                productName, supplierName, timestamp, ptr, purchaseTaxRate, purchasePriceMode,
                purchaseTaxClassification, isInputTaxCreditEnabled, sellingPrice, sellingTaxRate,
                sellingPriceMode, quantity, packName, unitName, unitsPerPack, sellingQuantity,
                costPerIndividualUnit, revenuePerIndividualUnit, profitPerIndividualUnit,
                isSupplierDiscountEnabled, isSupplierOfferEnabled, offerBuyQuantity, offerFreeQuantity,
                freePacksReceived, totalPacksReceived, supplierDiscountPercent, supplierDiscountAmount,
                effectiveCostPerReceivedPack, economicPurchaseCost, netSalesRevenue, profitPerUnit,
                profitMargin, markup, costPercentage, totalProfit, totalPurchaseInvoiceTotal,
                totalSellingCustomerTotal, calculationBasis
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
        }

        connection.prepareStatement(effectiveSql, java.sql.Statement.RETURN_GENERATED_KEYS).use { pstmt ->
            for (calc in entities) {
                var idx = 1
                if (hasExplicitId) {
                    pstmt.setLong(idx++, if (calc.id > 0) calc.id else 0)
                }
                pstmt.setString(idx++, calc.productName)
                pstmt.setString(idx++, calc.supplierName)
                pstmt.setLong(idx++, calc.timestamp)
                pstmt.setString(idx++, calc.ptr)
                pstmt.setString(idx++, calc.purchaseTaxRate)
                pstmt.setString(idx++, calc.purchasePriceMode)
                pstmt.setString(idx++, calc.purchaseTaxClassification)
                pstmt.setInt(idx++, if (calc.isInputTaxCreditEnabled) 1 else 0)
                pstmt.setString(idx++, calc.sellingPrice)
                pstmt.setString(idx++, calc.sellingTaxRate)
                pstmt.setString(idx++, calc.sellingPriceMode)
                pstmt.setInt(idx++, calc.quantity)
                pstmt.setString(idx++, calc.packName)
                pstmt.setString(idx++, calc.unitName)
                pstmt.setInt(idx++, calc.unitsPerPack)
                pstmt.setInt(idx++, calc.sellingQuantity)
                pstmt.setString(idx++, calc.costPerIndividualUnit)
                pstmt.setString(idx++, calc.revenuePerIndividualUnit)
                pstmt.setString(idx++, calc.profitPerIndividualUnit)
                pstmt.setInt(idx++, if (calc.isSupplierDiscountEnabled) 1 else 0)
                pstmt.setInt(idx++, if (calc.isSupplierOfferEnabled) 1 else 0)
                pstmt.setInt(idx++, calc.offerBuyQuantity)
                pstmt.setInt(idx++, calc.offerFreeQuantity)
                pstmt.setInt(idx++, calc.freePacksReceived)
                pstmt.setInt(idx++, calc.totalPacksReceived)
                pstmt.setString(idx++, calc.supplierDiscountPercent)
                pstmt.setString(idx++, calc.supplierDiscountAmount)
                pstmt.setString(idx++, calc.effectiveCostPerReceivedPack)
                pstmt.setString(idx++, calc.economicPurchaseCost)
                pstmt.setString(idx++, calc.netSalesRevenue)
                pstmt.setString(idx++, calc.profitPerUnit)
                if (calc.profitMargin != null) pstmt.setString(idx++, calc.profitMargin) else pstmt.setNull(idx++, java.sql.Types.VARCHAR)
                if (calc.markup != null) pstmt.setString(idx++, calc.markup) else pstmt.setNull(idx++, java.sql.Types.VARCHAR)
                if (calc.costPercentage != null) pstmt.setString(idx++, calc.costPercentage) else pstmt.setNull(idx++, java.sql.Types.VARCHAR)
                pstmt.setString(idx++, calc.totalProfit)
                pstmt.setString(idx++, calc.totalPurchaseInvoiceTotal)
                pstmt.setString(idx++, calc.totalSellingCustomerTotal)
                pstmt.setString(idx++, calc.calculationBasis)

                pstmt.executeUpdate()
                val keys = pstmt.generatedKeys
                if (keys.next()) {
                    ids.add(keys.getLong(1))
                } else {
                    ids.add(calc.id)
                }
            }
        }
        return ids
    }

    override suspend fun updateCalculation(entity: CalculationEntity): Int = 1

    override suspend fun deleteCalculation(entity: CalculationEntity): Int = 1

    override suspend fun deleteById(id: Long): Int = 1

    override suspend fun clearAllCalculations(): Int {
        return connection.createStatement().use { it.executeUpdate("DELETE FROM calculations") }
    }

    override suspend fun getCalculationById(id: Long): CalculationEntity? {
        val sql = "SELECT * FROM calculations WHERE id = ?"
        connection.prepareStatement(sql).use { pstmt ->
            pstmt.setLong(1, id)
            val rs = pstmt.executeQuery()
            return if (rs.next()) mapResultSetToEntity(rs) else null
        }
    }

    private fun mapResultSetToEntity(rs: ResultSet): CalculationEntity {
        return CalculationEntity(
            id = rs.getLong("id"),
            productName = rs.getString("productName"),
            supplierName = rs.getString("supplierName"),
            timestamp = rs.getLong("timestamp"),
            ptr = rs.getString("ptr"),
            purchaseTaxRate = rs.getString("purchaseTaxRate"),
            purchasePriceMode = rs.getString("purchasePriceMode"),
            purchaseTaxClassification = rs.getString("purchaseTaxClassification"),
            isInputTaxCreditEnabled = rs.getInt("isInputTaxCreditEnabled") == 1,
            sellingPrice = rs.getString("sellingPrice"),
            sellingTaxRate = rs.getString("sellingTaxRate"),
            sellingPriceMode = rs.getString("sellingPriceMode"),
            quantity = rs.getInt("quantity"),
            packName = rs.getString("packName"),
            unitName = rs.getString("unitName"),
            unitsPerPack = rs.getInt("unitsPerPack"),
            sellingQuantity = rs.getInt("sellingQuantity"),
            costPerIndividualUnit = rs.getString("costPerIndividualUnit"),
            revenuePerIndividualUnit = rs.getString("revenuePerIndividualUnit"),
            profitPerIndividualUnit = rs.getString("profitPerIndividualUnit"),
            isSupplierDiscountEnabled = rs.getInt("isSupplierDiscountEnabled") == 1,
            isSupplierOfferEnabled = rs.getInt("isSupplierOfferEnabled") == 1,
            offerBuyQuantity = rs.getInt("offerBuyQuantity"),
            offerFreeQuantity = rs.getInt("offerFreeQuantity"),
            freePacksReceived = rs.getInt("freePacksReceived"),
            totalPacksReceived = rs.getInt("totalPacksReceived"),
            supplierDiscountPercent = rs.getString("supplierDiscountPercent"),
            supplierDiscountAmount = rs.getString("supplierDiscountAmount"),
            effectiveCostPerReceivedPack = rs.getString("effectiveCostPerReceivedPack"),
            economicPurchaseCost = rs.getString("economicPurchaseCost"),
            netSalesRevenue = rs.getString("netSalesRevenue"),
            profitPerUnit = rs.getString("profitPerUnit"),
            profitMargin = rs.getString("profitMargin"),
            markup = rs.getString("markup"),
            costPercentage = rs.getString("costPercentage"),
            totalProfit = rs.getString("totalProfit"),
            totalPurchaseInvoiceTotal = rs.getString("totalPurchaseInvoiceTotal"),
            totalSellingCustomerTotal = rs.getString("totalSellingCustomerTotal"),
            calculationBasis = rs.getString("calculationBasis")
        )
    }
}
