package com.habittracker.app.data.smoking

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quit_plan")
data class QuitPlan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: QuitPlanType,
    val startDateMillis: Long,
    val targetQuitDateMillis: Long,
    /** Starting minutes-between-cigarettes for INTERVAL_TAPER, starting daily count for DAILY_COUNT_TAPER, unused for COLD_TURKEY. */
    val startValue: Int,
    val isActive: Boolean
)
