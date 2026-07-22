package com.habittracker.app.data.smoking

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.smokingDataStore by preferencesDataStore(name = "smoking_settings")
private val KEY_INTERVAL_MINUTES = intPreferencesKey("interval_minutes")
private val KEY_CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
private val KEY_CARD_ORDER = stringPreferencesKey("card_order")
private val KEY_HIDDEN_CARDS = stringSetPreferencesKey("hidden_cards")

private val defaultCardOrder = SmokingCardId.entries.toList()

/** Per-tracker settings: the interval reminder and the currency used for cost figures. */
class SmokingSettingsRepository(private val context: Context) {
    /** The user's manually-set minimum interval (minutes) between cigarettes. Null = not set. */
    val intervalMinutes: Flow<Int?> = context.smokingDataStore.data.map { prefs ->
        prefs[KEY_INTERVAL_MINUTES]?.takeIf { it > 0 }
    }

    suspend fun setIntervalMinutes(minutes: Int?) {
        context.smokingDataStore.edit { prefs ->
            if (minutes == null || minutes <= 0) prefs.remove(KEY_INTERVAL_MINUTES)
            else prefs[KEY_INTERVAL_MINUTES] = minutes
        }
    }

    val currencySymbol: Flow<String> = context.smokingDataStore.data.map { prefs ->
        prefs[KEY_CURRENCY_SYMBOL] ?: "$"
    }

    suspend fun setCurrencySymbol(symbol: String) {
        context.smokingDataStore.edit { prefs -> prefs[KEY_CURRENCY_SYMBOL] = symbol }
    }

    /** Card display order for the Smoking tab. Any card type not yet in stored prefs (e.g. added in a later
     * release) is appended at the end, so the persisted order never silently drops a new card. */
    val cardOrder: Flow<List<SmokingCardId>> = context.smokingDataStore.data.map { prefs ->
        val stored = prefs[KEY_CARD_ORDER]
            ?.split(",")
            ?.mapNotNull { name -> runCatching { SmokingCardId.valueOf(name) }.getOrNull() }
            ?.distinct()
        if (stored.isNullOrEmpty()) {
            defaultCardOrder
        } else {
            stored + defaultCardOrder.filter { it !in stored }
        }
    }

    suspend fun setCardOrder(order: List<SmokingCardId>) {
        context.smokingDataStore.edit { prefs -> prefs[KEY_CARD_ORDER] = order.joinToString(",") { it.name } }
    }

    val hiddenCards: Flow<Set<SmokingCardId>> = context.smokingDataStore.data.map { prefs ->
        prefs[KEY_HIDDEN_CARDS]
            ?.mapNotNull { name -> runCatching { SmokingCardId.valueOf(name) }.getOrNull() }
            ?.toSet()
            ?: emptySet()
    }

    suspend fun setCardHidden(id: SmokingCardId, hidden: Boolean) {
        context.smokingDataStore.edit { prefs ->
            val current = prefs[KEY_HIDDEN_CARDS]?.toMutableSet() ?: mutableSetOf()
            if (hidden) current.add(id.name) else current.remove(id.name)
            prefs[KEY_HIDDEN_CARDS] = current
        }
    }
}
