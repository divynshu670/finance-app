package com.example.financecompanion.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financecompanion.data.local.db.DatabaseProvider
import com.example.financecompanion.domain.model.TransactionCategory
import com.example.financecompanion.domain.model.TransactionType
import com.example.financecompanion.domain.repository.TransactionRepository
import com.example.financecompanion.domain.repository.TransactionRepositoryImpl
import com.example.financecompanion.utils.parseDateToMillis
import com.example.financecompanion.views.components.goal.getStartOfDay
import com.example.financecompanion.views.components.goal.getStartOfWeek
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class WeeklyComparisonUiState(
    val thisWeekTotal: Double = 0.0,
    val lastWeekTotal: Double = 0.0
)

data class MonthlyTrendUiState(
    val label: String,
    val amount: Double
)

data class CategoryInsightUiState(
    val name: String,
    val amount: Double,
    val percentage: Double
)

data class TopCategoryUiState(
    val name: String,
    val amount: Double
)

data class InsightsUiState(
    val hasExpenses: Boolean = false,
    val weeklyComparison: WeeklyComparisonUiState = WeeklyComparisonUiState(),
    val monthlyTrend: List<MonthlyTrendUiState> = emptyList(),
    val categoryBreakdown: List<CategoryInsightUiState> = emptyList(),
    val topCategory: TopCategoryUiState? = null,
    val averageExpense: Double = 0.0
)

class InsightsViewModel(
    repository: TransactionRepository
) : ViewModel() {

    val uiState: StateFlow<InsightsUiState> = repository
        .getAllTransactions()
        .map { transactions ->
            val today = getStartOfDay(System.currentTimeMillis())
            val expenseTransactions = transactions
                .asSequence()
                .filter { it.type == "EXPENSE" }
                .mapNotNull { transaction ->
                    val transactionDay = getStartOfDay(
                        parseDateToMillis(transaction.date) ?: transaction.createdAt
                    )

                    if (transactionDay > today) {
                        null
                    } else {
                        ExpenseInsightItem(
                            category = normalizeExpenseCategory(transaction.category),
                            amount = transaction.amount,
                            day = transactionDay
                        )
                    }
                }
                .toList()

            val weeklyComparison = buildWeeklyComparison(
                expenseTransactions = expenseTransactions,
                today = today
            )
            val monthlyTrend = buildMonthlyTrend(expenseTransactions, today)
            val categoryBreakdown = buildCategoryBreakdown(expenseTransactions)
            val topCategory = buildTopCategory(expenseTransactions)
            val averageExpense = if (expenseTransactions.isNotEmpty()) {
                expenseTransactions.sumOf { it.amount } / expenseTransactions.size
            } else {
                0.0
            }

            InsightsUiState(
                hasExpenses = expenseTransactions.isNotEmpty(),
                weeklyComparison = weeklyComparison,
                monthlyTrend = monthlyTrend,
                categoryBreakdown = categoryBreakdown,
                topCategory = topCategory,
                averageExpense = averageExpense
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = InsightsUiState(
                monthlyTrend = buildMonthlyTrend(emptyList(), getStartOfDay(System.currentTimeMillis()))
            )
        )

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = DatabaseProvider.getDatabase(context)
                    val repository = TransactionRepositoryImpl(db.transactionDao())
                    return InsightsViewModel(repository) as T
                }
            }
    }
}

private data class ExpenseInsightItem(
    val category: String,
    val amount: Double,
    val day: Long
)

private data class CategoryAggregate(
    val amount: Double,
    val mostRecentDay: Long
)

private fun buildWeeklyComparison(
    expenseTransactions: List<ExpenseInsightItem>,
    today: Long
): WeeklyComparisonUiState {
    val thisWeekStart = getStartOfWeek(today)
    val lastWeekStart = thisWeekStart - WEEK_IN_MILLIS

    val thisWeekTotal = expenseTransactions
        .filter { it.day in thisWeekStart..today }
        .sumOf { it.amount }

    val lastWeekTotal = expenseTransactions
        .filter { it.day in lastWeekStart until thisWeekStart }
        .sumOf { it.amount }

    return WeeklyComparisonUiState(
        thisWeekTotal = thisWeekTotal,
        lastWeekTotal = lastWeekTotal
    )
}

private fun buildMonthlyTrend(
    expenseTransactions: List<ExpenseInsightItem>,
    today: Long,
    monthCount: Int = 6
): List<MonthlyTrendUiState> {
    val monthFormatter = SimpleDateFormat("MMM", Locale.getDefault())
    val cursor = Calendar.getInstance().apply {
        timeInMillis = getStartOfMonth(today)
        add(Calendar.MONTH, -(monthCount - 1))
    }

    return buildList {
        repeat(monthCount) {
            val monthStart = getStartOfMonth(cursor.timeInMillis)
            val monthEnd = Calendar.getInstance().apply {
                timeInMillis = monthStart
                add(Calendar.MONTH, 1)
            }.timeInMillis

            add(
                MonthlyTrendUiState(
                    label = monthFormatter.format(Date(monthStart)),
                    amount = expenseTransactions
                        .filter { it.day in monthStart until monthEnd }
                        .sumOf { it.amount }
                )
            )

            cursor.add(Calendar.MONTH, 1)
        }
    }
}

private fun buildCategoryBreakdown(
    expenseTransactions: List<ExpenseInsightItem>
): List<CategoryInsightUiState> {
    val categoryTotals = expenseTransactions
        .groupBy { it.category }
        .mapValues { (_, items) -> items.sumOf { it.amount } }

    val totalExpenses = categoryTotals.values.sum()
    if (totalExpenses <= 0.0) return emptyList()

    return TransactionCategory
        .byType(TransactionType.EXPENSE)
        .map { category ->
            val amount = categoryTotals[category.label] ?: 0.0
            CategoryInsightUiState(
                name = category.label,
                amount = amount,
                percentage = (amount / totalExpenses) * 100.0
            )
        }
}

private fun buildTopCategory(
    expenseTransactions: List<ExpenseInsightItem>
): TopCategoryUiState? {
    val categoryAggregate = expenseTransactions
        .groupBy { it.category }
        .mapValues { (_, items) ->
            CategoryAggregate(
                amount = items.sumOf { it.amount },
                mostRecentDay = items.maxOf { it.day }
            )
        }
        .filterValues { it.amount > 0.0 }
        .toList()
        .maxWithOrNull(
            compareBy<Pair<String, CategoryAggregate>>(
                { it.second.amount },
                { it.second.mostRecentDay }
            )
        ) ?: return null

    return TopCategoryUiState(
        name = categoryAggregate.first,
        amount = categoryAggregate.second.amount
    )
}

private fun getStartOfMonth(time: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = time
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun normalizeExpenseCategory(category: String): String {
    return TransactionCategory
        .byType(TransactionType.EXPENSE)
        .firstOrNull { it.label.equals(category, ignoreCase = true) }
        ?.label
        ?: TransactionCategory.OTHER_EXPENSE.label
}

private const val WEEK_IN_MILLIS = 7L * 24L * 60L * 60L * 1000L
