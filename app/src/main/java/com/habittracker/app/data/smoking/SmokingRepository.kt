package com.habittracker.app.data.smoking

import kotlinx.coroutines.flow.Flow

class SmokingRepository(private val dao: SmokingDao) {
    val entries: Flow<List<SmokingLog>> = dao.observeAll()

    suspend fun logNow(note: String? = null) {
        dao.insert(SmokingLog(timestampMillis = System.currentTimeMillis(), note = note))
    }

    suspend fun delete(entry: SmokingLog) {
        dao.delete(entry)
    }
}
