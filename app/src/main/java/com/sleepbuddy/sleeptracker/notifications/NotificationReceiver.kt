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

        // Add logging to help debug notification timing
        val now = LocalDateTime.now()
        println("Notification received at: $now")
        println("Notification type: ${intent.getStringExtra(NOTIFICATION_TYPE)}")

        val bedTimeStr = intent.getStringExtra(BEDTIME) ?: return
        val bedTime = LocalTime.parse(bedTimeStr)

        if (isTracking) {
            println("""NotificationReciever, isTracking = true""")

            when (intent.getStringExtra(NOTIFICATION_TYPE)) {
                NotificationType.HOUR_BEFORE.name -> {
                    // Schedule next day's notification
                    notificationManager.scheduleNextNotification(NotificationType.HOUR_BEFORE, bedTime)
                }
                NotificationType.HALF_HOUR_BEFORE.name -> {
                    // Schedule next day's notification
                    notificationManager.scheduleNextNotification(NotificationType.HALF_HOUR_BEFORE, bedTime)
                }
                NotificationType.BEDTIME.name -> {
                    // Schedule next day's notification
                    notificationManager.scheduleNextNotification(NotificationType.BEDTIME, bedTime)
                }
            }

            return // Don't show notifications while tracking
        }


        when (intent.getStringExtra(NOTIFICATION_TYPE)) {
            NotificationType.HOUR_BEFORE.name -> {
                notificationManager.showHourBeforeNotification(bedTime)
                // Schedule next day's notification
                notificationManager.scheduleNextNotification(NotificationType.HOUR_BEFORE, bedTime)
            }
            NotificationType.HALF_HOUR_BEFORE.name -> {
                notificationManager.showHalfHourNotification(bedTime)
                // Schedule next day's notification
                notificationManager.scheduleNextNotification(NotificationType.HALF_HOUR_BEFORE, bedTime)
            }
            NotificationType.BEDTIME.name -> {
                notificationManager.showBedtimeNotification()
                // Schedule next day's notification
                notificationManager.scheduleNextNotification(NotificationType.BEDTIME, bedTime)
            }
        }
    }
} 