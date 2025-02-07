package com.sleepbuddy.sleeptracker.ui.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.sleepbuddy.sleeptracker.R
import androidx.compose.ui.graphics.luminance

object IconUtils {
    @Composable
    fun getClockIcon() = if (MaterialTheme.colorScheme.isLight()) {
        R.drawable.ic_clock_dark
    } else {
        R.drawable.ic_clock_light
    }

    @Composable
    fun getMoonIcon() = if (MaterialTheme.colorScheme.isLight()) {
        R.drawable.ic_moon_dark
    } else {
        R.drawable.ic_moon_light
    }

    @Composable
    fun getSunIcon() = if (MaterialTheme.colorScheme.isLight()) {
        R.drawable.ic_sun_dark
    } else {
        R.drawable.ic_sun_light
    }

    @Composable
    fun getSaveIcon() = if (MaterialTheme.colorScheme.isLight()) {
        R.drawable.save_icon_dark
    } else {
        R.drawable.save_icon_light
    }

    @Composable
    fun getUpdateIcon() = if (MaterialTheme.colorScheme.isLight()) {
        R.drawable.update_icon_dark
    } else {
        R.drawable.update_icon_light
    }
}

@Composable
private fun androidx.compose.material3.ColorScheme.isLight() = this.background.luminance() > 0.5 