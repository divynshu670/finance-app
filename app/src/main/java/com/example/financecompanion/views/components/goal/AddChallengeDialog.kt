package com.example.financecompanion.views.components.goal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.financecompanion.dev.DeveloperOptions
import com.example.financecompanion.domain.model.TransactionCategory
import com.example.financecompanion.domain.model.TransactionType
import com.example.financecompanion.utils.FinanceDatePickerDialog
import com.example.financecompanion.utils.formatMillisToDate
import com.example.financecompanion.utils.parseDateToMillis

@Composable
fun AddChallengeDialog(
    onDismiss: () -> Unit,
    onCreate: (Challenge) -> String?
) {
    val today = remember { getStartOfDay(System.currentTimeMillis()) }
    val endOfWeek = remember { getEndOfWeek() }
    val maxAllowedDate = remember {
        if (DeveloperOptions.allowFlexibleChallengeDates) {
            null
        } else {
            endOfWeek
        }
    }

    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf<TransactionCategory?>(null) }
    var date by remember { mutableStateOf(today) }
    var showDatePicker by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                val amt = amount.toDoubleOrNull()

                when {
                    name.isBlank() -> error = "Enter challenge name"
                    category == null -> error = "Select category"
                    amt == null || amt <= 0 -> error = "Amount must be > 0"
                    date < today -> error = "Cannot select past date"
                    !DeveloperOptions.allowFlexibleChallengeDates && date > endOfWeek -> {
                        error = "Only current week allowed"
                    }
                    else -> {
                        error = onCreate(
                            Challenge(
                                name = name.trim(),
                                category = category!!.label,
                                target = amt,
                                dateMillis = date,
                                completed = false
                            )
                        )

                        if (error == null) {
                            onDismiss()
                        }
                    }
                }
            }) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("New Challenge") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "Create a weekly spending challenge for one category.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        error = null
                    },
                    label = { Text("Challenge Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = sanitizeMoneyInput(it)
                        error = null
                    },
                    label = { Text("Target Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                DropdownMenuBox(
                    category = category,
                    onSelect = {
                        category = it
                        error = null
                    }
                )

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Target Date: ${formatMillisToDate(date)}")
                }

                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    )

    if (showDatePicker) {
        FinanceDatePickerDialog(
            initialDateMillis = date,
            minDateMillis = today,
            maxDateMillis = maxAllowedDate,
            onDateSelected = {
                date = getStartOfDay(parseDateToMillis(it) ?: date)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownMenuBox(
    category: TransactionCategory?,
    onSelect: (TransactionCategory) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val categories = remember {
        TransactionCategory.entries.filter { it.type == TransactionType.EXPENSE }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = category?.label.orEmpty(),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text("Category") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
