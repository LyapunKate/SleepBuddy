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
import com.sleepbuddy.sleeptracker.notifications.SleepNotificationManager
import android.content.Context
import android.content.SharedPreferences
import com.sleepbuddy.sleeptracker.notifications.NotificationType
import com.sleepbuddy.sleeptracker.data.MessageState

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

    private val _messageState = MutableStateFlow<MessageState>(MessageState.Default)
    val messageState = _messageState.asStateFlow()

    private val trackingPrefs = application.getSharedPreferences("sleep_tracking", Context.MODE_PRIVATE)

    private val mascotPrefs = application.getSharedPreferences("mascot_state", Context.MODE_PRIVATE)
    private val mascotListener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == "current_mascot_state") {
                prefs.getString("current_mascot_state", null)?.let { stateName ->
                    try {
                        val newState = MascotState.valueOf(stateName)
                        println("Current mascot state: $stateName")
                        _mascotState.value = newState
                    } catch (e: IllegalArgumentException) {
                        // Handle invalid state name
                    }
                }
            }
        }

    init {
        viewModelScope.launch {
            dataStore.sleepGoal.collect { goal ->
                _sleepGoal.value = goal
                println("""SleepGoalViewModel""")
                notificationManager.scheduleNotifications(goal.bedTime, goal.sleepDuration)
            }
        }

        viewModelScope.launch {
            checkAndUpdateStreak()
        }

        // Register listener OUTSIDE coroutine
        mascotPrefs.registerOnSharedPreferenceChangeListener(mascotListener)

        // Load initial mascot state
        _mascotState.value = mascotPrefs.getString("current_mascot_state", null)
            ?.let { MascotState.valueOf(it) } ?: MascotState.NEUTRAL
    }

    suspend fun checkAndUpdateStreak() {
        val isTracking = trackingPrefs.getBoolean("is_tracking", false)
        if (!isTracking) {
            val lastSessionEndStr = trackingPrefs.getString("last_session_end", null)
            
            lastSessionEndStr?.let { endTimeStr ->
                try {
                    val lastSessionEnd = LocalDateTime.parse(endTimeStr)
                    val hoursSinceLastSleep = Duration.between(
                        lastSessionEnd,
                        LocalDateTime.now()
                    ).toHours()

                    println("Hours since last sleep: $hoursSinceLastSleep")
                    println("Last session end time: $lastSessionEnd")
                    println("Current time: ${LocalDateTime.now()}")

                    if (hoursSinceLastSleep >= 24) {
                        _currentStreak.value = 0
                        // Update streak in database
                        dao.updateLastRecordStreak(0)
                        println("Streak reset due to inactivity. Hours since last sleep: $hoursSinceLastSleep")
                    }
                } catch (e: Exception) {
                    println("Error parsing last session end time: ${e.message}")
                }
            }
        }
        updateStreak()
    }

    suspend fun updateStreak() {
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

        updateMascotState(MascotState.NEUTRAL)
        _messageState.value = MessageState.Default
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

        // Check if duration exceeded limit
        val durationHours = duration.toMinutes() / 60.0f
        val exceededLimit = durationHours > (sleepGoal.value.sleepDuration + 4)

        viewModelScope.launch {
            // Calculate new streak based on goal achievement
            val lastStreak = dao.getLastStreak() ?: 0
            val newStreak = if (isGoalMet) lastStreak + 1 else 0

            // Update streak
            _currentStreak.value = newStreak

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

            // Update message state
            _messageState.value = when {
                newStreak == 30 -> MessageState.ThirtyDayStreak
                newStreak == sleepGoal.value.targetStreak -> MessageState.TargetStreak
                isGoalMet -> MessageState.GoalMet
                exceededLimit -> MessageState.ForgotToStop
                else -> MessageState.GoalNotMet
            }

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
        //Save to SharedPreferences so it persists
        mascotPrefs.edit().putString("current_mascot_state", newState.name).apply()
    }

    private fun checkGoalMet(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        duration: Duration,
        goal: SleepGoal
    ): Boolean {
        val durationHours = duration.toMinutes() / 60.0f
        val targetDuration = goal.sleepDuration

        // Check if duration is within an acceptable range
        val isDurationValid = durationHours in targetDuration..(targetDuration + 4)

        // Check bedtime (handles after-midnight cases)
        val minutesDiff = Duration.between(goal.bedTime, startTime.toLocalTime()).toMinutes()
        val isStartTimeValid = minutesDiff <= 60

        return isDurationValid && isStartTimeValid
    }


    fun updateSleepGoal(newGoal: SleepGoal) {
        viewModelScope.launch {
            // If bedtime or duration changed, reset streak
            if (newGoal.bedTime != sleepGoal.value.bedTime || 
                newGoal.sleepDuration != sleepGoal.value.sleepDuration) {
                
                _currentStreak.value = 0
                _messageState.value = MessageState.Default
                updateMascotState(MascotState.NEUTRAL)
                println("Last Record Before Update ${dao.getLastRecord()}")
                // Update streak in the last record
                dao.updateLastRecordStreak(0)
                println("Current Streak Value: ${_currentStreak.value}")
                println("Last Record After Update ${dao.getLastRecord()}")
            }
            
            dataStore.updateSleepGoal(newGoal)
        }
    }

    override fun onCleared() {
        super.onCleared()
        //Unregister listener to prevent memory leaks
        mascotPrefs.unregisterOnSharedPreferenceChangeListener(mascotListener)
    }
} 