package com.habittracker.app.data.smoking

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cigarette_purchase")
data class CigarettePurchase(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMillis: Long,
    val packsBought: Int,
    val pricePerPack: Double,
    val sticksPerPack: Int
)
