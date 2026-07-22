package com.habittracker.app.data.calories

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calorie_log")
data class CalorieLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMillis: Long,
    val photoPath: String?,
    val foodDescription: String,
    val calories: Int,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double
)
