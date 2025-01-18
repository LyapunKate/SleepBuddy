package com.sleepbuddy.sleeptracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime

class SleepGoalDataStore(private val context: Context) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sleep_goals")
        private val BED_TIME_HOUR = intPreferencesKey("bed_time_hour")
        private val BED_TIME_MINUTE = intPreferencesKey("bed_time_minute")
        private val SLEEP_DURATION = floatPreferencesKey("sleep_duration")
        private val TARGET_STREAK = intPreferencesKey("target_streak")
    }

    val sleepGoal: Flow<SleepGoal> = context.dataStore.data.map { preferences ->
        val hour = preferences[BED_TIME_HOUR] ?: 22
        val minute = preferences[BED_TIME_MINUTE] ?: 0
        SleepGoal(
            bedTime = LocalTime.of(hour, minute),
            sleepDuration = preferences[SLEEP_DURATION] ?: 8f,
            targetStreak = preferences[TARGET_STREAK] ?: 7
        )
    }

    suspend fun updateSleepGoal(sleepGoal: SleepGoal) {
        context.dataStore.edit { preferences ->
            preferences[BED_TIME_HOUR] = sleepGoal.bedTime.hour
            preferences[BED_TIME_MINUTE] = sleepGoal.bedTime.minute
            preferences[SLEEP_DURATION] = sleepGoal.sleepDuration
            preferences[TARGET_STREAK] = sleepGoal.targetStreak
        }
    }
} 