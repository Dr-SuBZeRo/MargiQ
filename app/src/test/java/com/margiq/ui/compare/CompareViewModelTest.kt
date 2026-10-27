package com.margiq.ui.compare

import com.margiq.data.local.ProductDao
import com.margiq.data.local.ProductEntity
import com.margiq.data.repository.ProductRepository
import com.margiq.domain.comparison.WinnerSide
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CompareViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeDao: FakeProductDao
    private lateinit var repository: ProductRepository
    private lateinit var viewModel: CompareViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeProductDao()
        repository = ProductRepository(fakeDao)
        viewModel = CompareViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSwitchTabs() {
        viewModel.selectTab(CompareTab.SUPPLIERS)
        assertEquals(CompareTab.SUPPLIERS, viewModel.uiState.value.activeTab)

        viewModel.selectTab(CompareTab.PRODUCTS)
        assertEquals(CompareTab.PRODUCTS, viewModel.uiState.value.activeTab)
    }

    @Test
    fun testLoadProductIntoSidesAndCompare() {
        val prodA = ProductEntity(
            id = 1,
            productName = "Product A",
            ptr = "40.00",
            sellingPrice = "100.00",
            unitsPerPack = 10
        )
        val prodB = ProductEntity(
            id = 2,
            productName = "Product B",
            ptr = "50.00",
            sellingPrice = "100.00",
            unitsPerPack = 10
        )

        viewModel.loadProductIntoSideA(prodA)
        viewModel.loadProductIntoSideB(prodB)

        val outcome = viewModel.uiState.value.productsComparison
        assertNotNull(outcome)
        assertTrue(outcome!!.isValid)
        assertEquals(WinnerSide.SIDE_A, outcome.highlight!!.lowestCostSide)
    }

    @Test
    fun testSaveProductFromSide() = runTest {
        viewModel.updateSideA { it.copy(productName = "New Reusable Drug", ptrText = "60.00") }
        viewModel.saveProductFromSide(isSideA = true)
        advanceUntilIdle()

        assertEquals(1, fakeDao.items.size)
        assertEquals("New Reusable Drug", fakeDao.items[0].productName)
    }

    @Test
    fun testSupplierOfferComparisonAndSaving() = runTest {
        viewModel.updateSupplierBase { it.copy(productName = "Amoxicillin", sellingPriceText = "200.00") }
        viewModel.updateSupplierA { it.copy(supplierName = "Distributor Alpha", ptrText = "90.00") }
        viewModel.updateSupplierB { it.copy(supplierName = "Distributor Beta", ptrText = "95.00") }

        val outcome = viewModel.uiState.value.suppliersComparison
        assertNotNull(outcome)
        assertTrue(outcome!!.isValid)
        // Supplier B has Buy 10 Get 2 Free offer, making effective cost per unit cheaper
        assertEquals(WinnerSide.SIDE_B, outcome.highlight!!.lowestCostSide)

        viewModel.saveSupplierOfferAsProduct(isSupplierA = true)
        advanceUntilIdle()

        assertEquals(1, fakeDao.items.size)
        assertEquals("Amoxicillin", fakeDao.items[0].productName)
        assertEquals("Distributor Alpha", fakeDao.items[0].supplierName)
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
