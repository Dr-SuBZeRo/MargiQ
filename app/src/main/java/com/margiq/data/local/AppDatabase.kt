package com.margiq.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [CalculationEntity::class, ProductEntity::class], version = 5, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun calculationDao(): CalculationDao
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE calculations ADD COLUMN packName TEXT NOT NULL DEFAULT 'Pack'")
                db.execSQL("ALTER TABLE calculations ADD COLUMN unitName TEXT NOT NULL DEFAULT 'Unit'")
                db.execSQL("ALTER TABLE calculations ADD COLUMN unitsPerPack INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE calculations ADD COLUMN sellingQuantity INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE calculations ADD COLUMN costPerIndividualUnit TEXT NOT NULL DEFAULT '0.00'")
                db.execSQL("ALTER TABLE calculations ADD COLUMN revenuePerIndividualUnit TEXT NOT NULL DEFAULT '0.00'")
                db.execSQL("ALTER TABLE calculations ADD COLUMN profitPerIndividualUnit TEXT NOT NULL DEFAULT '0.00'")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE calculations ADD COLUMN isSupplierOfferEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE calculations ADD COLUMN offerBuyQuantity INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE calculations ADD COLUMN offerFreeQuantity INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE calculations ADD COLUMN freePacksReceived INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE calculations ADD COLUMN totalPacksReceived INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE calculations ADD COLUMN supplierDiscountPercent TEXT NOT NULL DEFAULT '0.00'")
                db.execSQL("ALTER TABLE calculations ADD COLUMN supplierDiscountAmount TEXT NOT NULL DEFAULT '0.00'")
                db.execSQL("ALTER TABLE calculations ADD COLUMN effectiveCostPerReceivedPack TEXT NOT NULL DEFAULT '0.00'")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE calculations ADD COLUMN supplierName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE calculations ADD COLUMN isSupplierDiscountEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE calculations SET isSupplierDiscountEnabled = 1 WHERE supplierDiscountPercent != '0.00' AND supplierDiscountPercent != '0'")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `products` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `productName` TEXT NOT NULL,
                        `supplierName` TEXT NOT NULL DEFAULT '',
                        `ptr` TEXT NOT NULL DEFAULT '0.00',
                        `purchaseTaxRate` TEXT NOT NULL DEFAULT '5.00',
                        `purchasePriceMode` TEXT NOT NULL DEFAULT 'TAX_EXCLUSIVE',
                        `purchaseTaxClassification` TEXT NOT NULL DEFAULT 'RECOVERABLE_GST',
                        `isInputTaxCreditEnabled` INTEGER NOT NULL DEFAULT 1,
                        `sellingPrice` TEXT NOT NULL DEFAULT '0.00',
                        `sellingTaxRate` TEXT NOT NULL DEFAULT '5.00',
                        `sellingPriceMode` TEXT NOT NULL DEFAULT 'TAX_INCLUSIVE',
                        `quantity` INTEGER NOT NULL DEFAULT 1,
                        `packName` TEXT NOT NULL DEFAULT 'Pack',
                        `unitName` TEXT NOT NULL DEFAULT 'Unit',
                        `unitsPerPack` INTEGER NOT NULL DEFAULT 1,
                        `sellingQuantity` INTEGER NOT NULL DEFAULT 1,
                        `isSupplierDiscountEnabled` INTEGER NOT NULL DEFAULT 0,
                        `supplierDiscountPercent` TEXT NOT NULL DEFAULT '0.00',
                        `isSupplierOfferEnabled` INTEGER NOT NULL DEFAULT 0,
                        `offerBuyQuantity` INTEGER NOT NULL DEFAULT 1,
                        `offerFreeQuantity` INTEGER NOT NULL DEFAULT 0,
                        `createdAt` INTEGER NOT NULL DEFAULT 0,
                        `updatedAt` INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "profit_calculator_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
