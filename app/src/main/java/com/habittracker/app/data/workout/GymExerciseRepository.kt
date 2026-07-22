package com.habittracker.app.data.workout

import kotlinx.coroutines.flow.Flow

class GymExerciseRepository(private val dao: GymExerciseDao) {
    val entries: Flow<List<GymExerciseLog>> = dao.observeAll()

    suspend fun logExercise(exerciseName: String, muscleGroup: String, sets: Int, reps: Int, weightKg: Float) {
        dao.insert(
            GymExerciseLog(
                timestampMillis = System.currentTimeMillis(),
                exerciseName = exerciseName,
                muscleGroup = muscleGroup,
                sets = sets,
                reps = reps,
                weightKg = weightKg
            )
        )
    }

    suspend fun delete(entry: GymExerciseLog) {
        dao.delete(entry)
    }
}
