package com.example.financecompanion.viewmodel

import android.app.Application
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.financecompanion.data.local.db.DatabaseProvider
import com.example.financecompanion.data.local.entity.TransactionEntity
import com.example.financecompanion.data.local.prefrences.PreferenceManager
import com.example.financecompanion.dev.DeveloperOptions
import com.example.financecompanion.domain.model.AppCurrency
import com.example.financecompanion.domain.model.AppSettings
import com.example.financecompanion.notifications.DailyReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface SettingsEvent {
    data class Message(val message: String) : SettingsEvent
    data class ShareExport(val uri: Uri, val fileName: String) : SettingsEvent
}

class SettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val preferenceManager = PreferenceManager(application)
    private val database = DatabaseProvider.getDatabase(application)

    val uiState: StateFlow<AppSettings> = preferenceManager.appSettings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSettings()
    )

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.setDarkMode(enabled)
        }
    }

    fun setCurrency(currency: AppCurrency) {
        viewModelScope.launch {
            preferenceManager.setCurrency(currency)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.setNotificationsEnabled(enabled)
            if (enabled) {
                DailyReminderScheduler.schedule(getApplication())
                if (DeveloperOptions.useFastReminderSchedule) {
                    DailyReminderScheduler.triggerNow(getApplication())
                }
            } else {
                DailyReminderScheduler.cancel(getApplication())
            }
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.setBiometricLockEnabled(enabled)
        }
    }

    fun exportData() {
        viewModelScope.launch {
            try {
                val exportFile = withContext(Dispatchers.IO) {
                    val transactions = database.transactionDao().getAllTransactions().first()
                    if (transactions.isEmpty()) return@withContext null
                    writeTransactionsCsv(transactions)
                }

                if (exportFile == null) {
                    _events.emit(SettingsEvent.Message("No data available to export"))
                    return@launch
                }

                val uri = FileProvider.getUriForFile(
                    getApplication(),
                    "${getApplication<Application>().packageName}.fileprovider",
                    exportFile
                )

                _events.emit(SettingsEvent.Message("Data exported successfully"))
                _events.emit(
                    SettingsEvent.ShareExport(
                        uri = uri,
                        fileName = exportFile.name
                    )
                )
            } catch (exception: Exception) {
                _events.emit(
                    SettingsEvent.Message(
                        exception.message ?: "Unable to export data"
                    )
                )
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    database.withTransaction {
                        database.transactionDao().clearAllTransactions()
                        database.goalDao().deleteGoal()
                        database.goalDao().clearChallenges()
                    }
                    preferenceManager.resetUserPreferences()
                }

                DailyReminderScheduler.cancel(getApplication())
                _events.emit(SettingsEvent.Message("All data cleared"))
            } catch (exception: Exception) {
                _events.emit(
                    SettingsEvent.Message(
                        exception.message ?: "Unable to clear app data"
                    )
                )
            }
        }
    }

    private fun writeTransactionsCsv(transactions: List<TransactionEntity>): File {
        val exportRoot = getApplication<Application>()
            .getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: getApplication<Application>().filesDir
        val exportDir = File(exportRoot, "exports").apply { mkdirs() }
        val fileName = "finance_export_${
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        }.csv"
        val file = File(exportDir, fileName)

        val totalIncome = transactions
            .filter { it.type == "INCOME" }
            .sumOf { it.amount }
        val totalExpense = transactions
            .filter { it.type == "EXPENSE" }
            .sumOf { it.amount }

        file.bufferedWriter().use { writer ->
            writer.appendLine("Summary")
            writer.appendLine("Total Income,${formatCsvAmount(totalIncome)}")
            writer.appendLine("Total Expense,${formatCsvAmount(totalExpense)}")
            writer.appendLine("Net Balance,${formatCsvAmount(totalIncome - totalExpense)}")
            writer.appendLine()
            writer.appendLine("Date,Category,Amount,Type,Note")

            transactions.forEach { transaction ->
                writer.appendLine(
                    listOf(
                        escapeCsv(transaction.date),
                        escapeCsv(transaction.category),
                        formatCsvAmount(transaction.amount),
                        escapeCsv(transaction.type),
                        escapeCsv(transaction.note)
                    ).joinToString(",")
                )
            }
        }

        return file
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(application) as T
                }
            }
    }
}

private fun escapeCsv(value: String): String {
    val escaped = value.replace("\"", "\"\"")
    return "\"$escaped\""
}

private fun formatCsvAmount(amount: Double): String {
    return String.format(Locale.US, "%.2f", amount)
}
