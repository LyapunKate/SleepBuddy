package com.sleepbuddy.sleeptracker.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sleepbuddy.sleeptracker.data.SleepGoalDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Job

class BootReceiver : BroadcastReceiver() {

    private var job: Job? = null

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val notificationManager = SleepNotificationManager(context)
            val dataStore = SleepGoalDataStore(context)

            // Launch in a coroutine since DataStore operations are suspend functions
            job = CoroutineScope(Dispatchers.IO).launch {
                dataStore.sleepGoal.collect { goal ->
                    notificationManager.scheduleNotifications(goal.bedTime, goal.sleepDuration)
                    println("""BootReceiver""")
                    // We only need to collect once to reschedule notifications
                    job?.cancel() // Cancel the job after notifications are scheduled
                }
            }
        }
    }
}