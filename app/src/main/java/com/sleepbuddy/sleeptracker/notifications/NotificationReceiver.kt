package com.sleepbuddy.sleeptracker.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.time.LocalTime
import java.time.LocalDateTime
import com.sleepbuddy.sleeptracker.data.MascotState
import com.sleepbuddy.sleeptracker.viewmodel.SleepGoalViewModel
import androidx.lifecycle.ViewModelProvider
import android.app.Application

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

        // Update mascot state in SharedPreferences
        val mascotPrefs = context.getSharedPreferences("mascot_state", Context.MODE_PRIVATE)
        
        fun updateMascotState(state: MascotState) {
            mascotPrefs.edit().putString("current_mascot_state", state.name).apply()
            println("updateMascotState: ${state.name}")
        }

        if (isTracking) {
            when (intent.getStringExtra(NOTIFICATION_TYPE)) {
                NotificationType.HOUR_BEFORE.name,
                NotificationType.HALF_HOUR_BEFORE.name,
                NotificationType.BEDTIME.name -> {
                    updateMascotState(MascotState.ENCOURAGING)
                    notificationManager.scheduleNextNotification(
                        NotificationType.valueOf(intent.getStringExtra(NOTIFICATION_TYPE)!!),
                        bedTime
                    )
                }

                NotificationType.DAILY_REMINDER.name -> {
                    updateMascotState(MascotState.NEUTRAL)
                    notificationManager.scheduleNextNotification(
                        NotificationType.DAILY_REMINDER,
                        bedTime
                    )
                }

                NotificationType.STOP_TRACKING_REMINDER.name -> {
                    notificationManager.showStopTrackingReminder()
                }

                NotificationType.FIVE_AFTER_BEDTIME.name -> {
                    notificationManager.scheduleNextNotification(
                        NotificationType.FIVE_AFTER_BEDTIME,
                        bedTime
                    )
                }

                NotificationType.FIFTY_FIVE_AFTER_BEDTIME.name -> {
                    notificationManager.scheduleNextNotification(
                        NotificationType.FIFTY_FIVE_AFTER_BEDTIME,
                        bedTime
                    )
                }
            }
        } else {
            when (intent.getStringExtra(NOTIFICATION_TYPE)) {
                NotificationType.HOUR_BEFORE.name,
                NotificationType.HALF_HOUR_BEFORE.name,
                NotificationType.BEDTIME.name -> {
                    updateMascotState(MascotState.ENCOURAGING)
                    when (intent.getStringExtra(NOTIFICATION_TYPE)) {
                        NotificationType.HOUR_BEFORE.name -> notificationManager.showHourBeforeNotification(bedTime)
                        NotificationType.HALF_HOUR_BEFORE.name -> notificationManager.showHalfHourNotification(bedTime)
                        NotificationType.BEDTIME.name -> notificationManager.showBedtimeNotification()
                    }
                    notificationManager.scheduleNextNotification(
                        NotificationType.valueOf(intent.getStringExtra(NOTIFICATION_TYPE)!!),
                        bedTime
                    )
                }

                NotificationType.FIVE_AFTER_BEDTIME.name,
                NotificationType.FIFTY_FIVE_AFTER_BEDTIME.name -> {
                    updateMascotState(MascotState.ANGRY)
                    when (intent.getStringExtra(NOTIFICATION_TYPE)) {
                        NotificationType.FIVE_AFTER_BEDTIME.name -> notificationManager.showFiveAfterBedtimeNotification()
                        NotificationType.FIFTY_FIVE_AFTER_BEDTIME.name -> notificationManager.showFiftyFiveAfterBedtimeNotification()
                    }
                    notificationManager.scheduleNextNotification(
                        NotificationType.valueOf(intent.getStringExtra(NOTIFICATION_TYPE)!!),
                        bedTime
                    )
                }

                NotificationType.DAILY_REMINDER.name -> {
                    updateMascotState(MascotState.NEUTRAL)
                    notificationManager.showDailyReminder()
                    notificationManager.scheduleNextNotification(
                        NotificationType.DAILY_REMINDER,
                        bedTime
                    )
                }
            }
        }
    }
} 