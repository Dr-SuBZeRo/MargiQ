package com.margiq.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.margiq.data.local.ProductEntity
import com.margiq.data.repository.ProductRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ProductLibraryViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductLibraryUiState())
    val uiState: StateFlow<ProductLibraryUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        viewModelScope.launch {
            _searchQuery.flatMapLatest { query ->
                if (query.isBlank()) {
                    productRepository.allProducts
                } else {
                    productRepository.searchProducts(query.trim())
                }
            }.collect { list ->
                _uiState.update { it.copy(products = list) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun openNewProductEditor() {
        _uiState.update {
            it.copy(
                isEditorOpen = true,
                isEditMode = false,
                editingProduct = ProductEntity(productName = "")
            )
        }
    }

    fun openEditProductEditor(product: ProductEntity) {
        _uiState.update {
            it.copy(
                isEditorOpen = true,
                isEditMode = true,
                editingProduct = product
            )
        }
    }

    fun dismissEditor() {
        _uiState.update {
            it.copy(
                isEditorOpen = false,
                editingProduct = null
            )
        }
    }

    fun saveProduct(product: ProductEntity) {
        if (product.productName.isBlank()) {
            _uiState.update { it.copy(userMessage = "Product name cannot be blank") }
            return
        }

        viewModelScope.launch {
            try {
                productRepository.saveOrUpdateProduct(product)
                _uiState.update {
                    it.copy(
                        isEditorOpen = false,
                        editingProduct = null,
                        userMessage = if (product.id > 0) "Product '${product.productName}' updated" else "Product '${product.productName}' saved to library"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = "Error saving product: ${e.message}") }
            }
        }
    }

    fun requestDelete(product: ProductEntity) {
        _uiState.update { it.copy(productToDelete = product) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(productToDelete = null) }
    }

    fun confirmDelete() {
        val product = _uiState.value.productToDelete ?: return
        viewModelScope.launch {
            try {
                productRepository.deleteProduct(product.id)
                _uiState.update {
                    it.copy(
                        productToDelete = null,
                        userMessage = "Deleted '${product.productName}' from library"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        productToDelete = null,
                        userMessage = "Error deleting product: ${e.message}"
                    )
                }
            }
        }
    }

    fun dismissUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
