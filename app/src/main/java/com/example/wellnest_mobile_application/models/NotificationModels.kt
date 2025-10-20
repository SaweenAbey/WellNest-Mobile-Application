package com.example.wellnest_mobile_application.models


data class ActivityTrack(
    val timestamp: Long,
    val category: String,
    val activity: String,
    val value: String? = null,
    val metadata: String? = null
)


data class ActivitySummary(
    val waterIntake: Int,
    val waterGoal: Int,
    val moodEntriesCount: Int,
    val completedHabits: Int,
    val totalHabits: Int,
    val remindersSent: Int,
    val date: String
)


data class DailyDataSummary(
    val userName: String,
    val waterIntake: Int,
    val waterGoal: Int,
    val hydrationPercentage: Int,
    val moodData: MoodData,
    val moodEntriesCount: Int,
    val completedHabits: Int,
    val totalHabits: Int,
    val habitCompletionRate: Int,
    val hydrationRemindersSent: Int,
    val wellnessScore: Int
)

data class MoodData(
    val emoji: String,
    val mood: String
)


data class NotificationSettings(
    val notificationsEnabled: Boolean = true,
    val hydrationRemindersEnabled: Boolean = true,
    val dailySummaryEnabled: Boolean = true,
    val hydrationReminderInterval: Int = 60,
    val todayRemindersSent: Int = 0
)
