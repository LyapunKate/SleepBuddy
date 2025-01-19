package com.sleepbuddy.sleeptracker.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sleepbuddy.sleeptracker.data.SleepRecord
import java.time.LocalDateTime
import java.time.Duration

@Entity(tableName = "sleep_records")
data class SleepRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val durationMinutes: Long,
    val isGoalMet: Boolean,
    val currentStreak: Int,
    val date: LocalDateTime = startTime
)

fun SleepRecordEntity.toSleepRecord() = SleepRecord(
    startTime = startTime,
    endTime = endTime,
    duration = Duration.ofMinutes(durationMinutes),
    isGoalMet = isGoalMet
) 