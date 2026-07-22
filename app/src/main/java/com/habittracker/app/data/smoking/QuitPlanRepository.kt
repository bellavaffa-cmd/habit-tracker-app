package com.habittracker.app.data.smoking

import kotlinx.coroutines.flow.Flow

class QuitPlanRepository(private val dao: QuitPlanDao) {
    val activePlan: Flow<QuitPlan?> = dao.observeActive()

    suspend fun startPlan(type: QuitPlanType, targetQuitDateMillis: Long, startValue: Int) {
        dao.deactivateAll()
        dao.insert(
            QuitPlan(
                type = type,
                startDateMillis = System.currentTimeMillis(),
                targetQuitDateMillis = targetQuitDateMillis,
                startValue = startValue,
                isActive = true
            )
        )
    }

    suspend fun cancelActivePlan() {
        dao.deactivateAll()
    }
}
