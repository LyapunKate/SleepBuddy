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
import com.sleepbuddy.sleeptracker.data.database.SleepRecordDao
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

class SleepGoalViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = SleepGoalDataStore(application)
    private val database = SleepDatabase.getDatabase(application)
    private val dao = database.sleepRecordDao()

    val sleepGoal = dataStore.sleepGoal.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SleepGoal()
    )

    private val _trackingState = MutableStateFlow(TrackingState())
    val trackingState = _trackingState.asStateFlow()

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak = _currentStreak.asStateFlow()

    init {
        viewModelScope.launch {
            updateStreak()
        }
    }

    private suspend fun updateStreak() {
        val startDate = LocalDateTime.now().minusDays(30) // Look back 30 days max
        _currentStreak.value = dao.getCurrentStreak(startDate)
    }

    fun startTracking() {
        val startTime = LocalDateTime.now()
        _trackingState.value = TrackingState(
            isTracking = true,
            currentRecord = SleepRecord(startTime = startTime)
        )
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
            // Save to database
            dao.insert(
                SleepRecordEntity(
                    startTime = currentRecord.startTime,
                    endTime = endTime,
                    durationMinutes = duration.toMinutes(),
                    isGoalMet = isGoalMet
                )
            )

            // Update streak
            updateStreak()

            // Update tracking state
            _trackingState.value = TrackingState(
                isTracking = false,
                currentRecord = currentRecord.copy(
                    endTime = endTime,
                    duration = duration,
                    isGoalMet = isGoalMet
                )
            )
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
        val isStartTimeValid = sleepStartTime.plusHours(1) <= targetBedTime

        return isDurationValid && isStartTimeValid
    }

    private fun printSleepResults(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        duration: Duration,
        isGoalMet: Boolean
    ) {
        println("""
            Sleep Record:
            Start Time: $startTime
            End Time: $endTime
            Duration: ${duration.toHours()} hours ${duration.toMinutesPart()} minutes
            Goal Met: $isGoalMet
        """.trimIndent())
    }

    fun updateSleepGoal(newGoal: SleepGoal) {
        viewModelScope.launch {
            dataStore.updateSleepGoal(newGoal)
        }
    }
} 