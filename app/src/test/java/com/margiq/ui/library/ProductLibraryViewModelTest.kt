package com.margiq.ui.library

import com.margiq.data.local.ProductDao
import com.margiq.data.local.ProductEntity
import com.margiq.data.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductLibraryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeDao: FakeProductDao
    private lateinit var repository: ProductRepository
    private lateinit var viewModel: ProductLibraryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeProductDao()
        repository = ProductRepository(fakeDao)
        viewModel = ProductLibraryViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testAddAndSearchProduct() = runTest {
        val prod1 = ProductEntity(productName = "Paracetamol 650", supplierName = "Apex")
        val prod2 = ProductEntity(productName = "Cetirizine 10", supplierName = "Cipla")

        viewModel.saveProduct(prod1)
        viewModel.saveProduct(prod2)
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("Paracetamol")
        advanceUntilIdle()

        assertEquals("Paracetamol", viewModel.searchQuery.value)
    }

    @Test
    fun testDeleteProduct() = runTest {
        val prod = ProductEntity(id = 1, productName = "Metformin 500")
        fakeDao.items.add(prod)

        viewModel.requestDelete(prod)
        assertEquals(prod, viewModel.uiState.value.productToDelete)

        viewModel.confirmDelete()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.productToDelete)
        assertTrue(fakeDao.items.isEmpty())
    }

    @Test
    fun testOpenAndDismissEditor() {
        viewModel.openNewProductEditor()
        assertTrue(viewModel.uiState.value.isEditorOpen)
        assertFalse(viewModel.uiState.value.isEditMode)

        viewModel.dismissEditor()
        assertFalse(viewModel.uiState.value.isEditorOpen)
    }

    private class FakeProductDao : ProductDao {
        val items = mutableListOf<ProductEntity>()
        var idCounter = 1L

        override fun getAllProducts(): Flow<List<ProductEntity>> = flowOf(items)
        override fun searchProducts(query: String): Flow<List<ProductEntity>> =
            flowOf(items.filter { it.productName.contains(query, ignoreCase = true) || it.supplierName.contains(query, ignoreCase = true) })
        override suspend fun getProductById(id: Long): ProductEntity? = items.find { it.id == id }
        override suspend fun getProductByNameAndSupplier(name: String, supplier: String): ProductEntity? =
            items.find { it.productName.equals(name, ignoreCase = true) && it.supplierName.equals(supplier, ignoreCase = true) }
        override suspend fun insertProduct(entity: ProductEntity): Long {
            val id = if (entity.id > 0) entity.id else idCounter++
            items.removeAll { it.id == id }
            items.add(entity.copy(id = id))
            return id
        }
        override suspend fun updateProduct(entity: ProductEntity): Int {
            items.removeAll { it.id == entity.id }
            items.add(entity)
            return 1
        }
        override suspend fun deleteProduct(entity: ProductEntity): Int {
            return if (items.removeAll { it.id == entity.id }) 1 else 0
        }
        override suspend fun deleteById(id: Long): Int {
            return if (items.removeAll { it.id == id }) 1 else 0
        }
        override suspend fun clearAllProducts(): Int {
            val count = items.size
            items.clear()
            return count
        }
    }
}
