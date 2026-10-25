package com.margiq.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import com.margiq.domain.calculation.ProfitCalculationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy
import java.math.BigDecimal
import java.sql.DriverManager

class AppDatabaseMigrationTest {

    @Test
    fun testMigration1To2ExecutesAllAlterTableStatements() {
        val executedStatements = mutableListOf<String>()

        val fakeDb = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java)
        ) { _, method, args ->
            if (method.name == "execSQL" && args != null && args.isNotEmpty()) {
                executedStatements.add(args[0] as String)
            }
            null
        } as SupportSQLiteDatabase

        assertEquals(1, AppDatabase.MIGRATION_1_2.startVersion)
        assertEquals(2, AppDatabase.MIGRATION_1_2.endVersion)

        AppDatabase.MIGRATION_1_2.migrate(fakeDb)

        assertEquals(7, executedStatements.size)
        assertTrue(executedStatements.any { it.contains("packName") && it.contains("DEFAULT 'Pack'") })
        assertTrue(executedStatements.any { it.contains("unitName") && it.contains("DEFAULT 'Unit'") })
        assertTrue(executedStatements.any { it.contains("unitsPerPack") && it.contains("DEFAULT 1") })
        assertTrue(executedStatements.any { it.contains("sellingQuantity") && it.contains("DEFAULT 1") })
        assertTrue(executedStatements.any { it.contains("costPerIndividualUnit") && it.contains("DEFAULT '0.00'") })
        assertTrue(executedStatements.any { it.contains("revenuePerIndividualUnit") && it.contains("DEFAULT '0.00'") })
        assertTrue(executedStatements.any { it.contains("profitPerIndividualUnit") && it.contains("DEFAULT '0.00'") })
    }

    @Test
    fun testMigration2To3ExecutesAllAlterTableStatements() {
        val executedStatements = mutableListOf<String>()

        val fakeDb = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java)
        ) { _, method, args ->
            if (method.name == "execSQL" && args != null && args.isNotEmpty()) {
                executedStatements.add(args[0] as String)
            }
            null
        } as SupportSQLiteDatabase

        assertEquals(2, AppDatabase.MIGRATION_2_3.startVersion)
        assertEquals(3, AppDatabase.MIGRATION_2_3.endVersion)

        AppDatabase.MIGRATION_2_3.migrate(fakeDb)

        assertEquals(8, executedStatements.size)
        assertTrue(executedStatements.any { it.contains("isSupplierOfferEnabled") && it.contains("DEFAULT 0") })
        assertTrue(executedStatements.any { it.contains("offerBuyQuantity") && it.contains("DEFAULT 1") })
        assertTrue(executedStatements.any { it.contains("offerFreeQuantity") && it.contains("DEFAULT 0") })
        assertTrue(executedStatements.any { it.contains("freePacksReceived") && it.contains("DEFAULT 0") })
        assertTrue(executedStatements.any { it.contains("totalPacksReceived") && it.contains("DEFAULT 1") })
        assertTrue(executedStatements.any { it.contains("supplierDiscountPercent") && it.contains("DEFAULT '0.00'") })
        assertTrue(executedStatements.any { it.contains("supplierDiscountAmount") && it.contains("DEFAULT '0.00'") })
        assertTrue(executedStatements.any { it.contains("effectiveCostPerReceivedPack") && it.contains("DEFAULT '0.00'") })
    }

    @Test
    fun testMigration3To4ExecutesAllAlterTableStatements() {
        val executedStatements = mutableListOf<String>()

        val fakeDb = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java)
        ) { _, method, args ->
            if (method.name == "execSQL" && args != null && args.isNotEmpty()) {
                executedStatements.add(args[0] as String)
            }
            null
        } as SupportSQLiteDatabase

        assertEquals(3, AppDatabase.MIGRATION_3_4.startVersion)
        assertEquals(4, AppDatabase.MIGRATION_3_4.endVersion)

        AppDatabase.MIGRATION_3_4.migrate(fakeDb)

        assertEquals(3, executedStatements.size)
        assertTrue(executedStatements.any { it.contains("supplierName") && it.contains("DEFAULT ''") })
        assertTrue(executedStatements.any { it.contains("isSupplierDiscountEnabled") && it.contains("DEFAULT 0") })
        assertTrue(executedStatements.any { it.contains("UPDATE calculations") && it.contains("isSupplierDiscountEnabled = 1") })
    }

    @Test
    fun testMigration1To2To3To4OnRealSqlitePreservesExistingRecordsAndReopensCorrectly() {
        val connection = DriverManager.getConnection("jdbc:sqlite::memory:")
        val stmt = connection.createStatement()

        // 1. Create real version-1 schema
        stmt.execute("""
            CREATE TABLE calculations (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                productName TEXT NOT NULL,
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
        """.trimIndent())

        // 2. Insert real version-1 record
        stmt.execute("""
            INSERT INTO calculations (
                id, productName, timestamp, ptr, purchaseTaxRate, purchasePriceMode,
                purchaseTaxClassification, isInputTaxCreditEnabled, sellingPrice,
                sellingTaxRate, sellingPriceMode, quantity, economicPurchaseCost,
                netSalesRevenue, profitPerUnit, profitMargin, markup, costPercentage,
                totalProfit, totalPurchaseInvoiceTotal, totalSellingCustomerTotal, calculationBasis
            ) VALUES (
                101, 'Paracetamol 650 Strip', 1700000000000, '48.00', '5.00', 'TAX_EXCLUSIVE',
                'RECOVERABLE_GST', 1, '135.00', '5.00', 'TAX_INCLUSIVE', 5, '48.00',
                '128.57', '80.57', '62.67', '167.86', '37.33', '402.86', '252.00', '675.00',
                'Profit after excluding eligible GST credit'
            );
        """.trimIndent())

        val supportDb = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java)
        ) { _, method, args ->
            if (method.name == "execSQL" && args != null && args.isNotEmpty()) {
                val sql = args[0] as String
                connection.createStatement().use { it.execute(sql) }
            }
            null
        } as SupportSQLiteDatabase

        // 3. Execute Migration 1 -> 2
        AppDatabase.MIGRATION_1_2.migrate(supportDb)

        // 4. Execute Migration 2 -> 3
        AppDatabase.MIGRATION_2_3.migrate(supportDb)

        // 5. Execute Migration 3 -> 4
        AppDatabase.MIGRATION_3_4.migrate(supportDb)

        // 6. Query migrated table and assert fields
        val rs = stmt.executeQuery("SELECT * FROM calculations WHERE id = 101")
        assertTrue("Record with id 101 must exist after migration", rs.next())

        // Assert existing v1 fields preserved intact
        assertEquals(101L, rs.getLong("id"))
        assertEquals("Paracetamol 650 Strip", rs.getString("productName"))
        assertEquals(1700000000000L, rs.getLong("timestamp"))
        assertEquals("48.00", rs.getString("ptr"))
        assertEquals(5, rs.getInt("quantity"))
        assertEquals("135.00", rs.getString("sellingPrice"))
        assertEquals("80.57", rs.getString("profitPerUnit"))
        assertEquals("402.86", rs.getString("totalProfit"))

        // Assert v2 columns have default values
        assertEquals("Pack", rs.getString("packName"))
        assertEquals("Unit", rs.getString("unitName"))
        assertEquals(1, rs.getInt("unitsPerPack"))
        assertEquals(1, rs.getInt("sellingQuantity"))
        assertEquals("0.00", rs.getString("costPerIndividualUnit"))
        assertEquals("0.00", rs.getString("revenuePerIndividualUnit"))
        assertEquals("0.00", rs.getString("profitPerIndividualUnit"))

        // Assert v3 columns have safe default values
        assertEquals(0, rs.getInt("isSupplierOfferEnabled"))
        assertEquals(1, rs.getInt("offerBuyQuantity"))
        assertEquals(0, rs.getInt("offerFreeQuantity"))
        assertEquals(0, rs.getInt("freePacksReceived"))
        assertEquals(1, rs.getInt("totalPacksReceived"))
        assertEquals("0.00", rs.getString("supplierDiscountPercent"))
        assertEquals("0.00", rs.getString("supplierDiscountAmount"))
        assertEquals("0.00", rs.getString("effectiveCostPerReceivedPack"))

        // Assert v4 columns have safe default values
        assertEquals("", rs.getString("supplierName"))
        assertEquals(0, rs.getInt("isSupplierDiscountEnabled"))

        // 7. Construct CalculationEntity from migrated database row and verify reopening
        val entity = CalculationEntity(
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

        val reopenedInput = entity.toCalculationInput()
        assertEquals("Paracetamol 650 Strip", reopenedInput.productName)
        assertEquals("", reopenedInput.supplierName)
        assertEquals(BigDecimal("48.00"), reopenedInput.ptr)
        assertEquals(5, reopenedInput.quantity)
        assertEquals(1, reopenedInput.unitsPerPack)
        assertFalse(reopenedInput.isSupplierDiscountEnabled)
        assertFalse(reopenedInput.isSupplierOfferEnabled)
        assertEquals(0, reopenedInput.supplierDiscountPercent.compareTo(BigDecimal.ZERO))

        val recalculated = ProfitCalculationEngine.calculate(reopenedInput)
        assertTrue(recalculated.isValid)
        assertEquals(BigDecimal("80.57"), recalculated.roundedProfitPerUnit)

        rs.close()
        stmt.close()
        connection.close()
    }
}
