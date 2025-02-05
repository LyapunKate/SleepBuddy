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
import com.sleepbuddy.sleeptracker.notifications.SleepNotificationManager
import android.content.Context
import com.sleepbuddy.sleeptracker.notifications.NotificationType

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

    private val trackingPrefs = application.getSharedPreferences("sleep_tracking", Context.MODE_PRIVATE)

    init {
        viewModelScope.launch {
            dataStore.sleepGoal.collect { goal ->
                _sleepGoal.value = goal
                println("""SleepGoalViewModel""")
                notificationManager.scheduleNotifications(goal.bedTime, goal.sleepDuration)
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

    fun startTracking() {
        val startTime = LocalDateTime.now()
        // Save start time to preferences
        trackingPrefs.edit()
            .putString("sleep_start_time", startTime.toString())
            .putBoolean("is_tracking", true)
            .apply()

        notificationManager.updateTrackingState(true)
        
        // Calculate reminder time and schedule notification
        val reminderTime = startTime
            .plusMinutes((sleepGoal.value.sleepDuration * 60).toLong())
            .plusMinutes(15)

        notificationManager.scheduleExactNotification(
            time = reminderTime,
            type = NotificationType.STOP_TRACKING_REMINDER,
            bedTime = sleepGoal.value.bedTime // This won't be used for this notification type
        )
    }

    fun stopTracking() {
        // Get start time from preferences
        val startTimeStr = trackingPrefs.getString("sleep_start_time", null)
        val startTime = startTimeStr?.let { LocalDateTime.parse(it) } ?: return

        val endTime = LocalDateTime.now()
        val duration = Duration.between(startTime, endTime)
        
        // Save last session info
        trackingPrefs.edit()
            .putString("last_session_start", startTime.toString())
            .putString("last_session_end", endTime.toString())
            .putLong("last_session_duration", duration.toMinutes())
            .remove("sleep_start_time")
            .putBoolean("is_tracking", false)
            .apply()

        notificationManager.updateTrackingState(false)
        
        val isGoalMet = checkGoalMet(
            startTime = startTime,
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
                    startTime = startTime,
                    endTime = endTime,
                    durationMinutes = duration.toMinutes(),
                    isGoalMet = isGoalMet,
                    currentStreak = newStreak
                )
            )

            // Update streak
            _currentStreak.value = newStreak

            // Update mascot state based on achievement
            val newState = when {
                newStreak == 30 -> MascotState.SPECIAL
                newStreak == sleepGoal.value.targetStreak -> MascotState.EXTREMELY_HAPPY
                isGoalMet -> MascotState.HAPPY
                else -> MascotState.ANGRY
            }

            updateMascotState(newState)

            // Update tracking state
            _trackingState.value = TrackingState(
                isTracking = false,
                currentRecord = SleepRecord(startTime = startTime)
            )
        }
    }

    fun updateMascotState(newState: MascotState) {
        _mascotState.value = newState
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


    fun updateSleepGoal(newGoal: SleepGoal) {
        viewModelScope.launch {
            dataStore.updateSleepGoal(newGoal)
            // No need to manually update _sleepGoal here as it will be updated through the DataStore collection
        }
    }
} 