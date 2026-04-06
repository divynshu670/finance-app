package com.example.financecompanion.domain.repository

import com.example.financecompanion.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    suspend fun insertTransaction(transaction: Transaction)
    fun getAllTransactions(): Flow<List<Transaction>>

    suspend fun deleteTransaction(id: Long)

    suspend fun updateTransaction(transaction: Transaction)
}