package com.example.data

import com.example.model.CalculationHistory
import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val dao: HistoryDao) {
    val allHistory: Flow<List<CalculationHistory>> = dao.getAllHistory()
    val totalCalculationsCount: Flow<Int> = dao.getCount()

    fun searchHistory(query: String): Flow<List<CalculationHistory>> =
        dao.searchHistory(query)

    suspend fun insert(expression: String, result: String, category: String = "General"): Long {
        if (expression.isBlank() || result.isBlank()) return -1
        val item = CalculationHistory(
            expression = expression.trim(),
            result = result.trim(),
            timestamp = System.currentTimeMillis(),
            category = category
        )
        return dao.insert(item)
    }

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun delete(item: CalculationHistory) = dao.deleteById(item.id)

    suspend fun clearAll() = dao.clearAll()
}
