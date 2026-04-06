package com.example.financecompanion.domain.repository

import com.example.financecompanion.data.local.dao.GoalDao
import com.example.financecompanion.data.local.entity.*
import kotlinx.coroutines.flow.Flow

class GoalRepository(
    private val dao: GoalDao
) {

    fun getGoal(): Flow<GoalEntity?> = dao.getGoal()

    suspend fun saveGoal(goal: GoalEntity) {
        dao.upsertGoal(goal)
    }

    suspend fun deleteGoal() {
        dao.deleteGoal()
    }

    fun getChallenges(): Flow<List<ChallengeEntity>> = dao.getChallenges()

    suspend fun addChallenge(challenge: ChallengeEntity) {
        dao.insertChallenge(challenge)
    }

    suspend fun deleteChallenge(id: Int) {
        dao.deleteChallengeById(id)
    }

    suspend fun deleteExpiredChallenges(currentDayMillis: Long) {
        dao.deleteExpiredChallenges(currentDayMillis)
    }
}
