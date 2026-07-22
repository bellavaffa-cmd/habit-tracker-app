package com.habittracker.app.data.workout

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_log ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<WorkoutLog>>

    @Insert
    suspend fun insert(entry: WorkoutLog): Long

    @Delete
    suspend fun delete(entry: WorkoutLog)
}
