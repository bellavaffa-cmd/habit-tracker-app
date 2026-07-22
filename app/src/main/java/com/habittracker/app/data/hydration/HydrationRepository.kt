package com.habittracker.app.data.hydration

import kotlinx.coroutines.flow.Flow

class HydrationRepository(private val dao: HydrationDao) {
    val entries: Flow<List<HydrationLog>> = dao.observeAll()

    suspend fun log(amountMl: Int) {
        dao.insert(HydrationLog(timestampMillis = System.currentTimeMillis(), amountMl = amountMl))
    }

    suspend fun delete(entry: HydrationLog) {
        dao.delete(entry)
    }
}
