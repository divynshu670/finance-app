package com.example.financecompanion.dev

import androidx.biometric.BiometricManager
import com.example.financecompanion.BuildConfig

object DeveloperOptions {
    val isDebugMode: Boolean
        get() = BuildConfig.DEBUG

    val biometricAuthenticators: Int
        get() = if (BuildConfig.DEBUG) {
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        } else {
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK
        }

    val useFastReminderSchedule: Boolean
        get() = BuildConfig.DEBUG

    val allowFlexibleChallengeDates: Boolean
        get() = BuildConfig.DEBUG
}
