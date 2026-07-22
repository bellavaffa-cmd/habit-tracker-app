package com.habittracker.app.data.calories

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CalorieLogDao {
    @Query("SELECT * FROM calorie_log ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<CalorieLog>>

    @Insert
    suspend fun insert(entry: CalorieLog): Long

    @Delete
    suspend fun delete(entry: CalorieLog)
}
