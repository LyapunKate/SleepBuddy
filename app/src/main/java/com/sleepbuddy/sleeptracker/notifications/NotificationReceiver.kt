package com.sleepbuddy.sleeptracker.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.time.LocalTime
import java.time.LocalDateTime

class NotificationReceiver : BroadcastReceiver() {
    companion object {
        const val NOTIFICATION_TYPE = "notification_type"
        const val BEDTIME = "bedtime"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = SleepNotificationManager(context)
        val trackingPrefs = context.getSharedPreferences("sleep_tracking", Context.MODE_PRIVATE)
        val isTracking = trackingPrefs.getBoolean("is_tracking", false)

        if (isTracking) {
            return // Don't show notifications while tracking
        }

        // Add logging to help debug notification timing
        val now = LocalDateTime.now()
        println("Notification received at: $now")
        println("Notification type: ${intent.getStringExtra(NOTIFICATION_TYPE)}")

        when (intent.getStringExtra(NOTIFICATION_TYPE)) {
            NotificationType.HOUR_BEFORE.name -> {
                val bedTimeStr = intent.getStringExtra(BEDTIME) ?: return
                val bedTime = LocalTime.parse(bedTimeStr)
                notificationManager.showHourBeforeNotification(bedTime)
            }
            NotificationType.HALF_HOUR_BEFORE.name -> {
                val bedTimeStr = intent.getStringExtra(BEDTIME) ?: return
                val bedTime = LocalTime.parse(bedTimeStr)
                notificationManager.showHalfHourNotification(bedTime)
            }
            NotificationType.BEDTIME.name -> {
                notificationManager.showBedtimeNotification()
            }
        }
    }
} 