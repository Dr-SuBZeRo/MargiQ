package com.margiq

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.margiq.data.local.CalculationEntity
import com.margiq.ui.AppViewModelProvider
import com.margiq.ui.calculator.CalculatorScreen
import com.margiq.ui.calculator.CalculatorViewModel
import com.margiq.ui.compare.CompareScreen
import com.margiq.ui.compare.CompareViewModel
import com.margiq.ui.history.HistoryScreen
import com.margiq.ui.history.HistoryViewModel
import com.margiq.ui.settings.SettingsScreen
import com.margiq.ui.settings.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun MainNavigation(
    calculatorViewModel: CalculatorViewModel = viewModel(factory = AppViewModelProvider.Factory),
    historyViewModel: HistoryViewModel = viewModel(factory = AppViewModelProvider.Factory),
    settingsViewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    compareViewModel: CompareViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val backStack = rememberNavBackStack(CalculatorDestination)
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var historyPickerCallback by remember { mutableStateOf<((CalculationEntity) -> Unit)?>(null) }
    var historyPickerTitle by remember { mutableStateOf<String?>(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = "MargiQ Logo",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.size(12.dp))
                        Column {
                            Text(
                                text = "MargiQ",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "v2.1.1",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Know your cost. Own your margin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(12.dp))

                val currentKey = backStack.lastOrNull()

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Calculate, contentDescription = null) },
                    label = { Text("Calculator", fontWeight = FontWeight.SemiBold) },
                    selected = currentKey == CalculatorDestination,
                    onClick = {
                        scope.launch { drawerState.close() }
                        historyPickerCallback = null
                        historyPickerTitle = null
                        if (currentKey != CalculatorDestination) {
                            while (backStack.size > 1) { backStack.removeLastOrNull() }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.CompareArrows, contentDescription = null) },
                    label = { Text("Compare", fontWeight = FontWeight.SemiBold) },
                    selected = currentKey == CompareDestination,
                    onClick = {
                        scope.launch { drawerState.close() }
                        historyPickerCallback = null
                        historyPickerTitle = null
                        if (currentKey != CompareDestination) {
                            backStack.add(CompareDestination)
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = { Text("History", fontWeight = FontWeight.SemiBold) },
                    selected = currentKey == HistoryDestination && historyPickerCallback == null,
                    onClick = {
                        scope.launch { drawerState.close() }
                        historyPickerCallback = null
                        historyPickerTitle = null
                        if (currentKey != HistoryDestination) {
                            backStack.add(HistoryDestination)
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                    selected = currentKey == SettingsDestination,
                    onClick = {
                        scope.launch { drawerState.close() }
                        historyPickerCallback = null
                        historyPickerTitle = null
                        if (currentKey != SettingsDestination) {
                            backStack.add(SettingsDestination)
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        NavDisplay(
            backStack = backStack,
            onBack = {
                historyPickerCallback = null
                historyPickerTitle = null
                backStack.removeLastOrNull()
            },
            entryProvider = entryProvider {
                entry<CalculatorDestination> {
                    CalculatorScreen(
                        viewModel = calculatorViewModel,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onLoadFromHistory = {
                            historyPickerTitle = "Load Calculation into Calculator"
                            historyPickerCallback = { entity ->
                                calculatorViewModel.loadCalculation(entity)
                            }
                            backStack.add(HistoryDestination)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                entry<CompareDestination> {
                    CompareScreen(
                        viewModel = compareViewModel,
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onSelectHistoryForSideA = {
                            historyPickerTitle = "Select Saved Calculation for Side A"
                            historyPickerCallback = { entity ->
                                compareViewModel.loadCalculationIntoSideA(entity)
                            }
                            backStack.add(HistoryDestination)
                        },
                        onSelectHistoryForSideB = {
                            historyPickerTitle = "Select Saved Calculation for Side B"
                            historyPickerCallback = { entity ->
                                compareViewModel.loadCalculationIntoSideB(entity)
                            }
                            backStack.add(HistoryDestination)
                        },
                        onSelectHistoryForSupplierBase = {
                            historyPickerTitle = "Select Base Product from History"
                            historyPickerCallback = { entity ->
                                compareViewModel.loadCalculationIntoSupplierBase(entity)
                            }
                            backStack.add(HistoryDestination)
                        },
                        onSelectHistoryForSupplierOfferA = {
                            historyPickerTitle = "Select Supplier Offer A from History"
                            historyPickerCallback = { entity ->
                                compareViewModel.loadCalculationIntoSupplierOfferA(entity)
                            }
                            backStack.add(HistoryDestination)
                        },
                        onSelectHistoryForSupplierOfferB = {
                            historyPickerTitle = "Select Supplier Offer B from History"
                            historyPickerCallback = { entity ->
                                compareViewModel.loadCalculationIntoSupplierOfferB(entity)
                            }
                            backStack.add(HistoryDestination)
                        },
                        onLoadIntoCalculator = { input ->
                            calculatorViewModel.loadCalculationInput(input)
                            while (backStack.size > 1) { backStack.removeLastOrNull() }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                entry<HistoryDestination> {
                    val activePickerCallback = historyPickerCallback
                    val activeTitle = historyPickerTitle
                    HistoryScreen(
                        viewModel = historyViewModel,
                        onNavigateBack = {
                            historyPickerCallback = null
                            historyPickerTitle = null
                            backStack.removeLastOrNull()
                        },
                        selectionTitle = activeTitle,
                        onSelectCalculation = activePickerCallback?.let { callback ->
                            { entity ->
                                callback(entity)
                                historyPickerCallback = null
                                historyPickerTitle = null
                                backStack.removeLastOrNull()
                            }
                        },
                        onOpenCalculation = { entity ->
                            calculatorViewModel.loadCalculation(entity)
                            historyPickerCallback = null
                            historyPickerTitle = null
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
}
