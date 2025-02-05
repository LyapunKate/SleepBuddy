package com.sleepbuddy.sleeptracker.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface SleepRecordDao {
    @Insert
    suspend fun insert(record: SleepRecordEntity)

    @Query("UPDATE sleep_records SET currentStreak = :newStreak WHERE id = (SELECT id FROM sleep_records ORDER BY id DESC LIMIT 1)")
    suspend fun updateLastRecordStreak(newStreak: Int)

    @Query("SELECT * FROM sleep_records ORDER BY id DESC LIMIT 1")
    suspend fun getLastRecord(): SleepRecordEntity?

    @Query("SELECT * FROM sleep_records WHERE isGoalMet = 1 ORDER BY date DESC")
    fun getSuccessfulRecords(): Flow<List<SleepRecordEntity>>

    @Query("SELECT currentStreak FROM sleep_records ORDER BY id DESC LIMIT 1")
    suspend fun getLastStreak(): Int?
} 