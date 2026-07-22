package com.habittracker.app.data.profile

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.profileDataStore by preferencesDataStore(name = "user_profile")
private val KEY_SEX = stringPreferencesKey("sex")
private val KEY_HEIGHT_CM = floatPreferencesKey("height_cm")
private val KEY_WEIGHT_KG = floatPreferencesKey("weight_kg")
private val KEY_AGE = intPreferencesKey("age")

data class UserProfile(
    val sex: String? = null,
    val heightCm: Float? = null,
    val weightKg: Float? = null,
    val age: Int? = null
) {
    val bmi: Double?
        get() {
            val h = heightCm
            val w = weightKg
            if (h == null || h <= 0f || w == null || w <= 0f) return null
            val heightM = h / 100.0
            return w / (heightM * heightM)
        }
}

fun bmiCategory(bmi: Double): String = when {
    bmi < 18.5 -> "Underweight"
    bmi < 25.0 -> "Normal weight"
    bmi < 30.0 -> "Overweight"
    else -> "Obese"
}

/** The user's basic profile (sex, height, weight, age) — used to compute general health metrics like BMI. */
class UserProfileRepository(private val context: Context) {
    val profile: Flow<UserProfile> = context.profileDataStore.data.map { prefs ->
        UserProfile(
            sex = prefs[KEY_SEX],
            heightCm = prefs[KEY_HEIGHT_CM],
            weightKg = prefs[KEY_WEIGHT_KG],
            age = prefs[KEY_AGE]
        )
    }

    suspend fun save(sex: String, heightCm: Float, weightKg: Float, age: Int) {
        context.profileDataStore.edit { prefs ->
            prefs[KEY_SEX] = sex
            prefs[KEY_HEIGHT_CM] = heightCm
            prefs[KEY_WEIGHT_KG] = weightKg
            prefs[KEY_AGE] = age
        }
    }
}
