package com.habittracker.app.ui.smoking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.habittracker.app.data.smoking.CigarettePurchase
import com.habittracker.app.data.smoking.CigarettePurchaseRepository
import com.habittracker.app.data.smoking.QuitPlan
import com.habittracker.app.data.smoking.QuitPlanCalculator
import com.habittracker.app.data.smoking.QuitPlanRepository
import com.habittracker.app.data.smoking.QuitPlanType
import com.habittracker.app.data.smoking.SmokingLog
import com.habittracker.app.data.smoking.SmokingRepository
import com.habittracker.app.data.smoking.SmokingSettingsRepository
import com.habittracker.app.notifications.SmokingReminderScheduler
import com.habittracker.app.ui.common.StreakUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlanProgress(
    val plan: QuitPlan,
    val targetCountToday: Int?,
    val actualCountToday: Int,
    val targetIntervalMinutesToday: Int?,
    val daysRemaining: Long
)

class SmokingViewModel(
    application: Application,
    private val repository: SmokingRepository,
    private val settingsRepository: SmokingSettingsRepository,
    private val purchaseRepository: CigarettePurchaseRepository,
    private val quitPlanRepository: QuitPlanRepository
) : AndroidViewModel(application) {

    val entries: StateFlow<List<SmokingLog>> = repository.entries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayCount: StateFlow<Int> = entries.map { list ->
        val start = StreakUtils.startOfToday()
        list.count { it.timestampMillis >= start }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val weekCount: StateFlow<Int> = entries.map { list ->
        val start = StreakUtils.startOfWeek()
        list.count { it.timestampMillis >= start }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val lastLogTimestamp: StateFlow<Long?> = entries
        .map { list -> list.maxOfOrNull { it.timestampMillis } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val manualIntervalMinutes: StateFlow<Int?> = settingsRepository.intervalMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activePlan: StateFlow<QuitPlan?> = quitPlanRepository.activePlan
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** The interval actually in effect: an INTERVAL_TAPER plan overrides the manual setting. */
    val effectiveIntervalMinutes: StateFlow<Int?> = combine(manualIntervalMinutes, activePlan) { manual, plan ->
        if (plan != null && plan.type == QuitPlanType.INTERVAL_TAPER) {
            QuitPlanCalculator.todayTargetIntervalMinutes(plan, System.currentTimeMillis())
        } else {
            manual
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val nextAllowedTimestamp: StateFlow<Long?> = combine(lastLogTimestamp, effectiveIntervalMinutes) { last, interval ->
        if (last == null || interval == null) null else last + interval * 60_000L
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val planProgress: StateFlow<PlanProgress?> = combine(activePlan, todayCount) { plan, today ->
        plan?.let {
            val now = System.currentTimeMillis()
            PlanProgress(
                plan = it,
                targetCountToday = if (it.type == QuitPlanType.DAILY_COUNT_TAPER) QuitPlanCalculator.todayTargetCount(it, now) else null,
                actualCountToday = today,
                targetIntervalMinutesToday = if (it.type == QuitPlanType.INTERVAL_TAPER) QuitPlanCalculator.todayTargetIntervalMinutes(it, now) else null,
                daysRemaining = QuitPlanCalculator.daysRemaining(it, now)
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val latestPurchase: StateFlow<CigarettePurchase?> = purchaseRepository.latestPurchase
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val costPerCigarette: StateFlow<Double?> = latestPurchase
        .map { it?.let { p -> p.pricePerPack / p.sticksPerPack } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val spentToday: StateFlow<Double?> = combine(todayCount, costPerCigarette) { count, cost ->
        cost?.let { count * it }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val spentWeek: StateFlow<Double?> = combine(weekCount, costPerCigarette) { count, cost ->
        cost?.let { count * it }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun logNow() {
        viewModelScope.launch {
            repository.logNow()

            val plan = quitPlanRepository.activePlan.first()
            val manual = settingsRepository.intervalMinutes.first()
            val interval = if (plan != null && plan.type == QuitPlanType.INTERVAL_TAPER) {
                QuitPlanCalculator.todayTargetIntervalMinutes(plan, System.currentTimeMillis())
            } else {
                manual
            }

            val app = getApplication<Application>()
            if (interval != null) {
                SmokingReminderScheduler.scheduleIn(app, interval * 60_000L)
            } else {
                SmokingReminderScheduler.cancel(app)
            }
        }
    }

    fun delete(entry: SmokingLog) {
        viewModelScope.launch { repository.delete(entry) }
    }

    fun setManualIntervalMinutes(minutes: Int?) {
        viewModelScope.launch { settingsRepository.setIntervalMinutes(minutes) }
    }

    fun logPurchase(packsBought: Int, pricePerPack: Double, sticksPerPack: Int) {
        viewModelScope.launch { purchaseRepository.logPurchase(packsBought, pricePerPack, sticksPerPack) }
    }

    fun startPlan(type: QuitPlanType, weeksToQuit: Int, startValue: Int) {
        viewModelScope.launch {
            val target = System.currentTimeMillis() + weeksToQuit * 7L * 24 * 60 * 60 * 1000
            quitPlanRepository.startPlan(type, target, startValue)
        }
    }

    fun cancelPlan() {
        viewModelScope.launch { quitPlanRepository.cancelActivePlan() }
    }

    class Factory(
        private val application: Application,
        private val repository: SmokingRepository,
        private val settingsRepository: SmokingSettingsRepository,
        private val purchaseRepository: CigarettePurchaseRepository,
        private val quitPlanRepository: QuitPlanRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SmokingViewModel(application, repository, settingsRepository, purchaseRepository, quitPlanRepository) as T
        }
    }
}
