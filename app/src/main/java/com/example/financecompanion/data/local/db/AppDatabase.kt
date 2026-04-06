package com.example.financecompanion.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.financecompanion.data.local.dao.GoalDao
import com.example.financecompanion.data.local.dao.TransactionDao
import com.example.financecompanion.data.local.entity.ChallengeEntity
import com.example.financecompanion.data.local.entity.TransactionEntity
import com.example.financecompanion.data.local.entity.GoalEntity

@Database(
    entities = [
        TransactionEntity::class,
        GoalEntity::class,
        ChallengeEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    // ✅ NEW
    abstract fun goalDao(): GoalDao
}
