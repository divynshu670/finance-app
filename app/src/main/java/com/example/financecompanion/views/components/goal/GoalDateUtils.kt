package com.example.financecompanion.views.components.goal

import java.util.Calendar
import java.util.concurrent.TimeUnit

private const val MAX_INTEGER_DIGITS = 9
private const val MAX_FRACTION_DIGITS = 2

fun getStartOfDay(time: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = time
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

fun getStartOfWeek(time: Long): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = getStartOfDay(time)
        firstDayOfWeek = Calendar.MONDAY
        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
    }
    return getStartOfDay(cal.timeInMillis)
}

fun getEndOfWeek(time: Long = System.currentTimeMillis()): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = getStartOfWeek(time)
        add(Calendar.DAY_OF_YEAR, 6)
    }
    return getStartOfDay(cal.timeInMillis)
}

fun calculateDaysRemaining(
    targetDateMillis: Long,
    nowMillis: Long = System.currentTimeMillis()
): Int {
    if (targetDateMillis <= 0L) return 0
    val today = getStartOfDay(nowMillis)
    val targetDay = getStartOfDay(targetDateMillis)
    val diff = targetDay - today
    return TimeUnit.MILLISECONDS.toDays(diff).toInt().coerceAtLeast(0)
}

fun sanitizeMoneyInput(
    input: String,
    maxIntegerDigits: Int = MAX_INTEGER_DIGITS,
    maxFractionDigits: Int = MAX_FRACTION_DIGITS
): String {
    val builder = StringBuilder()
    var hasDecimalPoint = false
    var integerDigits = 0
    var fractionDigits = 0

    input.forEach { char ->
        when {
            char.isDigit() && !hasDecimalPoint && integerDigits < maxIntegerDigits -> {
                builder.append(char)
                integerDigits++
            }

            char == '.' && !hasDecimalPoint -> {
                builder.append(char)
                hasDecimalPoint = true
            }

            char.isDigit() && hasDecimalPoint && fractionDigits < maxFractionDigits -> {
                builder.append(char)
                fractionDigits++
            }
        }
    }

    return builder.toString()
}
