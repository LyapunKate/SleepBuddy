package com.sleepbuddy.sleeptracker.data

import java.time.LocalDateTime

data class ScheduledUpdate(
    val updateTime: LocalDateTime,
    val updateType: UpdateType
)

enum class UpdateType {
    POST_SLEEP_NEUTRAL,    // 2 hours after sleep end
    DAYTIME_ENCOURAGING,   // 10 hours after sleep end
    BEDTIME_CHECK         // At bedtime + 1 hour
} 