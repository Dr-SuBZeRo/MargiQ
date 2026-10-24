package com.example.profitmargincalculator.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.profitmargincalculator.ProfitCalculatorApp
import com.example.profitmargincalculator.ui.calculator.CalculatorViewModel
import com.example.profitmargincalculator.ui.history.HistoryViewModel
import com.example.profitmargincalculator.ui.settings.SettingsViewModel

object AppViewModelProvider {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val app = profitCalculatorApp()
            CalculatorViewModel(
                calculationRepository = app.calculationRepository,
                preferencesRepository = app.preferencesRepository
            )
        }
        initializer {
            val app = profitCalculatorApp()
            HistoryViewModel(
                calculationRepository = app.calculationRepository
            )
        }
        initializer {
            val app = profitCalculatorApp()
            SettingsViewModel(
                preferencesRepository = app.preferencesRepository
            )
        }
    }
}

fun CreationExtras.profitCalculatorApp(): ProfitCalculatorApp {
    return (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ProfitCalculatorApp)
}
