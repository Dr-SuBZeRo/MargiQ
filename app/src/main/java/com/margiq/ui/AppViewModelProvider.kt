package com.margiq.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.margiq.ProfitCalculatorApp
import com.margiq.ui.calculator.CalculatorViewModel
import com.margiq.ui.history.HistoryViewModel
import com.margiq.ui.settings.SettingsViewModel

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
