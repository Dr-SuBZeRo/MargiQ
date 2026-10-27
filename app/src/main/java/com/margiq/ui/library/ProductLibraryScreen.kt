package com.margiq.ui.library

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.margiq.data.local.ProductEntity
import com.margiq.domain.calculation.ProfitCalculationEngine
import com.margiq.domain.model.PurchaseTaxClassification
import com.margiq.domain.model.TaxPriceMode
import com.margiq.domain.util.IndianCurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductLibraryScreen(
    viewModel: ProductLibraryViewModel,
    onNavigateBack: () -> Unit,
    onProductSelected: ((ProductEntity) -> Unit)? = null,
    onLoadIntoCalculator: ((ProductEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
                    Text(
                        text = if (onProductSelected != null) "Select Product" else "Product Library",
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openNewProductEditor() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Product")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                label = { Text("Search products or suppliers") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            if (uiState.products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = if (uiState.searchQuery.isBlank()) "No saved products yet" else "No products found for '${uiState.searchQuery}'",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (uiState.searchQuery.isBlank()) {
                            Text(
                                text = "Save reusable products from the calculator or tap '+' to create one.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(uiState.products, key = { it.id }) { product ->
                        ProductLibraryCard(
                            product = product,
                            onSelect = if (onProductSelected != null) { { onProductSelected(product) } } else null,
                            onLoadInCalculator = onLoadIntoCalculator?.let { action -> { action(product) } },
                            onEdit = { viewModel.openEditProductEditor(product) },
                            onDelete = { viewModel.requestDelete(product) }
                        )
                    }
                }
            }
        }
    }

    // Confirmation Dialog for Delete
    uiState.productToDelete?.let { prod ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = { Text("Delete Product?") },
            text = {
                Text("Are you sure you want to delete '${prod.productName}'? This will not delete calculation history.")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDelete() },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add / Edit Product Dialog
    if (uiState.isEditorOpen) {
        ProductEditorDialog(
            initialProduct = uiState.editingProduct ?: ProductEntity(productName = ""),
            isEdit = uiState.isEditMode,
            onDismiss = { viewModel.dismissEditor() },
            onSave = { updated -> viewModel.saveProduct(updated) }
        )
    }
}

