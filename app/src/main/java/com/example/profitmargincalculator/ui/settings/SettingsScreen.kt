package com.example.profitmargincalculator.ui.settings

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.profitmargincalculator.R
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.profitmargincalculator.data.preferences.ThemePreference
import com.example.profitmargincalculator.domain.model.PurchaseTaxClassification
import com.example.profitmargincalculator.domain.model.TaxPriceMode
import com.example.profitmargincalculator.ui.components.CleanNumberInputField
import com.example.profitmargincalculator.ui.components.SegmentedTaxModeSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.dismissUserMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. APPEARANCE SECTION
            SettingsSectionCard(title = "Appearance", icon = Icons.Default.ColorLens) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ThemeOptionRow(
                        title = "Follow System",
                        subtitle = "Automatically match system light/dark theme",
                        icon = Icons.Default.BrightnessAuto,
                        isSelected = uiState.preferences.themePreference == ThemePreference.SYSTEM,
                        onClick = { viewModel.setThemePreference(ThemePreference.SYSTEM) }
                    )

                    ThemeOptionRow(
                        title = "Light Theme",
                        subtitle = "Clean white & slate aesthetic",
                        icon = Icons.Default.LightMode,
                        isSelected = uiState.preferences.themePreference == ThemePreference.LIGHT,
                        onClick = { viewModel.setThemePreference(ThemePreference.LIGHT) }
                    )

                    ThemeOptionRow(
                        title = "Dark Theme",
                        subtitle = "Deep slate & obsidian dark mode",
                        icon = Icons.Default.DarkMode,
                        isSelected = uiState.preferences.themePreference == ThemePreference.DARK,
                        onClick = { viewModel.setThemePreference(ThemePreference.DARK) }
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Material Dynamic Color",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "Use wallpaper-derived system colors on Android 12+",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = uiState.preferences.dynamicColor,
                                onCheckedChange = { viewModel.setDynamicColor(it) }
                            )
                        }
                    }
                }
            }

            // 2. CALCULATOR DEFAULTS SECTION
            SettingsSectionCard(title = "Calculator Defaults", icon = Icons.Default.Tune) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Customize the initial parameters loaded when resetting or creating calculations.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Default Purchase Tax Mode
                    Text(
                        text = "Default Purchase Tax Mode",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    SegmentedTaxModeSelector(
                        selectedMode = uiState.preferences.defaultPurchasePriceMode,
                        onModeSelected = { mode ->
                            viewModel.updateDefaults(
                                purchaseTaxRate = uiState.preferences.defaultPurchaseTaxRate,
                                sellingTaxRate = uiState.preferences.defaultSellingTaxRate,
                                purchasePriceMode = mode,
                                sellingPriceMode = uiState.preferences.defaultSellingPriceMode,
                                purchaseTaxClassification = uiState.preferences.defaultPurchaseTaxClassification,
                                inputTaxCreditEnabled = uiState.preferences.defaultInputTaxCreditEnabled,
                                quantity = uiState.preferences.defaultQuantity
                            )
                        }
                    )

                    // Default Selling Tax Mode
                    Text(
                        text = "Default Selling Tax Mode (MRP)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    SegmentedTaxModeSelector(
                        selectedMode = uiState.preferences.defaultSellingPriceMode,
                        onModeSelected = { mode ->
                            viewModel.updateDefaults(
                                purchaseTaxRate = uiState.preferences.defaultPurchaseTaxRate,
                                sellingTaxRate = uiState.preferences.defaultSellingTaxRate,
                                purchasePriceMode = uiState.preferences.defaultPurchasePriceMode,
                                sellingPriceMode = mode,
                                purchaseTaxClassification = uiState.preferences.defaultPurchaseTaxClassification,
                                inputTaxCreditEnabled = uiState.preferences.defaultInputTaxCreditEnabled,
                                quantity = uiState.preferences.defaultQuantity
                            )
                        }
                    )

                    // Reset Defaults button
                    OutlinedButton(
                        onClick = { viewModel.requestResetDefaults() },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text("Reset Calculator Defaults")
                    }
                }
            }

            // 3. CURRENCY & NUMBER FORMATTING
            SettingsSectionCard(title = "Regional & Number Formatting", icon = Icons.Default.Info) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsInfoRow(label = "Currency Symbol", value = "₹ (INR, Indian Rupee)")
                    SettingsInfoRow(label = "Grouping Style", value = "Indian Numbering (Lakhs & Crores)")
                    SettingsInfoRow(label = "Example", value = "₹1,23,456.78")
                    SettingsInfoRow(label = "Calculation Engine", value = "Deterministic BigDecimal (10 decimal places)")
                }
            }

            // 4. PRIVACY & RELIABILITY
            SettingsSectionCard(title = "Privacy & Local Storage", icon = Icons.Default.Lock) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "• 100% Offline: This app operates entirely on your device and requires no internet permission.\n" +
                                "• No User Tracking: No analytics, tracking IDs, accounts, or telemetry.\n" +
                                "• Private Room Database: Your calculation history is securely stored on internal storage and never transmitted.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 5. ABOUT APP
            SettingsSectionCard(title = "About MargiQ", icon = Icons.Default.Info) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "MargiQ",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Know your cost. Own your margin.",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Version ${com.example.profitmargincalculator.BuildConfig.VERSION_NAME} (Native Android with Jetpack Compose)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Created by SuBZeRo",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Disclaimer: MargiQ is a professional product-pricing and margin estimation tool designed for pharmacies, retailers, and distributors. Tax treatment and GST Input Tax Credit eligibility depend on applicable statutory provisions and dealer registration.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            // 6. CREATOR & VERSION FOOTER
            HorizontalDivider(
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_app_logo),
                    contentDescription = "MargiQ Logo",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Text(
                    text = "MargiQ",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Created by SuBZeRo",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Version ${com.example.profitmargincalculator.BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Reset Defaults Confirmation Dialog
    if (uiState.showResetDefaultsConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelResetDefaults() },
            title = { Text("Reset Calculator Defaults?") },
            text = { Text("This will restore default tax rates, modes, and quantity to factory values (5% tax, Tax-exclusive PTR, Tax-inclusive MRP). Previously saved calculations will not be modified.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmResetDefaults() }) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelResetDefaults() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            content()
        }
    }
}

@Composable
private fun ThemeOptionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 12.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
    }
}

@Composable
private fun SettingsInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
