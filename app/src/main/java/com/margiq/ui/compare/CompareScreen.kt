package com.margiq.ui.compare

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.margiq.domain.comparison.ProductComparisonOutcome
import com.margiq.domain.comparison.WinnerSide
import com.margiq.domain.util.IndianCurrencyFormatter
import com.margiq.domain.model.CalculationInput
import com.margiq.domain.model.TaxPriceMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    viewModel: CompareViewModel,
    onNavigateBack: () -> Unit,
    onOpenProductPickerForSideA: () -> Unit,
    onOpenProductPickerForSideB: () -> Unit,
    onOpenProductPickerForSupplierBase: () -> Unit,
    onLoadIntoCalculator: (CalculationInput) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Comparison Mode",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
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
            // Mode Tabs: Compare Products vs Compare Suppliers
            TabRow(
                selectedTabIndex = if (uiState.activeTab == CompareTab.PRODUCTS) 0 else 1,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
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
                        onOpenPickerA = onOpenProductPickerForSideA,
                        onOpenPickerB = onOpenProductPickerForSideB,
                        onLoadIntoCalculator = onLoadIntoCalculator
                    )
                } else {
                    CompareSuppliersContent(
                        uiState = uiState,
                        viewModel = viewModel,
                        onOpenPickerBase = onOpenProductPickerForSupplierBase,
                        onLoadIntoCalculator = onLoadIntoCalculator
                    )
                }
            }
        }
    }
}

@Composable
fun CompareProductsContent(
    uiState: CompareUiState,
    viewModel: CompareViewModel,
    onOpenPickerA: () -> Unit,
    onOpenPickerB: () -> Unit,
    onLoadIntoCalculator: (CalculationInput) -> Unit
) {
    // 1. Comparison Results Summary Card
    uiState.productsComparison?.let { outcome ->
        if (outcome.isValid && outcome.highlight != null) {
            ComparisonResultCard(
                outcome = outcome,
                titleA = uiState.productSideA.productName.ifBlank { "Product A" },
                titleB = uiState.productSideB.productName.ifBlank { "Product B" },
                subtitleA = uiState.productSideA.supplierName,
                subtitleB = uiState.productSideB.supplierName
            )
        }
    }

    // 2. Side A Product Details
    ProductInputSideCard(
        sideLabel = "Side A",
        state = uiState.productSideA,
        onSelectFromLibrary = onOpenPickerA,
        onUpdate = { modifier -> viewModel.updateSideA(modifier) },
        onSaveToLibrary = { viewModel.saveProductFromSide(isSideA = true) },
        onLoadInCalculator = { onLoadIntoCalculator(uiState.productSideA.toCalculationInput()) }
    )

    // 3. Side B Product Details
    ProductInputSideCard(
        sideLabel = "Side B",
        state = uiState.productSideB,
        onSelectFromLibrary = onOpenPickerB,
        onUpdate = { modifier -> viewModel.updateSideB(modifier) },
        onSaveToLibrary = { viewModel.saveProductFromSide(isSideA = false) },
        onLoadInCalculator = { onLoadIntoCalculator(uiState.productSideB.toCalculationInput()) }
    )
}

@Composable
fun CompareSuppliersContent(
    uiState: CompareUiState,
    viewModel: CompareViewModel,
    onOpenPickerBase: () -> Unit,
    onLoadIntoCalculator: (CalculationInput) -> Unit
) {
    // 1. Supplier Comparison Results Summary Card
    uiState.suppliersComparison?.let { outcome ->
        if (outcome.isValid && outcome.highlight != null) {
            ComparisonResultCard(
                outcome = outcome,
                titleA = uiState.supplierSideA.supplierName.ifBlank { "Supplier A" },
                titleB = uiState.supplierSideB.supplierName.ifBlank { "Supplier B" },
                subtitleA = "Offer A",
                subtitleB = "Offer B"
            )
        }
    }

    // 2. Base Product Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Base Product Details",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                OutlinedButton(
                    onClick = onOpenPickerBase,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp).padding(end = 4.dp))
                    Text("Select Product", style = MaterialTheme.typography.labelSmall)
                }
            }

            OutlinedTextField(
                value = uiState.supplierBaseProduct.productName,
                onValueChange = { name -> viewModel.updateSupplierBase { it.copy(productName = name) } },
                label = { Text("Product Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.supplierBaseProduct.sellingPriceText,
                    onValueChange = { price -> viewModel.updateSupplierBase { it.copy(sellingPriceText = price) } },
                    label = { Text("Selling Price / MRP (₹)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.supplierBaseProduct.unitsPerPackText,
                    onValueChange = { units -> viewModel.updateSupplierBase { it.copy(unitsPerPackText = units) } },
                    label = { Text("Units / Pack") },
                    modifier = Modifier.weight(0.8f),
                    singleLine = true
                )
            }
        }
    }

    // 3. Supplier Offer A
    SupplierOfferCard(
        label = "Supplier Offer A",
        offer = uiState.supplierSideA,
        onUpdate = { modifier -> viewModel.updateSupplierA(modifier) },
        onSaveAsProduct = { viewModel.saveSupplierOfferAsProduct(isSupplierA = true) },
        onLoadInCalculator = { onLoadIntoCalculator(uiState.supplierSideA.toCalculationInput(uiState.supplierBaseProduct)) }
    )

    // 4. Supplier Offer B
    SupplierOfferCard(
        label = "Supplier Offer B",
        offer = uiState.supplierSideB,
        onUpdate = { modifier -> viewModel.updateSupplierB(modifier) },
        onSaveAsProduct = { viewModel.saveSupplierOfferAsProduct(isSupplierA = false) },
        onLoadInCalculator = { onLoadIntoCalculator(uiState.supplierSideB.toCalculationInput(uiState.supplierBaseProduct)) }
    )
}

