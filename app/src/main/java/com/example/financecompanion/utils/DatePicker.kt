package com.example.financecompanion.utils

import android.app.DatePickerDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val DATE_PATTERN = "dd/MM/yyyy"

fun formatMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())
    return formatter.format(Date(millis))
}

fun parseDateToMillis(date: String): Long? {
    val formatter = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())
    return formatter.parse(date)?.time
}

@Composable
fun FinanceDatePickerDialog(
    initialDateMillis: Long,
    minDateMillis: Long? = null,
    maxDateMillis: Long? = System.currentTimeMillis(),
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    DisposableEffect(Unit) {

        val calendar = Calendar.getInstance().apply {
            timeInMillis = initialDateMillis
        }

        val dialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 0, 0, 0)
                }
                onDateSelected(formatMillisToDate(selectedDate.timeInMillis))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        minDateMillis?.let { dialog.datePicker.minDate = it }
        maxDateMillis?.let { dialog.datePicker.maxDate = it }

        dialog.setOnDismissListener { onDismiss() }
        dialog.show()

        onDispose {
            dialog.dismiss()
        }
    }
}
