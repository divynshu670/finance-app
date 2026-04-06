package com.example.financecompanion.domain.model

data class HomeUiState(
    val balance: Double = 0.0,
    val income: Double = 0.0,
    val expense: Double = 0.0,

    val hasWeeklyData: Boolean = false,
    val weeklyData: List<Float> = emptyList(),

    val categoryData: List<Pair<String, Double>> = emptyList(),

    val hasEmergencyFund: Boolean = false,
    val emergencySaved: Double = 0.0,
    val emergencyGoal: Double = 0.0,

    val recentTransactions: List<Transaction> = emptyList()
)