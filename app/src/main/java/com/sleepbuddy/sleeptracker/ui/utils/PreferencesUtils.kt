package com.sleepbuddy.sleeptracker.ui.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDateTime
import java.time.Duration

@Composable
fun rememberPreference(
    key: String,
    defaultValue: Boolean,
    preferences: SharedPreferences = LocalContext.current.getSharedPreferences("sleep_tracking", Context.MODE_PRIVATE)
): State<Boolean> {
    val state = remember { mutableStateOf(preferences.getBoolean(key, defaultValue)) }

    DisposableEffect(preferences) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, changedKey ->
            if (changedKey == key) {
                state.value = prefs.getBoolean(key, defaultValue)
            }
        }

        preferences.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            preferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    return state
}

@Composable
fun rememberSleepStartTime(
    preferences: SharedPreferences = LocalContext.current.getSharedPreferences("sleep_tracking", Context.MODE_PRIVATE)
): State<LocalDateTime?> {
    val state = remember { mutableStateOf<LocalDateTime?>(null) }

    DisposableEffect(preferences) {
        val startTimeStr = preferences.getString("sleep_start_time", null)
        state.value = startTimeStr?.let { LocalDateTime.parse(it) }

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, changedKey ->
            if (changedKey == "sleep_start_time") {
                val timeStr = prefs.getString("sleep_start_time", null)
                state.value = timeStr?.let { LocalDateTime.parse(it) }
            }
        }

        preferences.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            preferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    return state
}

@Composable
fun rememberLastSession(
    preferences: SharedPreferences = LocalContext.current.getSharedPreferences("sleep_tracking", Context.MODE_PRIVATE)
): State<LastSessionInfo?> {
    val state = remember { mutableStateOf<LastSessionInfo?>(null) }

    DisposableEffect(preferences) {
        val startTimeStr = preferences.getString("last_session_start", null)
        val endTimeStr = preferences.getString("last_session_end", null)
        val durationMinutes = preferences.getLong("last_session_duration", 0)

        if (startTimeStr != null && endTimeStr != null) {
            state.value = LastSessionInfo(
                startTime = LocalDateTime.parse(startTimeStr),
                endTime = LocalDateTime.parse(endTimeStr),
                duration = Duration.ofMinutes(durationMinutes)
            )
        }

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, _ ->
            val newStartTimeStr = prefs.getString("last_session_start", null)
            val newEndTimeStr = prefs.getString("last_session_end", null)
            val newDurationMinutes = prefs.getLong("last_session_duration", 0)

            if (newStartTimeStr != null && newEndTimeStr != null) {
                state.value = LastSessionInfo(
                    startTime = LocalDateTime.parse(newStartTimeStr),
                    endTime = LocalDateTime.parse(newEndTimeStr),
                    duration = Duration.ofMinutes(newDurationMinutes)
                )
            }
        }

        preferences.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            preferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    return state
}

data class LastSessionInfo(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val duration: Duration
) 