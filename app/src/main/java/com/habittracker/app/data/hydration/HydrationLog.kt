package com.habittracker.app.data.hydration

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hydration_log")
data class HydrationLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMillis: Long,
    val amountMl: Int
)
