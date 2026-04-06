package com.example.financecompanion.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val amount: Double,
    val type: String,
    val category: String,
    val date: String,
    val note: String,
    val createdAt: Long = System.currentTimeMillis()
)