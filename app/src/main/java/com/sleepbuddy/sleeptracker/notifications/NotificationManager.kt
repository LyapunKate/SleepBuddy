package com.sleepbuddy.sleeptracker.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.sleepbuddy.sleeptracker.MainActivity
import com.sleepbuddy.sleeptracker.R
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.random.Random
import com.sleepbuddy.sleeptracker.permissions.NotificationPermissionHandler
import java.time.format.DateTimeFormatter

enum class NotificationType {
    HOUR_BEFORE,
    HALF_HOUR_BEFORE,
    BEDTIME
}

class SleepNotificationManager(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val trackingPrefs = context.getSharedPreferences("sleep_tracking", Context.MODE_PRIVATE)
    
    companion object {
        private const val CHANNEL_ID = "sleep_reminders"
        private const val HOUR_BEFORE_ID = 1
        private const val HALF_HOUR_BEFORE_ID = 2
        private const val BEDTIME_ID = 3

        private val HOUR_BEFORE_MESSAGES = listOf(
            "🕒 Hey! Just a friendly reminder: bedtime is at %s. Let's keep that streak alive! 🐾",
            "🌙 Your cozy bed is waiting! Get ready to wind down—just an hour left before bedtime."
        )

        private val HALF_HOUR_MESSAGES = listOf(
            "🐶 Your dog's getting sleepy... shouldn't you be too? 💤 Bedtime is coming up in 30 minutes!",
            "🔔 Tick tock! Time to start wrapping things up. Bedtime is at %s, and your streak depends on it!"
        )

        private val BEDTIME_MESSAGES = listOf(
            "🌟 Bedtime is here! 🛌 Stick to your plan and hit the hay to keep your streak going strong. Your dog is counting on you! 🐾",
            "🐕 Good night, champion! Let's make tonight count. Start winding down and press 'Start Sleep' when you're ready."
        )
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sleep Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for maintaining sleep schedule"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(
        title: String,
        message: String,
        notificationId: Int
    ) {
        if (!NotificationPermissionHandler.hasNotificationPermission(context)) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Make sure to create this icon
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun showHourBeforeNotification(bedTime: LocalTime) {
        val message = HOUR_BEFORE_MESSAGES[Random.nextInt(HOUR_BEFORE_MESSAGES.size)]
            .format(bedTime.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a")))
        
        buildNotification(
            "Bedtime Reminder",
            message,
            HOUR_BEFORE_ID
        )
    }

    fun showHalfHourNotification(bedTime: LocalTime) {
        val message = HALF_HOUR_MESSAGES[Random.nextInt(HALF_HOUR_MESSAGES.size)]
            .format(bedTime.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a")))
        
        buildNotification(
            "Bedtime Soon",
            message,
            HALF_HOUR_BEFORE_ID
        )
    }

    fun showBedtimeNotification() {
        val message = BEDTIME_MESSAGES[Random.nextInt(BEDTIME_MESSAGES.size)]
        
        buildNotification(
            "Time for Bed",
            message,
            BEDTIME_ID
        )
    }

    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }

    fun scheduleNotifications(bedTime: LocalTime) {
        cancelAllAlarms()
        
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        
        // Calculate notification times
        var hourBefore = today.atTime(bedTime.minusHours(1))
        var halfHourBefore = today.atTime(bedTime.minusMinutes(30))
        var atBedtime = today.atTime(bedTime)

        // If times have passed for today, schedule for tomorrow
        if (now.isAfter(hourBefore)) {
            hourBefore = hourBefore.plusDays(1)
        }
        if (now.isAfter(halfHourBefore)) {
            halfHourBefore = halfHourBefore.plusDays(1)
        }
        if (now.isAfter(atBedtime)) {
            atBedtime = atBedtime.plusDays(1)
        }

        // Save bedtime in preferences for boot receiver
        context.getSharedPreferences("sleep_settings", Context.MODE_PRIVATE)
            .edit()
            .putString("bedtime", bedTime.format(DateTimeFormatter.ISO_LOCAL_TIME))
            .apply()

        scheduleExactNotification(hourBefore, NotificationType.HOUR_BEFORE, bedTime)
        scheduleExactNotification(halfHourBefore, NotificationType.HALF_HOUR_BEFORE, bedTime)
        scheduleExactNotification(atBedtime, NotificationType.BEDTIME, bedTime)
    }

    private fun scheduleExactNotification(
        time: LocalDateTime,
        type: NotificationType,
        bedTime: LocalTime
    ) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(NotificationReceiver.NOTIFICATION_TYPE, type.name)
            putExtra(NotificationReceiver.BEDTIME, bedTime.toString())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            type.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }

        println("Scheduled ${type.name} notification for: $time")
    }

    fun scheduleNextNotification(type: NotificationType, bedTime: LocalTime) {
        val tomorrow = LocalDateTime.now().plusDays(1).toLocalDate()
        val nextTime = when (type) {
            NotificationType.HOUR_BEFORE -> tomorrow.atTime(bedTime.minusHours(1))
            NotificationType.HALF_HOUR_BEFORE -> tomorrow.atTime(bedTime.minusMinutes(30))
            NotificationType.BEDTIME -> tomorrow.atTime(bedTime)
        }

        scheduleExactNotification(nextTime, type, bedTime)
    }

    private fun cancelAllAlarms() {
        NotificationType.values().forEach { type ->
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                type.ordinal,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let { alarmManager.cancel(it) }
        }
    }

    fun updateTrackingState(isTracking: Boolean) {
        trackingPrefs.edit().putBoolean("is_tracking", isTracking).apply()
        if (isTracking) {
            cancelAllNotifications()
        }
    }
} 