package com.example.financecompanion.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financecompanion.data.local.db.DatabaseProvider
import com.example.financecompanion.domain.model.Transaction
import com.example.financecompanion.domain.model.TransactionCategory
import com.example.financecompanion.domain.model.TransactionSheetUiState
import com.example.financecompanion.domain.model.TransactionType
import com.example.financecompanion.domain.repository.TransactionRepository
import com.example.financecompanion.domain.repository.TransactionRepositoryImpl
import com.example.financecompanion.utils.formatMillisToDate
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface HomeTransactionSheetEvent {
    data object CloseSheet : HomeTransactionSheetEvent
}

class HomeTransactionSheetViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionSheetUiState())
    val uiState: StateFlow<TransactionSheetUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeTransactionSheetEvent>()
    val events: SharedFlow<HomeTransactionSheetEvent> = _events.asSharedFlow()

    private var editingTransactionId: Long? = null
    private var editingTransactionCreatedAt: Long? = null
    private val maxIntegerDigits = 9

    fun startEditing(transaction: Transaction) {
        editingTransactionId = transaction.id
        editingTransactionCreatedAt = transaction.createdAt

        _uiState.value = TransactionSheetUiState(
            amount = transaction.amount.toString(),
            transactionType = TransactionType.valueOf(transaction.type),
            selectedCategory = TransactionCategory.entries.find {
                it.label == transaction.category
            },
            date = transaction.date,
            note = transaction.note,
            isEditing = true
        )
    }

    // ✅ FIXED: limit + real-time validation
    fun onAmountChange(value: String) {
        val filtered = buildString {
            var hasDecimalPoint = false
            value.forEach { char ->
                when {
                    char.isDigit() -> append(char)
                    char == '.' && !hasDecimalPoint -> {
                        append(char)
                        hasDecimalPoint = true
                    }
                }
            }
        }

        if (filtered.substringBefore('.').length > maxIntegerDigits) return

        val amountValue = filtered.toDoubleOrNull()

        _uiState.update {
            it.copy(
                amount = filtered,
                error = if (amountValue != null && amountValue == 0.0) {
                    "Amount must be greater than 0"
                } else null
            )
        }
    }

    fun onTransactionTypeChange(type: TransactionType) {
        val defaultCategory = TransactionCategory.byType(type).firstOrNull()
        _uiState.update {
            it.copy(
                transactionType = type,
                selectedCategory = defaultCategory
            )
        }
    }

    fun onCategorySelected(category: TransactionCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onDateChange(date: String) {
        _uiState.update { it.copy(date = date) }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun onPrimaryAction() {
        val state = _uiState.value
        val amountValue = state.amount.toDoubleOrNull() ?: 0.0
        val category = state.selectedCategory?.label ?: return

        // ✅ FIXED: validation
        if (amountValue <= 0.0) {
            _uiState.update {
                it.copy(error = "Amount must be greater than 0")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                if (editingTransactionId != null) {
                    repository.updateTransaction(
                        Transaction(
                            id = editingTransactionId!!,
                            amount = amountValue,
                            type = state.transactionType.name,
                            category = category,
                            date = state.date,
                            note = state.note,
                            createdAt = editingTransactionCreatedAt
                                ?: System.currentTimeMillis()
                        )
                    )
                } else {
                    repository.insertTransaction(
                        Transaction(
                            amount = amountValue,
                            type = state.transactionType.name,
                            category = category,
                            date = state.date,
                            note = state.note,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                }

                _events.emit(HomeTransactionSheetEvent.CloseSheet)

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Something went wrong")
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun saveScannedExpense(
        amount: Double,
        category: TransactionCategory,
        note: String = ""
    ) {
        if (amount <= 0.0) {
            _uiState.update { it.copy(error = "Amount must be greater than 0") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                repository.insertTransaction(
                    Transaction(
                        amount = amount,
                        type = TransactionType.EXPENSE.name,
                        category = category.label,
                        date = formatMillisToDate(System.currentTimeMillis()),
                        note = note,
                        createdAt = System.currentTimeMillis()
                    )
                )

                _events.emit(HomeTransactionSheetEvent.CloseSheet)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Something went wrong")
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun reset() {
        editingTransactionId = null
        editingTransactionCreatedAt = null
        _uiState.value = TransactionSheetUiState()
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = DatabaseProvider.getDatabase(context)
                    val repo = TransactionRepositoryImpl(db.transactionDao())

                    return HomeTransactionSheetViewModel(repo) as T
                }
            }
    }
}
