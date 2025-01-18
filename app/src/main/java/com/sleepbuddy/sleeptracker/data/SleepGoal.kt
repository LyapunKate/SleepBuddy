package com.sleepbuddy.sleeptracker.data

import java.time.LocalTime

data class SleepGoal(
    val bedTime: LocalTime = LocalTime.of(22, 0), // Default 10:00 PM
    val sleepDuration: Float = 8f, // Default 8 hours
    val targetStreak: Int = 7 // Default 7 days
) 