@Composable
fun ProductLibraryCard(
    product: ProductEntity,
    onSelect: (() -> Unit)?,
    onLoadInCalculator: (() -> Unit)?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val input = remember(product) { product.toCalculationInput() }
    val result = remember(input) { ProfitCalculationEngine.calculate(input) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onSelect != null) Modifier.clickable { onSelect() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: Product Name & Action Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.productName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (product.supplierName.isNotBlank()) {
                        Text(
                            text = "Supplier: ${product.supplierName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Row 2: Price & Economics Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PTR: ₹${product.ptr} | MRP: ₹${product.sellingPrice}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${product.unitsPerPack} ${product.unitName}/Pack (${product.packName})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (result.isValid) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Text(
                            text = "Margin: ${result.roundedProfitMargin?.toPlainString() ?: "0.00"}%",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Badges if discount or offer active
            if (product.isSupplierDiscountEnabled || product.isSupplierOfferEnabled) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (product.isSupplierDiscountEnabled) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ) {
                            Text(
                                text = "${product.supplierDiscountPercent}% Disc",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (product.isSupplierOfferEnabled) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ) {
                            Text(
                                text = "Buy ${product.offerBuyQuantity} + ${product.offerFreeQuantity} Free",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Action row
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (onSelect != null) {
                    Button(
                        onClick = onSelect,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Select Product", style = MaterialTheme.typography.labelMedium)
                    }
                }

                if (onLoadInCalculator != null) {
                    OutlinedButton(
                        onClick = onLoadInCalculator,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp).padding(end = 4.dp)
                        )
                        Text("Open Calculator", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductEditorDialog(
    initialProduct: ProductEntity,
    isEdit: Boolean,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct.productName) }
    var supplier by remember { mutableStateOf(initialProduct.supplierName) }
    var ptrText by remember { mutableStateOf(initialProduct.ptr) }
    var sellingPriceText by remember { mutableStateOf(initialProduct.sellingPrice) }
    var purchaseTaxText by remember { mutableStateOf(initialProduct.purchaseTaxRate) }
    var sellingTaxText by remember { mutableStateOf(initialProduct.sellingTaxRate) }
    var unitsPerPackText by remember { mutableStateOf(initialProduct.unitsPerPack.toString()) }
    var packName by remember { mutableStateOf(initialProduct.packName) }
    var unitName by remember { mutableStateOf(initialProduct.unitName) }

    var isDiscountOn by remember { mutableStateOf(initialProduct.isSupplierDiscountEnabled) }
    var discountText by remember { mutableStateOf(initialProduct.supplierDiscountPercent) }

    var isOfferOn by remember { mutableStateOf(initialProduct.isSupplierOfferEnabled) }
    var buyQtyText by remember { mutableStateOf(initialProduct.offerBuyQuantity.toString()) }
    var freeQtyText by remember { mutableStateOf(initialProduct.offerFreeQuantity.toString()) }

    var purchasePriceMode by remember {
        mutableStateOf(try { TaxPriceMode.valueOf(initialProduct.purchasePriceMode) } catch (_: Exception) { TaxPriceMode.TAX_EXCLUSIVE })
    }
    var sellingPriceMode by remember {
        mutableStateOf(try { TaxPriceMode.valueOf(initialProduct.sellingPriceMode) } catch (_: Exception) { TaxPriceMode.TAX_INCLUSIVE })
    }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isEdit) "Edit Saved Product" else "New Product")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = supplier,
                    onValueChange = { supplier = it },
                    label = { Text("Supplier Name (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ptrText,
                        onValueChange = { ptrText = it },
                        label = { Text("PTR (₹)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = purchaseTaxText,
                        onValueChange = { purchaseTaxText = it },
                        label = { Text("Tax %") },
                        modifier = Modifier.weight(0.8f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sellingPriceText,
                        onValueChange = { sellingPriceText = it },
                        label = { Text("MRP / Selling (₹)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = sellingTaxText,
                        onValueChange = { sellingTaxText = it },
                        label = { Text("Tax %") },
                        modifier = Modifier.weight(0.8f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = unitsPerPackText,
                        onValueChange = { unitsPerPackText = it },
                        label = { Text("Units / Pack") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = packName,
                        onValueChange = { packName = it },
                        label = { Text("Pack Type") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Supplier Discount Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Supplier Discount", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isDiscountOn, onCheckedChange = { isDiscountOn = it })
                }
                if (isDiscountOn) {
                    OutlinedTextField(
                        value = discountText,
                        onValueChange = { discountText = it },
                        label = { Text("Discount (%)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Buy X Get Y Free Offer Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Buy X, Get Y Free Offer", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isOfferOn, onCheckedChange = { isOfferOn = it })
                }
                if (isOfferOn) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = buyQtyText,
                            onValueChange = { buyQtyText = it },
                            label = { Text("Buy Qty") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = freeQtyText,
                            onValueChange = { freeQtyText = it },
                            label = { Text("Free Qty") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedUnits = unitsPerPackText.toIntOrNull() ?: 1
                    val parsedBuy = buyQtyText.toIntOrNull() ?: 1
                    val parsedFree = freeQtyText.toIntOrNull() ?: 0

                    val updated = initialProduct.copy(
                        productName = name.trim(),
                        supplierName = supplier.trim(),
                        ptr = ptrText.trim().ifBlank { "0.00" },
                        purchaseTaxRate = purchaseTaxText.trim().ifBlank { "5.00" },
                        purchasePriceMode = purchasePriceMode.name,
                        sellingPrice = sellingPriceText.trim().ifBlank { "0.00" },
                        sellingTaxRate = sellingTaxText.trim().ifBlank { "5.00" },
                        sellingPriceMode = sellingPriceMode.name,
                        packName = packName.trim().ifBlank { "Pack" },
                        unitName = unitName.trim().ifBlank { "Unit" },
                        unitsPerPack = if (parsedUnits > 0) parsedUnits else 1,
                        isSupplierDiscountEnabled = isDiscountOn,
                        supplierDiscountPercent = discountText.trim().ifBlank { "0.00" },
                        isSupplierOfferEnabled = isOfferOn,
                        offerBuyQuantity = if (parsedBuy > 0) parsedBuy else 1,
                        offerFreeQuantity = if (parsedFree >= 0) parsedFree else 0
                    )
                    onSave(updated)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