@Composable
fun ComparisonResultCard(
    outcome: ProductComparisonOutcome,
    titleA: String,
    titleB: String,
    subtitleA: String,
    subtitleB: String
) {
    val hl = outcome.highlight ?: return
    val resA = outcome.resultA
    val resB = outcome.resultB

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        ),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Side-by-Side Comparison",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = "Basis: Unit Economics",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Highlights Row: Lowest Cost, Highest Profit/Unit, Highest Margin
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HighlightBadge(
                    label = "Lowest Cost",
                    winner = when (hl.lowestCostSide) {
                        WinnerSide.SIDE_A -> titleA
                        WinnerSide.SIDE_B -> titleB
                        WinnerSide.TIE -> "Tie"
                        WinnerSide.NONE -> "-"
                    },
                    modifier = Modifier.weight(1f)
                )
                HighlightBadge(
                    label = "Highest Profit/Unit",
                    winner = when (hl.highestProfitPerUnitSide) {
                        WinnerSide.SIDE_A -> titleA
                        WinnerSide.SIDE_B -> titleB
                        WinnerSide.TIE -> "Tie"
                        WinnerSide.NONE -> "-"
                    },
                    modifier = Modifier.weight(1f)
                )
                HighlightBadge(
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

            // Savings banner if one side is cheaper
            if (hl.costSavingsPercent != null && hl.costSavingsPercent.compareTo(java.math.BigDecimal.ZERO) > 0) {
                val cheaperName = if (hl.lowestCostSide == WinnerSide.SIDE_A) titleA else titleB
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Potential Savings: $cheaperName saves ₹${hl.costDifferencePerUnit.toPlainString()}/unit (${hl.costSavingsPercent.toPlainString()}%)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Comparison Metrics Grid
            ComparisonRow(label = "Item", valA = titleA, valB = titleB, isHeader = true)
            ComparisonRow(label = "Cost / Unit", valA = "₹${resA.roundedCostPerIndividualUnit}", valB = "₹${resB.roundedCostPerIndividualUnit}")
            ComparisonRow(label = "Cost / Pack", valA = "₹${resA.roundedEffectiveCostPerReceivedPack}", valB = "₹${resB.roundedEffectiveCostPerReceivedPack}")
            ComparisonRow(label = "Selling Price", valA = "₹${resA.roundedCustomerSellingPrice}", valB = "₹${resB.roundedCustomerSellingPrice}")
            ComparisonRow(label = "Profit / Unit", valA = "₹${resA.roundedProfitPerIndividualUnit}", valB = "₹${resB.roundedProfitPerIndividualUnit}")
            ComparisonRow(label = "Profit Margin %", valA = "${resA.roundedProfitMargin?.toPlainString() ?: "0.00"}%", valB = "${resB.roundedProfitMargin?.toPlainString() ?: "0.00"}%")
            ComparisonRow(label = "Markup %", valA = "${resA.roundedMarkup?.toPlainString() ?: "0.00"}%", valB = "${resB.roundedMarkup?.toPlainString() ?: "0.00"}%")
            ComparisonRow(label = "Purchase Tax", valA = IndianCurrencyFormatter.formatCurrency(resA.purchaseTaxAmount), valB = IndianCurrencyFormatter.formatCurrency(resB.purchaseTaxAmount))
            ComparisonRow(
                label = "ITC Treatment",
                valA = if (resA.isInputTaxCreditApplied) "Credit Eligible" else "Cost Component",
                valB = if (resB.isInputTaxCreditApplied) "Credit Eligible" else "Cost Component"
            )
        }
    }
}

