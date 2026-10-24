package com.margiq.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [CalculationEntity::class], version = 3, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun calculationDao(): CalculationDao

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

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "profit_calculator_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
