package com.habittracker.app.data.workout

const val DEFAULT_WEIGHT_KG = 70f

private val metByType = mapOf(
    "Cardio" to 7.0,
    "Strength" to 6.0,
    "Yoga" to 3.0,
    "Walking" to 3.5,
    "Cycling" to 7.5,
    "Swimming" to 6.0,
    "Other" to 4.0
)

/**
 * Rough calorie-burn estimate — not medical advice. Uses standard MET (metabolic equivalent of
 * task) values per workout type: calories = MET * weight(kg) * duration(hours).
 */
fun caloriesBurnedFor(type: String, durationMinutes: Int, weightKg: Float): Int {
    val met = metByType[type] ?: metByType.getValue("Other")
    return (met * weightKg * (durationMinutes / 60.0)).toInt()
}