@Composable
fun HighlightBadge(
    label: String,
    winner: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = winner,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ComparisonRow(
    label: String,
    valA: String,
    valB: String,
    isHeader: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isHeader) MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1.1f)
        )
        Text(
            text = valA,
            style = if (isHeader) MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valB,
            style = if (isHeader) MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ProductInputSideCard(
    sideLabel: String,
    state: CompareSideState,
    onSelectFromLibrary: () -> Unit,
    onUpdate: ((CompareSideState) -> CompareSideState) -> Unit,
    onSaveToLibrary: () -> Unit,
    onLoadInCalculator: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$sideLabel: ${state.productName.ifBlank { "Untitled" }}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                OutlinedButton(
                    onClick = onSelectFromLibrary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp).padding(end = 4.dp))
                    Text("Select Saved", style = MaterialTheme.typography.labelSmall)
                }
            }

            OutlinedTextField(
                value = state.productName,
                onValueChange = { name -> onUpdate { it.copy(productName = name) } },
                label = { Text("Product Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.supplierName,
                onValueChange = { sup -> onUpdate { it.copy(supplierName = sup) } },
                label = { Text("Supplier Name (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.ptrText,
                    onValueChange = { ptr -> onUpdate { it.copy(ptrText = ptr) } },
                    label = { Text("PTR (₹)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.purchaseTaxRateText,
                    onValueChange = { tax -> onUpdate { it.copy(purchaseTaxRateText = tax) } },
                    label = { Text("Purchase Tax %") },
                    modifier = Modifier.weight(0.8f),
                    singleLine = true
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.sellingPriceText,
                    onValueChange = { sp -> onUpdate { it.copy(sellingPriceText = sp) } },
                    label = { Text("Selling MRP (₹)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.unitsPerPackText,
                    onValueChange = { u -> onUpdate { it.copy(unitsPerPackText = u) } },
                    label = { Text("Units / Pack") },
                    modifier = Modifier.weight(0.8f),
                    singleLine = true
                )
            }

            // Supplier Discount Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Supplier Discount", style = MaterialTheme.typography.bodySmall)
                Switch(
                    checked = state.isSupplierDiscountEnabled,
                    onCheckedChange = { onUpdate { s -> s.copy(isSupplierDiscountEnabled = it) } }
                )
            }
            if (state.isSupplierDiscountEnabled) {
                OutlinedTextField(
                    value = state.supplierDiscountText,
                    onValueChange = { d -> onUpdate { it.copy(supplierDiscountText = d) } },
                    label = { Text("Discount %") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Free Goods Offer Switch
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
                        onValueChange = { b -> onUpdate { it.copy(offerBuyQuantityText = b) } },
                        label = { Text("Buy Qty") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.offerFreeQuantityText,
                        onValueChange = { f -> onUpdate { it.copy(offerFreeQuantityText = f) } },
                        label = { Text("Free Qty") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }

            // Actions: Save to Library, Load in Calculator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSaveToLibrary,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(16.dp).padding(end = 4.dp))
                    Text("Save to Library", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = onLoadInCalculator,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp).padding(end = 4.dp))
                    Text("Use in Calculator", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun SupplierOfferCard(
    label: String,
    offer: SupplierOfferSide,
    onUpdate: ((SupplierOfferSide) -> SupplierOfferSide) -> Unit,
    onSaveAsProduct: () -> Unit,
    onLoadInCalculator: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            OutlinedTextField(
                value = offer.supplierName,
                onValueChange = { name -> onUpdate { it.copy(supplierName = name) } },
                label = { Text("Supplier Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = offer.ptrText,
                    onValueChange = { ptr -> onUpdate { it.copy(ptrText = ptr) } },
                    label = { Text("PTR (₹)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = offer.purchaseTaxRateText,
                    onValueChange = { tax -> onUpdate { it.copy(purchaseTaxRateText = tax) } },
                    label = { Text("Purchase Tax %") },
                    modifier = Modifier.weight(0.8f),
                    singleLine = true
                )
            }

            // Discount Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Supplier Discount", style = MaterialTheme.typography.bodySmall)
                Switch(
                    checked = offer.isSupplierDiscountEnabled,
                    onCheckedChange = { onUpdate { s -> s.copy(isSupplierDiscountEnabled = it) } }
                )
            }
            if (offer.isSupplierDiscountEnabled) {
                OutlinedTextField(
                    value = offer.supplierDiscountText,
                    onValueChange = { d -> onUpdate { it.copy(supplierDiscountText = d) } },
                    label = { Text("Discount %") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Free Goods Offer Toggle
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
                        onValueChange = { b -> onUpdate { it.copy(offerBuyQuantityText = b) } },
                        label = { Text("Buy Qty") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = offer.offerFreeQuantityText,
                        onValueChange = { f -> onUpdate { it.copy(offerFreeQuantityText = f) } },
                        label = { Text("Free Qty") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }

            // Actions: Save as Product, Load in Calculator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSaveAsProduct,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(16.dp).padding(end = 4.dp))
                    Text("Save as Product", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = onLoadInCalculator,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp).padding(end = 4.dp))
                    Text("Use in Calculator", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
