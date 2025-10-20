package com.example.wellnest_mobile_application.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val dailyWaterGoal: Int = 4000,
    val notificationsEnabled: Boolean = true,
    val hydrationReminderEnabled: Boolean = false,
    val hydrationReminderInterval: Int = 60,
    val darkModeEnabled: Boolean = false,
    val snapDuration: Int = 60,
    val dailySummaryEnabled: Boolean = true,
    val hydrationRemindersSentCount: Int = 0,
    val hydrationRemindersSentDate: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
