package com.margiq.data.repository

import com.margiq.data.local.CalculationDao
import com.margiq.data.local.CalculationEntity
import com.margiq.domain.model.CalculationInput
import com.margiq.domain.model.CalculationResult
import kotlinx.coroutines.flow.Flow

class CalculationRepository(private val dao: CalculationDao) {

    fun getAllCalculations(): Flow<List<CalculationEntity>> = dao.getAllCalculations()

    fun searchCalculations(query: String): Flow<List<CalculationEntity>> = dao.searchCalculations(query)

    suspend fun getCalculationById(id: Long): CalculationEntity? = dao.getCalculationById(id)

    suspend fun saveCalculation(
        input: CalculationInput,
        result: CalculationResult,
        existingId: Long = 0
    ): Long {
        val entity = CalculationEntity.fromInputAndResult(input, result, id = existingId)
        return dao.insertCalculation(entity)
    }

    suspend fun deleteCalculation(entity: CalculationEntity) {
        dao.deleteCalculation(entity)
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun clearAll() {
        dao.clearAllCalculations()
    }
}
