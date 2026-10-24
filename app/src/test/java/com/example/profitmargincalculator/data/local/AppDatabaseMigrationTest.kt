package com.example.profitmargincalculator.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy

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
}
