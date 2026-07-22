package com.habittracker.app.data.hydration

import com.habittracker.app.data.profile.UserProfile

/**
 * Rough daily fluid-intake estimate — not medical advice. Prefers a bodyweight-based formula
 * (~33ml per kg, a commonly cited general guideline) when the profile has a weight; falls back
 * to broad sex-based averages, then a generic default, when it doesn't.
 */
fun recommendedDailyMl(profile: UserProfile): Int {
    val weight = profile.weightKg
    if (weight != null && weight > 0f) {
        return (weight * 33).toInt()
    }
    return when (profile.sex) {
        "Male" -> 3000
        "Female" -> 2200
        else -> 2500
    }
}
