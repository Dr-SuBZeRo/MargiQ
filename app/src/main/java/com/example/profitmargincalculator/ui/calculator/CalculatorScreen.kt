package com.example.profitmargincalculator.ui.calculator

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.profitmargincalculator.domain.model.PurchaseTaxClassification
import com.example.profitmargincalculator.ui.components.CleanNumberInputField
import com.example.profitmargincalculator.ui.components.FinancialBreakdownCard
import com.example.profitmargincalculator.ui.components.MetricHeroCard
import com.example.profitmargincalculator.ui.components.SegmentedTaxModeSelector
import com.example.profitmargincalculator.ui.components.SimpleCashComparisonCard
import com.example.profitmargincalculator.ui.components.TaxRatePresetsRow

private val PACK_PRESETS = listOf("Strip", "Box", "Bottle", "Pack", "Carton", "Vial")
private val UNIT_PRESETS = listOf("Tablet", "Capsule", "Piece", "ml", "g", "Sachet")
private val UNITS_PER_PACK_PRESETS = listOf("1", "10", "15", "30", "100")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Column {
                            Text(
                                text = "MargiQ",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Know your cost. Own your margin.",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Live Hero Results Summary
            MetricHeroCard(result = uiState.calculationResult)

            // 2. Action Bar: Save, Copy, Share, Reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.saveCalculation() },
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.currentSavedId != null) Icons.Default.Check else Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = if (uiState.currentSavedId != null) "Saved" else "Save",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = {
                        val shareText = uiState.toFormattedShareText()
                        clipboardManager.setText(AnnotatedString(shareText))
                        viewModel.dismissUserMessage()
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Calculation"))
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Share")
                }

                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(uiState.toFormattedShareText()))
                    },
                    modifier = Modifier.weight(0.9f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy")
                }
            }

            // 3. Product Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Product Details",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        TextButton(onClick = { viewModel.restoreExampleScenario() }) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text("Example Values")
                        }
                    }

                    OutlinedTextField(
                        value = uiState.productName,
                        onValueChange = { viewModel.onProductNameChanged(it) },
                        label = { Text("Product / Medicine / Item Name") },
                        placeholder = { Text("e.g. Paracetamol 650mg Strip, Cefixime 200mg...") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 4. Pack & Unit Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Pack & Unit Details",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    // Pack Type Field & Presets
                    OutlinedTextField(
                        value = uiState.packName,
                        onValueChange = { viewModel.onPackNameChanged(it) },
                        label = { Text("Pack Name / Selling Unit") },
                        placeholder = { Text("e.g. Strip, Box, Bottle, Pack") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PACK_PRESETS.forEach { preset ->
                            FilterChip(
                                selected = uiState.packName.equals(preset, ignoreCase = true),
                                onClick = { viewModel.onPackNameChanged(preset) },
                                label = { Text(preset) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    // Individual Unit Field & Presets
                    OutlinedTextField(
                        value = uiState.unitName,
                        onValueChange = { viewModel.onUnitNameChanged(it) },
                        label = { Text("Individual Unit Name") },
                        placeholder = { Text("e.g. Tablet, Capsule, Piece, ml") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        UNIT_PRESETS.forEach { preset ->
                            FilterChip(
                                selected = uiState.unitName.equals(preset, ignoreCase = true),
                                onClick = { viewModel.onUnitNameChanged(preset) },
                                label = { Text(preset) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Units per Pack Field & Presets
                    CleanNumberInputField(
                        value = uiState.unitsPerPackText,
                        onValueChange = { viewModel.onUnitsPerPackChanged(it) },
                        label = "Units Per Pack",
                        suffixText = "${uiState.unitName}s",
                        helperText = "Number of individual units inside 1 ${uiState.packName}",
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UNITS_PER_PACK_PRESETS.forEach { preset ->
                            FilterChip(
                                selected = uiState.unitsPerPackText == preset,
                                onClick = { viewModel.onUnitsPerPackChanged(preset) },
                                label = { Text(preset) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    // Purchase Quantity in Packs
                    CleanNumberInputField(
                        value = uiState.quantityText,
                        onValueChange = { viewModel.onQuantityChanged(it) },
                        label = "Purchase Quantity (${uiState.packName}s)",
                        suffixText = "${uiState.packName}s",
                        helperText = "Number of whole ${uiState.packName.lowercase()}s purchased",
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )

                    // Selling Quantity in Packs
                    CleanNumberInputField(
                        value = uiState.sellingQuantityText,
                        onValueChange = { viewModel.onSellingQuantityChanged(it) },
                        label = "Selling Quantity (${uiState.packName}s)",
                        suffixText = "${uiState.packName}s",
                        helperText = "Number of ${uiState.packName.lowercase()}s sold to calculate total profit",
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )

                    // Live Units Calculation Callout
                    val pQty = uiState.quantityText.toIntOrNull() ?: 1
                    val uPack = uiState.unitsPerPackText.toIntOrNull() ?: 1
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💡 $pQty ${uiState.packName}s × $uPack ${uiState.unitName}s = ${pQty * uPack} total ${uiState.unitName.lowercase()}s purchased",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            // 5. Purchase Side Section Card (PTR, Tax, Mode, ITC)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Purchase Side (PTR per ${uiState.packName})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    CleanNumberInputField(
                        value = uiState.ptrText,
                        onValueChange = { viewModel.onPtrChanged(it) },
                        label = "Purchase Price (PTR) per ${uiState.packName}",
                        prefixText = "₹",
                        helperText = "Wholesale or distributor cost for 1 ${uiState.packName}",
                        imeAction = ImeAction.Next
                    )

                    // Purchase Tax Price Mode
                    Text(
                        text = "Purchase Price Tax Mode",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SegmentedTaxModeSelector(
                        selectedMode = uiState.purchasePriceMode,
                        onModeSelected = { viewModel.onPurchasePriceModeChanged(it) }
                    )

                    // Purchase Tax Rate
                    CleanNumberInputField(
                        value = uiState.purchaseTaxRateText,
                        onValueChange = { viewModel.onPurchaseTaxRateChanged(it) },
                        label = "Purchase Tax Rate (GST %)",
                        suffixText = "%",
                        helperText = "GST rate applicable on purchase",
                        imeAction = ImeAction.Next
                    )

                    TaxRatePresetsRow(
                        currentRateText = uiState.purchaseTaxRateText,
                        onRateSelected = { viewModel.setQuickPurchaseTaxRate(it) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Purchase Tax Classification (Recoverable vs Non-recoverable)
                    Text(
                        text = "Purchase Tax Classification",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PurchaseTaxClassification.entries.forEach { classification ->
                            val isSelected = classification == uiState.purchaseTaxClassification
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.onPurchaseTaxClassificationChanged(classification) },
                                label = { Text(classification.label) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    // Input Tax Credit Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Eligible for Input Tax Credit (ITC)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (uiState.canEnableItcToggle) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                text = if (uiState.canEnableItcToggle) "Exclude purchase GST from economic cost" else "Only available for Recoverable GST",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = uiState.isInputTaxCreditEnabled && uiState.canEnableItcToggle,
                            onCheckedChange = { viewModel.onInputTaxCreditToggled(it) },
                            enabled = uiState.canEnableItcToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }

            // 6. Selling Side Section Card (MRP, Tax, Mode)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Selling Side (MRP per ${uiState.packName})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    CleanNumberInputField(
                        value = uiState.sellingPriceText,
                        onValueChange = { viewModel.onSellingPriceChanged(it) },
                        label = "Selling Price (MRP) per ${uiState.packName}",
                        prefixText = "₹",
                        helperText = "MRP in India normally includes all applicable taxes",
                        imeAction = ImeAction.Next
                    )

                    // Selling Tax Price Mode
                    Text(
                        text = "Selling Price Tax Mode",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SegmentedTaxModeSelector(
                        selectedMode = uiState.sellingPriceMode,
                        onModeSelected = { viewModel.onSellingPriceModeChanged(it) }
                    )

                    // Selling Tax Rate
                    CleanNumberInputField(
                        value = uiState.sellingTaxRateText,
                        onValueChange = { viewModel.onSellingTaxRateChanged(it) },
                        label = "Selling Tax Rate (GST %)",
                        suffixText = "%",
                        helperText = "Output GST charged or embedded in retail price",
                        imeAction = ImeAction.Done
                    )

                    TaxRatePresetsRow(
                        currentRateText = uiState.sellingTaxRateText,
                        onRateSelected = { viewModel.setQuickSellingTaxRate(it) }
                    )
                }
            }

            // 7. Detailed Financial Breakdown Card
            FinancialBreakdownCard(
                result = uiState.calculationResult,
                isExpanded = uiState.showDetailedBreakdown,
                onToggleExpand = { viewModel.toggleDetailedBreakdown() }
            )

            // 8. Simple Cash Flow Comparison Card
            SimpleCashComparisonCard(result = uiState.calculationResult)

            // 9. Bottom Form Controls: Reset / Clear
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = { viewModel.resetToDefaults() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Reset Defaults")
                }

                TextButton(
                    onClick = { viewModel.clearForm() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Clear Form")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
