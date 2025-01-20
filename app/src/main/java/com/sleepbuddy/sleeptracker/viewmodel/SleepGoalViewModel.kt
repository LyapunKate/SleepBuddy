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
import com.sleepbuddy.sleeptracker.data.MascotState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.delay
import com.sleepbuddy.sleeptracker.data.ScheduledUpdate
import com.sleepbuddy.sleeptracker.data.UpdateType
import kotlinx.coroutines.Job
import com.sleepbuddy.sleeptracker.notifications.SleepNotificationManager

class SleepGoalViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = SleepGoalDataStore(application)
    private val database = SleepDatabase.getDatabase(application)
    private val dao = database.sleepRecordDao()
    private val notificationManager = SleepNotificationManager(application)

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

    private var nextScheduledUpdate: ScheduledUpdate? = null
    private var updateJob: Job? = null
    private var notificationJob: Job? = null

    init {
        viewModelScope.launch {
            dataStore.sleepGoal.collect { goal ->
                _sleepGoal.value = goal
                notificationManager.scheduleNotifications(goal.bedTime)
            }
        }

        viewModelScope.launch {
            updateStreak()
        }
    }

    private suspend fun updateStreak() {
        val lastStreak = dao.getLastStreak() ?: 0
        _currentStreak.value = lastStreak
    }

    private fun scheduleNextBedtimeCheck(bedTime: LocalTime) {
        updateJob?.cancel()
        val now = LocalDateTime.now()
        val bedTimePlusOneHour = bedTime.plusHours(1)
        
        // Calculate next bedtime check
        var nextBedtimeCheck = now.with(bedTimePlusOneHour)
        if (now.toLocalTime().isAfter(bedTimePlusOneHour)) {
            nextBedtimeCheck = nextBedtimeCheck.plusDays(1)
        }

        scheduleUpdate(ScheduledUpdate(nextBedtimeCheck, UpdateType.BEDTIME_CHECK))
    }

    private fun schedulePostSleepUpdates(sleepEndTime: LocalDateTime) {
        val neutralTime = sleepEndTime.plusHours(2)
        val encouragingTime = sleepEndTime.plusHours(10)
        
        // Schedule the next immediate update
        val now = LocalDateTime.now()
        when {
            now.isBefore(neutralTime) -> {
                scheduleUpdate(ScheduledUpdate(neutralTime, UpdateType.POST_SLEEP_NEUTRAL))
            }
            now.isBefore(encouragingTime) -> {
                scheduleUpdate(ScheduledUpdate(encouragingTime, UpdateType.DAYTIME_ENCOURAGING))
            }
            else -> {
                scheduleNextBedtimeCheck(sleepGoal.value.bedTime)
            }
        }
    }

    private fun scheduleUpdate(update: ScheduledUpdate) {
        updateJob?.cancel()
        nextScheduledUpdate = update
        
        updateJob = viewModelScope.launch {
            val delayMillis = java.time.Duration.between(LocalDateTime.now(), update.updateTime).toMillis()
            if (delayMillis > 0) {
                delay(delayMillis)
                handleScheduledUpdate(update)
            }
        }
    }

    private suspend fun handleScheduledUpdate(update: ScheduledUpdate) {
        when (update.updateType) {
            UpdateType.POST_SLEEP_NEUTRAL -> {
                _mascotState.value = MascotState.NEUTRAL
                // Schedule next update (encouraging state)
                scheduleUpdate(ScheduledUpdate(
                    update.updateTime.plusHours(8),
                    UpdateType.DAYTIME_ENCOURAGING
                ))
            }
            UpdateType.DAYTIME_ENCOURAGING -> {
                _mascotState.value = MascotState.ENCOURAGING
                // Schedule next update (bedtime check)
                scheduleNextBedtimeCheck(sleepGoal.value.bedTime)
            }
            UpdateType.BEDTIME_CHECK -> {
                val isTracking = trackingState.value.isTracking
                _mascotState.value = if (isTracking) MascotState.NEUTRAL else MascotState.ANGRY
                // Schedule next day's bedtime check
                scheduleNextBedtimeCheck(sleepGoal.value.bedTime)
            }
        }
    }

    fun startTracking() {
        notificationManager.updateTrackingState(true)
        val startTime = LocalDateTime.now()
        _trackingState.value = TrackingState(
            isTracking = true,
            currentRecord = SleepRecord(startTime = startTime)
        )
    }

    fun stopTracking() {
        notificationManager.updateTrackingState(false)
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

            // Save to database
            dao.insert(
                SleepRecordEntity(
                    startTime = currentRecord.startTime,
                    endTime = endTime,
                    durationMinutes = duration.toMinutes(),
                    isGoalMet = isGoalMet,
                    currentStreak = newStreak
                )
            )

            // Update streak
            _currentStreak.value = newStreak

            // Update mascot state based on achievement
            _mascotState.value = when {
                newStreak >= 30 -> MascotState.SPECIAL
                newStreak >= 7 -> MascotState.EXTREMELY_HAPPY
                isGoalMet -> MascotState.HAPPY
                else -> MascotState.ANGRY
            }

            // Schedule next updates
            schedulePostSleepUpdates(endTime)

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

    override fun onCleared() {
        super.onCleared()
        updateJob?.cancel()
        notificationJob?.cancel()
    }
} 