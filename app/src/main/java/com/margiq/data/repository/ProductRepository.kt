package com.margiq.data.repository

import com.margiq.data.local.ProductDao
import com.margiq.data.local.ProductEntity
import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val productDao: ProductDao
) {
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()

    fun searchProducts(query: String): Flow<List<ProductEntity>> =
        productDao.searchProducts(query)

    suspend fun getProductById(id: Long): ProductEntity? =
        productDao.getProductById(id)

    suspend fun saveOrUpdateProduct(product: ProductEntity): Long {
        return if (product.id > 0) {
            val existing = productDao.getProductById(product.id)
            if (existing != null) {
                productDao.updateProduct(product.copy(createdAt = existing.createdAt, updatedAt = System.currentTimeMillis()))
                product.id
            } else {
                productDao.insertProduct(product.copy(id = 0, updatedAt = System.currentTimeMillis()))
            }
        } else {
            val existing = productDao.getProductByNameAndSupplier(product.productName.trim(), product.supplierName.trim())
            if (existing != null) {
                productDao.updateProduct(product.copy(id = existing.id, createdAt = existing.createdAt, updatedAt = System.currentTimeMillis()))
                existing.id
            } else {
                productDao.insertProduct(product.copy(updatedAt = System.currentTimeMillis()))
            }
        }
    }

    suspend fun insertProduct(product: ProductEntity): Long =
        productDao.insertProduct(product.copy(updatedAt = System.currentTimeMillis()))

    suspend fun updateProduct(product: ProductEntity): Int =
        productDao.updateProduct(product.copy(updatedAt = System.currentTimeMillis()))

    suspend fun deleteProduct(id: Long): Int =
        productDao.deleteById(id)
}
