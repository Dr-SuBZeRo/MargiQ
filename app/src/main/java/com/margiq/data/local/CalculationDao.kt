package com.margiq.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationDao {

    @Query("SELECT * FROM calculations ORDER BY timestamp DESC")
    fun getAllCalculations(): Flow<List<CalculationEntity>>

    @Query("SELECT * FROM calculations WHERE productName LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchCalculations(query: String): Flow<List<CalculationEntity>>

    @Query("SELECT * FROM calculations WHERE id = :id LIMIT 1")
    suspend fun getCalculationById(id: Long): CalculationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(entity: CalculationEntity): Long

    @Update
    suspend fun updateCalculation(entity: CalculationEntity): Int

    @Delete
    suspend fun deleteCalculation(entity: CalculationEntity): Int

    @Query("DELETE FROM calculations WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM calculations")
    suspend fun clearAllCalculations(): Int
}
