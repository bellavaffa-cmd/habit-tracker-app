package com.habittracker.app.data.hydration

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HydrationDao {
    @Query("SELECT * FROM hydration_log ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<HydrationLog>>

    @Insert
    suspend fun insert(entry: HydrationLog): Long

    @Delete
    suspend fun delete(entry: HydrationLog)
}
