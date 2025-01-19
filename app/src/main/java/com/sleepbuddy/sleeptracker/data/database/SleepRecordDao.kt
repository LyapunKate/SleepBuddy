package com.sleepbuddy.sleeptracker.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface SleepRecordDao {
    @Insert
    suspend fun insert(record: SleepRecordEntity)

    @Query("SELECT * FROM sleep_records ORDER BY date DESC LIMIT 1")
    suspend fun getLastRecord(): SleepRecordEntity?

    @Query("SELECT * FROM sleep_records WHERE isGoalMet = 1 ORDER BY date DESC")
    fun getSuccessfulRecords(): Flow<List<SleepRecordEntity>>

    @Query("""
        SELECT COUNT(*) 
        FROM sleep_records 
        WHERE isGoalMet = 1 
        AND date >= :startDate 
        ORDER BY date DESC
    """)
    suspend fun getCurrentStreak(startDate: LocalDateTime): Int
} 