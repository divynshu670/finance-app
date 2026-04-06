package com.example.financecompanion.feature.receipt

import com.example.financecompanion.domain.model.TransactionCategory

data class ReceiptScanDraft(
    val amountText: String,
    val suggestedCategory: TransactionCategory,
    val merchantHint: String?,
    val rawText: String
)
