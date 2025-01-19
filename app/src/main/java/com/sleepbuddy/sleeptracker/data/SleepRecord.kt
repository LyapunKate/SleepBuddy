package com.sleepbuddy.sleeptracker.data

import java.time.LocalDateTime
import java.time.Duration

data class SleepRecord(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val duration: Duration? = null,
    val isGoalMet: Boolean = false
)

data class TrackingState(
    val isTracking: Boolean = false,
    val currentRecord: SleepRecord? = null
) 