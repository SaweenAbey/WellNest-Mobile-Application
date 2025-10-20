package com.example.wellnest_mobile_application.database.daos

import androidx.room.*
import com.example.wellnest_mobile_application.database.entities.AppSettingsEntity

@Dao
interface AppSettingsDao {
    
    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getAppSettings(): AppSettingsEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppSettings(settings: AppSettingsEntity)
    
    @Update
    suspend fun updateAppSettings(settings: AppSettingsEntity)
    
    @Query("UPDATE app_settings SET dailyWaterGoal = :goal WHERE id = 1")
    suspend fun updateDailyWaterGoal(goal: Int)
    
    @Query("UPDATE app_settings SET notificationsEnabled = :enabled WHERE id = 1")
    suspend fun updateNotificationsEnabled(enabled: Boolean)
    
    @Query("UPDATE app_settings SET hydrationReminderEnabled = :enabled WHERE id = 1")
    suspend fun updateHydrationReminderEnabled(enabled: Boolean)
    
    @Query("UPDATE app_settings SET hydrationReminderInterval = :interval WHERE id = 1")
    suspend fun updateHydrationReminderInterval(interval: Int)
    
    @Query("UPDATE app_settings SET darkModeEnabled = :enabled WHERE id = 1")
    suspend fun updateDarkModeEnabled(enabled: Boolean)
    
    @Query("UPDATE app_settings SET snapDuration = :duration WHERE id = 1")
    suspend fun updateSnapDuration(duration: Int)
    
    @Query("UPDATE app_settings SET dailySummaryEnabled = :enabled WHERE id = 1")
    suspend fun updateDailySummaryEnabled(enabled: Boolean)
    
    @Query("UPDATE app_settings SET hydrationRemindersSentCount = :count WHERE id = 1")
    suspend fun updateHydrationRemindersSentCount(count: Int)
    
    @Query("UPDATE app_settings SET hydrationRemindersSentDate = :date WHERE id = 1")
    suspend fun updateHydrationRemindersSentDate(date: String)
    
    @Query("DELETE FROM app_settings")
    suspend fun deleteAppSettings()
}
