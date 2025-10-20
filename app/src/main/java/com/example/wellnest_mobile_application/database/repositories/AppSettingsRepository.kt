package com.example.wellnest_mobile_application.database.repositories

import com.example.wellnest_mobile_application.database.WellnestDatabase
import com.example.wellnest_mobile_application.database.daos.AppSettingsDao
import com.example.wellnest_mobile_application.database.entities.AppSettingsEntity

class AppSettingsRepository(private val database: WellnestDatabase) {
    
    private val appSettingsDao: AppSettingsDao = database.appSettingsDao()
    
    suspend fun initializeDefaultSettings() {
        val existingSettings = appSettingsDao.getAppSettings()
        if (existingSettings == null) {
            val defaultSettings = AppSettingsEntity()
            appSettingsDao.insertAppSettings(defaultSettings)
        }
    }
    
    suspend fun getDailyWaterGoal(): Int {
        val settings = appSettingsDao.getAppSettings()
        return settings?.dailyWaterGoal ?: 4000
    }
    
    suspend fun setDailyWaterGoal(goal: Int) {
        appSettingsDao.updateDailyWaterGoal(goal)
    }
    
    suspend fun areNotificationsEnabled(): Boolean {
        val settings = appSettingsDao.getAppSettings()
        return settings?.notificationsEnabled ?: true
    }
    
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        appSettingsDao.updateNotificationsEnabled(enabled)
    }
    
    suspend fun isHydrationReminderEnabled(): Boolean {
        val settings = appSettingsDao.getAppSettings()
        return settings?.hydrationReminderEnabled ?: false
    }
    
    suspend fun setHydrationReminderEnabled(enabled: Boolean) {
        appSettingsDao.updateHydrationReminderEnabled(enabled)
    }
    
    suspend fun getHydrationReminderInterval(): Int {
        val settings = appSettingsDao.getAppSettings()
        return settings?.hydrationReminderInterval ?: 60
    }
    
    suspend fun setHydrationReminderInterval(intervalMinutes: Int) {
        appSettingsDao.updateHydrationReminderInterval(intervalMinutes)
    }
    
    suspend fun isDarkModeEnabled(): Boolean {
        val settings = appSettingsDao.getAppSettings()
        return settings?.darkModeEnabled ?: false
    }
    
    suspend fun setDarkModeEnabled(enabled: Boolean) {
        appSettingsDao.updateDarkModeEnabled(enabled)
    }
    
    suspend fun getSnapDuration(): Int {
        val settings = appSettingsDao.getAppSettings()
        return settings?.snapDuration ?: 60
    }
    
    suspend fun setSnapDuration(durationSeconds: Int) {
        appSettingsDao.updateSnapDuration(durationSeconds)
    }
    
    suspend fun isDailySummaryEnabled(): Boolean {
        val settings = appSettingsDao.getAppSettings()
        return settings?.dailySummaryEnabled ?: true
    }
    
    suspend fun setDailySummaryEnabled(enabled: Boolean) {
        appSettingsDao.updateDailySummaryEnabled(enabled)
    }
    
    suspend fun getHydrationRemindersSentCount(): Int {
        val settings = appSettingsDao.getAppSettings()
        return settings?.hydrationRemindersSentCount ?: 0
    }
    
    suspend fun setHydrationRemindersSentCount(count: Int) {
        appSettingsDao.updateHydrationRemindersSentCount(count)
    }
    
    suspend fun getHydrationRemindersSentDate(): String {
        val settings = appSettingsDao.getAppSettings()
        return settings?.hydrationRemindersSentDate ?: ""
    }
    
    suspend fun setHydrationRemindersSentDate(date: String) {
        appSettingsDao.updateHydrationRemindersSentDate(date)
    }
    
    suspend fun clearAllSettings() {
        appSettingsDao.deleteAppSettings()
    }
}
