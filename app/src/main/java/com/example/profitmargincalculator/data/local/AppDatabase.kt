package com.example.profitmargincalculator.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [CalculationEntity::class], version = 2, exportSchema = false)
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

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "profit_calculator_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
