package com.habittracker.app.data.workout

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gym_log")
data class GymExerciseLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMillis: Long,
    val exerciseName: String,
    val muscleGroup: String,
    val sets: Int,
    val reps: Int,
    val weightKg: Float
)
