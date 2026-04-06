package com.example.financecompanion.domain.repository

import com.example.financecompanion.data.local.dao.TransactionDao
import com.example.financecompanion.data.local.mapper.toDomain
import com.example.financecompanion.data.local.mapper.toEntity
import com.example.financecompanion.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction.toEntity())
    }

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun deleteTransaction(id: Long) {
        transactionDao.deleteTransactionById(id)
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(
            id = transaction.id,
            amount = transaction.amount,
            type = transaction.type,
            category = transaction.category,
            date = transaction.date,
            note = transaction.note,
            createdAt = transaction.createdAt
        )
    }
}
