package com.example.financecompanion.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financecompanion.data.local.db.DatabaseProvider
import com.example.financecompanion.domain.model.HomeUiState
import com.example.financecompanion.domain.repository.GoalRepository
import com.example.financecompanion.domain.repository.TransactionRepositoryImpl
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: TransactionRepositoryImpl,
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeTransactions()
    }

    private fun observeTransactions() {
        viewModelScope.launch {
            combine(
                repository.getAllTransactions(),
                goalRepository.getGoal()
            ) { transactions, goal ->
                val today = com.example.financecompanion.views.components.goal.getStartOfDay(
                    System.currentTimeMillis()
                )
                val activeGoal = goal?.takeUnless {
                    today > com.example.financecompanion.views.components.goal.getStartOfDay(it.createdAt) &&
                        it.saved < it.target
                }
                if (goal != null && activeGoal == null) {
                    viewModelScope.launch {
                        goalRepository.deleteGoal()
                    }
                }

                val income = transactions
                    .filter { it.type == "INCOME" }
                    .sumOf { it.amount }

                val expense = transactions
                    .filter { it.type == "EXPENSE" }
                    .sumOf { it.amount }

                val balance = income - expense

                val recent = transactions.take(3)

                val categoryData = buildCategoryData(transactions)

                // WEEKLY
                val groupedByDate = transactions
                    .filter { it.type == "EXPENSE" }
                    .groupBy { it.date }

                val sortedDates = groupedByDate.keys.sorted()
                val last7 = sortedDates.takeLast(7)

                val hasWeeklyData = last7.size == 7

                val weeklyData = last7.map { date ->
                    groupedByDate[date]?.sumOf { it.amount }?.toFloat() ?: 0f
                }

                val hasEmergencyFund = activeGoal != null && activeGoal.target > 0.0
                val emergencySaved = activeGoal?.saved ?: 0.0
                val emergencyGoal = activeGoal?.target ?: 0.0

                HomeUiState(
                    balance = balance,
                    income = income,
                    expense = expense,
                    recentTransactions = recent,
                    categoryData = categoryData,
                    hasWeeklyData = hasWeeklyData,
                    weeklyData = weeklyData,
                    hasEmergencyFund = hasEmergencyFund,
                    emergencySaved = emergencySaved,
                    emergencyGoal = emergencyGoal
                )
            }
                .distinctUntilChanged()
                .collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun buildCategoryData(
        transactions: List<com.example.financecompanion.domain.model.Transaction>
    ): List<Pair<String, Double>> {
        val expenseTotals = transactions
            .asSequence()
            .filter { it.type == "EXPENSE" }
            .groupBy { it.category }
            .mapValues { (_, items) -> items.sumOf { it.amount } }

        if (expenseTotals.isEmpty()) return emptyList()

        val sortedExpenses = expenseTotals
            .filterKeys { !it.equals("Other", ignoreCase = true) }
            .toList()
            .sortedByDescending { it.second }

        val topCategories = sortedExpenses.take(3)
        val otherTotal = expenseTotals
            .filterKeys { it.equals("Other", ignoreCase = true) }
            .values
            .sum() + sortedExpenses.drop(3).sumOf { it.second }

        return topCategories + listOf("Other" to otherTotal)
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {

                    val db = DatabaseProvider.getDatabase(context)
                    val repo = TransactionRepositoryImpl(db.transactionDao())
                    val goalRepo = GoalRepository(db.goalDao())

                    return HomeViewModel(repo, goalRepo) as T
                }
            }
    }
}
