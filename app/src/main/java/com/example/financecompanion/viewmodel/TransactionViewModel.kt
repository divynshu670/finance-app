package com.example.financecompanion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financecompanion.domain.model.Transaction
import com.example.financecompanion.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TransactionsUiState(
    val transactions: List<Transaction> = emptyList(),
    val filter: String = "All",
    val searchQuery: String = "",
    val hasAnyTransactions: Boolean = false
)

class TransactionsViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _filter = MutableStateFlow("All")
    private val _searchQuery = MutableStateFlow("")

    private val allTransactions = repository.getAllTransactions()

    val uiState: StateFlow<TransactionsUiState> =
        combine(allTransactions, _filter, _searchQuery) { transactions, filter, query ->

            val filtered = transactions.filter { txn ->

                val matchesFilter = when (filter) {
                    "Income" -> txn.type == "INCOME"
                    "Expenses" -> txn.type == "EXPENSE"
                    else -> true
                }

                val matchesSearch =
                    txn.category.contains(query, ignoreCase = true) ||
                            txn.note.contains(query, ignoreCase = true)

                matchesFilter && matchesSearch
            }

            TransactionsUiState(
                transactions = filtered,
                filter = filter,
                searchQuery = query,
                hasAnyTransactions = transactions.isNotEmpty()
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TransactionsUiState()
        )

    fun onFilterChange(value: String) {
        _filter.value = value
    }

    fun onSearchChange(value: String) {
        _searchQuery.value = value
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(transaction.id)
            } catch (_: Exception) {
                // ignore for now
            }
        }
    }

    companion object {
        fun factory(repository: TransactionRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TransactionsViewModel(repository) as T
                }
            }
    }
}
