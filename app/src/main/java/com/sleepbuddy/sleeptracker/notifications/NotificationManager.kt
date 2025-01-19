package com.sleepbuddy.sleeptracker.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.sleepbuddy.sleeptracker.MainActivity
import com.sleepbuddy.sleeptracker.R
import java.time.LocalTime
import kotlin.random.Random

class SleepNotificationManager(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
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
} 