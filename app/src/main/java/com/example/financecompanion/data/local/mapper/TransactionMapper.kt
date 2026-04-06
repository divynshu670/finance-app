package com.example.financecompanion.data.local.mapper

import com.example.financecompanion.data.local.entity.TransactionEntity
import com.example.financecompanion.domain.model.Transaction

fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = id,
        amount = amount,
        type = type,
        category = category,
        date = date,
        note = note,
        createdAt = createdAt
    )
}

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        amount = amount,
        type = type,
        category = category,
        date = date,
        note = note,
        createdAt = createdAt
    )
}