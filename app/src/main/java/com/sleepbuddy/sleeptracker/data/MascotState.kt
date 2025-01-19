package com.sleepbuddy.sleeptracker.data

import com.sleepbuddy.sleeptracker.R
import java.time.LocalDateTime
import java.time.LocalTime

enum class MascotState(val animationRes: Int) {
    HAPPY(R.raw.happy_pet),
    NEUTRAL(R.raw.neutral_pet),
    ANGRY(R.raw.angry_pet),
    ENCOURAGING(R.raw.encouraging_pet),
    EXTREMELY_HAPPY(R.raw.extremely_happy_pet),
    SPECIAL(R.raw.special_pet)
}

class MascotStateManager {
    fun getMascotState(
        currentTime: LocalDateTime,
        targetBedTime: LocalTime,
        isTracking: Boolean,
        currentStreak: Int,
        lastSleepRecord: SleepRecord?
    ): MascotState {
        val currentTimeOfDay = currentTime.toLocalTime()

        // Check for milestone streaks first (until 11 AM)
        if (currentTimeOfDay.isBefore(LocalTime.of(11, 0))) {
            when (currentStreak) {
                in 30..Int.MAX_VALUE -> return MascotState.SPECIAL
                in 7..Int.MAX_VALUE -> return MascotState.EXTREMELY_HAPPY
            }
        }

        // Morning state (until 11 AM)
        if (currentTimeOfDay.isBefore(LocalTime.of(11, 0))) {
            return if (lastSleepRecord?.isGoalMet == true) {
                MascotState.HAPPY
            } else {
                MascotState.ANGRY
            }
        }

        // Daytime state (11 AM to 7 PM)
        if (currentTimeOfDay.isBefore(LocalTime.of(19, 0))) {
            return MascotState.NEUTRAL
        }

        // Evening state (7 PM to Bedtime + 1 Hr)
        val bedTimePlusOneHour = targetBedTime.plusHours(1)
        if (currentTimeOfDay.isBefore(bedTimePlusOneHour)) {
            return MascotState.ENCOURAGING
        }

        // Post-Bedtime state
        return if (!isTracking) {
            MascotState.ANGRY
        } else {
            MascotState.NEUTRAL
        }
    }
} 