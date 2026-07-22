package com.habittracker.app.data.calories

import kotlinx.coroutines.flow.Flow

class CalorieLogRepository(private val dao: CalorieLogDao) {
    val entries: Flow<List<CalorieLog>> = dao.observeAll()

    suspend fun logEntry(
        photoPath: String?,
        foodDescription: String,
        calories: Int,
        proteinGrams: Double,
        carbsGrams: Double,
        fatGrams: Double
    ) {
        dao.insert(
            CalorieLog(
                timestampMillis = System.currentTimeMillis(),
                photoPath = photoPath,
                foodDescription = foodDescription,
                calories = calories,
                proteinGrams = proteinGrams,
                carbsGrams = carbsGrams,
                fatGrams = fatGrams
            )
        )
    }

    suspend fun delete(entry: CalorieLog) {
        dao.delete(entry)
    }
}
