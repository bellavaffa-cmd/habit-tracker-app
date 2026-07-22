package com.habittracker.app.data.smoking

enum class QuitPlanType {
    /** Minimum minutes between cigarettes ramps up from a starting value toward the quit date. */
    INTERVAL_TAPER,

    /** Daily cigarette allowance steps down from a starting count to zero by the quit date. */
    DAILY_COUNT_TAPER,

    /** No gradual steps — just a target quit date. */
    COLD_TURKEY
}
