package com.habittracker.app.data.smoking

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CigarettePurchaseRepository(private val dao: CigarettePurchaseDao) {
    val purchases: Flow<List<CigarettePurchase>> = dao.observeAll()
    val latestPurchase: Flow<CigarettePurchase?> = purchases.map { it.firstOrNull() }

    suspend fun logPurchase(packsBought: Int, pricePerPack: Double, sticksPerPack: Int) {
        dao.insert(
            CigarettePurchase(
                timestampMillis = System.currentTimeMillis(),
                packsBought = packsBought,
                pricePerPack = pricePerPack,
                sticksPerPack = sticksPerPack
            )
        )
    }
}
