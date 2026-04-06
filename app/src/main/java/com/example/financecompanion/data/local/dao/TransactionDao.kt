package com.example.financecompanion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.financecompanion.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("DELETE FROM transactions")
    suspend fun clearAllTransactions()

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query(
        """
        UPDATE transactions
        SET amount = :amount,
            type = :type,
            category = :category,
            date = :date,
            note = :note,
            createdAt = :createdAt
        WHERE id = :id
        """
    )
    suspend fun updateTransaction(
        id: Long,
        amount: Double,
        type: String,
        category: String,
        date: String,
        note: String,
        createdAt: Long
    )
}
