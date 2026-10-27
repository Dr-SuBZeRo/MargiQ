package com.margiq.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products ORDER BY updatedAt DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE productName LIKE '%' || :query || '%' OR supplierName LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE productName = :name AND supplierName = :supplier LIMIT 1")
    suspend fun getProductByNameAndSupplier(name: String, supplier: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(entity: ProductEntity): Long

    @Update
    suspend fun updateProduct(entity: ProductEntity): Int

    @Delete
    suspend fun deleteProduct(entity: ProductEntity): Int

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM products")
    suspend fun clearAllProducts(): Int
}
