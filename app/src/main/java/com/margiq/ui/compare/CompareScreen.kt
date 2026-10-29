package com.margiq.ui.compare

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.margiq.domain.comparison.ProductComparisonOutcome
import com.margiq.domain.comparison.WinnerSide
import com.margiq.domain.model.CalculationInput
import com.margiq.domain.model.TaxPriceMode
import com.margiq.domain.sharing.ShareReport
import com.margiq.domain.sharing.ShareReportFactory
import com.margiq.domain.util.IndianCurrencyFormatter
import com.margiq.theme.FinanceTheme
import com.margiq.ui.sharing.ShareFormatDialog
import java.math.BigDecimal
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    viewModel: CompareViewModel,
    onNavigateBack: () -> Unit,
    onSelectHistoryForSideA: () -> Unit,
    onSelectHistoryForSideB: () -> Unit,
    onSelectHistoryForSupplierBase: () -> Unit,
    onSelectHistoryForSupplierOfferA: () -> Unit = {},
    onSelectHistoryForSupplierOfferB: () -> Unit = {},
    onLoadIntoCalculator: (CalculationInput) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    var shareReportToOpen by remember { mutableStateOf<ShareReport?>(null) }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissUserMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Commercial Comparison",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Side-by-side cost, profit & margin analysis",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    val canShare = if (uiState.activeTab == CompareTab.PRODUCTS) {
                        uiState.productsComparison?.isValid == true
                    } else {
                        uiState.suppliersComparison?.isValid == true
                    }
                    IconButton(
                        onClick = {
                            if (uiState.activeTab == CompareTab.PRODUCTS) {
                                uiState.productsComparison?.let { outcome ->
                                    if (outcome.isValid) {
                                        shareReportToOpen = ShareReportFactory.fromProductComparison(
                                            outcome,
                                            uiState.productSideA,
                                            uiState.productSideB
                                        )
                                    }
                                }
                            } else {
                                uiState.suppliersComparison?.let { outcome ->
                                    if (outcome.isValid) {
                                        shareReportToOpen = ShareReportFactory.fromSupplierComparison(
                                            outcome,
                                            uiState.supplierBaseProduct,
                                            uiState.supplierSideA,
                                            uiState.supplierSideB
                                        )
                                    }
                                }
                            }
                        },
                        enabled = canShare
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Comparison"
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
        ) {
            TabRow(
                selectedTabIndex = if (uiState.activeTab == CompareTab.PRODUCTS) 0 else 1,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[if (uiState.activeTab == CompareTab.PRODUCTS) 0 else 1]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(
                    selected = uiState.activeTab == CompareTab.PRODUCTS,
                    onClick = { viewModel.selectTab(CompareTab.PRODUCTS) },
                    text = { Text("Compare Products", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = uiState.activeTab == CompareTab.SUPPLIERS,
                    onClick = { viewModel.selectTab(CompareTab.SUPPLIERS) },
                    text = { Text("Compare Suppliers", fontWeight = FontWeight.SemiBold) }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.activeTab == CompareTab.PRODUCTS) {
                    CompareProductsContent(
                        uiState = uiState,
                        viewModel = viewModel,
                        onOpenPickerA = onSelectHistoryForSideA,
                        onOpenPickerB = onSelectHistoryForSideB,
                        onLoadIntoCalculator = onLoadIntoCalculator,
                        onShare = {
                            uiState.productsComparison?.let { outcome ->
                                if (outcome.isValid) {
                                    shareReportToOpen = ShareReportFactory.fromProductComparison(
                                        outcome,
                                        uiState.productSideA,
                                        uiState.productSideB
                                    )
                                }
                            }
                        }
                    )
                } else {
                    CompareSuppliersContent(
                        uiState = uiState,
                        viewModel = viewModel,
                        onOpenPickerBase = onSelectHistoryForSupplierBase,
                        onOpenPickerOfferA = onSelectHistoryForSupplierOfferA,
                        onOpenPickerOfferB = onSelectHistoryForSupplierOfferB,
                        onLoadIntoCalculator = onLoadIntoCalculator,
                        onShare = {
                            uiState.suppliersComparison?.let { outcome ->
                                if (outcome.isValid) {
                                    shareReportToOpen = ShareReportFactory.fromSupplierComparison(
                                        outcome,
                                        uiState.supplierBaseProduct,
                                        uiState.supplierSideA,
                                        uiState.supplierSideB
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    shareReportToOpen?.let { report ->
        ShareFormatDialog(
            report = report,
            onDismissRequest = { shareReportToOpen = null }
        )
    }
}
@Composable
fun CompareProductsContent(
    uiState: CompareUiState,
    viewModel: CompareViewModel,
    onOpenPickerA: () -> Unit,
    onOpenPickerB: () -> Unit,
    onLoadIntoCalculator: (CalculationInput) -> Unit,
    onShare: (() -> Unit)? = null
) {
    val outcome = uiState.productsComparison
    if (outcome != null) {
        if (outcome.isValid && outcome.highlight != null) {
            ComparisonResultCard(
                outcome = outcome,
                titleA = uiState.productSideA.productName.ifBlank { "Option A" },
                titleB = uiState.productSideB.productName.ifBlank { "Option B" },
                onShare = onShare
            )
        } else if (!outcome.isValid && outcome.validationMessage != null) {
            ValidationBanner(message = outcome.validationMessage)
        }
    }

    ProductInputSideCard(
        sideLabel = "Option A",
        state = uiState.productSideA,
        onSelectFromHistory = onOpenPickerA,
        onUpdate = { modifier -> viewModel.updateSideA(modifier) },
        onLoadInCalculator = { onLoadIntoCalculator(uiState.productSideA.toCalculationInput()) }
    )

    ProductInputSideCard(
        sideLabel = "Option B",
        state = uiState.productSideB,
        onSelectFromHistory = onOpenPickerB,
        onUpdate = { modifier -> viewModel.updateSideB(modifier) },
        onLoadInCalculator = { onLoadIntoCalculator(uiState.productSideB.toCalculationInput()) }
    )
}

@Composable
fun CompareSuppliersContent(
    uiState: CompareUiState,
    viewModel: CompareViewModel,
    onOpenPickerBase: () -> Unit,
    onOpenPickerOfferA: () -> Unit = {},
    onOpenPickerOfferB: () -> Unit = {},
    onLoadIntoCalculator: (CalculationInput) -> Unit,
    onShare: (() -> Unit)? = null
) {
    val outcome = uiState.suppliersComparison
    if (outcome != null) {
        if (outcome.isValid && outcome.highlight != null) {
            ComparisonResultCard(
                outcome = outcome,
                titleA = uiState.supplierSideA.supplierName.ifBlank { "Supplier A" },
                titleB = uiState.supplierSideB.supplierName.ifBlank { "Supplier B" },
                onShare = onShare
            )
        } else if (!outcome.isValid && outcome.validationMessage != null) {
            ValidationBanner(message = outcome.validationMessage)
        }
    }

    BaseProductSellingCard(
        base = uiState.supplierBaseProduct,
        onSelectFromHistory = onOpenPickerBase,
        onUpdate = { modifier -> viewModel.updateSupplierBase(modifier) }
    )

    SupplierOfferCard(
        label = "Supplier Offer A",
        offer = uiState.supplierSideA,
        base = uiState.supplierBaseProduct,
        onSelectFromHistory = onOpenPickerOfferA,
        onUpdate = { modifier -> viewModel.updateSupplierA(modifier) },
        onLoadInCalculator = { onLoadIntoCalculator(uiState.supplierSideA.toCalculationInput(uiState.supplierBaseProduct)) }
    )

    SupplierOfferCard(
        label = "Supplier Offer B",
        offer = uiState.supplierSideB,
        base = uiState.supplierBaseProduct,
        onSelectFromHistory = onOpenPickerOfferB,
        onUpdate = { modifier -> viewModel.updateSupplierB(modifier) },
        onLoadInCalculator = { onLoadIntoCalculator(uiState.supplierSideB.toCalculationInput(uiState.supplierBaseProduct)) }
    )
}

@Composable
fun ValidationBanner(message: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}
@Composable
fun ComparisonResultCard(
    outcome: ProductComparisonOutcome,
    titleA: String,
    titleB: String,
    onShare: (() -> Unit)? = null
) {
    val hl = outcome.highlight ?: return
    val resA = outcome.resultA
    val resB = outcome.resultB

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = "Side-by-Side Comparison",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "Normalized Unit Basis",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (onShare != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onShare,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WinnerBadge(
                    icon = Icons.Default.Savings,
                    label = "Lowest Cost",
                    winner = when (hl.lowestCostSide) {
                        WinnerSide.SIDE_A -> titleA
                        WinnerSide.SIDE_B -> titleB
                        WinnerSide.TIE -> "Tie"
                        WinnerSide.NONE -> "-"
                    },
                    modifier = Modifier.weight(1f)
                )
                WinnerBadge(
                    icon = Icons.Default.CheckCircle,
                    label = "Highest Profit",
                    winner = when (hl.highestProfitPerUnitSide) {
                        WinnerSide.SIDE_A -> titleA
                        WinnerSide.SIDE_B -> titleB
                        WinnerSide.TIE -> "Tie"
                        WinnerSide.NONE -> "-"
                    },
                    modifier = Modifier.weight(1f)
                )
                WinnerBadge(
                    icon = Icons.Default.Percent,
                    label = "Highest Margin",
                    winner = when (hl.highestMarginSide) {
                        WinnerSide.SIDE_A -> titleA
                        WinnerSide.SIDE_B -> titleB
                        WinnerSide.TIE -> "Tie"
                        WinnerSide.NONE -> "-"
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            if (hl.costSavingsPercent != null && hl.costSavingsPercent.compareTo(BigDecimal.ZERO) > 0) {
                val cheaperName = if (hl.lowestCostSide == WinnerSide.SIDE_A) titleA else titleB
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp).padding(end = 4.dp)
                        )
                        Text(
                            text = "$cheaperName saves \u20b9${hl.costDifferencePerUnit.toPlainString()}/unit (${hl.costSavingsPercent.toPlainString()}%)",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            ComparisonMetricRow(label = "Metric", valA = titleA, valB = titleB, isHeader = true)
            ComparisonMetricRow(
                label = "Cost / Unit",
                valA = "\u20b9${resA.roundedCostPerIndividualUnit}",
                valB = "\u20b9${resB.roundedCostPerIndividualUnit}",
                isWinnerA = hl.lowestCostSide == WinnerSide.SIDE_A,
                isWinnerB = hl.lowestCostSide == WinnerSide.SIDE_B
            )
            ComparisonMetricRow(
                label = "Cost / Pack",
                valA = "\u20b9${resA.roundedEffectiveCostPerReceivedPack}",
                valB = "\u20b9${resB.roundedEffectiveCostPerReceivedPack}"
            )
            ComparisonMetricRow(
                label = "Selling Price (MRP)",
                valA = "\u20b9${resA.roundedCustomerSellingPrice}",
                valB = "\u20b9${resB.roundedCustomerSellingPrice}"
            )
            ComparisonMetricRow(
                label = "Profit / Unit",
                valA = "\u20b9${resA.roundedProfitPerIndividualUnit}",
                valB = "\u20b9${resB.roundedProfitPerIndividualUnit}",
                isWinnerA = hl.highestProfitPerUnitSide == WinnerSide.SIDE_A,
                isWinnerB = hl.highestProfitPerUnitSide == WinnerSide.SIDE_B
            )
            ComparisonMetricRow(
                label = "Profit Margin",
                valA = "${resA.roundedProfitMargin?.toPlainString() ?: "0.00"}%",
                valB = "${resB.roundedProfitMargin?.toPlainString() ?: "0.00"}%",
                isWinnerA = hl.highestMarginSide == WinnerSide.SIDE_A,
                isWinnerB = hl.highestMarginSide == WinnerSide.SIDE_B
            )
            ComparisonMetricRow(
                label = "Markup",
                valA = "${resA.roundedMarkup?.toPlainString() ?: "0.00"}%",
                valB = "${resB.roundedMarkup?.toPlainString() ?: "0.00"}%"
            )
            ComparisonMetricRow(
                label = "Purchase Tax",
                valA = IndianCurrencyFormatter.formatCurrency(resA.purchaseTaxAmount),
                valB = IndianCurrencyFormatter.formatCurrency(resB.purchaseTaxAmount)
            )
            ComparisonMetricRow(
                label = "Selling Tax",
                valA = IndianCurrencyFormatter.formatCurrency(resA.sellingTaxAmount),
                valB = IndianCurrencyFormatter.formatCurrency(resB.sellingTaxAmount)
            )
            ComparisonMetricRow(
                label = "ITC Treatment",
                valA = if (resA.isInputTaxCreditApplied) "Credit Eligible" else "Cost Component",
                valB = if (resB.isInputTaxCreditApplied) "Credit Eligible" else "Cost Component"
            )
        }
    }
}

@Composable
fun WinnerBadge(
    icon: ImageVector,
    label: String,
    winner: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = winner,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ComparisonMetricRow(
    label: String,
    valA: String,
    valB: String,
    isHeader: Boolean = false,
    isWinnerA: Boolean = false,
    isWinnerB: Boolean = false
) {
    val financeColors = FinanceTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isHeader) MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1.2f)
        )
        Text(
            text = valA,
            style = if (isHeader) MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodySmall.copy(fontWeight = if (isWinnerA) FontWeight.Bold else FontWeight.Normal),
            color = if (isWinnerA) financeColors.profit else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valB,
            style = if (isHeader) MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodySmall.copy(fontWeight = if (isWinnerB) FontWeight.Bold else FontWeight.Normal),
            color = if (isWinnerB) financeColors.profit else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}
@Composable
fun ProductInputSideCard(
    sideLabel: String,
    state: CompareSideState,
    onSelectFromHistory: () -> Unit,
    onUpdate: ((CompareSideState) -> CompareSideState) -> Unit,
    onLoadInCalculator: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = sideLabel,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (state.productName.isNotBlank()) {
                        Text(
                            text = state.productName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onSelectFromHistory,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(14.dp).padding(end = 4.dp))
                        Text("From History", style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedButton(
                        onClick = onLoadInCalculator,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(14.dp).padding(end = 2.dp))
                        Text("Use", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Text(text = "Identity", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = state.productName,
                onValueChange = { onUpdate { s -> s.copy(productName = it) } },
                label = { Text("Product / Medicine Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = state.supplierName,
                onValueChange = { onUpdate { s -> s.copy(supplierName = it) } },
                label = { Text("Supplier / Distributor Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text(text = "Purchase Pricing & Tax", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.ptrText,
                    onValueChange = { onUpdate { s -> s.copy(ptrText = it) } },
                    label = { Text("PTR (\u20b9)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.purchaseTaxRateText,
                    onValueChange = { onUpdate { s -> s.copy(purchaseTaxRateText = it) } },
                    label = { Text("GST %") },
                    modifier = Modifier.weight(0.8f),
                    singleLine = true
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = state.purchasePriceMode == TaxPriceMode.TAX_EXCLUSIVE,
                    onClick = { onUpdate { s -> s.copy(purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE) } },
                    label = { Text("PTR + GST", style = MaterialTheme.typography.labelSmall) }
                )
                FilterChip(
                    selected = state.purchasePriceMode == TaxPriceMode.TAX_INCLUSIVE,
                    onClick = { onUpdate { s -> s.copy(purchasePriceMode = TaxPriceMode.TAX_INCLUSIVE) } },
                    label = { Text("PTR Incl.", style = MaterialTheme.typography.labelSmall) }
                )
                Spacer(modifier = Modifier.weight(1f))
                FilterChip(
                    selected = state.isInputTaxCreditEnabled,
                    onClick = { onUpdate { s -> s.copy(isInputTaxCreditEnabled = !s.isInputTaxCreditEnabled) } },
                    label = { Text(if (state.isInputTaxCreditEnabled) "ITC Claimed" else "No ITC", style = MaterialTheme.typography.labelSmall) }
                )
            }

            Text(text = "Selling Pricing & Tax", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.sellingPriceText,
                    onValueChange = { onUpdate { s -> s.copy(sellingPriceText = it) } },
                    label = { Text("Selling Price / MRP (\u20b9)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.sellingTaxRateText,
                    onValueChange = { onUpdate { s -> s.copy(sellingTaxRateText = it) } },
                    label = { Text("Selling GST %") },
                    modifier = Modifier.weight(0.8f),
                    singleLine = true
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = state.sellingPriceMode == TaxPriceMode.TAX_INCLUSIVE,
                    onClick = { onUpdate { s -> s.copy(sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE) } },
                    label = { Text("MRP (Tax Incl.)", style = MaterialTheme.typography.labelSmall) }
                )
                FilterChip(
                    selected = state.sellingPriceMode == TaxPriceMode.TAX_EXCLUSIVE,
                    onClick = { onUpdate { s -> s.copy(sellingPriceMode = TaxPriceMode.TAX_EXCLUSIVE) } },
                    label = { Text("Rate + Tax", style = MaterialTheme.typography.labelSmall) }
                )
            }

            Text(text = "Pack Details", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = state.unitsPerPackText,
                onValueChange = { onUpdate { s -> s.copy(unitsPerPackText = it) } },
                label = { Text("Units per Pack (e.g. 10 tablets/strip)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text(text = "Discounts & Offers", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Supplier Cash Discount", style = MaterialTheme.typography.bodySmall)
                Switch(
                    checked = state.isSupplierDiscountEnabled,
                    onCheckedChange = { onUpdate { s -> s.copy(isSupplierDiscountEnabled = it) } }
                )
            }
            if (state.isSupplierDiscountEnabled) {
                OutlinedTextField(
                    value = state.supplierDiscountText,
                    onValueChange = { onUpdate { s -> s.copy(supplierDiscountText = it) } },
                    label = { Text("Discount %") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Buy X, Get Y Free Offer", style = MaterialTheme.typography.bodySmall)
                Switch(
                    checked = state.isSupplierOfferEnabled,
                    onCheckedChange = { onUpdate { s -> s.copy(isSupplierOfferEnabled = it) } }
                )
            }
            if (state.isSupplierOfferEnabled) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.offerBuyQuantityText,
                        onValueChange = { onUpdate { s -> s.copy(offerBuyQuantityText = it) } },
                        label = { Text("Buy Qty") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.offerFreeQuantityText,
                        onValueChange = { onUpdate { s -> s.copy(offerFreeQuantityText = it) } },
                        label = { Text("Free Qty") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        }
    }
}
@Composable
fun BaseProductSellingCard(
    base: SupplierComparisonBaseProduct,
    onSelectFromHistory: () -> Unit,
    onUpdate: ((SupplierComparisonBaseProduct) -> SupplierComparisonBaseProduct) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Base Product Selling Parameters",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Applied across compared supplier offers",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(
                    onClick = onSelectFromHistory,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(14.dp).padding(end = 4.dp))
                    Text("From History", style = MaterialTheme.typography.labelSmall)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            OutlinedTextField(
                value = base.productName,
                onValueChange = { onUpdate { b -> b.copy(productName = it) } },
                label = { Text("Product / Item Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = base.sellingPriceText,
                    onValueChange = { onUpdate { b -> b.copy(sellingPriceText = it) } },
                    label = { Text("Selling Price / MRP (\u20b9)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = base.sellingTaxRateText,
                    onValueChange = { onUpdate { b -> b.copy(sellingTaxRateText = it) } },
                    label = { Text("Selling GST %") },
                    modifier = Modifier.weight(0.8f),
                    singleLine = true
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = base.sellingPriceMode == TaxPriceMode.TAX_INCLUSIVE,
                    onClick = { onUpdate { b -> b.copy(sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE) } },
                    label = { Text("MRP (Tax Incl.)", style = MaterialTheme.typography.labelSmall) }
                )
                FilterChip(
                    selected = base.sellingPriceMode == TaxPriceMode.TAX_EXCLUSIVE,
                    onClick = { onUpdate { b -> b.copy(sellingPriceMode = TaxPriceMode.TAX_EXCLUSIVE) } },
                    label = { Text("Rate + Tax", style = MaterialTheme.typography.labelSmall) }
                )
                Spacer(modifier = Modifier.weight(1f))
                OutlinedTextField(
                    value = base.unitsPerPackText,
                    onValueChange = { onUpdate { b -> b.copy(unitsPerPackText = it) } },
                    label = { Text("Units/Pack") },
                    modifier = Modifier.width(110.dp),
                    singleLine = true
                )
            }
        }
    }
}

@Composable
fun SupplierOfferCard(
    label: String,
    offer: SupplierOfferSide,
    base: SupplierComparisonBaseProduct,
    onSelectFromHistory: () -> Unit = {},
    onUpdate: ((SupplierOfferSide) -> SupplierOfferSide) -> Unit,
    onLoadInCalculator: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (offer.supplierName.isNotBlank()) {
                        Text(
                            text = offer.supplierName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onSelectFromHistory,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(14.dp).padding(end = 2.dp))
                        Text("From History", style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedButton(
                        onClick = onLoadInCalculator,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(14.dp).padding(end = 2.dp))
                        Text("Use in Calc", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            OutlinedTextField(
                value = offer.supplierName,
                onValueChange = { onUpdate { s -> s.copy(supplierName = it) } },
                label = { Text("Supplier Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = offer.ptrText,
                    onValueChange = { onUpdate { s -> s.copy(ptrText = it) } },
                    label = { Text("PTR (\u20b9)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = offer.purchaseTaxRateText,
                    onValueChange = { onUpdate { s -> s.copy(purchaseTaxRateText = it) } },
                    label = { Text("Purchase Tax %") },
                    modifier = Modifier.weight(0.8f),
                    singleLine = true
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = offer.purchasePriceMode == TaxPriceMode.TAX_EXCLUSIVE,
                    onClick = { onUpdate { s -> s.copy(purchasePriceMode = TaxPriceMode.TAX_EXCLUSIVE) } },
                    label = { Text("PTR + Tax", style = MaterialTheme.typography.labelSmall) }
                )
                FilterChip(
                    selected = offer.purchasePriceMode == TaxPriceMode.TAX_INCLUSIVE,
                    onClick = { onUpdate { s -> s.copy(purchasePriceMode = TaxPriceMode.TAX_INCLUSIVE) } },
                    label = { Text("PTR Incl.", style = MaterialTheme.typography.labelSmall) }
                )
                Spacer(modifier = Modifier.weight(1f))
                FilterChip(
                    selected = offer.isInputTaxCreditEnabled,
                    onClick = { onUpdate { s -> s.copy(isInputTaxCreditEnabled = !s.isInputTaxCreditEnabled) } },
                    label = { Text(if (offer.isInputTaxCreditEnabled) "ITC Claimed" else "No ITC", style = MaterialTheme.typography.labelSmall) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Customize Selling Tax for this Offer", style = MaterialTheme.typography.bodySmall)
                Switch(
                    checked = offer.overrideSellingTax,
                    onCheckedChange = { onUpdate { s -> s.copy(overrideSellingTax = it) } }
                )
            }
            if (offer.overrideSellingTax) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = offer.sellingTaxRateText,
                        onValueChange = { onUpdate { s -> s.copy(sellingTaxRateText = it) } },
                        label = { Text("Selling GST %") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = offer.sellingPriceMode == TaxPriceMode.TAX_INCLUSIVE,
                            onClick = { onUpdate { s -> s.copy(sellingPriceMode = TaxPriceMode.TAX_INCLUSIVE) } },
                            label = { Text("Incl.", style = MaterialTheme.typography.labelSmall) }
                        )
                        FilterChip(
                            selected = offer.sellingPriceMode == TaxPriceMode.TAX_EXCLUSIVE,
                            onClick = { onUpdate { s -> s.copy(sellingPriceMode = TaxPriceMode.TAX_EXCLUSIVE) } },
                            label = { Text("Excl.", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Supplier Cash Discount", style = MaterialTheme.typography.bodySmall)
                Switch(
                    checked = offer.isSupplierDiscountEnabled,
                    onCheckedChange = { onUpdate { s -> s.copy(isSupplierDiscountEnabled = it) } }
                )
            }
            if (offer.isSupplierDiscountEnabled) {
                OutlinedTextField(
                    value = offer.supplierDiscountText,
                    onValueChange = { onUpdate { s -> s.copy(supplierDiscountText = it) } },
                    label = { Text("Discount %") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Buy X, Get Y Free Offer", style = MaterialTheme.typography.bodySmall)
                Switch(
                    checked = offer.isSupplierOfferEnabled,
                    onCheckedChange = { onUpdate { s -> s.copy(isSupplierOfferEnabled = it) } }
                )
            }
            if (offer.isSupplierOfferEnabled) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = offer.offerBuyQuantityText,
                        onValueChange = { onUpdate { s -> s.copy(offerBuyQuantityText = it) } },
                        label = { Text("Buy Qty") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = offer.offerFreeQuantityText,
                        onValueChange = { onUpdate { s -> s.copy(offerFreeQuantityText = it) } },
                        label = { Text("Free Qty") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        }
    }
}
