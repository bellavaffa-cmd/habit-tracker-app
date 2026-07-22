package com.habittracker.app.data.smoking

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CigarettePurchaseDao {
    @Query("SELECT * FROM cigarette_purchase ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<CigarettePurchase>>

    @Insert
    suspend fun insert(purchase: CigarettePurchase): Long

    @Delete
    suspend fun delete(purchase: CigarettePurchase)
}
