package com.habittracker.app.ui.common

import java.util.Calendar

/**
 * Shared by every tracker (smoking/drinking/workout/calories) for streak and
 * period-count math, so each tracker's ViewModel just supplies its own timestamp list.
 */
object StreakUtils {
    fun startOfToday(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun startOfWeek(): Long {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        if (cal.timeInMillis > System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, -7)
        }
        return cal.timeInMillis
    }

    fun startOfMonth(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun startOfPreviousWeek(): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = startOfWeek()
        cal.add(Calendar.DAY_OF_YEAR, -7)
        return cal.timeInMillis
    }

    /** Start-of-day timestamp for [daysAgo] days before today (0 = today). */
    fun startOfDay(daysAgo: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /** Formats an elapsed duration in millis as e.g. "2d 4h 15m" (drops leading zero units). */
    fun formatElapsed(millis: Long): String {
        if (millis < 60_000) return "<1m"
        val totalMinutes = millis / 60_000
        val days = totalMinutes / (60 * 24)
        val hours = (totalMinutes / 60) % 24
        val minutes = totalMinutes % 60
        return buildString {
            if (days > 0) append("${days}d ")
            if (days > 0 || hours > 0) append("${hours}h ")
            append("${minutes}m")
        }
    }

    /** Like [formatElapsed] but keeps seconds precision, e.g. "4h 15m 03s" — for a live-ticking timer. */
    fun formatElapsedWithSeconds(millis: Long): String {
        if (millis < 0) return "0s"
        val totalSeconds = millis / 1000
        val days = totalSeconds / (60 * 60 * 24)
        val hours = (totalSeconds / 3600) % 24
        val minutes = (totalSeconds / 60) % 60
        val seconds = totalSeconds % 60
        return buildString {
            if (days > 0) append("${days}d ")
            if (days > 0 || hours > 0) append("${hours}h ")
            if (days > 0 || hours > 0 || minutes > 0) append("${minutes}m ")
            append("%02ds".format(seconds))
        }
    }
}
