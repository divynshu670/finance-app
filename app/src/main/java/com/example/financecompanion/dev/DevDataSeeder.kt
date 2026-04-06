package com.example.financecompanion.dev

import android.content.Context
import com.example.financecompanion.data.local.db.AppDatabase
import com.example.financecompanion.data.local.entity.ChallengeEntity
import com.example.financecompanion.data.local.entity.GoalEntity
import com.example.financecompanion.data.local.entity.TransactionEntity
import com.example.financecompanion.domain.model.TransactionCategory
import com.example.financecompanion.domain.model.TransactionType
import com.example.financecompanion.utils.formatMillisToDate
import com.example.financecompanion.views.components.goal.getEndOfWeek
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import java.util.concurrent.TimeUnit

object DevDataSeeder {
    private const val PREFS_NAME = "developer_mode"
    private const val SEEDED_KEY = "sample_data_seeded"

    fun seedIfNeeded(context: Context, database: AppDatabase) {
        if (!DeveloperOptions.isDebugMode) return

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(SEEDED_KEY, false)) return

        runBlocking(Dispatchers.IO) {
            val transactionDao = database.transactionDao()
            val goalDao = database.goalDao()

            val hasTransactions = transactionDao.getAllTransactions().first().isNotEmpty()
            val hasGoal = goalDao.getGoal().first() != null
            val hasChallenges = goalDao.getChallenges().first().isNotEmpty()

            if (hasTransactions || hasGoal || hasChallenges) {
                prefs.edit().putBoolean(SEEDED_KEY, true).apply()
                return@runBlocking
            }

            buildSampleTransactions().forEach { transaction ->
                transactionDao.insertTransaction(transaction)
            }
            goalDao.upsertGoal(buildSampleGoal())
            goalDao.insertChallenge(buildSampleChallenge())

            prefs.edit().putBoolean(SEEDED_KEY, true).apply()
        }
    }

    private fun buildSampleTransactions(nowMillis: Long = System.currentTimeMillis()): List<TransactionEntity> {
        val expenseCategories = TransactionCategory.byType(TransactionType.EXPENSE)
        val incomeCategories = TransactionCategory.byType(TransactionType.INCOME)

        return buildList {
            for (dayOffset in 0 until 24) {
                val primaryExpenseTime = buildTimestamp(
                    nowMillis = nowMillis,
                    dayOffset = dayOffset,
                    hour = 10 + (dayOffset % 4),
                    minute = 15
                )
                val primaryExpenseCategory = expenseCategories[dayOffset % expenseCategories.size]
                add(
                    TransactionEntity(
                        amount = 18.75 + (dayOffset * 2.85),
                        type = TransactionType.EXPENSE.name,
                        category = primaryExpenseCategory.label,
                        date = formatMillisToDate(primaryExpenseTime),
                        note = "Debug ${primaryExpenseCategory.label} expense",
                        createdAt = primaryExpenseTime
                    )
                )

                if (dayOffset % 4 == 0) {
                    val secondaryExpenseTime = buildTimestamp(
                        nowMillis = nowMillis,
                        dayOffset = dayOffset,
                        hour = 18,
                        minute = 20
                    )
                    val secondaryExpenseCategory =
                        expenseCategories[(dayOffset + 3) % expenseCategories.size]
                    add(
                        TransactionEntity(
                            amount = 11.40 + dayOffset,
                            type = TransactionType.EXPENSE.name,
                            category = secondaryExpenseCategory.label,
                            date = formatMillisToDate(secondaryExpenseTime),
                            note = "Debug ${secondaryExpenseCategory.label} expense",
                            createdAt = secondaryExpenseTime
                        )
                    )
                }

                if (dayOffset % 7 == 0) {
                    val incomeTime = buildTimestamp(
                        nowMillis = nowMillis,
                        dayOffset = dayOffset,
                        hour = 8,
                        minute = 45
                    )
                    val incomeCategory = incomeCategories[(dayOffset / 7) % incomeCategories.size]
                    add(
                        TransactionEntity(
                            amount = 1450.0 + (dayOffset * 40),
                            type = TransactionType.INCOME.name,
                            category = incomeCategory.label,
                            date = formatMillisToDate(incomeTime),
                            note = "Debug ${incomeCategory.label} income",
                            createdAt = incomeTime
                        )
                    )
                }
            }
        }.sortedByDescending { it.createdAt }
    }

    private fun buildSampleGoal(nowMillis: Long = System.currentTimeMillis()): GoalEntity {
        return GoalEntity(
            target = 10000.0,
            saved = 6500.0,
            createdAt = getStartOfDay(nowMillis) + TimeUnit.DAYS.toMillis(30)
        )
    }

    private fun buildSampleChallenge(nowMillis: Long = System.currentTimeMillis()): ChallengeEntity {
        return ChallengeEntity(
            name = "Weekly Food Cap",
            category = TransactionCategory.FOOD.label,
            target = 250.0,
            dateMillis = getEndOfWeek(nowMillis),
            completed = false
        )
    }

    private fun buildTimestamp(
        nowMillis: Long,
        dayOffset: Int,
        hour: Int,
        minute: Int
    ): Long {
        return Calendar.getInstance().apply {
            timeInMillis = getStartOfDay(nowMillis)
            add(Calendar.DAY_OF_YEAR, -dayOffset)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getStartOfDay(timeMillis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timeMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
