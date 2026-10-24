package com.example.profitmargincalculator.data.repository

import com.example.profitmargincalculator.data.local.CalculationDao
import com.example.profitmargincalculator.data.local.CalculationEntity
import com.example.profitmargincalculator.domain.model.CalculationInput
import com.example.profitmargincalculator.domain.model.CalculationResult
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
