package com.example.financecompanion.views.components.goal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.financecompanion.utils.FinanceDatePickerDialog
import com.example.financecompanion.utils.formatMillisToDate
import com.example.financecompanion.utils.formatCurrencyDisplay
import com.example.financecompanion.utils.parseDateToMillis

private enum class EmergencyFundMode {
    CREATE,
    ADD
}

@Composable
fun EmergencyFundDialog(
    saved: Double,
    target: Double,
    targetDateMillis: Long,
    onDismiss: () -> Unit,
    onCreateFund: (Double, Long) -> Unit,
    onAddAmount: (Double, Long) -> Unit
) {
    val today = remember { getStartOfDay(System.currentTimeMillis()) }
    val remaining = (target - saved).coerceAtLeast(0.0)

    var mode by remember(target) {
        mutableStateOf(if (target > 0.0) EmergencyFundMode.ADD else EmergencyFundMode.CREATE)
    }
    var input by remember { mutableStateOf("") }
    var selectedDate by remember(targetDateMillis) {
        mutableStateOf(
            if (targetDateMillis > 0L) getStartOfDay(targetDateMillis) else today
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                val value = input.toDoubleOrNull()

                when {
                    value == null || value <= 0 -> {
                        error = "Enter a valid amount"
                    }

                    mode == EmergencyFundMode.ADD && target <= 0.0 -> {
                        error = "Create a fund first"
                    }

                    mode == EmergencyFundMode.ADD && remaining <= 0.0 -> {
                        error = "Goal already completed"
                    }

                    mode == EmergencyFundMode.ADD && value > remaining -> {
                        error = "Amount exceeds remaining fund"
                    }

                    selectedDate < today -> {
                        error = "Date cannot be in the past"
                    }

                    mode == EmergencyFundMode.CREATE -> {
                        onCreateFund(value, selectedDate)
                        onDismiss()
                    }

                    else -> {
                        onAddAmount(value, selectedDate)
                        onDismiss()
                    }
                }
            }) {
                Text(if (mode == EmergencyFundMode.CREATE) "Save Goal" else "Add Amount")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Emergency Fund") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = mode == EmergencyFundMode.CREATE,
                        onClick = {
                            mode = EmergencyFundMode.CREATE
                            error = null
                        },
                        label = { Text("Create") }
                    )
                    FilterChip(
                        selected = mode == EmergencyFundMode.ADD,
                        onClick = {
                            mode = EmergencyFundMode.ADD
                            error = null
                        },
                        enabled = target > 0.0,
                        label = { Text("Add") }
                    )
                }

                Text(
                    text = if (target > 0.0) {
                        "Saved ${formatCurrencyDisplay(saved)} of ${formatCurrencyDisplay(target)}"
                    } else {
                        "Create a savings target and pick the date you want to reach it."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )

                OutlinedTextField(
                    value = input,
                    onValueChange = {
                        input = sanitizeMoneyInput(it)
                        error = null
                    },
                    label = {
                        Text(
                            if (mode == EmergencyFundMode.CREATE) "Target Amount"
                            else "Add Amount"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Target Date: ${formatMillisToDate(selectedDate)}")
                }

                if (mode == EmergencyFundMode.ADD && target > 0.0) {
                    Text(
                        text = "Remaining: ${formatCurrencyDisplay(remaining)}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    )

    if (showDatePicker) {
        FinanceDatePickerDialog(
            initialDateMillis = selectedDate,
            minDateMillis = today,
            maxDateMillis = null,
            onDateSelected = {
                selectedDate = getStartOfDay(parseDateToMillis(it) ?: selectedDate)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}
