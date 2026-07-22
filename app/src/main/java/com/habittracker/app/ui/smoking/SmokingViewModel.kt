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
import com.habittracker.app.data.smoking.SmokingCardId
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PlanProgress(
    val plan: QuitPlan,
    val targetCountToday: Int?,
    val actualCountToday: Int,
    val targetIntervalMinutesToday: Int?,
    val daysRemaining: Long
)

data class DayCount(val label: String, val count: Int)

private val dayLabelFormat = SimpleDateFormat("EEE", Locale.getDefault())

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

    val monthCount: StateFlow<Int> = entries.map { list ->
        val start = StreakUtils.startOfMonth()
        list.count { it.timestampMillis >= start }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val previousWeekCount: StateFlow<Int> = entries.map { list ->
        val start = StreakUtils.startOfPreviousWeek()
        val end = StreakUtils.startOfWeek()
        list.count { it.timestampMillis in start until end }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val last7DaysBreakdown: StateFlow<List<DayCount>> = entries.map { list ->
        (6 downTo 0).map { daysAgo ->
            val start = StreakUtils.startOfDay(daysAgo)
            val end = start + 24 * 60 * 60 * 1000
            val count = list.count { it.timestampMillis in start until end }
            val label = if (daysAgo == 0) "Today" else dayLabelFormat.format(Date(start))
            DayCount(label, count)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    val purchases: StateFlow<List<CigarettePurchase>> = purchaseRepository.purchases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestPurchase: StateFlow<CigarettePurchase?> = purchaseRepository.latestPurchase
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val costPerCigarette: StateFlow<Double?> = latestPurchase
        .map { it?.let { p -> p.pricePerPack / p.sticksPerPack } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Packs actually bought within each period. */
    val packsPurchasedThisWeek: StateFlow<Int> = purchases.map { list ->
        val start = StreakUtils.startOfWeek()
        list.filter { it.timestampMillis >= start }.sumOf { it.packsBought }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val packsPurchasedThisMonth: StateFlow<Int> = purchases.map { list ->
        val start = StreakUtils.startOfMonth()
        list.filter { it.timestampMillis >= start }.sumOf { it.packsBought }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** Money actually paid out on purchases within each period (not an estimate). */
    val costThisWeek: StateFlow<Double> = purchases.map { list ->
        val start = StreakUtils.startOfWeek()
        list.filter { it.timestampMillis >= start }.sumOf { it.packsBought * it.pricePerPack }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val costThisMonth: StateFlow<Double> = purchases.map { list ->
        val start = StreakUtils.startOfMonth()
        list.filter { it.timestampMillis >= start }.sumOf { it.packsBought * it.pricePerPack }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    /** Estimated value of what was smoked (count * cost/cigarette) — used for week-over-week trend, since actual purchases are lumpy. */
    val weekSpendEstimate: StateFlow<Double?> = combine(weekCount, costPerCigarette) { count, cost ->
        cost?.let { count * it }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val previousWeekSpendEstimate: StateFlow<Double?> = combine(previousWeekCount, costPerCigarette) { count, cost ->
        cost?.let { count * it }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currencySymbol: StateFlow<String> = settingsRepository.currencySymbol
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "$")

    val cardOrder: StateFlow<List<SmokingCardId>> = settingsRepository.cardOrder
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SmokingCardId.entries.toList())

    val hiddenCards: StateFlow<Set<SmokingCardId>> = settingsRepository.hiddenCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

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

    fun setCurrencySymbol(symbol: String) {
        viewModelScope.launch { settingsRepository.setCurrencySymbol(symbol) }
    }

    fun logPurchase(packsBought: Int, pricePerPack: Double, sticksPerPack: Int) {
        viewModelScope.launch { purchaseRepository.logPurchase(packsBought, pricePerPack, sticksPerPack) }
    }

    fun deletePurchase(purchase: CigarettePurchase) {
        viewModelScope.launch { purchaseRepository.delete(purchase) }
    }

    fun moveCard(id: SmokingCardId, direction: Int) {
        viewModelScope.launch {
            val current = cardOrder.value.toMutableList()
            val index = current.indexOf(id)
            val newIndex = index + direction
            if (index !in current.indices || newIndex !in current.indices) return@launch
            val moved = current[newIndex]
            current[newIndex] = current[index]
            current[index] = moved
            settingsRepository.setCardOrder(current)
        }
    }

    fun setCardHidden(id: SmokingCardId, hidden: Boolean) {
        viewModelScope.launch { settingsRepository.setCardHidden(id, hidden) }
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
