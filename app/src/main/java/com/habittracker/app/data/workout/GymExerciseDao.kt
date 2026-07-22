package com.habittracker.app.data.workout

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GymExerciseDao {
    @Query("SELECT * FROM gym_log ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<GymExerciseLog>>

    @Insert
    suspend fun insert(entry: GymExerciseLog): Long

    @Delete
    suspend fun delete(entry: GymExerciseLog)
}
