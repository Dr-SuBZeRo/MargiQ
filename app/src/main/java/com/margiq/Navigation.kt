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
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
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
import com.margiq.data.local.ProductEntity
import com.margiq.ui.AppViewModelProvider
import com.margiq.ui.calculator.CalculatorScreen
import com.margiq.ui.calculator.CalculatorViewModel
import com.margiq.ui.compare.CompareScreen
import com.margiq.ui.compare.CompareViewModel
import com.margiq.ui.history.HistoryScreen
import com.margiq.ui.history.HistoryViewModel
import com.margiq.ui.library.ProductLibraryScreen
import com.margiq.ui.library.ProductLibraryViewModel
import com.margiq.ui.settings.SettingsScreen
import com.margiq.ui.settings.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun MainNavigation(
    calculatorViewModel: CalculatorViewModel = viewModel(factory = AppViewModelProvider.Factory),
    historyViewModel: HistoryViewModel = viewModel(factory = AppViewModelProvider.Factory),
    settingsViewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    productLibraryViewModel: ProductLibraryViewModel = viewModel(factory = AppViewModelProvider.Factory),
    compareViewModel: CompareViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val backStack = rememberNavBackStack(CalculatorDestination)
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var productPickerCallback by remember { mutableStateOf<((ProductEntity) -> Unit)?>(null) }

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
                                text = "v2.1.0",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Know your cost. Own your margin.",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))

                val currentKey = backStack.lastOrNull() ?: CalculatorDestination

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Calculate, contentDescription = null) },
                    label = { Text("Calculator", fontWeight = FontWeight.SemiBold) },
                    selected = currentKey == CalculatorDestination,
                    onClick = {
                        scope.launch { drawerState.close() }
                        productPickerCallback = null
                        if (currentKey != CalculatorDestination) {
                            while (backStack.size > 1) { backStack.removeLastOrNull() }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = null) },
                    label = { Text("Product Library", fontWeight = FontWeight.SemiBold) },
                    selected = currentKey == ProductLibraryDestination,
                    onClick = {
                        scope.launch { drawerState.close() }
                        productPickerCallback = null
                        if (currentKey != ProductLibraryDestination) {
                            backStack.add(ProductLibraryDestination)
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.CompareArrows, contentDescription = null) },
                    label = { Text("Compare", fontWeight = FontWeight.SemiBold) },
                    selected = currentKey == CompareDestination,
                    onClick = {
                        scope.launch { drawerState.close() }
                        productPickerCallback = null
                        if (currentKey != CompareDestination) {
                            backStack.add(CompareDestination)
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = { Text("History", fontWeight = FontWeight.SemiBold) },
                    selected = currentKey == HistoryDestination,
                    onClick = {
                        scope.launch { drawerState.close() }
                        productPickerCallback = null
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
                        productPickerCallback = null
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
            onBack = { backStack.removeLastOrNull() },
            entryProvider = entryProvider {
                entry<CalculatorDestination> {
                    CalculatorScreen(
                        viewModel = calculatorViewModel,
                        onNavigateToHistory = { backStack.add(HistoryDestination) },
                        onNavigateToSettings = { backStack.add(SettingsDestination) },
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onNavigateToProductLibrary = {
                            productPickerCallback = { prod ->
                                calculatorViewModel.loadProduct(prod)
                            }
                            backStack.add(ProductLibraryDestination)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                entry<ProductLibraryDestination> {
                    ProductLibraryScreen(
                        viewModel = productLibraryViewModel,
                        onNavigateBack = {
                            productPickerCallback = null
                            backStack.removeLastOrNull()
                        },
                        onProductSelected = productPickerCallback?.let { callback ->
                            { prod ->
                                callback(prod)
                                productPickerCallback = null
                                backStack.removeLastOrNull()
                            }
                        },
                        onLoadIntoCalculator = { product ->
                            calculatorViewModel.loadProduct(product)
                            productPickerCallback = null
                            while (backStack.size > 1) { backStack.removeLastOrNull() }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                entry<CompareDestination> {
                    CompareScreen(
                        viewModel = compareViewModel,
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onOpenProductPickerForSideA = {
                            productPickerCallback = { prod ->
                                compareViewModel.loadProductIntoSideA(prod)
                            }
                            backStack.add(ProductLibraryDestination)
                        },
                        onOpenProductPickerForSideB = {
                            productPickerCallback = { prod ->
                                compareViewModel.loadProductIntoSideB(prod)
                            }
                            backStack.add(ProductLibraryDestination)
                        },
                        onOpenProductPickerForSupplierBase = {
                            productPickerCallback = { prod ->
                                compareViewModel.loadProductIntoSupplierBase(prod)
                            }
                            backStack.add(ProductLibraryDestination)
                        },
                        onLoadIntoCalculator = { input ->
                            calculatorViewModel.loadCalculationInput(input)
                            while (backStack.size > 1) { backStack.removeLastOrNull() }
                        },
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
}
