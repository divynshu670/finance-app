package com.example.financecompanion.domain.model

data class Transaction(
    val id: Long = 0L,
    val amount: Double,
    val type: String,
    val category: String,
    val date: String,
    val note: String,
    val createdAt: Long
)