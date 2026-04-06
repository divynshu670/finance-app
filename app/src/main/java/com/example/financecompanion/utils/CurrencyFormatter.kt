package com.example.financecompanion.utils

import com.example.financecompanion.domain.model.AppCurrency
import java.util.Locale
import kotlin.math.abs

object CurrencyFormatState {
    @Volatile
    var currentCurrency: AppCurrency = AppCurrency.USD
}

private fun safeCurrencyValue(value: Double): Double {
    return if (value.isFinite()) value else 0.0
}

private fun formatCurrencyNumber(value: Double): String {
    return String.format(Locale.US, "%.2f", abs(safeCurrencyValue(value)))
}

fun formatCurrencyDisplay(value: Double): String {
    val safeValue = safeCurrencyValue(value)
    val prefix = if (safeValue < 0) {
        "-${CurrencyFormatState.currentCurrency.symbol}"
    } else {
        CurrencyFormatState.currentCurrency.symbol
    }
    return prefix + formatCurrencyNumber(safeValue)
}

fun formatSignedTransactionAmount(
    value: Double,
    isIncome: Boolean
): String {
    val prefix = if (isIncome) {
        "+${CurrencyFormatState.currentCurrency.symbol}"
    } else {
        "-${CurrencyFormatState.currentCurrency.symbol}"
    }
    return prefix + formatCurrencyNumber(value)
}
