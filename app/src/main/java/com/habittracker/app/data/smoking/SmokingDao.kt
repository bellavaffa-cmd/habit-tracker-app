package com.habittracker.app.data.smoking

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SmokingDao {
    @Query("SELECT * FROM smoking_log ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<SmokingLog>>

    @Insert
    suspend fun insert(entry: SmokingLog): Long

    @Delete
    suspend fun delete(entry: SmokingLog)
}
