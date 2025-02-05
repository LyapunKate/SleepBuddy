package com.sleepbuddy.sleeptracker.data

sealed class MessageState {
    object Default : MessageState()
    object ThirtyDayStreak : MessageState()
    object TargetStreak : MessageState()
    object GoalMet : MessageState()
    object GoalNotMet : MessageState()
    object ForgotToStop : MessageState()

    fun getMessage(): String = when (this) {
        is ThirtyDayStreak -> "You're on fire! 30 days of consistent sleep. Your dog is throwing a little celebration in your honor."
        is TargetStreak -> "Wow! You've hit a new streak milestone. Your dog thinks you're the best sleeper ever! Keep it up!"
        is GoalMet -> listOf(
            "Look at that happy dog! You're crushing it. Keep up the great work!",
            "You did it! Another great night of sleep. Your dog is proud!",
            "You're doing amazing! Every night of good sleep is a step towards a healthier you!",
            "You're unstoppable! Each good night's rest is fueling your best self!",
            "Sweet dreams lead to brighter days! You're making fantastic progress!"
        ).random()
        is GoalNotMet -> listOf(
            "Your dog is disappointed... let's get back on track tonight!",
            "Let's try one more time today! Don't let the dog be angry."
        ).random()
        is ForgotToStop -> "It seems you forgot to press stop tracking, that's why the dog is angry and the progress is reset. Don't forget to track your sleep!"
        is Default -> listOf(
            "Keep up the good work! You're building great sleep habits!",
            "Way to go! Prioritizing your sleep is one of the best things you can do for yourself.",
            "You're investing in your health and well-being with every night of quality sleep!",
            "When you achieve 30 days of good sleep, you'll unlock a special surprise—a cheerful new state for your dog mascot! Keep going!",
            "Every night counts—rest well and wake up refreshed!",
            "Your future self will thank you for tonight’s good sleep!",
            "Great sleep leads to great days! Keep it going!",
            "Rest is your superpower—use it wisely!",
            "Just 30 good sleeps, and you'll unlock a special dog state! Keep those zzz’s coming!",
            "Early nights, brighter mornings—you're on the right track!"
        ).random()
    }
} 