package com.margiq

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.margiq.ui.AppViewModelProvider
import com.margiq.ui.calculator.CalculatorScreen
import com.margiq.ui.calculator.CalculatorViewModel
import com.margiq.ui.history.HistoryScreen
import com.margiq.ui.history.HistoryViewModel
import com.margiq.ui.settings.SettingsScreen
import com.margiq.ui.settings.SettingsViewModel

@Composable
fun MainNavigation(
    calculatorViewModel: CalculatorViewModel = viewModel(factory = AppViewModelProvider.Factory),
    historyViewModel: HistoryViewModel = viewModel(factory = AppViewModelProvider.Factory),
    settingsViewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val backStack = rememberNavBackStack(CalculatorDestination)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<CalculatorDestination> {
                CalculatorScreen(
                    viewModel = calculatorViewModel,
                    onNavigateToHistory = { backStack.add(HistoryDestination) },
                    onNavigateToSettings = { backStack.add(SettingsDestination) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            entry<HistoryDestination> {
                HistoryScreen(
                    viewModel = historyViewModel,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onOpenCalculation = { entity ->
                        calculatorViewModel.loadCalculation(entity)
                        backStack.removeLastOrNull()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            entry<SettingsDestination> {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    )
}
