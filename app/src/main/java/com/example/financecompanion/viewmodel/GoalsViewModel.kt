package com.example.financecompanion.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financecompanion.data.local.db.DatabaseProvider
import com.example.financecompanion.data.local.entity.ChallengeEntity
import com.example.financecompanion.data.local.entity.GoalEntity
import com.example.financecompanion.domain.model.Transaction
import com.example.financecompanion.domain.repository.GoalRepository
import com.example.financecompanion.domain.repository.TransactionRepository
import com.example.financecompanion.domain.repository.TransactionRepositoryImpl
import com.example.financecompanion.utils.parseDateToMillis
import com.example.financecompanion.views.components.goal.getStartOfDay
import com.example.financecompanion.views.components.goal.getStartOfWeek
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GoalChallengeUiState(
    val id: Int = 0,
    val name: String,
    val category: String,
    val target: Double,
    val dateMillis: Long,
    val spent: Double,
    val progress: Float,
    val completed: Boolean
)

data class GoalsUiState(
    val target: Double = 0.0,
    val saved: Double = 0.0,
    val targetDateMillis: Long = 0L,
    val streakActivityDays: List<Long> = emptyList(),
    val challenges: List<GoalChallengeUiState> = emptyList()
)

class GoalsViewModel(
    private val repo: GoalRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val uiState: StateFlow<GoalsUiState> =
        combine(
            repo.getGoal(),
            repo.getChallenges(),
            transactionRepository.getAllTransactions()
        ) { goal, challenges, transactions ->
            val today = getStartOfDay(System.currentTimeMillis())
            val transactionDays = buildValidStreakDays(
                transactions = transactions,
                today = today
            )

            val activeGoal = goal?.takeUnless { isExpiredIncompleteGoal(it, today) }
            if (goal != null && activeGoal == null) {
                viewModelScope.launch {
                    repo.deleteGoal()
                }
            }

            val activeChallenges = challenges.filter { challenge ->
                getStartOfDay(challenge.dateMillis) >= today
            }
            if (activeChallenges.size != challenges.size) {
                viewModelScope.launch {
                    repo.deleteExpiredChallenges(today)
                }
            }

            val expenseTransactions = transactions.filter { it.type == "EXPENSE" }

            GoalsUiState(
                target = activeGoal?.target ?: 0.0,
                saved = activeGoal?.saved ?: 0.0,
                targetDateMillis = activeGoal?.createdAt ?: 0L,
                streakActivityDays = transactionDays,
                challenges = activeChallenges.map { challenge ->
                    val challengeStart = getStartOfWeek(challenge.dateMillis)
                    val challengeDueDay = getStartOfDay(challenge.dateMillis)
                    val challengeEnd = minOf(today, challengeDueDay)
                    val spent = expenseTransactions
                        .filter { transaction ->
                            val transactionDay = parseDateToMillis(transaction.date)
                                ?.let(::getStartOfDay)
                                ?: getStartOfDay(transaction.createdAt)
                            transaction.category == challenge.category &&
                                transactionDay in challengeStart..challengeEnd
                        }
                        .sumOf { it.amount }
                    val totalDays = ((challengeDueDay - challengeStart) / DAY_IN_MILLIS)
                        .toInt()
                        .plus(1)
                        .coerceAtLeast(1)
                    val completedDays = when {
                        today < challengeStart -> 0
                        else -> ((challengeEnd - challengeStart) / DAY_IN_MILLIS)
                            .toInt()
                            .plus(1)
                            .coerceAtLeast(0)
                    }

                    GoalChallengeUiState(
                        id = challenge.id,
                        name = challenge.name,
                        category = challenge.category,
                        target = challenge.target,
                        dateMillis = challenge.dateMillis,
                        spent = spent,
                        progress = (completedDays.toFloat() / totalDays.toFloat())
                            .coerceIn(0f, 1f),
                        completed = today >= challengeDueDay && spent <= challenge.target
                    )
                }
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            GoalsUiState()
        )

    fun createFund(target: Double, targetDateMillis: Long) {
        viewModelScope.launch {
            repo.saveGoal(
                GoalEntity(
                    target = target,
                    saved = 0.0,
                    createdAt = targetDateMillis
                )
            )
        }
    }

    fun deleteFund() {
        viewModelScope.launch {
            repo.deleteGoal()
        }
    }

    fun addAmount(
        amount: Double,
        current: Double,
        target: Double,
        targetDateMillis: Long
    ) {
        viewModelScope.launch {
            repo.saveGoal(
                GoalEntity(
                    target = target,
                    saved = current + amount,
                    createdAt = targetDateMillis
                )
            )
        }
    }

    fun addChallenge(entity: ChallengeEntity): Boolean {
        val today = getStartOfDay(System.currentTimeMillis())
        if (getStartOfDay(entity.dateMillis) < today) return false

        val isDuplicate = uiState.value.challenges.any { challenge ->
            challenge.category.equals(entity.category, ignoreCase = true) &&
                getStartOfDay(challenge.dateMillis) == getStartOfDay(entity.dateMillis)
        }

        if (isDuplicate) return false

        viewModelScope.launch {
            repo.addChallenge(entity)
        }

        return true
    }

    fun deleteChallenge(id: Int) {
        viewModelScope.launch {
            repo.deleteChallenge(id)
        }
    }

    fun updateStreak() {
        viewModelScope.launch {
            val today = getStartOfDay(System.currentTimeMillis())
            val current = repo.getGoal().first()
            val existing = current?.streakDays
                ?.split(",")
                ?.mapNotNull { it.toLongOrNull() }
                ?.toMutableList() ?: mutableListOf()

            if (!existing.contains(today)) {
                existing.add(today)
            }

            repo.saveGoal(
                GoalEntity(
                    target = current?.target ?: 0.0,
                    saved = current?.saved ?: 0.0,
                    createdAt = current?.createdAt ?: System.currentTimeMillis(),
                    streakDays = existing.joinToString(",")
                )
            )
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = DatabaseProvider.getDatabase(context)
                    val repo = GoalRepository(db.goalDao())
                    val transactionRepo = TransactionRepositoryImpl(db.transactionDao())
                    return GoalsViewModel(repo, transactionRepo) as T
                }
            }
    }
}

private fun buildValidStreakDays(
    transactions: List<Transaction>,
    today: Long
): List<Long> {
    return transactions
        .asSequence()
        .mapNotNull { transaction ->
            val createdDay = getStartOfDay(transaction.createdAt)
            if (createdDay > today) return@mapNotNull null

            val selectedDay = parseDateToMillis(transaction.date)
                ?.let(::getStartOfDay)
                ?: createdDay

            if (selectedDay > today || selectedDay != createdDay) {
                return@mapNotNull null
            }

            createdDay
        }
        .distinct()
        .sortedDescending()
        .toList()
}

private fun isExpiredIncompleteGoal(
    goal: GoalEntity,
    today: Long
): Boolean {
    val targetDate = getStartOfDay(goal.createdAt)
    return today > targetDate && goal.saved < goal.target
}

private const val DAY_IN_MILLIS = 24L * 60L * 60L * 1000L
