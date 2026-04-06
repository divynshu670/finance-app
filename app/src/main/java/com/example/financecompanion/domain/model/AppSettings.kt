package com.example.financecompanion.domain.model

enum class AppCurrency(
    val code: String,
    val displayName: String,
    val symbol: String
) {
    USD("USD", "US Dollar", "$"),
    INR("INR", "Indian Rupee", "₹"),
    EUR("EUR", "Euro", "€"),
    GBP("GBP", "British Pound", "£");

    companion object {
        fun fromCode(code: String): AppCurrency {
            return entries.firstOrNull { it.code == code } ?: USD
        }
    }
}

data class AppSettings(
    val isDarkMode: Boolean = false,
    val currency: AppCurrency = AppCurrency.USD,
    val notificationsEnabled: Boolean = false,
    val biometricLockEnabled: Boolean = false
)
