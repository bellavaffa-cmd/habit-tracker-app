package com.habittracker.app.data.calories

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Holds the user's own Anthropic API key, used to call the Claude Vision API directly from the
 * device. Stored in EncryptedSharedPreferences (not plain DataStore, and deliberately NOT baked
 * into BuildConfig) since it's a billing-bearing credential — unlike the free-tier Gemini key
 * this briefly replaced, a leaked Anthropic key runs real charges, so it stays off the (public)
 * APK entirely. Write-only from the UI's perspective: callers can check whether a key is set,
 * but the screen never redisplays the stored value.
 */
class CaloriesSettingsRepository(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "calories_settings_encrypted",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getApiKey(): String? = prefs.getString(KEY_API_KEY, null)

    fun setApiKey(key: String?) {
        prefs.edit().apply {
            if (key.isNullOrBlank()) remove(KEY_API_KEY) else putString(KEY_API_KEY, key.trim())
        }.apply()
    }

    private companion object {
        const val KEY_API_KEY = "anthropic_api_key"
    }
}
