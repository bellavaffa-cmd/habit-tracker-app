package com.habittracker.app.data.workout

import kotlinx.coroutines.flow.Flow

class WorkoutRepository(private val dao: WorkoutDao) {
    val entries: Flow<List<WorkoutLog>> = dao.observeAll()

    suspend fun logWorkout(type: String, durationMinutes: Int, notes: String?) {
        dao.insert(
            WorkoutLog(
                timestampMillis = System.currentTimeMillis(),
                type = type,
                durationMinutes = durationMinutes,
                notes = notes?.takeIf { it.isNotBlank() }
            )
        )
    }

    suspend fun delete(entry: WorkoutLog) {
        dao.delete(entry)
    }
}
