package com.example.financecompanion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.financecompanion.data.local.entity.ChallengeEntity
import com.example.financecompanion.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGoal(goal: GoalEntity)

    @Query("SELECT * FROM goal LIMIT 1")
    fun getGoal(): Flow<GoalEntity?>

    @Query("DELETE FROM goal")
    suspend fun deleteGoal()

    @Insert
    suspend fun insertChallenge(challenge: ChallengeEntity)

    @Query("SELECT * FROM challenge")
    fun getChallenges(): Flow<List<ChallengeEntity>>

    @Query("DELETE FROM challenge WHERE id = :id")
    suspend fun deleteChallengeById(id: Int)

    @Query("DELETE FROM challenge WHERE dateMillis < :currentDayMillis")
    suspend fun deleteExpiredChallenges(currentDayMillis: Long)

    @Query("DELETE FROM challenge")
    suspend fun clearChallenges()
}
