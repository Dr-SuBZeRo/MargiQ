package com.example.profitmargincalculator

import android.app.Application
import com.example.profitmargincalculator.data.local.AppDatabase
import com.example.profitmargincalculator.data.preferences.PreferencesRepository
import com.example.profitmargincalculator.data.preferences.dataStore
import com.example.profitmargincalculator.data.repository.CalculationRepository

class ProfitCalculatorApp : Application() {

    lateinit var calculationRepository: CalculationRepository
        private set

    lateinit var preferencesRepository: PreferencesRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getDatabase(this)
        calculationRepository = CalculationRepository(database.calculationDao())
        preferencesRepository = PreferencesRepository(dataStore)
    }
}
