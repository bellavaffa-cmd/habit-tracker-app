package com.habittracker.app.data.smoking

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "smoking_log")
data class SmokingLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMillis: Long,
    val note: String? = null
)
