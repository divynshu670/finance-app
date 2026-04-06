package com.example.financecompanion.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goal")
data class GoalEntity(
    @PrimaryKey val id: Int = 1, // single row
    val target: Double,
    val saved: Double,
    val createdAt: Long,
    val streakDays: String = "" // comma separated millis
)