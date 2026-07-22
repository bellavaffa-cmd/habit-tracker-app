package com.habittracker.app.data.smoking

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuitPlanDao {
    @Query("SELECT * FROM quit_plan WHERE isActive = 1 LIMIT 1")
    fun observeActive(): Flow<QuitPlan?>

    @Insert
    suspend fun insert(plan: QuitPlan): Long

    @Query("UPDATE quit_plan SET isActive = 0 WHERE isActive = 1")
    suspend fun deactivateAll()
}
