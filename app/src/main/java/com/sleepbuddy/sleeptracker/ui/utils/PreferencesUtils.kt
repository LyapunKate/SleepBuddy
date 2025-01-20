package com.sleepbuddy.sleeptracker.ui.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

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