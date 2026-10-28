package com.margiq.ui.settings

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
import com.margiq.R
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.margiq.data.preferences.ThemePreference
import com.margiq.domain.model.PurchaseTaxClassification
import com.margiq.domain.model.TaxPriceMode
import com.margiq.ui.components.CleanNumberInputField
import com.margiq.ui.components.SegmentedTaxModeSelector
import com.margiq.util.AppVersionHelper
import com.margiq.util.UrlLauncher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val versionInfo = remember { AppVersionHelper.getAppVersionInfo(context) }
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

            // 3. GITHUB & UPDATES
            SettingsSectionCard(title = "GitHub & Updates", icon = Icons.Default.Code) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Dr-SuBZeRo/MargiQ",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Source code, releases and project updates",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Installed Version: ${versionInfo.displayVersion} (Build ${versionInfo.versionCode})",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { UrlLauncher.openUrl(context, UrlLauncher.GITHUB_REPO_URL) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp).padding(end = 4.dp)
                            )
                            Text("Open GitHub", maxLines = 1)
                        }

                        Button(
                            onClick = { viewModel.checkForUpdates(versionInfo.versionName) },
                            modifier = Modifier.weight(1f),
                            enabled = uiState.updateCheckState !is UpdateCheckState.Checking,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (uiState.updateCheckState is UpdateCheckState.Checking) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Checking...", maxLines = 1)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp).padding(end = 4.dp)
                                )
                                Text("Check Updates", maxLines = 1)
                            }
                        }
                    }

                    when (val updateState = uiState.updateCheckState) {
                        is UpdateCheckState.Idle -> {
                            Text(
                                text = "Tap 'Check Updates' to manually verify whether a new version has been published on GitHub.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        is UpdateCheckState.Checking -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Connecting to GitHub Releases API...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        is UpdateCheckState.UpToDate -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "You're using the latest version",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "v${updateState.currentVersion} is up to date.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    TextButton(
                                        onClick = { UrlLauncher.openUrl(context, updateState.releasePageUrl) }
                                    ) {
                                        Text("Releases", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                        is UpdateCheckState.UpdateAvailable -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Update Available: v${updateState.latestVersion}",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    if (updateState.releaseName.isNotBlank() && updateState.releaseName != updateState.latestVersion) {
                                        Text(
                                            text = updateState.releaseName,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    if (updateState.releaseNotes.isNotBlank()) {
                                        Text(
                                            text = updateState.releaseNotes.lines().take(5).joinToString("\n").trim(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 5
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { UrlLauncher.openUrl(context, updateState.releasePageUrl) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("View Release", fontSize = 12.sp)
                                        }

                                        if (updateState.apkAsset != null) {
                                            Button(
                                                onClick = {
                                                    UrlLauncher.openUrl(context, updateState.apkAsset.browserDownloadUrl)
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Download,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp).padding(end = 4.dp)
                                                )
                                                Text("Download APK", fontSize = 12.sp, maxLines = 1)
                                            }
                                        }
                                    }

                                    if (updateState.apkAsset == null && !updateState.isAmbiguousApk) {
                                        Text(
                                            text = "No direct APK asset attached. Please check the release page.",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else if (updateState.isAmbiguousApk) {
                                        Text(
                                            text = "Multiple APK builds found. Tap 'View Release' to select the build for your device.",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        text = "Note: Opening the download URL opens your browser. Android may ask you to allow downloads and installation from your browser or file manager.",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                        is UpdateCheckState.UncertainVersion -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Release on GitHub: ${updateState.rawTag}",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "The latest release tag could not be verified automatically. View the release on GitHub to inspect version details.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    TextButton(
                                        onClick = { UrlLauncher.openUrl(context, updateState.releasePageUrl) },
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text("View Release on GitHub")
                                    }
                                }
                            }
                        }
                        is UpdateCheckState.Error -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (updateState.isOffline) "Offline" else "Check Failed",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                    Text(
                                        text = updateState.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(
                                            onClick = { UrlLauncher.openUrl(context, UrlLauncher.GITHUB_REPO_URL) }
                                        ) {
                                            Text("Open GitHub")
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        TextButton(
                                            onClick = { viewModel.checkForUpdates(versionInfo.versionName) }
                                        ) {
                                            Text("Retry")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. PRIVACY & LOCAL STORAGE
            SettingsSectionCard(title = "Privacy & Local Storage", icon = Icons.Default.Lock) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "• Local Calculation & History: All your calculator inputs, comparison figures, and saved calculations remain stored safely on your device in a private SQLite/Room database.\n" +
                                "• Zero Tracking & Analytics: MargiQ contains no analytics, user tracking IDs, advertising identifiers, accounts, or background telemetry.\n" +
                                "• On-Demand Network Access: Internet access is used strictly on demand when you manually tap 'Check Updates' or open project links. MargiQ never runs background checks, scheduled sync jobs, or automatic network polling.",
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
                        text = "Version ${versionInfo.versionName} (Native Android with Jetpack Compose)",
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
                    text = "Version ${versionInfo.versionName}",
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
