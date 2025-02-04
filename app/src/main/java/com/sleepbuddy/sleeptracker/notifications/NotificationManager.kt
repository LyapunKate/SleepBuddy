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
    BEDTIME,
    STOP_TRACKING_REMINDER,
    FIVE_AFTER_BEDTIME,
    FIFTY_FIVE_AFTER_BEDTIME,
    DAILY_REMINDER
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

        private val STOP_TRACKING_MESSAGES = listOf(
            "Good morning! ☀️ Don't forget to press 'Stop' to log your sleep.",
            "Rise and shine! Your dog wants to know how you slept—press 'Stop' to finish tracking."
        )

        private val FIVE_AFTER_BEDTIME_MESSAGES = listOf(
            "Uh-oh, it's past your bedtime! 😔 You've still got a chance to save your streak—head to bed now!",
            "Your streak is on thin ice! 🐶 Go to bed within the next hour to keep it alive. Your dog believes in you!"
        )

        private val FIFTY_FIVE_AFTER_BEDTIME_MESSAGES = listOf(
            "Your dog looks worried... 🐾 It's not too late to save your streak! Go to bed now before it resets.",
            "Time's almost up! 💤 Get some rest to keep that streak going strong!"
        )

        private val DAILY_REMINDER_MESSAGES = listOf(
            "Did you know a great bedtime leads to an awesome day? Let's make tonight count!",
            "Consistency is key! 🌟 Stick to your bedtime tonight and keep your streak strong."
        )

        private const val DAILY_REMINDER_HOUR = 15 // 3 PM
        private const val DAILY_REMINDER_MINUTE = 0
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
            .setCategory(NotificationCompat.CATEGORY_ALARM)
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

    fun scheduleNotifications(bedTime: LocalTime, sleepDuration: Float) {
        cancelAllAlarms()
        val now = LocalDateTime.now()
        val today = now.toLocalDate()

        val startTimeStr = trackingPrefs.getString("sleep_start_time", null)
        val startTime = startTimeStr?.let { LocalDateTime.parse(it) }
        val isTracking = trackingPrefs.getBoolean("is_tracking", false)

        if (isTracking && startTime != null) {
            val reminderTime = startTime
                .plusMinutes((sleepDuration * 60).toLong())
                .plusMinutes(15)
            if (now.isBefore(reminderTime)) {
                scheduleExactNotification(
                    time = reminderTime,
                    type = NotificationType.STOP_TRACKING_REMINDER,
                    bedTime = bedTime // This won't be used for this notification type
                )
            }
        }
        // Calculate notification times
        var hourBefore = today.atTime(bedTime.minusHours(1))
        var halfHourBefore = today.atTime(bedTime.minusMinutes(30))
        var atBedtime = today.atTime(bedTime)
        var fiveAfter = today.atTime(bedTime.plusMinutes(5))
        var fiftyFiveAfter = today.atTime(bedTime.plusMinutes(55))
        var dailyReminder = today.atTime(DAILY_REMINDER_HOUR, DAILY_REMINDER_MINUTE)

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
        if (now.isAfter(fiveAfter)) {
            fiveAfter = fiveAfter.plusDays(1)
        }
        if (now.isAfter(fiftyFiveAfter)) {
            fiftyFiveAfter = fiftyFiveAfter.plusDays(1)
        }
        if (now.isAfter(dailyReminder)) {
            dailyReminder = dailyReminder.plusDays(1)
        }

        // Save bedtime in preferences for boot receiver
        context.getSharedPreferences("sleep_settings", Context.MODE_PRIVATE)
            .edit()
            .putString("bedtime", bedTime.format(DateTimeFormatter.ISO_LOCAL_TIME))
            .apply()

        scheduleExactNotification(hourBefore, NotificationType.HOUR_BEFORE, bedTime)
        scheduleExactNotification(halfHourBefore, NotificationType.HALF_HOUR_BEFORE, bedTime)
        scheduleExactNotification(atBedtime, NotificationType.BEDTIME, bedTime)
        scheduleExactNotification(fiveAfter, NotificationType.FIVE_AFTER_BEDTIME, bedTime)
        scheduleExactNotification(fiftyFiveAfter, NotificationType.FIFTY_FIVE_AFTER_BEDTIME, bedTime)
        scheduleExactNotification(dailyReminder, NotificationType.DAILY_REMINDER, bedTime)
    }

    fun scheduleExactNotification(
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
            if (alarmManager.canScheduleExactAlarms()) {  // Check if exact alarms are allowed
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerAtMillis, pendingIntent),
                    pendingIntent
                )
            }
            else {
                alarmManager.setAndAllowWhileIdle(  // Fallback if exact alarms are not allowed
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
             )
            }
        } else {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerAtMillis, pendingIntent),
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
            NotificationType.FIVE_AFTER_BEDTIME -> tomorrow.atTime(bedTime.plusMinutes(5))
            NotificationType.FIFTY_FIVE_AFTER_BEDTIME -> tomorrow.atTime(bedTime.plusMinutes(55))
            NotificationType.DAILY_REMINDER -> tomorrow.atTime(DAILY_REMINDER_HOUR, DAILY_REMINDER_MINUTE)
            NotificationType.STOP_TRACKING_REMINDER -> return
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

    fun showStopTrackingReminder() {
        val message = STOP_TRACKING_MESSAGES[Random.nextInt(STOP_TRACKING_MESSAGES.size)]
        
        buildNotification(
            "Time to Stop Tracking",
            message,
            4 // unique ID for stop tracking reminder
        )
    }

    fun showFiveAfterBedtimeNotification() {
        val message = FIVE_AFTER_BEDTIME_MESSAGES[Random.nextInt(FIVE_AFTER_BEDTIME_MESSAGES.size)]
        
        buildNotification(
            "Bedtime Passed",
            message,
            5 // unique ID for 5 minutes after bedtime notification
        )
    }

    fun showFiftyFiveAfterBedtimeNotification() {
        val message = FIFTY_FIVE_AFTER_BEDTIME_MESSAGES[Random.nextInt(FIFTY_FIVE_AFTER_BEDTIME_MESSAGES.size)]
        
        buildNotification(
            "Last Chance for Streak",
            message,
            6 // unique ID for 55 minutes after bedtime notification
        )
    }

    fun showDailyReminder() {
        val message = DAILY_REMINDER_MESSAGES[Random.nextInt(DAILY_REMINDER_MESSAGES.size)]
        
        buildNotification(
            "Daily Sleep Reminder",
            message,
            7 // unique ID for daily reminder notification
        )
    }
} 