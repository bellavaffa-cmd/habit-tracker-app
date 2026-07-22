package com.habittracker.app.data.smoking

import kotlin.math.roundToInt

object QuitPlanCalculator {
    /** How far along [plan] is between its start and target quit date, clamped to 0f..1f. */
    private fun progressFraction(plan: QuitPlan, now: Long): Float {
        val total = (plan.targetQuitDateMillis - plan.startDateMillis).toFloat()
        if (total <= 0f) return 1f
        val elapsed = (now - plan.startDateMillis).toFloat()
        return (elapsed / total).coerceIn(0f, 1f)
    }

    /** DAILY_COUNT_TAPER: today's allowed cigarette count, linearly stepping from startValue down to 0. */
    fun todayTargetCount(plan: QuitPlan, now: Long): Int {
        val fraction = progressFraction(plan, now)
        return (plan.startValue * (1f - fraction)).roundToInt()
    }

    /** INTERVAL_TAPER: today's minimum minutes between cigarettes, ramping from startValue up to a practical 12h ceiling. */
    fun todayTargetIntervalMinutes(plan: QuitPlan, now: Long): Int {
        val fraction = progressFraction(plan, now)
        val ceilingMinutes = 12 * 60
        return (plan.startValue + (ceilingMinutes - plan.startValue) * fraction).roundToInt()
    }

    fun daysRemaining(plan: QuitPlan, now: Long): Long {
        val millisLeft = plan.targetQuitDateMillis - now
        return if (millisLeft <= 0) 0 else millisLeft / (24 * 60 * 60 * 1000)
    }
}
