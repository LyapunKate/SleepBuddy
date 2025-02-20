
## **SleepBuddy MVP Requirements**

### **Overview**
SleepBuddy is an Android sleep-tracking app with a playful streak-based system and a mascot. The app helps users maintain consistent sleep habits through notifications, tracking, and progress visualization.

---

### **Core Features**
1. **Screens**:
    - **Home Screen**
    - **Set Goal Screen**

2. **Sleep Tracking**:
    - Users can manually press "Start" to begin sleep tracking and "Stop" to end it.
    - Sleep duration is calculated as the difference between these two times.

3. **Streak System**:
    - A streak is maintained if:
        - Sleep duration ≥ goal duration.
        - Start time ≤ bedtime + 1 hour.
    - Streak resets if:
        - Start time > bedtime + 1 hour and sleep duration < goal duration.
        - Sleep duration exceeds goal duration by **4+ hours** due to a forgotten "Stop."

4. **Mascot States**:
    - Mascot moods change based on user behavior, time of day, and streak milestones.

---

### **Screen Details**

#### **1. Home Screen**
The Home Screen contains:
- **Mascot Animation (GIF)**:
    - Displays mascot’s current mood (e.g., happy, angry, neutral).
    - Moods dynamically update based on streak performance and time.
- **Encouraging Messages**:
    - Contextual messages from the mascot (e.g., congratulatory after a successful night, motivational during the day).
- **Visual Streak Progress**:
    - A simple progress bar or line indicating the user's current streak and progress toward their streak goal.
- **Tracking Button**:
    - A single button for starting or stopping sleep tracking.
- **Navigation Button**:
    - Access to the "Set Goal" screen.


#### **2. Set Goal Screen**
On this screen, users can:
- **Set Sleep Preferences**:
    - **Preferred Bedtime**: Default is **11:00 PM**.
    - **Preferred Sleep Duration**: Default is **8 hours**.
    - **Preferred Streak Duration**: Options are **7, 14, or 21 days**.
- **Save Settings**:
    - All settings persist until manually changed by the user.

---

### **Notifications**

#### **1. Pre-Bedtime Reminders**
- Triggered based on the user's bedtime setting. Do **not repeat** if dismissed.
- There are two examples for each notifications. One of them should be selected randomly each time.
- **1 Hour Before Bedtime**:
    - Example 1: "Hey! Just a friendly reminder: bedtime is at **selected time**. Let’s keep that streak alive! 🐾"
    - Example 2: "Your cozy bed is waiting! Get ready to wind down—just an hour left before bedtime."
- **30 Minutes Before Bedtime**:
    - Example 1: "Your dog’s getting sleepy... shouldn’t you be too? 💤 Bedtime is coming up in 30 minutes!"
    - Example 2: Tick tock! Time to start wrapping things up. Bedtime is at **selected time**, and your streak depends on it!
- **At Bedtime**:
    - Example 1: "Bedtime is here! 🛌 Stick to your plan and hit the hay to keep your streak going strong. Your dog is counting on you! 🐾"
    - Example 2: "Good night, champion! Let’s make tonight count. Start winding down and press ‘Start Sleep’ when you’re ready."


#### **2. Post-Bedtime Missed Goal**
- If "Start" is not pressed by **5 minutes after bedtime**:
    - Example 1: "Uh-oh, it’s past your bedtime! 😔 You’ve still got a chance to save your streak—head to bed now!"
    - Example 2: "Your streak is on thin ice! 🐶 Go to bed within the next hour to keep it alive. Your dog believes in you!"
- If "Start" is still not pressed **55 minutes after bedtime**:
    - Example 1: "Your dog looks worried... 🐾 It’s not too late to save your streak! Go to bed now before it resets."
    - Example 2: "Time’s almost up! 💤 Get some rest to keep that streak going strong!"


