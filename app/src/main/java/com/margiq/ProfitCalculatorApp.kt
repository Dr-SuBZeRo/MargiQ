package com.margiq

import android.app.Application
import com.margiq.data.local.AppDatabase
import com.margiq.data.preferences.PreferencesRepository
import com.margiq.data.preferences.dataStore
import com.margiq.data.repository.CalculationRepository

class ProfitCalculatorApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var calculationRepository: CalculationRepository
        private set

    lateinit var preferencesRepository: PreferencesRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        calculationRepository = CalculationRepository(database.calculationDao())
        preferencesRepository = PreferencesRepository(dataStore)
    }
}
