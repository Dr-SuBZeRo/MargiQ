package com.margiq.ui.library

import com.margiq.data.local.ProductEntity

data class ProductLibraryUiState(
    val products: List<ProductEntity> = emptyList(),
    val searchQuery: String = "",
    val isEditMode: Boolean = false,
    val editingProduct: ProductEntity? = null,
    val isEditorOpen: Boolean = false,
    val productToDelete: ProductEntity? = null,
    val userMessage: String? = null
)