#### **3. Morning Reminders**
- Triggered at **(start tracking + goal duration + 15 minutes)**
- **Reminder to Stop Tracking**:
    - Example 1: "Good morning! ☀️ Don’t forget to press ‘Stop’ to log your sleep."
    - Example 2: "Rise and shine! Your dog wants to know how you slept—press ‘Stop’ to finish tracking."



#### **4. Motivational Notifications (Daytime)**
- **Midday (3 PM)**:
    - Example 1: "Keep your streak alive tonight! 🐶 Consistency is key."
    - Example 2: "Did you know a great bedtime leads to an awesome day? Let’s make tonight count!"
 
---

### **Mascot Logic**
| **Time**                | **Mascot Mood**         | **Condition**                                    |
|-------------------------|-------------------------|------------------------------------------------|
| Morning (Stop -> 3 PM) | Happy/Angry            | Based on sleep goal success.                  |
| Daytime (3 PM -> Bedtime - 1 Hr) | Neutral                | No conditions.                                |
| Evening (Bedtime - 1 Hr -> Bedtime) | Encouraging     | Always encouraging.                           |
| Post-Bedtime (Bedtime -> Stop) | Neutral/Angry | Angry if "Start" isn’t pressed.              |
| Streak Milestone (7/14/21 days) | Extremely Happy   | After a successful streak until 3 PM.        |
| Milestone (30/60 days)  | Special Dog            | Until 3 PM.                                   |

---

### **Streak Logic**
1. **Conditions to Maintain Streak**:
    - Start time ≤ bedtime + 1 hour.
    - Sleep duration ≥ goal duration.
2. **Conditions to Reset Streak**:
    - Start time > bedtime + 1 hour, **and** sleep duration < goal duration.
    - Sleep duration exceeds goal duration by **4+ hours** (forgotten "Stop").
3. **Milestone Triggers**:
    - Streak goals (e.g., 7, 14, 21 days) update mascot and trigger special messages.

---
### **Messages**
There are a few examples for some messages. One of them should be selected randomly each time.
1. - **Success Messages** (if goal is met):
    - Example 1: "You did it! 🐾 Another great night of sleep. Your dog is proud!"
    - Example 2: "Look at that happy dog! 🐾 You’re crushing it. Keep up the great work!"
    - Example 3: "You're doing amazing! Every night of good sleep is a step towards a healthier you!",
    - Example 4: "You're unstoppable! Each good night's rest is fueling your best self!",
    - Example 5: "Sweet dreams lead to brighter days! You're making fantastic progress!"
2. - **Failure Messages** (if goal is missed):
    - Example 1: "Your dog is disappointed... let’s get back on track tonight!"
    - Example 2: "Let's try one more time today! Don't let the dog be angry."
3. - Triggered when **streak milestones** (e.g., 7, 14, 21 days) are reached:
    - Example 1: "Wow! You’ve hit a new streak milestone. Your dog thinks you’re the best sleeper ever! 🐾 Keep it up!"
4. - For **30-day milestones**, show:
    - Example 1: "You’re on fire! 🔥 30 days of consistent sleep. Your dog is throwing a little celebration in your honor. 🐶🎉"
5. - **Forgot to stop**  (Sleep duration exceeds goal duration by 4+ hours):
    - Example 1: "It seems you forgot to press stop tracking, that's why the dog is angry and the progress is reset. Don't forget to track your sleep!"
6. - **Default**:
    - "Keep up the good work! You're building great sleep habits!",
    - "Way to go! Prioritizing your sleep is one of the best things you can do for yourself.",
    - "You're investing in your health and well-being with every night of quality sleep!",
    - "When you achieve 30 days of good sleep, you'll unlock a special surprise—a cheerful new state for your dog mascot! Keep going!",
    - "Every night counts—rest well and wake up refreshed!",
    - "Your future self will thank you for tonight’s good sleep!",
    - "Great sleep leads to great days! Keep it going!",
    - "Rest is your superpower—use it wisely!",
    - "Just 30 good sleeps, and you'll unlock a special dog state! Keep those zzz’s coming!",
    - "Early nights, brighter mornings—you're on the right track!"

