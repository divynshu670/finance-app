package com.example.financecompanion.domain.model

import com.example.financecompanion.utils.formatMillisToDate

data class TransactionSheetUiState(
    val amount: String = "",
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val selectedCategory: TransactionCategory? = TransactionCategory.FOOD,
    val date: String = formatMillisToDate(System.currentTimeMillis()),
    val note: String = "",
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
