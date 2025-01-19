package com.sleepbuddy.sleeptracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sleepbuddy.sleeptracker.data.SleepGoal
import com.sleepbuddy.sleeptracker.data.SleepGoalDataStore
import com.sleepbuddy.sleeptracker.data.SleepRecord
import com.sleepbuddy.sleeptracker.data.TrackingState
import com.sleepbuddy.sleeptracker.data.database.SleepDatabase
import com.sleepbuddy.sleeptracker.data.database.SleepRecordEntity
import com.sleepbuddy.sleeptracker.data.database.toSleepRecord
import com.sleepbuddy.sleeptracker.data.database.SleepRecordDao
import com.sleepbuddy.sleeptracker.data.MascotState
import com.sleepbuddy.sleeptracker.data.MascotStateManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.delay

class SleepGoalViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = SleepGoalDataStore(application)
    private val database = SleepDatabase.getDatabase(application)
    private val dao = database.sleepRecordDao()
    private val mascotManager = MascotStateManager()

    // Make sleepGoal private and create a public flow
    private val _sleepGoal = MutableStateFlow(SleepGoal())
    val sleepGoal = _sleepGoal.asStateFlow()

    private val _trackingState = MutableStateFlow(TrackingState())
    val trackingState = _trackingState.asStateFlow()

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak = _currentStreak.asStateFlow()

    // Add mascot state flow
    private val _mascotState = MutableStateFlow(MascotState.NEUTRAL)
    val mascotState = _mascotState.asStateFlow()

    init {
        viewModelScope.launch {
            // Collect sleep goal updates from DataStore
            dataStore.sleepGoal.collect { goal ->
                _sleepGoal.value = goal
                // Update mascot state when sleep goal changes
                updateMascotState()
            }
        }

        viewModelScope.launch {
            updateStreak()
        }

        viewModelScope.launch {
            // Update mascot state periodically
            while (true) {
                updateMascotState()
                delay(60000) // Update every minute
            }
        }
    }

    private suspend fun updateStreak() {
        val lastStreak = dao.getLastStreak() ?: 0
        _currentStreak.value = lastStreak
    }

    private suspend fun updateMascotState() {
        val currentTime = LocalDateTime.now()
        val lastRecord = dao.getLastRecord()
        val currentGoal = _sleepGoal.value  // Use the current goal from state

        // Log the values being passed to getMascotState
        println("""
            Updating mascot state with:
            Current Time: $currentTime
            Target Bed Time: ${currentGoal.bedTime}
            Is Tracking: ${trackingState.value.isTracking}
            Current Streak: ${currentStreak.value}
            Last Sleep Record: ${lastRecord?.toSleepRecord()}
        """.trimIndent())
        
        _mascotState.value = mascotManager.getMascotState(
            currentTime = currentTime,
            targetBedTime = currentGoal.bedTime,
            isTracking = trackingState.value.isTracking,
            currentStreak = currentStreak.value,
            lastSleepRecord = lastRecord?.toSleepRecord(),
            targetSleepDuration = (currentGoal.sleepDuration * 60).toInt() // Convert hours to minutes
        )

        println("""Mascot state: ${_mascotState.value} """)
    }

    fun startTracking() {
        val startTime = LocalDateTime.now()
        _trackingState.value = TrackingState(
            isTracking = true,
            currentRecord = SleepRecord(startTime = startTime)
        )
        viewModelScope.launch {
            updateMascotState()
        }
    }

    fun stopTracking() {
        val currentRecord = _trackingState.value.currentRecord ?: return
        val endTime = LocalDateTime.now()
        val duration = Duration.between(currentRecord.startTime, endTime)
        
        val isGoalMet = checkGoalMet(
            startTime = currentRecord.startTime,
            endTime = endTime,
            duration = duration,
            goal = sleepGoal.value
        )

        viewModelScope.launch {
            // Calculate new streak based on goal achievement
            val lastStreak = dao.getLastStreak() ?: 0
            val newStreak = if (isGoalMet) lastStreak + 1 else 0

            // Save to database with updated streak
            dao.insert(
                SleepRecordEntity(
                    startTime = currentRecord.startTime,
                    endTime = endTime,
                    durationMinutes = duration.toMinutes(),
                    isGoalMet = isGoalMet,
                    currentStreak = newStreak
                )
            )

            // Log the sleep session
            printSleepResults(
                startTime = currentRecord.startTime,
                endTime = endTime,
                duration = duration,
                isGoalMet = isGoalMet,
                currentStreak = newStreak
            )

            // Update streak in UI
            _currentStreak.value = newStreak

            // Update tracking state
            _trackingState.value = TrackingState(
                isTracking = false,
                currentRecord = currentRecord.copy(
                    endTime = endTime,
                    duration = duration,
                    isGoalMet = isGoalMet
                )
            )
            updateMascotState()
        }
    }

    private fun checkGoalMet(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        duration: Duration,
        goal: SleepGoal
    ): Boolean {
        val durationHours = duration.toMinutes() / 60.0f
        val targetDuration = goal.sleepDuration

        // Check if duration is within acceptable range
        val isDurationValid = durationHours >= targetDuration && 
                            durationHours <= (targetDuration + 4)

        // Convert start time to LocalTime for comparison
        val sleepStartTime = startTime.toLocalTime()
        val targetBedTime = goal.bedTime
        
        // Allow 1 hour flexibility for bed time
        val isStartTimeValid = sleepStartTime <= targetBedTime.plusHours(1)

        return isDurationValid && isStartTimeValid
    }

    private fun printSleepResults(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        duration: Duration,
        isGoalMet: Boolean,
        currentStreak: Int

    ) {
        println("""
            Sleep Record:
            Start Time: $startTime
            End Time: $endTime
            Duration: ${duration.toHours()} hours ${duration.toMinutesPart()} minutes
            Goal Met: $isGoalMet
            Current Streak: $currentStreak
        """.trimIndent())
    }

    fun updateSleepGoal(newGoal: SleepGoal) {
        viewModelScope.launch {
            dataStore.updateSleepGoal(newGoal)
            // No need to manually update _sleepGoal here as it will be updated through the DataStore collection
        }
    }
} 