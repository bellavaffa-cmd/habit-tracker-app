package com.habittracker.app.data.smoking

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.smokingDataStore by preferencesDataStore(name = "smoking_settings")
private val KEY_INTERVAL_MINUTES = intPreferencesKey("interval_minutes")

/** The user's manually-set minimum interval (minutes) between cigarettes. Null = not set. */
class SmokingSettingsRepository(private val context: Context) {
    val intervalMinutes: Flow<Int?> = context.smokingDataStore.data.map { prefs ->
        prefs[KEY_INTERVAL_MINUTES]?.takeIf { it > 0 }
    }

    suspend fun setIntervalMinutes(minutes: Int?) {
        context.smokingDataStore.edit { prefs ->
            if (minutes == null || minutes <= 0) prefs.remove(KEY_INTERVAL_MINUTES)
            else prefs[KEY_INTERVAL_MINUTES] = minutes
        }
    }
}
