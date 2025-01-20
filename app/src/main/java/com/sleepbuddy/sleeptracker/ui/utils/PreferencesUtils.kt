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