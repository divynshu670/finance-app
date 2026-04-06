package com.example.financecompanion.data.local.prefrences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.financecompanion.domain.model.AppCurrency
import com.example.financecompanion.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "app_prefs")

class PreferenceManager(private val context: Context) {

    companion object {
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        private val DARK_MODE = booleanPreferencesKey("dark_mode")
        private val CURRENCY_CODE = stringPreferencesKey("currency_code")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val BIOMETRIC_LOCK_ENABLED = booleanPreferencesKey("biometric_lock_enabled")
    }

    val isOnboardingDone: Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[ONBOARDING_DONE] ?: false
        }

    val appSettings: Flow<AppSettings> =
        context.dataStore.data.map { prefs ->
            AppSettings(
                isDarkMode = prefs[DARK_MODE] ?: false,
                currency = AppCurrency.fromCode(
                    prefs[CURRENCY_CODE] ?: AppCurrency.USD.code
                ),
                notificationsEnabled = prefs[NOTIFICATIONS_ENABLED] ?: false,
                biometricLockEnabled = prefs[BIOMETRIC_LOCK_ENABLED] ?: false
            )
        }

    suspend fun setOnboardingDone() {
        context.dataStore.edit { prefs ->
            prefs[ONBOARDING_DONE] = true
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_MODE] = enabled
        }
    }

    suspend fun setCurrency(currency: AppCurrency) {
        context.dataStore.edit { prefs ->
            prefs[CURRENCY_CODE] = currency.code
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setBiometricLockEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[BIOMETRIC_LOCK_ENABLED] = enabled
        }
    }

    suspend fun resetUserPreferences() {
        context.dataStore.edit { prefs ->
            val onboardingDone = prefs[ONBOARDING_DONE] ?: false
            prefs.clear()
            prefs[ONBOARDING_DONE] = onboardingDone
        }
    }
}
